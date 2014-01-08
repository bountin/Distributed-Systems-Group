package model;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

import util.EncryptionUtil;

public class DirectoryKeyHolder implements KeyHolder
{
	private File directory;

	public DirectoryKeyHolder(File directory)
	{
		this.directory = directory;
	}

	@Override
	public PrivateKey getPrivateKey(String username, String password) throws Exception
	{
		File userPublicKeyFile = new File(directory, username + ".pub.pem");
		if(!userPublicKeyFile.exists())
		{
			throw new Exception("private user key does not exist");
		}
		return EncryptionUtil.getPrivateKeyFromFile(userPublicKeyFile, password);
	}

	@Override
	public PublicKey getPublicKey(String username) throws Exception
	{
		File publicKeyFile = new File(directory, username + ".pub.pem");
		if(!publicKeyFile.exists())
		{
			throw new Exception("public user key does not exist");
		}
		return EncryptionUtil.getPublicKeyFromFile(publicKeyFile);
	}

}