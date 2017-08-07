package edu.uw.astef1.account;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.dao.AccountDao;

/**
 * Implementation class for the {@link AccountManager} interface. The
 * AccountManager is responsible for protecting account passwords as well as
 * creating, deleting, authenticating, and persistence account related data.
 * Instances of this class should be created via the factory.
 * 
 * @author AndrewStefanich
 */
public class SimpleAccountManager implements AccountManager {

	/**
	 * object utilized by account manager to get/set data from persistant memory
	 */
	private AccountDao accountDao;

	/** holds an reference to an AccountFactory, for use in creating accounts */
	private static final SimpleAccountFactory SIMPLE_ACCOUNT_FACTORY = new SimpleAccountFactory();

	/** hashing algorithm for Account passwords */
	private static final String HASHING_ALGORITHM = "SHA1";

	/**
	 * Constructor for AccountManager, to utilize an AccountDao argument to
	 * persist/retrieve data.
	 * 
	 * @param dao
	 *            the DAO to use to persist and retrieve accounts
	 */
	SimpleAccountManager(final AccountDao dao) {
		this.accountDao = dao;
	}

	/**
	 * Used to persist an account (updates Account info stored in the designated
	 * persistant memory location)
	 * 
	 * @param account
	 *            the Account to persist
	 * @throws AccountException
	 *             if operation fails
	 */
	@Override
	public void persist(final Account account) throws AccountException {
		accountDao.setAccount(account);
	}

	/**
	 * Lookup an account based on account name.
	 * 
	 * @param accountName
	 *            the name of the desired Account
	 * @return the Account (if located).
	 * @throws AccountException
	 *             if account is not found.
	 */
	@Override
	public Account getAccount(String accountName) throws AccountException {
		Account account = null;
		account = accountDao.getAccount(accountName);
		if (account == null) {
			throw new AccountException(String.format("Unable to get Account %s; not found!", accountName));
		}
		account.registerAccountManager(this);
		return account;
	}

	/**
	 * Remove the Account.
	 * 
	 * @param accountName
	 *            the name of the Account to remove
	 * @throws AccountException
	 *             if operation fails
	 */
	@Override
	public void deleteAccount(String accountName) throws AccountException {
		accountDao.deleteAccount(accountName);
	}

	/**
	 * Creates an Account, hashes the password, registers the AccountManager,
	 * and persists the Account (via the DAO).
	 * 
	 * @param accountName
	 *            the name for Account to add
	 * @param password
	 *            the password used to gain access to the Account
	 * @param balance
	 *            the initial balance of the Account
	 * @return the newly created Account
	 * @throws AccountException
	 *             if operation fails
	 */
	@Override
	public Account createAccount(String accountName, String password, int balance) throws AccountException {
		Account account = null;
		if (accountDao.getAccount(accountName) == null) { //checks if our Account is already stored in memory
			final byte[] hashedPassword = hashPassword(password);
			account = SIMPLE_ACCOUNT_FACTORY.newAccount(accountName, hashedPassword, balance);
			account.registerAccountManager(this);
			persist(account);
			return account;
		} else {
			throw new AccountException(String.format("Account: %s already registered..", accountName));
		}
	}

	/**
	 * Hashes password, from String to byte[]
	 * 
	 * @param password
	 *            the password to hash
	 * @return the hashed password
	 */
	private static byte[] hashPassword(String password) throws AccountException {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(HASHING_ALGORITHM);
			messageDigest.update(password.getBytes()); //default encoding of Java 1.8 is UTF-8
			return messageDigest.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new AccountException("Unable to find hashing algorithm:", e);
		}
	}

	/**
	 * Check whether a login is valid. An account must exist with the account
	 * name and the password must match.
	 * 
	 * @param accountName
	 *            name of Account the password is to be validated for
	 * @param password
	 *            password is to be validated
	 * @return true if password is valid for account identified by accountName
	 * @throws AccountException
	 *             if operation fails
	 */
	@Override
	public boolean validateLogin(String accountName, String password) throws AccountException {
		boolean isValid = false;
		final Account account = getAccount(accountName);
		if (account != null) {
			final byte[] hashedPasswordArgument = hashPassword(password);
			isValid = MessageDigest.isEqual(account.getPasswordHash(), hashedPasswordArgument);
		}
		return isValid;
	}

	/**
	 * Release any resources used by the AccountManager implementation. Once
	 * closed further operations on the AccountManager may fail.
	 * 
	 * @throws AccountException
	 *             if operation fails
	 */
	@Override
	public void close() throws AccountException {
		accountDao.close();

	}

}
