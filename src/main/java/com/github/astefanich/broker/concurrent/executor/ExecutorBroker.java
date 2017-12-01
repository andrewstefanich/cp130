package com.github.astefanich.broker.concurrent.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.astefanich.broker.concurrent.ConcurrentBroker;
import com.github.astefanich.exchange.ExchangeNetworkAdapter;

import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.broker.BrokerException;
import edu.uw.ext.framework.broker.OrderManager;
import edu.uw.ext.framework.exchange.StockExchange;
import edu.uw.ext.framework.order.Order;

/**
 * Uses a {@link ExecutorOrderManager}s and a {@link ExecutorOrderQueue} for market orders.
 * 
 * @author AndrewStefanich
 * @see OrderManager
 * @see ExecutorService
 */
public final class ExecutorBroker extends ConcurrentBroker {

	/** this class' logger */
	private static final Logger LOG = LoggerFactory.getLogger(ExecutorBroker.class);

	/** Executor for concurrent operations. Will be passed to order manager and order queues */
	private final ExecutorService threadPool = Executors.newCachedThreadPool();

	/**
	 * Constructs an ExecutorBroker. Creation should be done by public factory method.
	 * 
	 * @param brokerName
	 *            the name of the broker
	 * @param accountManager
	 *            the {@code AccountManager} to be used by the broker
	 * @param exchange
	 *            the {@code StockExchange} to be used by the broker.
	 */
	ExecutorBroker(final String brokerName, final AccountManager accountManager, final StockExchange exchange) {
		super(brokerName, accountManager, exchange);
		this.marketOrderQueue = new ExecutorOrderQueue<Boolean, Order>(exchange.isOpen(), market_order_filter,
				threadPool);
		marketOrderQueue.setOrderProcessor(tradeExecutor);
		initializeOrderManagerMap();
		exchange.addExchangeListener(this);
	}

	/**
	 * Creates an appropriate manager for this broker (ExecutorOrderManager).
	 * 
	 * @param tickerSymbol
	 *            the ticker symbol of the stock
	 * @param initialPrice
	 *            current price of the stock
	 * @return a new OrderManager, for the specified stock
	 */
	protected OrderManager createOrderManager(final String tickerSymbol, final int initialPrice) {
		return new ExecutorOrderManager(tickerSymbol, initialPrice, threadPool);
	}

	/**
	 * Release resources used by the broker. (closes the DAO utilized by the account manager)
	 * 
	 * @throws BrokerException
	 *             if an error occurs during the close operation
	 */
	@Override
	public void close() throws BrokerException {
		threadPool.shutdown();
		LOG.info(String.format("ExecutorBroker: %s closing ExecutorService: %s. No new requests will be accepted.",
				this, threadPool));
		try {
			threadPool.awaitTermination(ExchangeNetworkAdapter.getShutdownTimer(), TimeUnit.SECONDS);
			super.close();
		} catch (InterruptedException e) {
			throw new BrokerException(
					String.format("Pending requests not completed within %d seconds of shutdown",
							ExchangeNetworkAdapter.getShutdownTimer()));
		}
	}
}