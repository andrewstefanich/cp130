package com.github.astefanich.security;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class for cryptography operations.
 * 
 * @author AndrewStefanich
 */
public class CodecIOUtil {

	/** raw bytes to be written to/read from file */
	private byte[] symKey, orders, signature;

	/**
	 * This class should initially be instantiated by the sender, with ciphertext
	 * 
	 * @param symKeyCipherText
	 *            encrypted session key
	 * @param orderCipherText
	 *            encrypted order date
	 * @param signature
	 *            sender's signature
	 */
	CodecIOUtil(byte[] symKeyCipherText, byte[] orderCipherText, byte[] signature) {
		this.symKey = symKeyCipherText;
		this.orders = orderCipherText;
		this.signature = signature;
	}

	/**
	 * Writes the contents of this instance to a given file
	 * 
	 * @param file
	 *            the file to write to
	 * @throws IOException
	 *             if unable to access/write to the file
	 */
	void writeFile(final File file) throws IOException {
		try (DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(file))) {
			writeByteArray(dataOut, symKey);
			writeByteArray(dataOut, orders);
			writeByteArray(dataOut, signature);
			symKey = null;
			orders = null;
			signature = null;
		}
	}

	/**
	 * Writes a byte[] to an output stream, prepending with the array length
	 * 
	 * @param dataOut
	 *            the output stream
	 * @param byteArray
	 *            the bytes to write
	 * @throws IOException
	 *             if unable to write to the stream
	 */
	private void writeByteArray(final DataOutputStream dataOut, final byte[] byteArray) throws IOException {
		final int arrayLength = (byteArray == null) ? -1 : byteArray.length;
		dataOut.writeInt(arrayLength);
		if (arrayLength > 0) {
			dataOut.write(byteArray);
		}
	}

	/**
	 * Reads bytes from a file, and resets the byte arrays held by this instance. <br>
	 * Newly instantiated data should then be accessed via the getters.
	 * 
	 * @param file
	 *            the file to read from
	 * @throws IOException
	 *             if unable to read from the stream/file
	 */
	void readFile(final File file) throws IOException {
		try (DataInputStream dataIn = new DataInputStream(new FileInputStream(file))) {
			symKey = readByteArray(dataIn);
			orders = readByteArray(dataIn);
			signature = readByteArray(dataIn);
		}
	}

	/**
	 * Gets the current key data
	 * 
	 * @return the symKeyData
	 */
	byte[] getSymKeyData() {
		return symKey;
	}

	/**
	 * Gets the current order data
	 * 
	 * @return the orderData
	 */
	byte[] getOrderData() {
		return orders;
	}

	/**
	 * Gets the current signature data
	 * 
	 * @return the signatureData
	 */
	byte[] getSignatureData() {
		return signature;
	}

	/**
	 * Reads bytes from an input stream
	 * 
	 * @param dataIn
	 *            input stream to read from
	 * @return the bytes
	 * @throws IOException
	 *             if unable to read
	 */
	private byte[] readByteArray(final DataInputStream dataIn) throws IOException {
		byte[] byteArray = null;
		final int arrayLength = dataIn.readInt();

		if (arrayLength >= 0) {
			byteArray = new byte[arrayLength];
			dataIn.readFully(byteArray);
		}
		return byteArray;
	}

} //END OF UTIL CLASS
