package model;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface KeyHolder
{
	PrivateKey getPrivateKey(String username, String password) throws Exception;

	PublicKey getPublicKey(String username) throws Exception;

	void setPublicKey(String username, PublicKey key) throws IOException;
}
