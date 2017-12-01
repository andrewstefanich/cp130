package com.github.astefanich.account;

import edu.uw.ext.framework.account.CreditCard;

/**
 * Implementation class for the {@link CreditCard} interface.
 * 
 * @author AndrewStefanich
 *
 */
public class SimpleCreditCard implements CreditCard {

	/** version ID */
	private static final long serialVersionUID = 3634870611007133788L;

	/** the account number */
	public String accountNumber;

	/** expiration date of the card */
	public String expirationDate;

	/** the card holder */
	public String holder;

	/** bank issuing the card */
	public String issuer;

	/** type of card (e.g. Visa, Mastercard */
	public String type;
	
	/**
	 * No-argument constructor, for JavaBean
	 */
	public SimpleCreditCard(){
		
	}

	/**
	 * Gets the card issuer.
	 * 
	 * @return the card issuer
	 */
	@Override
	public String getIssuer() {
		return issuer;
	}

	/**
	 * Sets the card issuer.
	 * 
	 * @param issuer
	 *            the card issuer
	 */
	@Override
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	/**
	 * Gets the card type.
	 * 
	 * @return the card type
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * Sets the card type.
	 * 
	 * @param type
	 *            the card type
	 */
	@Override
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the card holder's name.
	 * 
	 * @return the card holders name
	 */
	@Override
	public String getHolder() {
		return holder;
	}

	/**
	 * Sets the card holder's name.
	 * 
	 * @param name
	 *            the card holders name
	 */
	@Override
	public void setHolder(String name) {
		this.holder = name;
	}

	/**
	 * Gets the card account number.
	 * 
	 * @return the account number
	 */
	@Override
	public String getAccountNumber() {
		return accountNumber;
	}

	/**
	 * Sets the card account number.
	 * 
	 * @param accountNumber
	 *            the account number
	 */
	@Override
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	/**
	 * Gets the card expiration date.
	 * 
	 * @return the expiration date
	 */
	@Override
	public String getExpirationDate() {
		return expirationDate;
	}

	/**
	 * Sets the card expiration date.
	 * 
	 * @param expDate
	 *            the expiration date
	 */
	@Override
	public void setExpirationDate(String expDate) {
		this.expirationDate = expDate;
	}
	
//	/**
//	 * Creates a string representation of a credit card. <br>
//	 * Card Holder: <br>
//	 * Issuer: <br>
//	 * Type: <br>
//	 * Last4: <br>
//	 * Expiration: 
//	 * 
//	 * @see java.lang.Object#toString()
//	 * @return the formatted address.
//	 */
//	@Override
//	public String toString() {
//		int lastDash = accountNumber.lastIndexOf("-");
//		final String lastFour = accountNumber.substring(++lastDash);
//		StringBuilder sb = new StringBuilder();
//		sb.append(String.format("Card Holder: %s \n", holder));
//		sb.append(String.format("Issuer: %s \n", issuer));
//		sb.append(String.format("Type: %s \n", type));
//		sb.append(String.format("Last Four: %s \n", lastFour));
//		sb.append(String.format("Expiration: %s \n", expirationDate));
//		return sb.toString();
//	}

}
