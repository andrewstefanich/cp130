package com.github.astefanich.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.account.Address;
import edu.uw.ext.framework.account.CreditCard;
import edu.uw.ext.framework.order.Order;

/**
 * Implementation class for {@link Account} interface; a pure JavaBean representation of an account.
 * Instances of this class should be generated via the factory.
 * 
 * @author AndrewStefanich
 * 
 */
public class SimpleAccount implements Account {

	/** version ID */
	private static final long serialVersionUID = -8473063199565265759L;

	/** logger for this class */
	private static final Logger LOG = LoggerFactory.getLogger(SimpleAccount.class);

	/** name of the Account */
	private String name;

	/** full name of the Account */
	private String fullName;

	/** Address of the Account */
	private Address address;

	/** the balance on the Account */
	private int balance;

	/** CreditCard associated with the Account */
	private CreditCard creditCard;

	/** the associated email */
	private String email;

	/** hashed password */
	private byte[] passwordHash;

	/** phone number of the Account */
	private String phone;

	/** the AccountManager for this Account */
	private AccountManager accountManager;

	/** minimum number of characters for an Account's name */
	static final int MINIMUM_ACCOUNT_NAME_LENGTH = 8;

	/** minimum initial account balance (in cents)= $1000 */
	static final int MINIMUM_INITIAL_ACCOUNT_BALANCE = 100000;

	/**
	 * No arg constructor, for JavaBean. We want accounts to be created by the factory.
	 */
	private SimpleAccount() {

	}

	/**
	 * Sets the account manager responsible for persisting/managing this account. This may be
	 * invoked exactly once on any given account, any subsequent invocations should be ignored. The
	 * account manager member should not be serialized with implementing class object.
	 * 
	 * @param m
	 *            the AccountManager
	 */
	@Override
	public void registerAccountManager(AccountManager m) {
		if (accountManager == null) {
			accountManager = m;
		} else {
			LOG.info(String.format("Account: %s already assigned an account manager(%s)", name, accountManager));
		}

	}

	/**
	 * Incorporates the effect of an order in the balance.
	 * 
	 * @param order
	 *            the order to be reflected in the Account
	 * @param executionPrice
	 *            the price the order was executed at
	 */
	@Override
	public void reflectOrder(Order order, int executionPrice) {
		try {
			balance += order.valueOfOrder(executionPrice);
			if (accountManager != null) {
				accountManager.persist(this);
			} else {
				LOG.error("Account manager has not been initialized for Account: " + name);
			}
		} catch (final AccountException e) {
			LOG.error("Failed to persist account " + name + " after adjusting for order");
		}
	}

	/**
	 * Get the account name.
	 * 
	 * @return the name of the account
	 * 
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the account name. This operation is not generally used but is provided for JavaBean
	 * conformance.
	 * 
	 * @param accountName
	 *            the value to be set for the account name
	 * @throws AccountException
	 *             if the account name is unacceptable
	 */
	@Override
	public void setName(String accountName) throws AccountException {
		if (accountName.length() < MINIMUM_ACCOUNT_NAME_LENGTH) {
			throw new AccountException("insufficient account name length");
		}
		this.name = accountName;
	}

	/**
	 * Gets the hashed password.
	 * 
	 * @return the hashed password
	 */
	@Override
	public byte[] getPasswordHash() {
		//copy our original passwordHash, for safety
		int arrayLength = passwordHash.length;
		byte[] passwordHashCopy = new byte[arrayLength];
		System.arraycopy(passwordHash, 0, passwordHashCopy, 0, arrayLength);
		return passwordHashCopy;
	}

	/**
	 * Sets the hashed password.
	 * 
	 * @param passwordHash
	 *            the value to be st for the password hash
	 */
	@Override
	public void setPasswordHash(byte[] passwordHash) {
		//copy our original passwordHash, for safety
		int arrayLength = passwordHash.length;
		byte[] passwordHashCopy = new byte[arrayLength];
		System.arraycopy(passwordHash, 0, passwordHashCopy, 0, arrayLength);
		this.passwordHash = passwordHashCopy;

	}

	/**
	 * Gets the account balance, in cents.
	 * 
	 * @return the current balance of the account
	 */
	@Override
	public int getBalance() {
		return balance;
	}

	/**
	 * Sets the account balance.
	 * 
	 * @param balance
	 *            the value to set the balance to in cents
	 */
	@Override
	public void setBalance(int balance) {
		this.balance = balance;
	}

	/**
	 * Gets the full name of the account holder.
	 * 
	 * @return the account holders full name
	 */
	@Override
	public String getFullName() {
		return fullName;
	}

	/**
	 * Sets the full name of the account holder.
	 * 
	 * @param fullName
	 *            the account holders full name
	 */
	@Override
	public void setFullName(String fullName) {
		this.fullName = fullName;

	}

	/**
	 * Gets the account address.
	 * 
	 * @return the accounts Address
	 */
	@Override
	public Address getAddress() {
		return address;
	}

	/**
	 * Sets the account Address.
	 * 
	 * @param address
	 *            the address for the account
	 */
	@Override
	public void setAddress(Address address) {
		this.address = address;

	}

	/**
	 * Gets the phone number.
	 * 
	 * @return Sets the account phone number.
	 */
	@Override
	public String getPhone() {
		return phone;
	}

	/**
	 * Sets the account phone number.
	 * 
	 * @param phone
	 *            value for the account phone number
	 */
	@Override
	public void setPhone(String phone) {
		this.phone = phone;

	}

	/**
	 * Gets the email address.
	 * 
	 * @return the email address
	 */
	@Override
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the account email address.
	 * 
	 * @param email
	 *            the email address
	 */
	@Override
	public void setEmail(String email) {
		this.email = email;

	}

	/**
	 * Gets the account CreditCard.
	 * 
	 * @return the CreditCard
	 */
	@Override
	public CreditCard getCreditCard() {
		return creditCard;
	}

	/**
	 * Sets the account credit card.
	 * 
	 * @param card
	 *            the value to be set for the CreditCard
	 */
	@Override
	public void setCreditCard(CreditCard card) {
		this.creditCard = card;
	}

//	/**
//	 * String representation of a SimpleAccount.
//	 * 
//	 * @return fullName, name, or Bean (if name is not yet set)
//	 */
//	public String toString() {
//		final StringBuilder acctStr = new StringBuilder();
//		acctStr.append(String.format("%s: %s \n", "Name", name));
//		acctStr.append(String.format("%s: %s \n", "Full Name", fullName));
//		acctStr.append(String.format("%s: %s \n", "Phone", phone));
//		acctStr.append(String.format("%s: %s \n", "email", email));
//		return acctStr.toString();
//		//		return (fullName != null) ? fullName : ((name != null) ? name : "AnonymousSimpleAccountBean");
//	}

}
