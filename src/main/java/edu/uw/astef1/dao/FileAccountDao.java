package edu.uw.astef1.dao;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.Address;
import edu.uw.ext.framework.account.CreditCard;
import edu.uw.ext.framework.dao.AccountDao;

/**
 * Implementation methods needed to store and load accounts from persistent file
 * storage.
 * 
 * @author AndrewStefanich
 */
public class FileAccountDao implements AccountDao {
	

	/** logger for this Dao implementation class */
	private static final Logger LOG = LoggerFactory.getLogger(FileAccountDao.class);

	/** binary file to hold the Account data */
	private static final String ACCOUNT_FILENAME = "account.dat";

	/** properties file to hold the Address data */
	private static final String ADDRESS_FILENAME = "address.properties";

	/** text file to hold the Address data */
	private static final String CREDITCARD_FILENAME = "creditcard.txt";

	/** root directory for Account files */
	private final File accountsRootDirectory = new File("target", "accounts");

	/**
	 * No-arg constructor, for JavaBean. We want object creation to come from
	 * the factory.
	 */
	FileAccountDao() {
	}

	/**
	 * Lookup an account in based on account name.
	 * 
	 * @param accountName
	 *            the name of the desired Account
	 * @return the Account (if located), otherwise null
	 */
	@Override
	public synchronized Account getAccount(String accountName) {
		Account account = null;
		final File accountDirectory = new File(accountsRootDirectory, accountName);
		final File accountFile = new File(accountDirectory, ACCOUNT_FILENAME);
		if (accountDirectory.exists() && accountDirectory.isDirectory()) {
			try {
				account = FileAccountDaoIOUtil.readAccount(accountFile);

				final File addressFile = new File(accountDirectory, ADDRESS_FILENAME);
				if (addressFile.exists()) {
					account.setAddress(FileAccountDaoIOUtil.readAddress(addressFile));
				}
				final File creditCardFile = new File(accountDirectory, CREDITCARD_FILENAME);
				if (creditCardFile.exists()) {
					account.setCreditCard(FileAccountDaoIOUtil.readCreditCard(creditCardFile));
				}
			} catch (IOException | AccountException e) {
				LOG.warn("Unable to retrieve Account: " + accountName);
				e.printStackTrace();
			}

		}
		return account;
	}

	/**
	 * Adds or updates an Account into persistent memory.
	 * 
	 * @param account
	 *            the Account to add/update
	 * @throws AccountException
	 *             if operation fails
	 */
	@Override
	public synchronized void setAccount(Account account) throws AccountException {
		final File accountDirectory = new File(accountsRootDirectory, account.getName());
		final Address address = account.getAddress();
		final CreditCard creditCard = account.getCreditCard();
		deleteAccount(account.getName());
		if (accountDirectory.exists() || !accountDirectory.mkdirs()) {
			throw new AccountException("Unable to create directory: " + accountDirectory.getAbsolutePath());
		}

		try {
			final File accountFile = new File(accountDirectory, ACCOUNT_FILENAME);
			FileAccountDaoIOUtil.writeAccount(accountFile, account);
			if (address != null) {
				final File addressFile = new File(accountDirectory, ADDRESS_FILENAME);
				FileAccountDaoIOUtil.writeAddress(addressFile, account.getAddress());
			}
			if (creditCard != null) {
				final File creditCardFile = new File(accountDirectory, CREDITCARD_FILENAME);
				FileAccountDaoIOUtil.writeCreditCard(creditCardFile, account.getCreditCard());
			}
		} catch (final IOException e) {
			LOG.warn("Failed to create/update Account: " + account.getName());
			e.printStackTrace();
			throw new AccountException(e.getCause());
		}
	}

	/**
	 * Removes the Account directory and any subcontents from persistent memory.
	 * 
	 * @param accountName
	 *            the name of the Account to be deleted
	 * @throws AccountException
	 *             if operation fails
	 */
	@Override
	public synchronized void deleteAccount(String accountName) throws AccountException {
		final File accountDirectory = new File(accountsRootDirectory, accountName);
		if (accountDirectory.exists()) {
			try {
				FileUtils.forceDelete(accountDirectory); //recursively deletes a directory including any subdirectories/files
			} catch (IOException e) {
				LOG.warn("Unable to delete account folder: " + accountName);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Remove all account directories from the FileAccountDao root target folder
	 * (used for testing purposes)
	 * 
	 * @throws AccountException
	 *             if operation fails
	 */
	@Override
	public synchronized void reset() throws AccountException {
		try {
			FileUtils.deleteDirectory(accountsRootDirectory);
		} catch (IOException e) {
			LOG.warn("Unable to delete directory: " + accountsRootDirectory);
			e.printStackTrace();
		}

	}

	/**
	 * Close the DAO. Release any resources used by the DAO implementation. If
	 * the DAO is already closed then invoking this method has no effect.
	 * 
	 * @throws AccountException
	 *             if operation fails
	 */
	@Override
	public synchronized void close() throws AccountException {
		//TBD. method is mainly for closing network connections
	}

} //end of FileAccountDaoClass
