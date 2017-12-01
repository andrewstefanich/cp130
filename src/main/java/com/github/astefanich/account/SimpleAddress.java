package com.github.astefanich.account;

import edu.uw.ext.framework.account.Address;

/**
 * Implementation class for {@link Address} interface
 * 
 * @author AndrewStefanich
 *
 */
public class SimpleAddress implements Address {


	/** version ID  */
	private static final long serialVersionUID = -5823405345344158634L;

	/** the city */
	public String city;

	/** the state */
	public String state;

	/** the street number */
	public String streetAddress;

	/** the ZIP code */
	public String zipCode;
	
	/**
	 * No-argument constructor, for JavaBean
	 */
	public SimpleAddress(){
		
	}

	/**
	 * Gets the street address.
	 * 
	 * @return the street address
	 */
	@Override
	public String getStreetAddress() {
		return streetAddress;
	}

	/**
	 * Sets the street address.
	 * 
	 * @param streetAddress
	 *            the street address
	 */
	@Override
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;

	}

	/**
	 * Gets the city.
	 * 
	 * @return the city
	 */
	@Override
	public String getCity() {
		return city;
	}

	/**
	 * Sets the city.
	 * 
	 * @param city
	 *            the city
	 */
	@Override
	public void setCity(String city) {
		this.city = city;

	}

	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	@Override
	public String getState() {
		return state;
	}

	/**
	 * Sets the state.
	 * 
	 * @param state
	 *            the state
	 */
	@Override
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * Gets the ZIP code.
	 * 
	 * @return the ZIP code
	 */
	@Override
	public String getZipCode() {
		return zipCode;
	}

	/**
	 * Sets the ZIP code.
	 * 
	 * @param zip
	 *            the ZIP code
	 */
	@Override
	public void setZipCode(String zip) {
		this.zipCode = zip;
	}
	
//	/**
//	 * Prints this address in the form: <br>
//	 * Street number <br>
//	 * City, State Zip
//	 * 
//	 * @see java.lang.Object#toString()
//	 * @return the formatted address.
//	 */
//	@Override
//	public String toString() {
//		final String addressString = String.format("%s \n %s, %s %s", streetAddress, city, state, zipCode);
//		return addressString;
//	}

}
