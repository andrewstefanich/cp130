package com.github.astefanich.broker.simple;

import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.broker.Broker;
import edu.uw.ext.framework.broker.BrokerException;
import edu.uw.ext.framework.broker.OrderManager;
import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.exchange.ExchangeEvent;
import edu.uw.ext.framework.exchange.ExchangeListener;
import edu.uw.ext.framework.exchange.StockExchange;
import edu.uw.ext.framework.exchange.StockQuote;
import edu.uw.ext.framework.order.MarketBuyOrder;
import edu.uw.ext.framework.order.MarketSellOrder;
import edu.uw.ext.framework.order.Order;
import edu.uw.ext.framework.order.StopBuyOrder;
import edu.uw.ext.framework.order.StopSellOrder;

/**
 * Implementation class of {@link Broker} interface; used by investors to create and access their
 * account, and to obtain quotes and place orders.
 * 
 * @author AndrewStefanich
 * 
 * @see Broker
 * @see AccountManager
 * @see StockExchange
 *
 */
public final class SimpleBroker implements Broker, ExchangeListener, EventListener {

	/** this class' logger */
	private static final Logger LOG = LoggerFactory.getLogger(SimpleBroker.class);

	/** name of this broker */
	private String brokerName;

	/** AccountManager instance for this broker to utilize */
	private AccountManager accountManager;

	/** holds a set of order managers, based on stock symbol */
	private Map<String, OrderManager> orderManagerMap;

	/** the exchange in which this broker operates */
	private StockExchange stockExchange;

	/** collection to hold market orders */
	private OrderQueue<Boolean, Order> marketOrderQueue;

	/**
	 * Dispatch filter for marketOrders. Exchange should be open and it should contain the ticker
	 * symbol for a given order
	 */
	private final BiPredicate<Boolean, Order> market_order_filter = (threshold, order) -> stockExchange.isOpen()
			&& listTickers().contains(order.getStockTicker());

	/**
	 * order processor for market orders; execute a trade with the exchange and account manager
	 * reflects the order in the account balance, persists the DAO/file. Market orders are executed
	 * at the market price, so price is obtained via a quote with the exchange.
	 */
	private final Consumer<Order> tradeExecutor = (order) -> {
		final String exceptionMessage = "Failed to reflect order %d in balance for Account: %s. %s";
		final String accountID = order.getAccountId();
		final String stockTicker = order.getStockTicker();
		stockExchange.executeTrade(order);
		try {
			accountManager.getAccount(accountID).reflectOrder(order, requestQuote(stockTicker).getPrice());
		} catch (final AccountException e) {
			LOG.error(String.format(exceptionMessage, order.getOrderId(), accountID, e.getMessage())); //account exception thrown by getAccount (if account is not found)  
		} catch (final BrokerException e) {
			LOG.error(String.format(exceptionMessage, order.getOrderId(), accountID, e.getMessage())); //broker exception thrown by requestQuote
		}
	};

	/**
	 * Constructs a SimpleBroker, used for singlethreaded operations. Creates a SimpleOrderQueue,
	 * and initializes orderManagers.
	 * Creation should be done by public factory method.
	 * 
	 * @param brokerName
	 *            the name of the broker
	 * @param acctMngr
	 *            the {@code AccountManager} to be used by the broker
	 * @param exchange
	 *            the {@code StockExchange} to be used by the broker.
	 */
	SimpleBroker(String brokerName, AccountManager accountManager, StockExchange stockExchange) {
		this.brokerName = brokerName;
		this.accountManager = accountManager;
		this.stockExchange = stockExchange;
		this.marketOrderQueue = new SimpleOrderQueue<Boolean, Order>(stockExchange.isOpen(), market_order_filter);
		marketOrderQueue.setOrderProcessor(tradeExecutor);
		initializeOrderManagerMap();
		stockExchange.addExchangeListener(this);
	}

	/**
	 * Assigns stock tickers (key) and order managers (value) in a HashMap, and sets each order
	 * manager as the "move to market" processor.
	 */
	private void initializeOrderManagerMap() {
		this.orderManagerMap = new HashMap<String, OrderManager>();
		for (String ticker : listTickers()) {
			StockQuote quote = stockExchange.getQuote(ticker);
			OrderManager orderManager = new SimpleOrderManager(ticker, quote.getPrice());
			orderManager.setBuyOrderProcessor((order) -> marketOrderQueue.enqueue(order));
			orderManager.setSellOrderProcessor((order) -> marketOrderQueue.enqueue(order));
			orderManagerMap.put(ticker, orderManager);
			LOG.info(String.format("Initialized OrderManager for Ticker:%s, Price: %d", ticker,
					quote.getPrice()));
		}
	}

	/**
	 * Checks that all needed variables for this class to perform operations have been intialized.
	 * 
	 * @throws IllegalStateException
	 *             if the broker is in an invalid state
	 */
	private void checkInvariants() throws IllegalStateException {
		if (brokerName == null ||
				accountManager == null ||
				stockExchange == null ||
				orderManagerMap == null ||
				marketOrderQueue == null) {
			throw new IllegalStateException("Broker is not yet initialized or is closed");
		}
	}

	/**
	 * Gets the tickers of a stock exchange as a Collection.
	 * 
	 * @return the List of tickers associated with this exchange
	 */
	private List<String> listTickers() {
		return Collections.unmodifiableList(Arrays.asList(stockExchange.getTickers()));
	}

	/**
	 * Gets the OrderManager value associated with a given ticker symbol (key)
	 * 
	 * @param tickerSymbol
	 *            the key/ticker symbols
	 * @return the value/OrderManager
	 */
	private OrderManager orderManagerLookup(String tickerSymbol) throws NoSuchElementException {
		OrderManager orderManager = orderManagerMap.get(tickerSymbol);
		if (orderManager == null) {
			throw new NoSuchElementException("No OrderManager found for ticker:" + tickerSymbol);
		}
		return orderManager;
	}

	/**
	 * Upon the exchange opening, sets the market dispatch filter threshold (to true) and processes
	 * any available orders.
	 * 
	 * @param event
	 *            the exchange (open) event
	 */
	@Override
	public void exchangeOpened(ExchangeEvent event) {
		checkInvariants();
		LOG.info("Exchange is now open. Trade away!!");
		marketOrderQueue.setThreshold(true); //setting threshold will automatically call dispatchOrders 
	}

	/**
	 * Upon the exchange opening, set the market dispatch filter threshold.
	 * 
	 * @param event
	 *            the exchange (closed) event
	 */
	@Override
	public void exchangeClosed(ExchangeEvent event) {
		checkInvariants();
		marketOrderQueue.setThreshold(false);
		LOG.info("Exchange is now closed!!");

	}

	/**
	 * Upon the exchange opening, sets the market dispatch filter threshold and processes any
	 * available orders.
	 * 
	 * @param event
	 *            the price change event.
	 */
	@Override
	public void priceChanged(ExchangeEvent event) {
		checkInvariants();
		final String ticker = event.getTicker();
		final int price = event.getPrice();
		orderManagerLookup(ticker).adjustPrice(price);
		LOG.info(String.format("Price for %s has changed to %d", ticker, price));

	}

	/**
	 * Get the name of this broker.
	 * 
	 * @return the broker's name
	 */
	@Override
	public String getName() {
		return brokerName;
	}

	/**
	 * Create an account with the broker. Operations are performed by the appropriate AccountManager
	 * and the corresponding DAO.
	 * 
	 * @param accountName
	 *            the user or account name for the Account
	 * @param password
	 *            the password for the new Account
	 * @param balance
	 *            the initial account balance in cents
	 * @return the new Account
	 * @throws BrokerException
	 *             if unable to create the Account
	 */
	@Override
	public Account createAccount(final String accountName, final String password, final int balance)
			throws BrokerException {
		checkInvariants();
		Account account = null;
		try {
			account = accountManager.createAccount(accountName, password, balance);
		} catch (final AccountException e) {
			LOG.warn(e.getMessage());
			throw new BrokerException(e);
		}
		return account;
	}

	/**
	 * Deletes an account with the broker. Operations are performed by the appropriate
	 * AccountManager and the corresponding DAO.
	 * 
	 * @param accountName
	 *            the user or account name for the Account
	 * @throws BrokerException
	 *             if unable to delete the Account
	 */
	@Override
	public void deleteAccount(final String accountName) throws BrokerException {
		checkInvariants();
		try {
			accountManager.deleteAccount(accountName); //deletes account via the JsonAccountDao
		} catch (final AccountException e) {
			LOG.warn(e.getMessage());  //exception/message thrown by DAO  (JsonAccountDao), then by the accountmanager
			throw new BrokerException(e);
		}
	}

	/**
	 * Locate an account with the broker. The username and password are first verified and the
	 * account is returned. Operations are performed by the appropriate AccountManager and the
	 * corresponding DAO.
	 * 
	 * @param accountName
	 *            the user or account name for the account
	 * @param password
	 *            the password for the account
	 * @return the Account
	 * @throws BrokerException
	 *             if username and/or password are invalid
	 */
	@Override
	public Account getAccount(final String accountName, final String password) throws BrokerException {
		checkInvariants();
		Account account = null;
		try {
			account = accountManager.getAccount(accountName);  //gets account via the JsonAccountDao. If account is null, accountmanagger throws exception
			if (!accountManager.validateLogin(accountName, password)) {
				throw new BrokerException(String.format("Cannot retrieve Account: %s. Invalid login!", accountName));
			}
		} catch (final AccountException e) {
			LOG.warn(e.getMessage());
			throw new BrokerException(e);
		}
		return account;
	}

	/**
	 * Get a price quote for a stock from the exchange.
	 * 
	 * @param ticker
	 *            the stocks ticker symbol
	 * @return the stocks current price
	 * @throws BrokerException
	 *             if unable to obtain quote
	 */
	@Override
	public StockQuote requestQuote(final String ticker) throws BrokerException {
		checkInvariants();
		StockQuote quote = stockExchange.getQuote(ticker);
		if (quote == null) {  //exchange returns null if ticker symbol is not listed
			throw new BrokerException(
					String.format("Unable to obtain quote. %s is not listed with this exchange", ticker));
		}
		return quote;
	}

	/**
	 * Place a market buy order with the broker, adding it to the market order queue.
	 * 
	 * @param order
	 *            the order being placed with the broker
	 * @throws BrokerException
	 *             if unable to place order
	 */
	@Override
	public void placeOrder(final MarketBuyOrder order) throws BrokerException {
		checkInvariants();
		marketOrderQueue.enqueue(order);

	}

	/**
	 * Place a market sell order with the broker.
	 * 
	 * @param order
	 *            the order being placed with the broker
	 * @throws BrokerException
	 *             if unable to place order
	 */
	@Override
	public void placeOrder(final MarketSellOrder order) throws BrokerException {
		checkInvariants();
		marketOrderQueue.enqueue(order);
	}

	/**
	 * Place a StopBuyOrder with the broker, which is then placed/stored with the order manager.
	 * 
	 * @param order
	 *            the order being placed with the broker
	 * @throws BrokerException
	 *             if unable to place order
	 */
	@Override
	public void placeOrder(final StopBuyOrder order) throws BrokerException {
		checkInvariants();
		orderManagerLookup(order.getStockTicker()).queueOrder(order);

	}

	/**
	 * Place a {@code StopSellOrder} with the broker, which is then placed/stored with the order
	 * manager.
	 * 
	 * @param order
	 *            the order being placed with the broker
	 * @throws BrokerException
	 *             if unable to place order
	 */
	@Override
	public void placeOrder(final StopSellOrder order) throws BrokerException {
		checkInvariants();
		orderManagerLookup(order.getStockTicker()).queueOrder(order);

	}

	/**
	 * Release resources used by the broker. (closes the DAO utilized by the account manager)
	 * 
	 * @throws BrokerException
	 *             if an error occurs during the close operation
	 */
	@Override
	public void close() throws BrokerException {
		LOG.info("Closing broker:" + brokerName);
		checkInvariants();
		stockExchange.removeExchangeListener(this);
		orderManagerMap = null;
		try {
			accountManager.close();
		} catch (AccountException e) {
			LOG.error(String.format("Broker: %s failed to close AccountManager: %s", brokerName, accountManager));
			throw new BrokerException(e);
		}

	}

}
