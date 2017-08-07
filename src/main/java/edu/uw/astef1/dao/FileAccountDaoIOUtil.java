package edu.uw.astef1.dao;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.Address;
import edu.uw.ext.framework.account.CreditCard;

/**
 * Utility class for IO operations, for a FileAccountDao.
 * 
 * @author AndrewStefanich
 *
 */
class FileAccountDaoIOUtil {

	/** defines how we write null values to files */
	private static final String NULL_STRING = "<null>";

	/** String key values for reading/writing Address properties files */
	private static final String STREET_ADDRESS_PROP_KEY = "StreetAddress";
	private static final String CITY_PROP_KEY = "City";
	private static final String STATE_PROP_KEY = "State";
	private static final String ZIP_CODE_PROP_KEY = "Zip";

	/**
	 * Utility class, no constructor access. Methods are accessed statically.
	 */
	private FileAccountDaoIOUtil() {

	}

	/**
	 * Writes account address data to a file.
	 * 
	 * @param file
	 *            the file to write to.
	 * @throws IOException
	 *             error in writing to the file
	 */
	static void writeAddress(final File file, final Address address) throws IOException {
		try (OutputStream fileOutStream = new FileOutputStream(file)) {
			final Properties props = new Properties();
			props.setProperty(STREET_ADDRESS_PROP_KEY, address.getStreetAddress());
			props.setProperty(CITY_PROP_KEY, address.getCity());
			props.setProperty(STATE_PROP_KEY, address.getState());
			props.setProperty(ZIP_CODE_PROP_KEY, address.getZipCode());
			props.store(fileOutStream, "Address Data");
		}
	}

	/**
	 * Creates an Address from data found in a user-defined file.
	 * 
	 * @param file
	 *            the file to read from
	 * @return a new Address
	 */
	static Address readAddress(final File file) throws IOException {
		Address address = null;
		try (InputStream fileInStream = new FileInputStream(file);
				ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("context.xml")) {
			address = appContext.getBean(Address.class);
			Properties props = new Properties();
			props.load(fileInStream);
			address.setStreetAddress(props.getProperty(STREET_ADDRESS_PROP_KEY));
			address.setCity(props.getProperty(CITY_PROP_KEY));
			address.setState(props.getProperty(STATE_PROP_KEY));
			address.setZipCode(props.getProperty(ZIP_CODE_PROP_KEY));
			return address;
		}
	}

	/**
	 * Writes a credit card to persistent memory.
	 * 
	 * @param file
	 *            the file to write to
	 * @param card
	 *            the CreditCard information to store
	 */
	static void writeCreditCard(final File file, final CreditCard card) throws IOException {
		try (final PrintStream printer = new PrintStream(new FileOutputStream(file))) {
			printer.println(card.getIssuer());
			printer.println(card.getType());
			printer.println(card.getHolder());
			printer.println(card.getAccountNumber());
			printer.println(card.getExpirationDate());
		}
	}

	/**
	 * Gets a CreditCard from persistant memory.
	 * 
	 * @param file
	 *            the file you wish to obtain data from
	 * @return the credit card
	 */
	static CreditCard readCreditCard(final File file) throws IOException {
		CreditCard card = null;
		try (InputStream fileInStream = new FileInputStream(file);
				ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("context.xml")) {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(fileInStream));
			card = appContext.getBean(CreditCard.class);
			card.setIssuer(reader.readLine());
			card.setType(reader.readLine());
			card.setHolder(reader.readLine());
			card.setAccountNumber(reader.readLine());
			card.setExpirationDate(reader.readLine());
			return card;
		}
	}

	/**
	 * Writes account information to a binary file.
	 * 
	 * @param file
	 *            the destination file
	 * @param account
	 *            encapsulated account
	 */
	static void writeAccount(final File file, final Account account) throws IOException {
		try (final DataOutputStream dataOutStream = new DataOutputStream(new FileOutputStream(file))) {
			dataOutStream.writeUTF(account.getName());
			writeByteArray(dataOutStream, account.getPasswordHash());
			dataOutStream.writeInt(account.getBalance());
			writeString(dataOutStream, account.getFullName());
			writeString(dataOutStream, account.getPhone());
			writeString(dataOutStream, account.getEmail());
		}
	}

	/**
	 * Reads account information from a binary file.
	 * 
	 * @param file
	 *            the account file to read from.
	 * @return an Account object (excluding CreditCard and Address information)
	 */
	static Account readAccount(final File file) throws IOException, AccountException {
		Account account = null;
		try (DataInputStream dataInStream = new DataInputStream(new FileInputStream(file));
				ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("context.xml")) {
			account = appContext.getBean(Account.class);
			account.setName(dataInStream.readUTF());
			account.setPasswordHash(readByteArray(dataInStream));
			account.setBalance(dataInStream.readInt());
			account.setFullName(readString(dataInStream));
			account.setPhone(readString(dataInStream));
			account.setEmail(readString(dataInStream));
			return account;
		}
	}

	/**
	 * Utility method for null-checking when writing data to a stream.
	 * 
	 * @param dataOut
	 *            stream to write to
	 * @param str
	 *            the String to write
	 * @throws IOException
	 *             an error occurs during writing to the stream
	 */
	private static void writeString(final DataOutputStream dataOut, final String str) throws IOException {
		dataOut.writeUTF(str == null ? NULL_STRING : str);
	}

	/**
	 * Utility method for null-checking when reading data from a stream.
	 * 
	 * @param dataIn
	 *            stream to read from
	 * @return the String read from the stream
	 * @throws IOException
	 *             if an exception occurs during reading
	 */
	private static String readString(final DataInputStream dataIn) throws IOException {
		final String str = dataIn.readUTF();
		return NULL_STRING.equals(str) ? null : str;
	}

	/**
	 * Utility method for reading byte arrays from a file (for account passwords)
	 * 
	 * @param dataIn
	 *            Input
	 * @return the byte array
	 * @throws IOException
	 *             for any errors in reading from the stream.
	 */
	private static byte[] readByteArray(final DataInputStream dataIn) throws IOException {
		byte[] byteArray = null;
		final int arrayLength = dataIn.readInt();

		if (arrayLength >= 0) {
			byteArray = new byte[arrayLength];
			dataIn.readFully(byteArray);
		}
		return byteArray;
	}

	/**
	 * Writes a byte[] to an output stream. Writes -1, if the array argument is null.
	 * 
	 * @param dataOut
	 *            outputstream to write to
	 * @param byteArray
	 *            array to analyze/write
	 * @throws IOException
	 *             any errors may occure in writing to the stream
	 */
	private static void writeByteArray(final DataOutputStream dataOut, final byte[] byteArray) throws IOException {
		final int arrayLength = (byteArray == null) ? -1 : byteArray.length;
		dataOut.writeInt(arrayLength);
		if (arrayLength > 0) {
			dataOut.write(byteArray);
		}
	}

} // end of FileAccountDaoUtil class
