package auth.message;

import message.Response;

import org.bouncycastle.util.encoders.Base64;

public class AuthProxyChallenge implements Response
{
	private static final long serialVersionUID = 7274190844784824788L;

	private final byte[] clientChallenge;
	private final byte[] proxyChallenge;
	private final byte[] secretKey;
	private final byte[] ivParameter;

	public AuthProxyChallenge(byte[] clientChallenge, byte[] proxyChallenge, byte[] secretKey, byte[] ivParameter)
	{
		super();
		this.clientChallenge = Base64.encode(clientChallenge);
		this.proxyChallenge = Base64.encode(proxyChallenge);
		this.secretKey = Base64.encode(secretKey);
		this.ivParameter = Base64.encode(ivParameter);
	}

	public byte[] getClientChallenge()
	{
		return Base64.decode(clientChallenge);
	}

	public byte[] getIvParameter()
	{
		return Base64.decode(ivParameter);
	}

	public byte[] getProxyChallenge()
	{
		return Base64.decode(proxyChallenge);
	}

	public byte[] getSecretKey()
	{
		return Base64.decode(secretKey);
	}

	@Override
	public String toString()
	{
		// !ok <client-challenge> <proxy-challenge> <secret-key> <iv-parameter>
		return String.format("!ok %s %s %s %s", getClientChallenge(), getProxyChallenge(), getSecretKey(), getIvParameter());
	}
}
