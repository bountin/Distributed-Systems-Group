package message.request;

import message.Request;
import util.HMAC;
import util.HMACException;

public class AbstractHMACRequest implements Request{
	final Request request;
	final String hmac;

	protected AbstractHMACRequest() {
		request = null;
		hmac = null;
	}

	public AbstractHMACRequest(Request request, String keyPath) throws HMACException {
		this.request = request;
		this.hmac = HMAC.getHMAC(request.toString(), keyPath);
	}

	public Request getRequest() {
		return request;
	}

	public boolean verify(String keyPath) throws HMACException {
		String expectedHMAC = HMAC.getHMAC(request.toString(), keyPath);
		return expectedHMAC.equals(this.hmac);
	}

	public String toString() {
		String request = String.format("%s %s", this.hmac, this.request);
		assert request.matches("[a-zA-Z0-9/+]{43}= [\\s[^\\s]]+");
		return request;
	}
}
