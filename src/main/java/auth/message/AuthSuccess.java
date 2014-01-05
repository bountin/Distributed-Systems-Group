package auth.message;

import message.Request;

import org.bouncycastle.util.encoders.Base64;

public class AuthSuccess implements Request
{
	private static final long serialVersionUID = -1596776158259072949L;

	private final byte[] proxyChallenge;

	public AuthSuccess(byte[] proxyChallenge)
	{
		this.proxyChallenge = Base64.encode(proxyChallenge);
	}

	public byte[] getProxyChallenge()
	{
		return Base64.decode(proxyChallenge);
	}

	@Override
	public String toString()
	{
		return String.format("%s", getProxyChallenge());
	}
}
