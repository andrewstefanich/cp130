package com.github.astefanich.broker.concurrent.threaded;

import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.broker.Broker;
import edu.uw.ext.framework.broker.BrokerFactory;
import edu.uw.ext.framework.exchange.StockExchange;

/**
 * {@link BrokerFactory} implementation that returns a {@link ThreadedBroker}.
 * 
 * @author AndrewStefanich
 *
 */
public final class ThreadedBrokerFactory implements BrokerFactory {

	/**
	 * Instantiates a new {@code ThreadedBroker}.
	 * 
	 * @param name
	 *            the broker's name
	 * @param accountManager
	 *            the {@link AccountManager} to be used by the broker
	 * @param exchange
	 *            the exchange to be used by the broker
	 * @return a new SimpleBroker instance
	 */
	@Override
	public Broker newBroker(String name, AccountManager accountManager, StockExchange exchange) {
		return new ThreadedBroker(name, accountManager, exchange);
	}
}
