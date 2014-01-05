package auth.message;

import message.Request;

import org.bouncycastle.util.encoders.Base64;

public class AuthClientChallenge implements Request
{
	private static final long serialVersionUID = -1596776158259072949L;

	private final String username;
	private final byte[] clientChallenge;

	public AuthClientChallenge(String username, byte[] clientChallenge)
	{
		this.username = username;
		this.clientChallenge = Base64.encode(clientChallenge);
	}

	public byte[] getClientChallenge()
	{
		return Base64.decode(clientChallenge);
	}

	public String getUsername()
	{
		return username;
	}

	@Override
	public String toString()
	{
		return String.format("!login %s %s", getUsername(), getClientChallenge());
	}
}
