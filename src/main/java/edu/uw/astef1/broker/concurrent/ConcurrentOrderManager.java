package edu.uw.astef1.broker.concurrent;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.reverseOrder;

import java.util.Comparator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import edu.uw.ext.framework.broker.OrderManager;
import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.AbstractOrder;
import edu.uw.ext.framework.order.StopBuyOrder;
import edu.uw.ext.framework.order.StopSellOrder;

/**
 * Abstract {@link OrderManager} class. Concrete subclasses maintain
 * queues for different types of orders and request the execution of orders when
 * the price conditions allow their execution.
 * 
 * @author AndrewStefanich
 * @see OrderQueue
 * @see AbstractOrder
 * @see BiPredicate
 * @see Comparator
 */
public abstract class ConcurrentOrderManager implements OrderManager {

	/****** CONSTANTS *******/

	/**
	 * Dispatch filter for StopBuyOrders. Price must be less than threshold for
	 * the order to be processed
	 */
	protected static final BiPredicate<Integer, StopBuyOrder> STOP_BUY_ORDER_FILTER = (threshold,
			order) -> order.getPrice() <= threshold;

	/**
	 * Dispatch filter for StopSellOrders. Price must be greater than threshold
	 * for the order to be processed
	 */
	protected static final BiPredicate<Integer, StopSellOrder> STOP_SELL_ORDER_FILTER = (threshold,
			order) -> order.getPrice() >= threshold;

	/** Comparator for StopBuyOrders. Sorts first by price, ascending order. */
	protected static final Comparator<StopBuyOrder> STOP_BUY_ORDER_COMPARATOR = comparingInt(StopBuyOrder::getPrice)
			.thenComparing(AbstractOrder::getNumberOfShares, reverseOrder())
			.thenComparingInt(AbstractOrder::getOrderId);

	/** Comparator for StopBuyOrders. Sorts first by price, ascending order. */
	protected static final Comparator<StopSellOrder> STOP_SELL_ORDER_COMPARATOR = comparing(StopSellOrder::getPrice,
			reverseOrder()).thenComparing(AbstractOrder::getNumberOfShares, reverseOrder())
					.thenComparingInt(AbstractOrder::getOrderId);

	/***** INSTANCE VARIABLES *******/

	/** Ticker symbol of the stock being managed by this OrderManager */
	private String stockSymbolTicker;

	/** collection of StopBuyOrders for this OrderManager to manage */
	protected OrderQueue<Integer, StopBuyOrder> stopBuyOrderQueue;

	/** collection of StopSellOrders for this OrderManager to manage */
	protected OrderQueue<Integer, StopSellOrder> stopSellOrderQueue;

	/**
	 * Constructor for subclasses; only instantiates the tickerSymbol.
	 * intializePricedOrderQueues() should be called direclty by the subclass
	 * constructors.
	 * 
	 * @param stockSymbolTicker
	 *            the ticker symbol of the stock this instance manages orders
	 *            for
	 */
	protected ConcurrentOrderManager(final String stockSymbolTicker) {
		this.stockSymbolTicker = stockSymbolTicker;
	}

	/**
	 * Initializes this OrderManager queues with SimpleOrderQueues
	 * 
	 * @param initialPrice
	 *            the initial "stop" price for the queues
	 */
	protected abstract void initializePricedOrderQueues(int initialPrice);

	/**
	 * Gets the stock ticker symbol for the stock managed by this stock manager.
	 * 
	 * @return the stock ticker symbol
	 */
	public String getSymbol() {
		return stockSymbolTicker;
	}

	/**
	 * Respond to a stock price adjustment by setting threshold on order queues.
	 * 
	 * @param price
	 *            the new price
	 */
	public void adjustPrice(final int price) {
		stopBuyOrderQueue.setThreshold(price);
		stopSellOrderQueue.setThreshold(price);
	}

	/**
	 * Queue a stop buy order.
	 * 
	 * @param order
	 *            the order to be queued
	 */
	public void queueOrder(final StopBuyOrder order) {
		this.stopBuyOrderQueue.enqueue(order);
	}

	/**
	 * Queue a stop sell order.
	 * 
	 * @param order
	 *            the order to be queued
	 */
	public void queueOrder(final StopSellOrder order) {
		stopSellOrderQueue.enqueue(order);
	}

	/**
	 * Registers the processor to be used during order processing. This will be
	 * passed on to the order queues as the dispatch callback.
	 * 
	 * @param processor
	 *            callback to be registered
	 */
	public void setBuyOrderProcessor(final Consumer<StopBuyOrder> processor) {
		stopBuyOrderQueue.setOrderProcessor(processor);
	}

	/**
	 * Registers the processor to be used during order processing. This will be
	 * passed on to the order queues as the dispatch callback.
	 * 
	 * @param processor
	 *            callback to be registered
	 */
	public void setSellOrderProcessor(final Consumer<StopSellOrder> processor) {
		stopSellOrderQueue.setOrderProcessor(processor);
	}

}
