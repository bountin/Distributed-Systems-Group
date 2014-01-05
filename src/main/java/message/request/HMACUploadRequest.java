package message.request;

import message.Request;
import util.HMAC;
import util.HMACException;

/**
 * Uploads the file with the given name.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code &lt;HMAC&gt; </>!upload &lt;filename&gt; &lt;content&gt;}<br/>
 * <b>Response:</b><br/>
 * {@code !upload &lt;message&gt;}<br/>
 */

public class HMACUploadRequest implements Request {
	final UploadRequest uploadRequest;
	final String hmac;

	public HMACUploadRequest(UploadRequest uploadRequest, String keyPath) throws HMACException {
		this.uploadRequest = uploadRequest;
		this.hmac = HMAC.getHMAC(uploadRequest.toString(), keyPath);
	}

	public UploadRequest getUploadRequest() {
		return uploadRequest;
	}

	public boolean verify(String keyPath) throws HMACException {
		String expectedHMAC = HMAC.getHMAC(uploadRequest.toString(), keyPath);
		return expectedHMAC.equals(this.hmac);
	}

	public String toString() {
		String request = String.format("%s %s", hmac, uploadRequest);
		assert request.matches("[a-zA-Z0-9/+]{43}= [\\s[^\\s]]+");
		return request;
	}
}
