package com.ygame.framework.crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class TripleDESEncryption implements IEncryption {
	static final byte[] SALT = { (byte) 0x09, /*
											 * snip - randomly chosen but static
											 * salt
											 */};
	static final int ITERATIONS = 11;
//	static String padding;
	

	private static SecretKey getKey(String key) {
		byte[] bKey = hexToBytes(key);
		try {
			DESedeKeySpec keyspec = new DESedeKeySpec(bKey);
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance("DESede");

			SecretKey lclSK = keyFactory.generateSecret(keyspec);

			return lclSK;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public static String encrypt(String key, String input)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, UnsupportedEncodingException, NoSuchProviderException {

		
		byte[] bytes = null;
		SecretKey sKey = null;
		Security.addProvider(new BouncyCastleProvider());
		Cipher encipher = Cipher.getInstance("DESede/ECB/PKCS5Padding", "BC");
		bytes = input.getBytes("UTF-8");
		sKey = getKey(key);
		// Encrypt
		byte[] enc;
		encipher.init(Cipher.ENCRYPT_MODE, sKey);
		enc = encipher.doFinal(bytes);
		return bytesToHex(enc);
	}
	
	public static String decrypt(String key, String cipher) throws NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException
	{
		byte[] bytes = null;
		SecretKey sKey = null;
		Security.addProvider(new BouncyCastleProvider());
		Cipher encipher = Cipher.getInstance("DESede/ECB/PKCS5Padding", "BC");
		bytes = hexToBytes(cipher);					     
		sKey = getKey(key);
		// Encrypt
		byte[] enc;
		encipher.init(Cipher.DECRYPT_MODE, sKey);
		enc = encipher.doFinal(bytes);
		return new String (enc);

	}

	private static String bytesToHex(final byte[] bytes) {
		final StringBuilder buf = new StringBuilder(bytes.length * 2);
		for (final byte b : bytes) {
			final String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1) {
				buf.append("0");
			}
			buf.append(hex);
		}
		return buf.toString();
	}

	private static byte[] hexToBytes(final String hex) {
		final byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2),
					16);
		}
		return bytes;
	}

	public static void main(String[] args) {
		String key = "9271cd9962ab576e138e9c61d3bac9a51889249cd1277dc3";//generateKey();//
		String text = "123456";
		String cipher;
		try {
			cipher = encrypt(key, text);
			System.out.println(cipher);
			text = decrypt(key, cipher);
			System.out.println(text);
			Scanner sc = new Scanner(System.in);
			sc.next();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

			
	}
}
