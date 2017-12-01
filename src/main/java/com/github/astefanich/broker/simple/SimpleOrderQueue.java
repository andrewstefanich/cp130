package com.github.astefanich.broker.simple;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.Order;

/**
 * A simple {@link OrderQueue} implementation which stores orders in a {@link TreeSet}.
 * 
 * @param <T>
 *            Type: the dispatchable threshold type
 * @param <E>
 *            Elements: the type of order(s) to be contained within the queue
 * 
 * @author AndrewStefanich
 *
 * @see OrderQueue
 * @see Consumer
 * @see BiPredicate
 */
public final class SimpleOrderQueue<T, E extends Order> implements OrderQueue<T, E> {

	/** Priority queue (Set) for a collection of orders */
	private TreeSet<E> orderQueue;

	/** threshold object, for use in determining dispatchability */
	private T threshold;

	/** The order processor */
	private Consumer<E> orderProcessor;

	/** Filter to control dispatching of orders */
	private BiPredicate<T, E> dispatchFilter;

	/**
	 * Constructs a SimpleOrderQueue, orders added will be automatically sorted according to natural
	 * ordering.
	 * 
	 * @param threshold
	 *            the initial threshold for dispatchability
	 * @param filter
	 *            the dispatch filter used to control dispatching from the queue
	 */
	public SimpleOrderQueue(final T threshold, final BiPredicate<T, E> filter) {
		this.orderQueue = new TreeSet<E>();
		this.threshold = threshold;
		this.dispatchFilter = filter;
	}

	/**
	 * Constructs a SimpleOrderQueue, orders added will be automatcially sorted according to a
	 * custom sorting order.
	 * 
	 * @param threshold
	 *            the initial threshold for dispatchability
	 * @param filter
	 *            the dispatch filter used to control dispatching from the queue.
	 * @param comparator
	 *            custom sorting order
	 */
	public SimpleOrderQueue(final T threshold, final BiPredicate<T, E> filter, final Comparator<E> comparator) {
		this.orderQueue = new TreeSet<E>(comparator);
		this.threshold = threshold;
		this.dispatchFilter = filter;
	}

	/**
	 * Adds the specified orders to the queue. Subsequent to adding the order, this method will
	 * dispatch any dispatchable orders.
	 * 
	 * @param order
	 *            the order to be added to the queue.
	 */
	@Override
	public void enqueue(final E order) {
		orderQueue.add(order);
		dispatchOrders();

	}

	/**
	 * Removes the highest dispatchable order in the queue. If there are orders, but they do not
	 * meet dispatch criteria, the order is not removed and {@code null} is returned.
	 * 
	 * @return the first dispatchable order in the queue; {@code null} if there are no dispatchable
	 *         orders in the queue.
	 */
	@Override
	public E dequeue() {
		if (!orderQueue.isEmpty()) { //check for null first
			if (dispatchFilter.test(threshold, orderQueue.first())) {
				/* calls the bipredicate function: True if the order passes the conditions, which
				 * are specified in the class which called this class' constructor; i.e. where the
				 * function is defined */
				E myOrder = orderQueue.first();
				orderQueue.remove(myOrder);
				return myOrder;
			}
		}
		return null;
	}

	/**
	 * Executes the callback for each dispatchable order. Each dispatchable order is removed from
	 * the queue and passed to the callback. If there is no callback/orderprocessor registered, the
	 * order is simply revmoed from the queue.
	 */
	@Override
	public void dispatchOrders() {
		E myOrder = null;
		while ((myOrder = dequeue()) != null) {
			if (orderProcessor != null) {	//the order will be processed only if an orderprocessor has been registered.
				orderProcessor.accept(myOrder);
			}
		}

	}

	/**
	 * Registers the callback to be used for order processing.
	 * 
	 * @param proc
	 *            the callback to be registerd.
	 */
	@Override
	public void setOrderProcessor(final Consumer<E> proc) {
		this.orderProcessor = proc;

	}

	/**
	 * Adjusts the threshold and dispatches orders.
	 * 
	 * @param threshold
	 *            the new threshold.
	 */
	@Override
	public void setThreshold(final T threshold) {
		this.threshold = threshold;
		dispatchOrders();

	}

	/**
	 * Obtains the current threshold value.
	 * 
	 * @return the current threshold.
	 */
	@Override
	public T getThreshold() {
		return threshold;
	}

}
