package edu.uw.astef1.broker.concurrent.executor;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.Order;

/**
 * A simple {@link OrderQueue} implementation which stores orders in a
 * {@link TreeSet}. Utilizes an ExecutorService for thread creation and multithreaded operations.
 * 
 * @param <T>
 *            Type: the dispatchable threshold type
 * @param <E>
 *            Elements: the type of order(s) to be contained within the queue
 * @author AndrewStefanich
 */
public class ExecutorOrderQueue<T, E extends Order> implements OrderQueue<T, E>, Runnable {

	/** TreeSet for a collection of orders - used to practice locks */
	private TreeSet<E> orderQueue;

	/** threshold object, for use in determining dispatchability */
	private T threshold;

	/**
	 * The order processor. This is a function which either moves the order to market, or adds them
	 * to an order manager queue
	 */
	private Consumer<E> orderProcessor;

	/** Filter to control dispatching of orders */
	private BiPredicate<T, E> dispatchFilter;

	/** thread pool for concurrent operations */
	private Executor threadPool;

	/** lock for concurrent operations, for the TreeSet */
	private final Lock collectionLock = new ReentrantLock();

	/** lock concurrent order processor operations */
	private final Lock orderProcessorLock = new ReentrantLock();

	/**
	 * Constructs a SimpleOrderQueue, orders added will be automatically sorted according to natural
	 * ordering.
	 * 
	 * @param threshold
	 *            the initial threshold for dispatchability
	 * @param filter
	 *            the dispatch filter used to control dispatching from the queue
	 * @param threadPool
	 *            the executor to be used to process this queue's orders
	 */
	public ExecutorOrderQueue(final T threshold, final BiPredicate<T, E> filter, Executor threadPool) {
		this.orderQueue = new TreeSet<E>();
		this.threshold = threshold;
		this.dispatchFilter = filter;
		this.threadPool = threadPool;
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
	 * @param threadPool
	 *            the Executor to utilize for multithreading tasks.
	 */
	public ExecutorOrderQueue(final T threshold, final BiPredicate<T, E> filter, final Comparator<E> comparator,
			Executor threadPool) {
		this.orderQueue = new TreeSet<E>(comparator);
		this.threshold = threshold;
		this.dispatchFilter = filter;
		this.threadPool = threadPool;
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
		collectionLock.lock();
		try {
			orderQueue.add(order);
		} finally {
			collectionLock.unlock();
		}
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
		collectionLock.lock();
		E myOrder = null;
		try {
			if (!orderQueue.isEmpty() && dispatchFilter.test(threshold, orderQueue.first())) { //the test condition is defined by the class who called this class' constructor
				myOrder = orderQueue.first();
				orderQueue.remove(myOrder);
			}
		} finally {
			collectionLock.unlock();
		}
		return myOrder;
	}

	/**
	 * Executes this instance's run(), via a cachedThreadPool.
	 */
	@Override
	public void dispatchOrders() {
		threadPool.execute(this);
	}

	/**
	 * Registers the callback to be used for order processing.
	 * 
	 * @param proc
	 *            the callback to be registerd.
	 */
	@Override
	public void setOrderProcessor(final Consumer<E> proc) {
		orderProcessorLock.lock();
		try {
			this.orderProcessor = proc;
		} finally {
			orderProcessorLock.unlock();
		}

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

	/**
	 * Executes the callback for each dispatchable order. Each dispatchable order is removed from
	 * the queue and passed to the callback. If there is no callback/orderprocessor registered, the
	 * order is simply revmoed from the queue.
	 */
	@Override
	public void run() {
		collectionLock.lock();
		E myOrder = null;
		try {
			while ((myOrder = dequeue()) != null) {
				orderProcessorLock.lock();
				try {
					if (orderProcessor != null) {	//the order will be processed only if an orderprocessor has been registered.
					}
					orderProcessor.accept(myOrder);
				} finally {
					orderProcessorLock.unlock();
				}
			}
		} finally {
			collectionLock.unlock();
		}
	} //end of run()

} //end of SimpleOrderQueue class
