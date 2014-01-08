package model;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface KeyHolder
{
	PrivateKey getPrivateKey(String username, String password) throws Exception;

	PublicKey getPublicKey(String username) throws Exception;
}
