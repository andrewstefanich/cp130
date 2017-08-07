package edu.uw.astef1.security;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import edu.uw.ext.framework.order.ClientOrder;
import edu.uw.ext.framework.order.ClientOrderCodec;

/**
 * @author AndrewStefanich
 */
public class FileClientOrderCodec implements ClientOrderCodec {

	/** algorithm for symmetric key creation */
	private static final String SYMMETRIC_ALGORITHM = "AES";

	/** bit size/length of symmetric key */
	private static final int AES_KEY_SIZE = 128;

	/** store type for creating trust stores */
	private static final String STORE_TYPE = "JCEKS";

	/** algorithm for signature */
	private static final String SIGNING_ALGORITHM = "MD5withRSA";

	private CodecIOUtil cipherIO;

	/**
	 * Writes the client order file. <br>
	 * Key stores will be accessed as resources from the classpath.
	 * 
	 * @param orders
	 *            the orders to be submitted by the client
	 * @param orderFile
	 *            the file the encrypted order list is to be stored in
	 * @param senderKeyStoreName
	 *            the name of the sender's key store resource
	 * @param senderKeyStorePassword
	 *            the sender's key store password
	 * @param senderKeyName
	 *            the alias of the sender's private key
	 * @param senderKeyPassword
	 *            the password for the sender's private key
	 * @param senderTrustStoreName
	 *            the name of the sender's trust key store resource
	 * @param senderTrustStorePassword
	 *            the sender's trust store key
	 * @param recipientCertName
	 *            the alias of the recipient's certificate key
	 * @throws GeneralSecurityException
	 *             if any cryptographic operations fail
	 * @throws IOException
	 *             if unable to write either of the files
	 */
	@Override
	public void encipher(List<ClientOrder> orders, File orderFile, String senderKeyStoreName,
			char[] senderKeyStorePassword, String senderKeyName, char[] senderKeyPassword, String senderTrustStoreName,
			char[] senderTrustStorePassword, String recipientCertName) throws GeneralSecurityException, IOException {

		//deletes any existing file
		if (orderFile.exists()) {
			orderFile.delete();
		}

		//convert our collection to bytes, and keep a reference to the unencrypted data
		ByteArrayOutputStream ordersBytes = new ByteArrayOutputStream();
		try (ObjectOutputStream objOut = new ObjectOutputStream(new BufferedOutputStream(ordersBytes))) {
			objOut.writeObject(orders);
		}
		final byte[] unencryptedOrderData = ordersBytes.toByteArray();

		//secret session key for encryption
		final SecretKey symKey = generateAesSecretKey();

		//encrypt order data
		byte[] encryptedOrderData = encrypt(symKey, unencryptedOrderData);

		//Retrieve the recipients public key from the truststore, and encrypt the session key
		final PublicKey recipientPublicKey = getPublicKey(senderTrustStoreName, senderTrustStorePassword,
				recipientCertName);
		final byte[] encryptedSymKey = encrypt(recipientPublicKey, symKey.getEncoded());

		//Sign with sender's private key
		final PrivateKey senderPrivateKey = getPrivateKey(senderKeyStoreName, senderKeyStorePassword, senderKeyName,
				senderKeyPassword);
		final Signature signer = Signature.getInstance(SIGNING_ALGORITHM);
		signer.initSign(senderPrivateKey);
		signer.update(unencryptedOrderData);
		final byte[] signature = signer.sign();

		//write the resulting data to a file
		cipherIO = new CodecIOUtil(encryptedSymKey, encryptedOrderData, signature);
		cipherIO.writeFile(orderFile);
	}

	/**
	 * Read an encrypted order list and signature from file and verify the
	 * order list data.
	 * Keystores will be accessed as resources, i.e. on the classpath.
	 *
	 * @param orderFile
	 *            the file the encrypted order list is stored in
	 * @param recipientKeyStoreName
	 *            the name of the recipient's key store resource
	 * @param recipientKeyStorePassword
	 *            the recipient's key store password
	 * @param recipientKeyName
	 *            the alias of the recipient's private key
	 * @param recipientKeyPassword
	 *            the password for the recipient's private key
	 * @param recipientTrustStoreName
	 *            the name of the trust store resource
	 * @param recipientTrustStorePassword
	 *            the trust store password
	 * @param signerCertName
	 *            the name of the signer's certificate
	 * @return the client order list from the file
	 * @throws GeneralSecurityException
	 *             if any cryptographic operations fail
	 * @throws IOException
	 *             if unable to write either of the files
	 */
	@Override
	public List<ClientOrder> decipher(File orderFile, String recipientKeyStoreName, char[] recipientKeyStorePassword,
			String recipientKeyName, char[] recipientKeyPassword, String recipientTrustStoreName,
			char[] recipientTrustStorePassword,
			String signerCertName) throws GeneralSecurityException, IOException {

		cipherIO.readFile(orderFile);

		//load the private key of the receiver
		PrivateKey recipientPrivateKey = getPrivateKey(recipientKeyStoreName, recipientKeyStorePassword,
				recipientKeyName, recipientKeyPassword);

		//retrieve the session key using the private key, and reconstruct it
		final byte[] symKeyData = decrypt(recipientPrivateKey, cipherIO.getSymKeyData());
		final SecretKey secretKey = new SecretKeySpec(symKeyData, 0, 16, SYMMETRIC_ALGORITHM);

		//decrypt data using private key
		byte[] orderData = decrypt(secretKey, cipherIO.getOrderData());

		//get the senders public key and verify the signature
		final PublicKey senderPublicKey = getPublicKey(recipientTrustStoreName, recipientTrustStorePassword,
				signerCertName);
		final Signature verifier = Signature.getInstance(SIGNING_ALGORITHM);
		verifier.initVerify(senderPublicKey);
		verifier.update(orderData);
		if (!verifier.verify(cipherIO.getSignatureData())) {
			throw new GeneralSecurityException("could not validate signature");
		}

		//Reconstruct the list of client orders from the order data
		List<ClientOrder> orders = null;
		try (ObjectInputStream objIn = new ObjectInputStream(
				new BufferedInputStream(new ByteArrayInputStream(orderData)))) {
			orders = ((List<ClientOrder>) objIn.readObject());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return orders;
	}

	/**
	 * Generates a symmetric key for one-time use.
	 * 
	 * @return the secret key
	 * @throws NoSuchAlgorithmException
	 *             if the given algorithm is not available by providers
	 */
	private static SecretKey generateAesSecretKey() throws NoSuchAlgorithmException {
		final KeyGenerator keyGen = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
		keyGen.init(AES_KEY_SIZE);
		final SecretKey symKey = keyGen.generateKey();
		return symKey;
	}

	/**
	 * @param storeFileName
	 *            name of the keystore file you wish to load
	 * @param storePassword
	 *            password for access
	 * @return the keystore
	 * @throws GeneralSecurityException
	 *             if any cryptographic operations fail
	 * @throws IOException
	 *             if unable to write either of the files
	 */
	private static KeyStore loadKeyStore(String storeFileName, char[] storePassword)
			throws GeneralSecurityException, IOException {
		KeyStore keyStore = null;
		try (InputStream in = FileClientOrderCodec.class.getClassLoader().getResourceAsStream(storeFileName)) {
			keyStore = KeyStore.getInstance(STORE_TYPE);
			keyStore.load(in, storePassword);
		}
		return keyStore;
	}

	/**
	 * @param encryptingKey
	 *            key used for encryption
	 * @param plainText
	 *            the input data to encrypt
	 * @return the encrypted data
	 * @throws GeneralSecurityException
	 *             if any cryptographic operations fail
	 */
	private static byte[] encrypt(Key encryptingKey, byte[] plainText) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(encryptingKey.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, encryptingKey);
		byte[] encryptedData = cipher.doFinal(plainText);
		return encryptedData;
	}

	/**
	 * @param decryptingKey
	 *            key used for decryption
	 * @param cipherText
	 *            the cipher text to decrypt
	 * @return the decrypted data
	 * @throws GeneralSecurityException
	 *             if any cryptographic operations fail
	 */
	private static byte[] decrypt(Key decryptingKey, byte[] cipherText) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(decryptingKey.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, decryptingKey);
		byte[] decryptedData = cipher.doFinal(cipherText);
		return decryptedData;
	}

	/**
	 * @param trustStoreName
	 *            name of the trust store file
	 * @param trustStorePassWord
	 *            password for access
	 * @param certificateAlias
	 *            name of the certificate, from which to extract public key
	 * @return the public key
	 * @throws GeneralSecurityException
	 *             if any cryptographic operations fail
	 * @throws IOException
	 *             if unable to load key store, certificate, or the key
	 */
	private static PublicKey getPublicKey(String trustStoreName, char[] trustStorePassWord, String certificateAlias)
			throws GeneralSecurityException, IOException {
		final KeyStore trustStore = loadKeyStore(trustStoreName, trustStorePassWord);
		final Certificate recipientCertificate = trustStore.getCertificate(certificateAlias);
		return recipientCertificate.getPublicKey();
	}

	/**
	 * @param keyStoreName
	 *            name of the keypair store
	 * @param keyStorePassword
	 *            password for access
	 * @param privateKeyName
	 *            name of the private key
	 * @param privateKeyPassword
	 *            password for access
	 * @return the private key
	 * @throws GeneralSecurityException
	 *             if any cryptographic operations fail
	 * @throws IOException
	 *             if unable to load key store
	 */
	private static PrivateKey getPrivateKey(String keyStoreName, char[] keyStorePassword, String privateKeyName,
			char[] privateKeyPassword) throws GeneralSecurityException, IOException {
		final KeyStore keyStore = loadKeyStore(keyStoreName, keyStorePassword);
		final PrivateKey privateKey = (PrivateKey) keyStore.getKey(privateKeyName, privateKeyPassword);
		return privateKey;
	}

}
