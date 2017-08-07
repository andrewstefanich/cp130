package edu.uw.astef1.broker.simple;

import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.broker.Broker;
import edu.uw.ext.framework.broker.BrokerFactory;
import edu.uw.ext.framework.exchange.StockExchange;

/**
 * {@link BrokerFactory} implementation that returns a {@link SimpleBroker}.
 * 
 * @author AndrewStefanich
 *
 */
public final class SimpleBrokerFactory implements BrokerFactory {

	/**
	 * Instantiates a new {@code SimpleBroker}.
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
		return new SimpleBroker(name, accountManager, exchange);
	}

}
