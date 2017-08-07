package edu.uw.astef1.exchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.exchange.ExchangeAdapter;
import edu.uw.ext.framework.exchange.ExchangeEvent;
import edu.uw.ext.framework.exchange.StockExchange;
import edu.uw.ext.framework.exchange.StockQuote;
import edu.uw.ext.framework.order.MarketBuyOrder;
import edu.uw.ext.framework.order.MarketSellOrder;

/**
 * Provides a network adapter for a {@link StockExchange}, which functions as our server
 * application. (Object creation should come from {@link ExchangeNetworkAdapterFactory}).
 * 
 * @author AndrewStefanich
 * @see ExchangeNetworkAdapterFactory
 * @see StockExchange
 * @see ExchangeEvent
 */
public final class ExchangeNetworkAdapter implements ExchangeAdapter {

	/** buffer size for UDP packets/messages */
	private static final int BUFFER_SIZE = 512;

	/** time allotted for thread execution after thread pool shutdown (in seconds) */
	private static int shutdownTimer;

	static {
		setShutdownTimer(10);  //initialized at 10
		System.setProperty("java.net.preferIPv4Stack", "true"); //force IPv4 in place of IPv6
	}

	/** logger for this class and inner classes */
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	/** thread pool for this server. Listens for connection requests and processes commands */
	private ExecutorService threadPool = Executors.newCachedThreadPool();

	/** port for serverSocket connections. TCP */
	private final int commandsPort;

	/** the real StockExchange for which this class is an adapter */
	private StockExchange exchange;

	/** port for multicast event */
	private int eventsMulticastPort;

	/** group for multicast events */
	private InetAddress eventsMulticastGroup;

	/** multicast socket for events */
	private MulticastSocket eventsMulticastSocket;

	/**
	 * Constructs an {@code ExchangeNetworkAdapater}. (Object creation should come from
	 * {@link ExchangeNetworkAdapterFactory}).
	 * 
	 * @param exchange
	 *            the {@code StockExchange} used to service network requests
	 * @param multicastIP
	 *            the IP address used to propogate price changes
	 * @param multicastPort
	 *            the IP port used to propogate price changes
	 * @param commandPort
	 *            the ports on which this instance will listen for commands
	 * @throws SocketException
	 *             if an error occurs on a socket operation
	 * @throws UnknownHostException
	 *             if unable to resolve multicast IP address
	 */
	ExchangeNetworkAdapter(StockExchange exchange, String eventsMulticastIPAddress, int eventsMulticastPort,
			int commandsPort)
			throws SocketException, UnknownHostException {

		this.exchange = exchange;

		//TCP
		this.commandsPort = commandsPort;
		threadPool.execute(new CommandListener());

		//UDP
		this.eventsMulticastPort = eventsMulticastPort;
		this.eventsMulticastGroup = InetAddress.getByName(eventsMulticastIPAddress);
		try {
			this.eventsMulticastSocket = new MulticastSocket();
			eventsMulticastSocket.joinGroup(eventsMulticastGroup);
			this.exchange.addExchangeListener(this);
			LOG.info(String.format("Server now accepting UDP messages. IP- %s, Port- %d",
					eventsMulticastIPAddress, eventsMulticastPort));
		} catch (IOException e) {
			LOG.error(String.format("Unable to join this server to multicast group: IP- %s, Port- %d",
					eventsMulticastIPAddress, eventsMulticastPort));
			e.printStackTrace();
		}
	}

	/**
	 * Sets the time allotment for threads to finish execution after this class' close() method is
	 * called.
	 * 
	 * @param numSeconds
	 *            the number of seconds allotted
	 */
	public static void setShutdownTimer(final int numSeconds) {
		shutdownTimer = numSeconds;
	}

	/**
	 * Gets the number of seconds allotted for thread completion after a shutdown has been called.
	 * 
	 * @return the number of seconds
	 */
	public static int getShutdownTimer() {
		return shutdownTimer;
	}

	/**
	 * The {@code StockExchange} has opened and prices are adjusting. Adds a
	 * listener to receive price change events from the real
	 * {@code StockExchange} and multicasts them to {@code Broker}s.
	 * 
	 * @param event
	 *            the {@code ExchangeEvent}
	 */
	@Override
	public void exchangeOpened(final ExchangeEvent event) {
		issueEvent(ProtocolConstants.OPEN_EVENT.toString());

	}

	/**
	 * The {@code StockExchange} has closed. Notifies clients and removes price
	 * change listener.
	 * 
	 * @param event
	 *            event the {@code ExchangeEvent}
	 */
	@Override
	public void exchangeClosed(final ExchangeEvent event) {
		issueEvent(ProtocolConstants.CLOSED_EVENT.toString());

	}

	/**
	 * Processes price change events.
	 * 
	 * @param event
	 *            event the {@code ExchangeEvent}
	 */
	@Override
	public void priceChanged(final ExchangeEvent event) {
		final StringBuilder message = new StringBuilder();
		message.append(ProtocolConstants.PRICE_CHANGE_EVENT);
		message.append(ProtocolConstants.ELEMENT_DELIMITER);
		message.append(event.getTicker());
		message.append(ProtocolConstants.ELEMENT_DELIMITER);
		message.append(event.getPrice());
		issueEvent(message.toString());

	}

	/**
	 * Issues packets via UDP
	 * 
	 * @param message
	 *            the event message to multicast/send
	 */
	private void issueEvent(final String message) {
		final byte[] buffer = new byte[BUFFER_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, eventsMulticastGroup, eventsMulticastPort);
		final byte[] messageBytes = message.getBytes();
		packet.setData(messageBytes);
		packet.setLength(messageBytes.length);
		try {
			eventsMulticastSocket.send(packet);
		} catch (IOException e) {
			LOG.error(String.format("Failed to issue event: %s", message));
			e.printStackTrace();
		}
	}

	/**
	 * Closes this adapter.
	 */
	@Override
	public void close() {
		LOG.info("Server shutting down, no new requests!!");
		try {
			threadPool.shutdown();
			threadPool.awaitTermination(shutdownTimer, TimeUnit.SECONDS);
			eventsMulticastSocket.leaveGroup(eventsMulticastGroup);
			eventsMulticastSocket.close();
			exchange.removeExchangeListener(this);
		} catch (IOException | InterruptedException e) {
			LOG.warn(String.format("Server shut down. Could not process pending request within %d seconds",
					shutdownTimer));
		}
	}

	/**
	 * Accepts command requests and dispatches them to a {@code CommandHandler}.
	 * 
	 * @author AndrewStefanich
	 * @see CommandHandler
	 */
	final class CommandListener implements Runnable {

		/**
		 * Accepts connections and creates a {@code CommandHandler} for processing commands.
		 */
		@Override
		public void run() {
			try (ServerSocket commandsServerSocket = new ServerSocket(commandsPort)) {
				while (!threadPool.isShutdown()) {
					LOG.info(String.format("Server running, awaiting TCP connection on port # %d...",
							commandsPort));
					Socket clientCommandsSocket = commandsServerSocket.accept();
					LOG.info("Connection established, awaiting commands...");
					threadPool.execute(new CommandHandler(clientCommandsSocket));
				}
			} catch (IOException e) {
				LOG.warn(String.format("Server error on port # %d", commandsPort));
				e.printStackTrace();
			}
		}
	} //END OF CommandListener CLASS

	/**
	 * Instances execute commands received from clients.
	 * 
	 * @author AndrewStefanich
	 * @see Socket
	 */
	final class CommandHandler implements Runnable {

		/** socket which this CommandHandler processes commands on */
		private final Socket clientCommandsSocket;

		/**
		 * Constructs a new {@code CommandHandler}.
		 * 
		 * @param clientSocket
		 *            the {@code Socket} (client) for which this handler processes commands
		 */
		CommandHandler(final Socket clientSocket) {
			this.clientCommandsSocket = clientSocket;
		}

		/**
		 * Processes a given command via a Socket.
		 */
		@Override
		public void run() {
			String receivedCmdString = null;
			try (PrintWriter writer = new PrintWriter(clientCommandsSocket.getOutputStream(), true);
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(clientCommandsSocket.getInputStream()))) {
				//this thread is constantly running/reading from a stream, awaiting commands from client
				while (!threadPool.isShutdown()) {
					if ((receivedCmdString = reader.readLine()) != null) {
						processCommand(receivedCmdString, writer);
					}
				}
			} catch (IOException e) {
				LOG.error("Could not process command: ", receivedCmdString);
				e.printStackTrace();
			}
		}

		/**
		 * Processes a command writes the appropriate output back to the stream.
		 * 
		 * @param receivedCmdString
		 *            the text line received from the client
		 * @param writer
		 *            writer to write server response to socket output stream
		 */
		private void processCommand(final String receivedCmdString, final PrintWriter writer) {
			final String[] tokens = receivedCmdString.split(ProtocolConstants.ELEMENT_DELIMITER.toString());
			final ProtocolConstants commandArg = ProtocolConstants
					.valueOf(tokens[(Integer) ProtocolConstants.CMD_ELEMENT.getValue()]);

			COMMAND_SWITCH: switch (commandArg) {

			//Request: [GET_STATE_CMD]
			//Response: [OPEN_STATE]|[CLOSED_STATE]
			case GET_STATE_CMD:
				if (exchange.isOpen()) {
					writer.println(ProtocolConstants.OPEN_STATE);
				} else {
					writer.println(ProtocolConstants.CLOSED_STATE);
				}
				break COMMAND_SWITCH;

			//Request: [GET_TICKERS_CMD]
			//Response:symbol[ELEMENT_DELIMITER]symbol....
			case GET_TICKERS_CMD:
				String[] tickers = exchange.getTickers();
				StringBuilder stringBuilder = new StringBuilder();
				for (int i = 0; i < tickers.length; i++) {
					stringBuilder.append(tickers[i]);
					if (i < tickers.length - 1)  //don't append ":" to the last element
						stringBuilder.append(ProtocolConstants.ELEMENT_DELIMITER.toString());
				}
				writer.println(stringBuilder.toString());
				break COMMAND_SWITCH;

			//Request: [GET_QUOTE_CMD][ELEMENT_DELIMITER]symbol
			//Response: price
			case GET_QUOTE_CMD:
				String tickerSymbol = tokens[(Integer) ProtocolConstants.QUOTE_CMD_TICKER_ELEMENT.getValue()];
				StockQuote quote = exchange.getQuote(tickerSymbol);
				writer.println(quote.getPrice());
				break COMMAND_SWITCH;

			//Request: [EXECUTE_TRADE_CMD][ELEMENT_DELIMITER][BUY_ORDER]|[SELL_ORDER]
			//[ELEMENT_DELIMITER]account_ID[ELEMENT_DELIMITER]symbol[ELEMENT_DELIMITER]shares
			//Response:execution_price
			case EXECUTE_TRADE_CMD:
				if (!exchange.isOpen()) {
					writer.println(0);  //0 if trade is not executed
					break COMMAND_SWITCH;
				} else {
					String accountID = tokens[(Integer) ProtocolConstants.EXECUTE_TRADE_CMD_ACCOUNT_ELEMENT
							.getValue()];
					String symbol = tokens[(Integer) ProtocolConstants.EXECUTE_TRADE_CMD_TICKER_ELEMENT.getValue()];
					int numShares = Integer.parseInt(
							tokens[(Integer) ProtocolConstants.EXECUTE_TRADE_CMD_SHARES_ELEMENT.getValue()]);

					ProtocolConstants orderType = ProtocolConstants
							.valueOf(tokens[(Integer) ProtocolConstants.EXECUTE_TRADE_CMD_TYPE_ELEMENT.getValue()]);
					switch (orderType) {

					case BUY_ORDER:
						int buyExecutionPrice = exchange.executeTrade(new MarketBuyOrder(accountID, numShares, symbol));
						writer.println(buyExecutionPrice);
						break COMMAND_SWITCH;

					case SELL_ORDER:
						int sellExecutionPrice = exchange
								.executeTrade(new MarketSellOrder(accountID, numShares, symbol));
						writer.println(sellExecutionPrice);
						break COMMAND_SWITCH;

					default:
						LOG.warn(String.format("Server received %s from client. Unknown order type!",
								ProtocolConstants.EXECUTE_TRADE_CMD));
						break COMMAND_SWITCH;
					}
				} //end of else (exchange.isOpened())

			default:
				LOG.warn(String.format("Server received unknown command from client: %s", receivedCmdString));
			} //end of COMMANDTYPE switch statement
		} //end of processCommands()

	} //END OF CommandHandler CLASS
} //END OF ExchangeNetworkAdapter CLASS
