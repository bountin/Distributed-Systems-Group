package model;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

public class MapKeyHolder implements KeyHolder
{
	private Map<String, KeyPair> keys;

	public MapKeyHolder(Map<String, KeyPair> keys)
	{
		this.keys = keys;
	}

	@Override
	public PrivateKey getPrivateKey(String username, String password) throws Exception
	{
		KeyPair pair = keys.get(username);
		if(pair != null)
		{
			return pair.getPrivate();
		}
		else
		{
			throw new Exception("private key not found");
		}
	}

	@Override
	public PublicKey getPublicKey(String username) throws Exception
	{
		KeyPair pair = keys.get(username);
		if(pair != null)
		{
			return pair.getPublic();
		}
		else
		{
			throw new Exception("public key not found");
		}
	}

}
