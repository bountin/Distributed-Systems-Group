package util;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.security.Key;

public class HMAC {

	public static final String VERIFICATION_ERROR_MESSAGE = "Verification of HMAC failed";

	static public String getHMAC(String message, String keyPath) throws HMACException {
		try {
			byte[] keyBytes = new byte[1024];
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
		} catch (Throwable e) {
			throw new HMACException(e);
		}
	}
}
