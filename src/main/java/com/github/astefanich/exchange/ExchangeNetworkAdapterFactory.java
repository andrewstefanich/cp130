package com.github.astefanich.exchange;

import java.net.SocketException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.exchange.ExchangeAdapter;
import edu.uw.ext.framework.exchange.NetworkExchangeAdapterFactory;
import edu.uw.ext.framework.exchange.StockExchange;

/**
 * Implementation class of {@link NetworkExchangeAdapterFactory}; creates instances of
 * {@link ExchangeNetworkAdapter}.
 * 
 * @author AndrewStefanich
 * 
 * @see NetworkExchangeAdapterFactory
 * @see ExchangeNetworkAdapter
 *
 */
public final class ExchangeNetworkAdapterFactory implements NetworkExchangeAdapterFactory {

	/** this class' logger */
	private static final Logger LOG = LoggerFactory.getLogger(ExchangeNetworkAdapterFactory.class);

	/**
	 * Instantiates an {@code ExchangeNetworkAdapter}.
	 * 
	 * @param exchange
	 *            the underlying 'real' {@link StockExchange}
	 * @param multicastIP
	 *            the multicast IP address used to distribute events
	 * @param multicastPort
	 *            the port used to distribute events
	 * @param commandPort
	 *            the listening port to be used to accept command requests
	 * @return a new {@code ExchangeNetworkAdapter}, {@code null} if instantiation fails
	 */
	@Override
	public ExchangeAdapter newAdapter(StockExchange exchange, String multicastIP, int multicastPort, int commandPort) {
		ExchangeNetworkAdapter exchangeNetworkAdapter = null;
		try {
			exchangeNetworkAdapter = new ExchangeNetworkAdapter(exchange, multicastIP, multicastPort,
					commandPort);
		} catch (SocketException e) {
			LOG.warn("Failed to create a new ExchangeNetworkAdapter. Error creating or accessing a Socket", e);
		} catch (UnknownHostException e) {
			LOG.warn("Failed to create a new ExchangeNetworkAdapter. IP host could not be determined", e);
		}
		return exchangeNetworkAdapter;
	}

}
