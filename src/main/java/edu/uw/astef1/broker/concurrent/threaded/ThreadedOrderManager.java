package edu.uw.astef1.broker.concurrent.threaded;

import edu.uw.astef1.broker.concurrent.ConcurrentOrderManager;
import edu.uw.ext.framework.order.PricedOrder;
import edu.uw.ext.framework.order.StopBuyOrder;
import edu.uw.ext.framework.order.StopSellOrder;

/**
 * Concrete implementation of {@link ConcurrentOrderManager}. Maintains a pair of
 * {@link ThreadedOrderQueue}s, of types {@link StopBuyOrder} and {@link StopSellOrder}.
 * 
 * @author AndrewStefanich
 * 
 */
public final class ThreadedOrderManager extends ConcurrentOrderManager {

	/**
	 * Constructs an instance of {@code ThreadedOrderManager}, and initializes {@link PricedOrder}
	 * queues.
	 * 
	 * @param stockSymbolTicker
	 *            the ticker symbol of the stock this instance manages orders for
	 * @param price
	 *            the current price of the stock to be managed
	 */
	public ThreadedOrderManager(String stockSymbolTicker, int price) {
		super(stockSymbolTicker);
		initializePricedOrderQueues(price);

	}

	/**
	 * Initializes this OrderManager queues with {@link ThreadedOrderQueue}s.
	 * 
	 * @param initialPrice
	 *            the initial "stop" price for the queues
	 */
	protected void initializePricedOrderQueues(int initialPrice) {
		stopBuyOrderQueue = new ThreadedOrderQueue<Integer, StopBuyOrder>(initialPrice, STOP_BUY_ORDER_FILTER,
				STOP_BUY_ORDER_COMPARATOR);
		stopSellOrderQueue = new ThreadedOrderQueue<Integer, StopSellOrder>(initialPrice, STOP_SELL_ORDER_FILTER,
				STOP_SELL_ORDER_COMPARATOR);
	}

}
