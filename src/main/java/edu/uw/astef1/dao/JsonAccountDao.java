package edu.uw.astef1.dao;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import edu.uw.astef1.account.SimpleAccount;
import edu.uw.astef1.account.SimpleAddress;
import edu.uw.astef1.account.SimpleCreditCard;
import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.Address;
import edu.uw.ext.framework.account.CreditCard;
import edu.uw.ext.framework.dao.AccountDao;

/**
 * Implementation class for persisting/retrieving data from a JSON file.
 * 
 * @author AndrewStefanich
 */
public final class JsonAccountDao implements AccountDao {

	/** logger for this Dao implementation class */
	private static final Logger LOG = LoggerFactory.getLogger(JsonAccountDao.class);

	/** JSON file which contains the Account data */
	private static final String ACCOUNT_JSON_FILE_PATH = "%s.json";

	/** root directory for Account files */
	private final File accountsRootDirectory = new File("target", "accounts");

	/** Serialization support for abstact type mappings */
	private final ObjectMapper mapper;

	/** lock for read/writing the JSON file */
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	/**
	 * No-arg constructor, for JavaBean.
	 * 
	 * @throws AccountException
	 *             if creation failed
	 */
	public JsonAccountDao() throws AccountException {
		final SimpleModule module = new SimpleModule();
		module.addAbstractTypeMapping(Account.class, SimpleAccount.class);
		module.addAbstractTypeMapping(Address.class, SimpleAddress.class);
		module.addAbstractTypeMapping(CreditCard.class, SimpleCreditCard.class);
		mapper = new ObjectMapper();
		mapper.registerModule(module);
	}

	/**
	 * Get an account in based on account name, from a single accounts
	 * directory.
	 * 
	 * @param accountName
	 *            the name of the desired Account to retrieve
	 * @return the Account (if located), otherwise null.
	 */
	@Override
	public Account getAccount(String accountName) {
		Account account = null;
		String accountFileName = String.format(ACCOUNT_JSON_FILE_PATH, accountName);
		if (accountsRootDirectory.exists() && accountsRootDirectory.isDirectory()) {
			try {
				readWriteLock.readLock().lock();
				final File jsonInFile = new File(accountsRootDirectory, accountFileName);
				account = mapper.readValue(jsonInFile, Account.class);
			} catch (IOException e) {
				System.out.println();
				LOG.warn("DAO unable to access/read account data: " + accountName);
			} finally {
				readWriteLock.readLock().unlock();
			}
		} else {
			LOG.warn(String.format("Unable to retrieve Account: %s. Account not found!", accountName));
		}
		return account;
	}

	/**
	 * Adds or updates an account, from a single accounts directory.
	 * 
	 * @param account
	 *            the account to add/update
	 * @throws AccountException
	 *             if operation fails
	 */
	@Override
	public void setAccount(Account account) throws AccountException {
		try {
			readWriteLock.writeLock().lock();
			String accountFileName = String.format(ACCOUNT_JSON_FILE_PATH, account.getName());
			final File jsonOutFile = new File(accountsRootDirectory, accountFileName);
			if (!accountsRootDirectory.exists()) { //creates the accounts directory (if does not already exists)
				final boolean makeDirSuccess = accountsRootDirectory.mkdirs();
				if (!makeDirSuccess) {
					throw new AccountException(
							"Unable to create directory: " + accountsRootDirectory.getAbsolutePath());
				}
			}
			//deletes any existing JSON files with same account name
			if (jsonOutFile.exists()) {
				jsonOutFile.delete();
			}
			mapper.writerWithDefaultPrettyPrinter().writeValue(jsonOutFile, account);

		} catch (final IOException e) {
			throw new AccountException("Unable to create/update account file. ", e);
		} finally {
			readWriteLock.writeLock().unlock();
		}
	} //end of setAccount

	/**
	 * Deletes the JSON file for a given account name.
	 * 
	 * @param accountName
	 *            name of the account to delete
	 * @throws AccountException
	 *             if operation fails
	 */
	@Override
	public void deleteAccount(String accountName) throws AccountException {
		String accountFileName = String.format(ACCOUNT_JSON_FILE_PATH, accountName);
		File accountFile = new File(accountsRootDirectory, accountFileName);
		try {
			readWriteLock.writeLock().lock();
			if (accountFile.exists() && !accountFile.delete()) {
				throw new AccountException("Failed to delete file: " + accountFile.getAbsolutePath());
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	/**
	 * Remove all accounts. This is primarily available to facilitate testing.
	 * 
	 * @throws AccountException
	 *             if reset operation fails.
	 */
	@Override
	public void reset() throws AccountException {
		try {
			readWriteLock.writeLock().lock();
			FileUtils.deleteDirectory(accountsRootDirectory); //recursively deletes a directory, including all subdirectories/files 
		} catch (IOException e) {
			throw new AccountException("Unable to delete directory: " + accountsRootDirectory, e);
		} finally {
			readWriteLock.writeLock().unlock();
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
	public void close() throws AccountException {
		//MYB used for closing network connections? TBD

	}

}
