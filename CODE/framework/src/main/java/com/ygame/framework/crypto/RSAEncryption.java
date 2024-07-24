package com.ygame.framework.crypto;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class RSAEncryption implements IEncryption {

	/**
	 * 
	 */
	String publicKeyStr;
	String privateKeyStr;
	private String padding = "NOPadding";

	public RSAEncryption() {
		super();
		// TODO Auto-generated constructor stub
	}
	

	/**
	 * @return the publicKeyStr
	 */
	public String getPublicKeyStr() {
		return publicKeyStr;
	}

	/**
	 * @param publicKeyStr
	 *            the publicKeyStr to set
	 */
	public void setPublicKeyStr(String publicKeyStr) {
		this.publicKeyStr = publicKeyStr;
	}

	/**
	 * @return the privateKeyStr
	 */
	public String getPrivateKeyStr() {
		
		return privateKeyStr;
	}

	/**
	 * @param privateKeyStr
	 *            the privateKeyStr to set
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public void setPrivateKeyStr(String privateKeyStr)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		this.privateKeyStr = privateKeyStr;

	}

	public String encrypt(String clear_text) throws InvalidKeySpecException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
		byte[] bpublicKey = Base64Utils.base64Decode(publicKeyStr);
		Security.addProvider(new BouncyCastleProvider());
		PublicKey key = KeyFactory.getInstance("RSA").generatePublic(
				new X509EncodedKeySpec(bpublicKey));
		Cipher cipher = Cipher.getInstance("RSA/ECB/" + padding, "BC");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipherData = cipher.doFinal(clear_text.getBytes());
		return Base64Utils.base64Encode(cipherData);
		// return "";
	}

	public String decrypt(String cipher_text) throws InvalidKeySpecException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
		byte[] bprivateKey = Base64Utils.base64Decode(privateKeyStr);
		//PKCS8EncodedKeySpec pkcs8EncodedKeySpec
		Security.addProvider(new BouncyCastleProvider());
		PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate((
				new PKCS8EncodedKeySpec(bprivateKey)));
		Cipher cipher = Cipher.getInstance("RSA/ECB/" + padding, "BC");
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] cipherData = cipher.doFinal(Base64Utils
				.base64Decode(cipher_text));
		return new String(cipherData);
	}
	
	public static void main(String[] args) {
		
		
		RSAEncryption rsa = new RSAEncryption();
		String text = "";
		try {					
			rsa.setPublicKeyStr("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk+X7FUybtP7uSKsVeqzgnXW4PFVt2NzPRSbsLondD3wu3Ct+N8CRueYIx/udkikHC9QuOYr07ZSMYVuzYzVKw/a/V0F2Yf3lVXlOvWHoPRgddgufA5i7u+saZMks7AqFd5B7D1vobpBHWOnsvQdNfqSk91lkvqDbnZBO6t86ME/A0D7/InC65TDrdLLCTEmd15PaFwnMiwuQDZWTcQ97hE8Lmqfy3yR8w6Ep3dvrQLAVKf/cYyMhegtX6i3dfnlofNPkAsLdxMXUX1B7i2iMJ8n5OV4jLkfAd3/RvmO7BKl5vLOGLVipoLCcXp0izwNl67Svh8V+Cxe63UnB4xoxNwIDAQAB");								 

			rsa.setPrivateKeyStr("MIIEowIBAAKCAQEAs+JvfyTOMHqvjxHJyDZGHZpz3atV7qcOT8mijXGGG3S+8Bb2p2kREGJwrzC2IIErCQUcZ3Wa3wTugKQDxqXESPt76HN2353ufegbvTI9kYgK0MLFpY8OZAMsaTytVrvUEVHjqGXZO4z7oVTqByuBwcZAvK+sN39+MqisS6ZejACbbQLkWZgcSgt5wBAaDaEa2lvRYcVbNyO/mqTU6SSfd+w78uM07BpmxhimOMwf+l/qs+Z04LUm4Ay7b+AHHAwbaHeehC1wInzNDfipgR0H0FCa/LOnEblj2HVpptB/NY4XNG+CDHTBKkxzEw92D/Nj1JIlr1oP0l+/VdAnxxiWuQIDAQABAoIBACCO/YtPyBoyOpB0sccRO8Oj7xDSMC2QAyEh59AeGh3iFaUzItXOSI5kouyyrmRXvMhXKnr3aF6xb9HM3foYityC2DyMmZmD5mTXnB20eDxdGUgL2OZMbFhh0qoBl4Y5XPr9jW0vchA7Rlg/HFTL5SKDlbgRO+SXqOnWackPhdFlBPKC80NbJEmHUNQ/gh8MziiknLi7Q9Zb0DN6jr8bFLh04nmp/l4eE5lLMcYH1EprKu7ub94EjKGOIdF4niDRcxoADu1XxEiG4KpqEMfq1mfOLmEkGnGUiaFMALKoJfaBOjTCyY+5EuhdbO99ipDJDg9dMri98fbr5W/0QKE6huECgYEA4iyTojsaDvw7pNvsjgZ8BgfOS8Vl4+I8FomGy1A58PtZy2jskr4bGQ5Ooy5S/JD8BrB2J+UTTtjOdDRreUIDVsWM16eceh6pTC6t78W+kt5cdzKL2gNk7oyYRd++PdvEgk4pWmdMJQeTbk0vjtY+YBLVNbqhBixITIJSK3a7Qr0CgYEAy5sqX/s4Lb+DCW4UpdPN0p9NOQK2eHZHCeWXtZDvburcs/2sZHRB0u7C/xYtz9e6A9KMbQAGnZVRzSz8e7E2DEF5G8a19bTbHhZaJu9NZoPJ9yx4cEMj6rQRPNdTVO7CtBBAD9nu7jNgYHmCSAh3XA8wRGJXsJWxUSmF1CPswa0CgYEAtUoL0EbVXVuap0tM8o6KDP9mwV9pi18E0P/NVtxohRvD3wKsVfchm1TXfX8dL6s7CFWbb+DzOAkaexSlbN2X9Y6FlTqZd11qUFE+RGnZlrm7niOa69Y6qedyCtgMEmM8wUHLbdZMt4W6C/i8fR6HgtS1p8p/VYZbMVBocsRJ13UCgYBoc5jAlquSZJ4/tzZmgAsmE01VgvvDPsJg5GT443rBx0EkIW5QNogSQyTMIj+sUbp+/YPG422BaDoH7R02A4p7rRcJVd7BR1/lxofHU5q8jb2NzUWYKgmB/ZCeSsTYXIiMQY3r09zMXJUDyTI57AX3t18GpgDgeVr3DTs3wppSHQKBgHdq2g8zCuq9aui+vQO43gJEh7BggiHYYYGweUu/Lx1EAMrPOrKwnTUUxEWqnjErPv08ECQmewiePS8bLu8PGMW02v6h8d3lZFIza4E0J4V/ZO0CVPEn3hh8CxGSyxqrC2dCw6/gTqQ3pAP6z2lxkFuehxLVXWvPE1jtOQIktijD");
			String cipher = "hh9q/5Qkj3A9C5LNEyKFi7/h2wL7OKV8cC1LT7/p8rkdcjH0OQVIM2ViaJdeddS43UFprb95vH6llxOqLOFVZW5dY7gQfr50I/z79po6gNCHdTR//hGmRYxvFZbGBlY4s4t7Ba33UWrJrazIiWMGpcNnRmm0mY2Whq0xy7gcP80Zn0gfS93/Qv2wo4gemGCaiuyDbbFCGyKzi9pRk06Q2Ho151M+q8HzSWXeP1muTSVMQHoYJt1XKkeGaTY0Ns8XN1fJ7OowAfB8Gxb+Qm6tRTozsI965H2oWI51CSrH99sUHmYvG1gj0KbdZSRwXVYzRdklVGcu2ASaCuxre8Hi7Do6Og==";
				//"cUfEzGccY9wOnT5e+mIdOTJI4hosXTtxIFYyPiC2OUaG8V6ST18hTZ0JjL0BVKGEt3e0J6fomWSDe6zjUUv+RaDBpXj7hyI4O1YMRQ2JOhctuCRCDZB2/2469xDUMmnkqbAk5XW6jWB/KyfN4YrvHNrm6feZXmbJWYJC1VvA4ZmJgdRIwlgjWVk6hYpr4qlWVOYfdDCJRejCY9UbC9PEmoms81qh9iiYxXjRR3mY59d/kwWbdqqSOwzKAUHFv7RtzGWA0QEde4dVf6BCC/pF3x/EO2Hb8D35RxZDFHM9iBwNKV3QAOF5Gk62jl8QFou0icd5X3oQSPRhbaseL7SpAg=="; 				
				//"WAlXEczrqt4Q5Dl5J7pfTvJ33v4YGDFJaxcj3KVuzCEG2TKU67rPX+xxdknvq6pLAYtst9z0UngJuB8wMTPtMf8fze3Kc6aZh9Kristb0VCtxjslC7Eb0/16kKye4uALG2ZOlZQP62tnlE48ZDEurfofVVGWjTsyakq8uJZDZwKfzuMB1/qotu32lye51WRU3W9EX+GCDvOk9o55ARA07q/LWwmp+BUDRV7c+J9f7gtPKW33oNsNCU83tkx/CC4hEywQ4o31H50Rp95llbG167tuoeu8RZ+efJ4fQ1xgzEHyUFuOoT/MBo1g4N7odBiPMy8MyH9w4EO0OJCLvo1tag==";//rsa.encrypt(text);
			System.out.println("Public Key: " + rsa.getPublicKeyStr());
			//System.out.println("Private Key: " + rsa.privateKeyStr);
			rsa.setPadding("pkcs1padding");
			System.out.println("ciper: " + cipher);
			text = rsa.decrypt(cipher);
			System.out.println("Clear text: " + text);
			Scanner sc = new Scanner(System.in);
			sc.next();			
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
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
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setPadding(String padding) {
		this.padding = padding;
	}

	public String getPadding() {
		return padding;
	}
		
}
