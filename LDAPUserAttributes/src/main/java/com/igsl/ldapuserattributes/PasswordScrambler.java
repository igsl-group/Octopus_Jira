package com.igsl.ldapuserattributes;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class PasswordScrambler {
	
	private static final String DELIMITER = "|";
	private static final int KEY_LENGTH = 256;
	private static final int IV_LENGTH = 16;
	private static final int SALT_LENGTH = 32;
	private static final int ITERATION_COUNT = 65536;
	private static final String ALGORITHM = "PBEWithHmacSHA512AndAES_256";
	private static final String SECRET = "ashdqwo987iuhg4*(&*TGYIBKiERD(HJGYTygutfgh@#$TFrtyu";
	private static final SecureRandom random = new SecureRandom();
	
	private static byte[] generateRandom(int size) throws Exception {
		byte[] data = new byte[size];
		random.nextBytes(data);
		return data;
	}
	
	private static SecretKey generateSecretKey(byte[] salt) throws Exception {
		byte[] iv = generateRandom(IV_LENGTH);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
		KeySpec keySpec = new PBEKeySpec(SECRET.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
		SecretKey key = keyFactory.generateSecret(keySpec);
		return key;
	}
	
	public static String scramble(String password) throws Exception {
		try {
			Encoder enc = Base64.getEncoder();
			byte[] iv = generateRandom(IV_LENGTH);
			byte[] salt = generateRandom(SALT_LENGTH);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			Key key = generateSecretKey(salt);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);   
			PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 0, ivSpec);
			cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
			byte[] cipherText = cipher.doFinal(password.getBytes());
			return enc.encodeToString(salt) + DELIMITER + enc.encodeToString(iv) + DELIMITER + enc.encodeToString(cipherText);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw e;
		}
	}
	
	public static String unscramble(String encryptedPassword) throws Exception {
		Decoder decoder = Base64.getDecoder();
		String[] tokens = encryptedPassword.split(Pattern.quote(DELIMITER));
		if (tokens.length == 3) {
			byte[] salt = decoder.decode(tokens[0]);
			byte[] iv = decoder.decode(tokens[1]);
			byte[] cipherText = decoder.decode(tokens[2]);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			Key key = generateSecretKey(salt);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);   
			PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 0, ivSpec);
			cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
			byte[] plainText = cipher.doFinal(cipherText);
			return new String(plainText);
		} else {
			throw new Exception("Invalid encrypted data");
		}
	}
	
//	public static void main(String[] args) throws Exception {
//		String p = "The quick red fox jumps over the lazy brown dog";
//		System.out.println("p: [" + p + "]");
//		String enc = scramble(p);
//		System.out.println("enc: [" + enc + "]");
//		String dec = unscramble(enc);
//		System.out.println("dec: [" + dec + "]");		
//	}
}
