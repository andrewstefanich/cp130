package com.github.astefanich.broker.simple;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.reverseOrder;

import java.util.Comparator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import edu.uw.ext.framework.broker.OrderManager;
import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.AbstractOrder;
import edu.uw.ext.framework.order.PricedOrder;
import edu.uw.ext.framework.order.StopBuyOrder;
import edu.uw.ext.framework.order.StopSellOrder;

/**
 * Maintains a pair of {@link PricedOrder} queues, of types {@link StopBuyOrder} and
 * {@link StopSellOrder}. Orders and request the execution of orders when the price
 * conditions allow their execution.
 * 
 * @author AndrewStefanich
 * 
 * @see PricedOrder
 * @see StopSellOrder
 * @see StopBuyOrder
 *
 */
public final class SimpleOrderManager implements OrderManager {

	/****** CONSTANTS *******/

	/**
	 * Dispatch filter for StopBuyOrders. Price must be less than threshold for the order to be
	 * processed
	 */
	private static final BiPredicate<Integer, StopBuyOrder> STOP_BUY_ORDER_FILTER = (threshold,
			order) -> order.getPrice() <= threshold;

	/**
	 * Dispatch filter for StopSellOrders. Price must be greater than threshold for the order to be
	 * processed
	 */
	private static final BiPredicate<Integer, StopSellOrder> STOP_SELL_ORDER_FILTER = (threshold,
			order) -> order.getPrice() >= threshold;

	/** Comparator for StopBuyOrders. Sorts first by price, ascending order. */
	private static final Comparator<StopBuyOrder> STOP_BUY_ORDER_COMPARATOR = comparingInt(StopBuyOrder::getPrice)
			.thenComparing(AbstractOrder::getNumberOfShares, reverseOrder())
			.thenComparingInt(AbstractOrder::getOrderId);

	/** Comparator for StopBuyOrders. Sorts first by price, ascending order. */
	private static final Comparator<StopSellOrder> STOP_SELL_ORDER_COMPARATOR = comparing(StopSellOrder::getPrice,
			reverseOrder()).thenComparing(AbstractOrder::getNumberOfShares, reverseOrder())
					.thenComparingInt(AbstractOrder::getOrderId);

	/***** INSTANCE VARIABLES *******/

	/** Ticker symbol of the stock being managed by this OrderManager */
	private String stockSymbolTicker;

	/** collection of StopBuyOrders for this OrderManager to manage */
	private OrderQueue<Integer, StopBuyOrder> stopBuyOrderQueue;

	/** collection of StopSellOrders for this OrderManager to manage */
	private OrderQueue<Integer, StopSellOrder> stopSellOrderQueue;

	/**
	 * Constructs a SimpleOrderManager, and instantiates two order queues with thresholds at the
	 * given price.
	 * 
	 * @param stockSymbolTicker
	 *            the ticker symbol of the stock this instance manages orders for
	 * @param initialPrice
	 *            the current price of the stock to be managed
	 */
	public SimpleOrderManager(final String stockSymbolTicker, final int initialPrice) {
		this.stockSymbolTicker = stockSymbolTicker;
		stopBuyOrderQueue = new SimpleOrderQueue<Integer, StopBuyOrder>(initialPrice, STOP_BUY_ORDER_FILTER,
				STOP_BUY_ORDER_COMPARATOR);
		stopSellOrderQueue = new SimpleOrderQueue<Integer, StopSellOrder>(initialPrice, STOP_SELL_ORDER_FILTER,
				STOP_SELL_ORDER_COMPARATOR);
	}

	/**
	 * Gets the stock ticker symbol for the stock managed by this stock manager.
	 * 
	 * @return the stock ticker symbol
	 */
	@Override
	public String getSymbol() {
		return stockSymbolTicker;
	}

	/**
	 * Respond to a stock price adjustment by setting threshold on order queues.
	 * 
	 * @param price
	 *            the new price
	 */
	@Override
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
	@Override
	public void queueOrder(final StopBuyOrder order) {
		this.stopBuyOrderQueue.enqueue(order);

	}

	/**
	 * Queue a stop sell order.
	 * 
	 * @param order
	 *            the order to be queued
	 */
	@Override
	public void queueOrder(final StopSellOrder order) {
		stopSellOrderQueue.enqueue(order);

	}

	/**
	 * Registers the processor to be used during order processing. This will be passed on to the
	 * order queues as the dispatch callback.
	 * 
	 * @param processor
	 *            callback to be registered
	 */
	@Override
	public void setBuyOrderProcessor(final Consumer<StopBuyOrder> processor) {
		stopBuyOrderQueue.setOrderProcessor(processor);

	}

	/**
	 * Registers the processor to be used during order processing. This will be passed on to the
	 * order queues as the dispatch callback.
	 * 
	 * @param processor
	 *            callback to be registered
	 */
	@Override
	public void setSellOrderProcessor(final Consumer<StopSellOrder> processor) {
		stopSellOrderQueue.setOrderProcessor(processor);

	}

}
