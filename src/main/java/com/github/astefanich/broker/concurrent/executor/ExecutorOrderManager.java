package com.github.astefanich.broker.concurrent.executor;

import java.util.concurrent.Executor;

import com.github.astefanich.broker.concurrent.ConcurrentOrderManager;

import edu.uw.ext.framework.order.PricedOrder;
import edu.uw.ext.framework.order.StopBuyOrder;
import edu.uw.ext.framework.order.StopSellOrder;

/**
 * Concrete implementation of {@link ConcurrentOrderManager}. Maintains a pair of
 * {@link ExecutorOrderQueue}s, of types {@link StopBuyOrder} and {@link StopSellOrder}.
 * 
 * @author AndrewStefanich
 * 
 * @see Executor
 * 
 */
public final class ExecutorOrderManager extends ConcurrentOrderManager {

	/** Executor for concurrent operations. ExecutorBroker.close() will shut down this Executor */
	private final Executor threadPool;

	/**
	 * Constructs an instance of {@code ExecutorOrderManager}, initializes {@link PricedOrder}
	 * queues.
	 * 
	 * @param stockSymbolTicker
	 *            the ticker symbol of the stock this instance manages orders for
	 * @param price
	 *            the current price of the stock to be managed
	 * @param threadPool
	 *            the Executor to utilize for multithreading tasks.
	 */
	public ExecutorOrderManager(String stockSymbolTicker, int price, Executor threadPool) {
		super(stockSymbolTicker);
		this.threadPool = threadPool;
		initializePricedOrderQueues(price);
	}

	/**
	 * Initializes this OrderManager queues with {@link ExecutorOrderQueue}s.
	 * 
	 * @param initialPrice
	 *            the initial "stop" price for the queues
	 */
	protected void initializePricedOrderQueues(int initialPrice) {
		stopBuyOrderQueue = new ExecutorOrderQueue<Integer, StopBuyOrder>(initialPrice, STOP_BUY_ORDER_FILTER,
				STOP_BUY_ORDER_COMPARATOR, this.threadPool);
		stopSellOrderQueue = new ExecutorOrderQueue<Integer, StopSellOrder>(initialPrice, STOP_SELL_ORDER_FILTER,
				STOP_SELL_ORDER_COMPARATOR, this.threadPool);
	}

}
