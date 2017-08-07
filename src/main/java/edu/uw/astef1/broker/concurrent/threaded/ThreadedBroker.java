package edu.uw.astef1.broker.concurrent.threaded;

import edu.uw.astef1.broker.concurrent.ConcurrentBroker;
import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.broker.OrderManager;
import edu.uw.ext.framework.exchange.StockExchange;
import edu.uw.ext.framework.order.Order;

/**
 * Uses a {@link ThreadedOrderManager} and a {@link ThreadedOrderQueue} for market orders.
 * 
 * @author AndrewStefanich
 * @see OrderManager
 */
public final class ThreadedBroker extends ConcurrentBroker {

	/**
	 * Constructs a ThreadBroker. Creation should be done by public factory method.
	 * 
	 * @param brokerName
	 *            the name of the broker
	 * @param accountManager
	 *            the {@code AccountManager} to be used by the broker
	 * @param exchange
	 *            the {@code StockExchange} to be used by the broker.
	 */
	protected ThreadedBroker(String brokerName, AccountManager accountManager, StockExchange exchange) {
		super(brokerName, accountManager, exchange);
		this.marketOrderQueue = new ThreadedOrderQueue<Boolean, Order>(exchange.isOpen(), market_order_filter);
		marketOrderQueue.setOrderProcessor(tradeExecutor);
		initializeOrderManagerMap();
		exchange.addExchangeListener(this);
	}

	/**
	 * Creates an appropriate manager type for this broker (ThreadedOrderManager).
	 * 
	 * @param tickerSymbol
	 *            the ticker symbol of the stock
	 * @param initialPrice
	 *            current price of the stock
	 * @return a new OrderManager, for the specified stock
	 */
	protected OrderManager createOrderManager(String tickerSymbol, int initialPrice) {
		return new ThreadedOrderManager(tickerSymbol, initialPrice);
	}
}
