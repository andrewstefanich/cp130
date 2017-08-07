package edu.uw.astef1.broker.concurrent.threaded;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.Order;

/**
 * A simple {@link OrderQueue} implementation which stores orders in a
 * {@link TreeSet}. Each queue runs in a separate thread.
 * 
 * @param <T>
 *            Type: the dispatchable threshold type
 * @param <E>
 *            Elements: the type of order(s) to be contained within the queue
 * @author AndrewStefanich
 */
public class ThreadedOrderQueue<T, E extends Order> implements OrderQueue<T, E>, Runnable {

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

	/** lock for concurrent operations on a collection */
	private final Lock collectionLock = new ReentrantLock();

	/** condition for waiting/notifying threads */
	private final Condition orderThresholdCondition = collectionLock.newCondition();

	/** each queue runs on it's own thread. Condition controls when it runs */
	private final Thread queueThread = new Thread(this);

	/**
	 * Constructs a SimpleOrderQueue, orders added will be automatically sorted according to natural
	 * ordering.
	 * 
	 * @param threshold
	 *            the initial threshold for dispatchability
	 * @param filter
	 *            the dispatch filter used to control dispatching from the queue
	 */
	public ThreadedOrderQueue(final T threshold, final BiPredicate<T, E> filter) {
		this.orderQueue = new TreeSet<E>();
		this.threshold = threshold;
		this.dispatchFilter = filter;
		queueThread.start();
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
	public ThreadedOrderQueue(final T threshold, final BiPredicate<T, E> filter, final Comparator<E> comparator) {
		this.orderQueue = new TreeSet<E>(comparator);
		this.threshold = threshold;
		this.dispatchFilter = filter;
		queueThread.start();
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
	 * Signals run(). The dispatching thread will be woken up, and will again check for any
	 * dispatchable orders, and process them accordingly.
	 */
	@Override
	public void dispatchOrders() {
		collectionLock.lock();
		try {
			orderThresholdCondition.signal();
		} finally {
			collectionLock.unlock();
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

	/**
	 * Dispatches orders in a separate thread. This thread will wait if there are no dispatchable
	 * orders in the queue.
	 * Each dispatchable order is removed from the queue and passed to the callback. If there is no
	 * callback/orderprocessor registered, the order is simply revmoed from the queue.
	 */
	@Override
	public void run() {
		for (;;) {
			collectionLock.lock();
			E myOrder = null;
			try {
				while ((myOrder = dequeue()) == null) {
					orderThresholdCondition.await();
				}
				final Consumer<E> oProcessor = orderProcessor; //creating a local variable makes this thread safe
				if (oProcessor != null) {
					oProcessor.accept(myOrder);
				}
			} catch (InterruptedException e) {
				break;  //we break the infinite loop if something tells this thread to terminate/interrupts it
			} finally {
				collectionLock.unlock();
			}
		} //end of for loop
	} //end of run()

} //end of SimpleOrderQueue class
