package message.response;

import message.Response;
import util.HMAC;
import util.HMACException;

public class HMACResponse<H extends Response> implements Response{
	private static final long serialVersionUID = 7957112185711407077L;
	final H response;
	final String hmac;

	public HMACResponse(H response, String keyPath) throws HMACException {
		this.response = response;
		this.hmac = HMAC.getHMAC(response.toString(), keyPath);
	}

	public Response getResponse() {
		return response;
	}

	public boolean verify(String keyPath) throws HMACException {
		String expectedHMAC = HMAC.getHMAC(response.toString(), keyPath);
		return expectedHMAC.equals(this.hmac);
	}

	public String toString() {
		String request = String.format("%s %s", this.hmac, this.response);
		assert request.matches("[a-zA-Z0-9/+]{43}= [\\s[^\\s]]+");
		return request;
	}
}
