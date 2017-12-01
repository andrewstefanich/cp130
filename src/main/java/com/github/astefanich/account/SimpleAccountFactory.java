package com.github.astefanich.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.AccountFactory;

/**
 * Implementation class for the {@link AccountFactory} interface. This is a Singleton class, as
 * defined by context.xml
 * 
 * @author AndrewStefanich
 *
 */
public class SimpleAccountFactory implements AccountFactory {
	
	/*SHOULD THIS BE A STATIC FACTORY CLASS WITH A PRIVATE CONSTRUCTOR? 
	(AccountFactory interface's method is not though, so it would be a violation) */

	/** logger for this class */
	private static final Logger LOG = LoggerFactory.getLogger(SimpleAccountFactory.class);

	/**
	 * No arg constructor, for Javabean.
	 */
	public SimpleAccountFactory() {

	}

	
	/**
	 * Instantiates a new Account instance
	 * 
	 * @param accountName
	 *            the account name
	 * @param hashedPassword
	 *            the password hash
	 * @param initialBalance
	 *            the balance
	 * @return the newly instantiated account, or null if unable to instantiate the account
	 */
	@Override
	public Account newAccount(String accountName, byte[] hashedPassword, int initialBalance) {
		Account account = null;
		try (ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("context.xml")) {
			if (accountName.length() < SimpleAccount.MINIMUM_ACCOUNT_NAME_LENGTH) {
				throw new AccountException("insufficient account name length");
			}
			if (initialBalance < SimpleAccount.MINIMUM_INITIAL_ACCOUNT_BALANCE) {
				throw new AccountException("insufficient initial balance");
			}
			account = appContext.getBean(SimpleAccount.class);
			account.setName(accountName);
			account.setPasswordHash(hashedPassword);
			account.setBalance(initialBalance);
		} catch (AccountException e) {
			LOG.warn(String.format("Failed to create Account: %s (%s)", accountName, e.getMessage()));
		}
		return account;
	}

} //end of SimpleAccountFactory class