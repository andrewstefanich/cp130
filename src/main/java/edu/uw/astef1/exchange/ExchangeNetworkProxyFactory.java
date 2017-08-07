package edu.uw.astef1.exchange;

import edu.uw.ext.framework.exchange.NetworkExchangeProxyFactory;
import edu.uw.ext.framework.exchange.StockExchange;

/**
 * Factory implementation for creating instances of {@link ExchangeNetworkProxy}
 * 
 * @author AndrewStefanich
 *
 * @see ExchangeNetworkProxy
 * @see StockExchange
 */
public final class ExchangeNetworkProxyFactory implements NetworkExchangeProxyFactory {

	/**
	 * Instantiates an {@code ExchangeNetworkProxy}.
	 * 
	 * @param multicastIP
	 *            the multicast IP address used to distribute events
	 * @param multicastPort
	 *            the port used to distribute events
	 * @param commandIP
	 *            the exchange host IP address
	 * @param commandPort
	 *            the listening port to be used to accept command requests
	 * @return a newly instantiated {@code ExchangeNetworkProxy}.
	 */
	@Override
	public StockExchange newProxy(String multicastIP, int multicastPort, String commandIP, int commandPort) {
		return new ExchangeNetworkProxy(multicastIP, multicastPort, commandIP, commandPort);
	}

}
