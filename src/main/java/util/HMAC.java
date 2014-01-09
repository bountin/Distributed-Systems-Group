package util;

import java.io.FileInputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

public class HMAC
{
	public static final int SIZE = 1024;
	public static final String VERIFICATION_ERROR_MESSAGE = "Verification of HMAC failed";

	public static Key generateHMACKey() throws NoSuchAlgorithmException
	{
		KeyGenerator generator = KeyGenerator.getInstance("HmacSHA256");
		generator.init(SIZE);
		return generator.generateKey();
	}

	static public String getHMAC(String message, String keyPath) throws HMACException
	{
		try
		{
			byte[] keyBytes = new byte[SIZE];
			FileInputStream fis = new FileInputStream(keyPath);
			fis.read(keyBytes);
			fis.close();

			byte[] input = Hex.decode(keyBytes);
			Key secretKey = new SecretKeySpec(input, "HmacSHA256");
			Mac hMac = Mac.getInstance("HmacSHA256");
			hMac.init(secretKey);
			hMac.update(message.getBytes());
			byte[] hash = hMac.doFinal();
			return new String(Base64.encode(hash));
		}
		catch(Throwable e)
		{
			throw new HMACException(e);
		}
	}
}
