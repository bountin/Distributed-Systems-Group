package message.request;

import message.Request;
import util.HMAC;
import util.HMACException;

public class HMACRequest<H extends Request> implements Request
{
	private static final long serialVersionUID = -8022262910276779500L;
	final H request;
	final String hmac;

	public HMACRequest(H request, String hmacKeyPath) throws HMACException
	{
		this.request = request;
		this.hmac = HMAC.getHMAC(request.toString(), hmacKeyPath);
	}

	public Request getRequest()
	{
		return request;
	}

	@Override
	public String toString()
	{
		String request = String.format("%s %s", this.hmac, this.request);
		assert request.matches("[a-zA-Z0-9/+]{43}= [\\s[^\\s]]+");
		return request;
	}

	public boolean verify(String keyPath) throws HMACException
	{
		String expectedHMAC = HMAC.getHMAC(request.toString(), keyPath);
		return expectedHMAC.equals(this.hmac);
	}
}
