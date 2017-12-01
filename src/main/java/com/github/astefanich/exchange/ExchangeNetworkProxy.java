package com.github.astefanich.exchange;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.exchange.ExchangeEvent;
import edu.uw.ext.framework.exchange.ExchangeListener;
import edu.uw.ext.framework.exchange.StockExchange;
import edu.uw.ext.framework.exchange.StockQuote;
import edu.uw.ext.framework.order.Order;

/**
 * Client-side class for interacting with a network accessible {@link StockExchange}, which is
 * accessed via a {@link ExchangeNetworkAdapter}. This class' methods perform the following steps:
 * <br>
 * - Encodes the method request as a {@link String} (based on {@link ProtocolConstants}) <br>
 * - Sends the command to the {@link ExchangeNetworkAdapter} <br>
 * - Decodes the received response <br>
 * - Returns the result
 * Object creation should come from {@link ExchangeNetworkProxyFactory}.
 * 
 * @author AndrewStefanich
 * @see StockExchange
 * @see StockQuote
 * @see ProtocolConstants
 * @see ExchangeNetworkAdapter
 * @see EventProcessor
 */
public final class ExchangeNetworkProxy implements StockExchange {

	static {
		System.setProperty("java.net.preferIPv4Stack", "true"); //force IPv4 in place of IPv6
	}

	/** this class' logger */
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	/** dispatches server responses messages to be processed on a separate thread */
	private ExecutorService threadPool = Executors.newCachedThreadPool();

	/** UDP group for multicast events */
	private InetAddress eventsGroup;

	/** UDP multicast socket for events */
	private MulticastSocket eventsMulticastSocket;

	/** TCP socket for commands communication */
	private Socket commandsSocket;

	/** list of registered event listeners */
	private Set<ExchangeListener> exchangeListeners;

	/**
	 * Constructs an {@code ExchangeNetworkProxy}. (Object creation should come from
	 * {@link ExchangeNetworkProxyFactory}).
	 * 
	 * @param eventIPAddress
	 *            the multicast IP address to connect to
	 * @param eventPort
	 *            the multicast port to connect to
	 * @param commandIPAddress
	 *            the IP address the {@code StockExchange} accepts requests on
	 * @param commandPort
	 *            the IP address the {@code StockExchange} accepts requests on
	 */
	ExchangeNetworkProxy(final String eventsIPAddress, final int eventsPort, final String commandsIPAddress,
			final int commandsPort) {
		try {
			exchangeListeners = new HashSet<ExchangeListener>();
			eventsGroup = InetAddress.getByName(eventsIPAddress);
			eventsMulticastSocket = new MulticastSocket(eventsPort);
			eventsMulticastSocket.joinGroup(eventsGroup);
			threadPool.execute(new EventProcessor());
			commandsSocket = new Socket(commandsIPAddress, commandsPort);
		} catch (UnknownHostException e) {
			LOG.error(String.format("IP address for events: %s could not be resolved", eventsIPAddress));
			e.printStackTrace();
		} catch (IOException e) {
			LOG.error("Unable to connect to MulticastSocket on port " + eventsPort);
			e.printStackTrace();
		}
	}

	/**
	 * Gets the current state of the {@code StockExchange}
	 * 
	 * @return true if the exchange is open, false if closed.
	 */
	@Override
	public boolean isOpen() {
		String serverResponse = issueCommand(ProtocolConstants.GET_STATE_CMD.toString());
		if (serverResponse.equals(ProtocolConstants.OPEN_STATE.toString())) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the ticker symbols for all of the stocks that is traded on the exhchange.
	 * 
	 * @return the stock ticker symbols
	 */
	@Override
	public String[] getTickers() {
		String serverResponse = issueCommand(ProtocolConstants.GET_TICKERS_CMD.toString());
		if (serverResponse == null) {
		} else {
		}
		String[] tickers = serverResponse.split(ProtocolConstants.ELEMENT_DELIMITER.toString());
		return tickers;
	}

	/**
	 * Gets a {@code StockQuote}. Useful for getting stock's current price.
	 * 
	 * @param ticker
	 *            the ticker symbol for the given stock
	 * @return the quote, {@code null} if the quote is unavailable
	 */
	@Override
	public StockQuote getQuote(final String ticker) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(ProtocolConstants.GET_QUOTE_CMD);
		stringBuilder.append(ProtocolConstants.ELEMENT_DELIMITER);
		stringBuilder.append(ticker);
		String serverQuotePriceResponse = issueCommand(stringBuilder.toString());
		return new StockQuote(ticker, Integer.parseInt(serverQuotePriceResponse));
	}

	/**
	 * Creates a command to execute a trade and sends it to the exchange.
	 * 
	 * @param order
	 *            the {@code Order} to execute
	 * @return the price the order was executed at
	 */
	@Override
	public int executeTrade(final Order order) {
		String orderType = order.isBuyOrder() ? ProtocolConstants.BUY_ORDER.toString()
				: ProtocolConstants.SELL_ORDER.toString();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(ProtocolConstants.EXECUTE_TRADE_CMD);
		stringBuilder.append(ProtocolConstants.ELEMENT_DELIMITER);
		stringBuilder.append(orderType);
		stringBuilder.append(ProtocolConstants.ELEMENT_DELIMITER);
		stringBuilder.append(order.getAccountId());
		stringBuilder.append(ProtocolConstants.ELEMENT_DELIMITER);
		stringBuilder.append(order.getStockTicker());
		stringBuilder.append(ProtocolConstants.ELEMENT_DELIMITER);
		stringBuilder.append(order.getNumberOfShares());
		String serverExecutionPriceResponse = issueCommand(stringBuilder.toString());
		return Integer.parseInt(serverExecutionPriceResponse);
	}

	/**
	 * Issues a command to a server via a socket and accepts a reply
	 * 
	 * @param clientRequest
	 *            the command to send to the server
	 */
	private String issueCommand(final String clientRequest) {
		String serverResponse = null;
		try {
			//closing the output stream will close the associated socket. We want to leave it open for future commands
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(commandsSocket.getOutputStream()), true);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(commandsSocket.getInputStream()));
			writer.println(clientRequest);
			serverResponse = reader.readLine();
		} catch (IOException e) {
			LOG.error("Could not issue command: " + clientRequest);
			e.printStackTrace();
		}
		return serverResponse;
	}

	/**
	 * Adds a market listener. Delegates to the {@link EventProcessor}.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	@Override
	public void addExchangeListener(final ExchangeListener listener) {
		exchangeListeners.add(listener);
	}

	/**
	 * Removes a market listener. Delegates to the {@link EventProcessor}.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	@Override
	public void removeExchangeListener(final ExchangeListener listener) {
		exchangeListeners.remove(listener);
	}

	/**
	 * Client-side class. Listens for events by joining the mulitcast group. Processes events
	 * received from a {@link StockExchange}. Processing the events consists of propogating them to
	 * registered listeners.
	 * 
	 * @author AndrewStefanich
	 * @see ExchangeListener
	 */
	final class EventProcessor implements Runnable {

		/** buffer size for UDP packets/messages */
		private static final int BUFFER_SIZE = 512;

		/**
		 * Continually receives and processes UDP transmissions.
		 */
		@Override
		public void run() {
			try {
				final byte[] buffer = new byte[BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				while (!threadPool.isShutdown()) {
					eventsMulticastSocket.receive(packet); //blocks until packet is received
					processEventPacket(packet);
				}
				eventsMulticastSocket.leaveGroup(eventsGroup);
			} catch (IOException e) {
				LOG.warn("Error processing datagram packet. Unable to process event", e);
			}
		}

		/**
		 * Reads and processes events as byte arrays, and fires to registered listeners.
		 * 
		 * @param packet
		 *            {@code DatagramPacket} to process
		 */
		private void processEventPacket(final DatagramPacket packet) {
			final String packetString = new String(packet.getData(), 0, packet.getLength(),
					Charset.forName(ProtocolConstants.ENCODING.toString()));
			final String[] tokens = packetString.split(ProtocolConstants.ELEMENT_DELIMITER.toString());
			final ProtocolConstants eventArg = ProtocolConstants
					.valueOf(tokens[(Integer) ProtocolConstants.EVENT_ELEMENT.getValue()].toString());
			ExchangeEvent event = null;

			EVENTTYPE: switch (eventArg) {

			case OPEN_EVENT:
				event = ExchangeEvent.newOpenedEvent(this);
				for (ExchangeListener listener : exchangeListeners) {
					listener.exchangeOpened(event);
				}
				break EVENTTYPE;

			case CLOSED_EVENT:
				event = ExchangeEvent.newClosedEvent(this);
				for (ExchangeListener listener : exchangeListeners) {
					listener.exchangeClosed(event);
				}
				break EVENTTYPE;

			case PRICE_CHANGE_EVENT:
				final String ticker = tokens[(Integer) ProtocolConstants.PRICE_CHANGE_EVENT_TICKER_ELEMENT
						.getValue()];
				final int price = Integer
						.parseInt(tokens[(Integer) ProtocolConstants.PRICE_CHANGE_EVENT_PRICE_ELEMENT.getValue()]);
				event = ExchangeEvent.newPriceChangedEvent(this, ticker, price);
				for (ExchangeListener listener : exchangeListeners) {
					listener.priceChanged(event);
				}
				break EVENTTYPE;

			default:
				LOG.warn(String.format("Event type not found: ", eventArg));
				break EVENTTYPE;

			} //end of EVENTTYPE switch/case
		} //end of processEventPacket()

	} //END OF EventProcessor CLASS

} //END OF ExchangeNetworkProxy CLASS	
