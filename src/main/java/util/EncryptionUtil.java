package util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

public class EncryptionUtil
{
	private final static String RSA = "RSA/NONE/OAEPWithSHA256AndMGF1Padding";
	private final static String AES = "AES/CTR/NoPadding";

	public static byte[] crypt(Cipher cipher, byte[] bytes)
	{
		try
		{
			return cipher.doFinal(bytes);
		}
		catch(IllegalBlockSizeException e)
		{
			e.printStackTrace();
		}
		catch(BadPaddingException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] decryptRSA(byte[] bytes, Key key)
	{
		Cipher cipher = initializeRSACipher(Cipher.DECRYPT_MODE, key);
		return crypt(cipher, bytes);
	}

	public static byte[] encryptRSA(byte[] bytes, Key key)
	{
		Cipher cipher = initializeRSACipher(Cipher.ENCRYPT_MODE, key);
		return crypt(cipher, bytes);
	}

	public static byte[] generateRandomNumber(int bytes)
	{
		SecureRandom secureRandom = new SecureRandom();
		final byte[] number = new byte[bytes];
		secureRandom.nextBytes(number);
		return number;
	}

	public static SecretKey generateSecretKey(int keySize) throws NoSuchAlgorithmException
	{
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		generator.init(keySize);
		return generator.generateKey();
	}

	public static PrivateKey getPrivateKeyFromFile(File privateKey, final String password) throws Exception
	{
		PEMReader in = null;
		try
		{
			in = new PEMReader(new FileReader(privateKey), new PasswordFinder()
			{
				@Override
				public char[] getPassword()
				{
					return password.toCharArray();
				}
			});
			KeyPair keyPair = (KeyPair)in.readObject();
			return keyPair.getPrivate();
		}
		finally
		{
			if(in != null)
			{
				in.close();
			}
		}
	}

	public static PublicKey getPublicKeyFromFile(File keyFile) throws IOException
	{
		PEMReader in = null;
		try
		{
			in = new PEMReader(new FileReader(keyFile));
			return (PublicKey)in.readObject();
		}
		finally
		{
			if(in != null)
			{
				in.close();
			}
		}
	}

	public static Cipher initializeAESCipher(int mode, byte[] secretKeyBytes, byte[] ivParameter)
	{
		Cipher cipher = null;
		try
		{
			SecretKey secretKey = new SecretKeySpec(secretKeyBytes, AES);
			cipher = Cipher.getInstance(AES);
			cipher.init(mode, secretKey, new IvParameterSpec(ivParameter));
		}
		catch(NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(NoSuchPaddingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(InvalidKeyException e)
		{
			e.printStackTrace();
		}
		catch(InvalidAlgorithmParameterException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return cipher;
	}

	public static Cipher initializeRSACipher(int mode, Key key)
	{
		Cipher cipher = null;
		try
		{
			cipher = Cipher.getInstance(RSA);
			cipher.init(mode, key);
		}
		catch(NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(NoSuchPaddingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(InvalidKeyException e)
		{
			e.printStackTrace();
		}

		return cipher;
	}
}
