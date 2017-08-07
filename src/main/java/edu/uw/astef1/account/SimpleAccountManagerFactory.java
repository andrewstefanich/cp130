package edu.uw.astef1.account;

import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.account.AccountManagerFactory;
import edu.uw.ext.framework.dao.AccountDao;

/**
 * Implementation class for the {@link AccountManagerFactory} interface.
 * 
 * @author AndrewStefanich
 *
 */
public class SimpleAccountManagerFactory implements AccountManagerFactory {
	
	/*SHOULD THIS BE A STATIC FACTORY CLASS WITH A PRIVATE CONSTRUCTOR? 
	(AccountManagerFactory interface's method is not though, so it would be a violation) */
	
	/**
	 * No arg constructor, for JavaBean.
	 */
	public SimpleAccountManagerFactory(){
		
	}


	/**
	 * Instantiates a new account manager instance.
	 * 
	 * @param dao
	 *            the data access object to be used by the AccountManager
	 * @return a newly instantiated AccountManager
	 */
	@Override
	public AccountManager newAccountManager(AccountDao dao) {
		AccountManager accountManager = new SimpleAccountManager(dao);
		return accountManager;
	}

}
