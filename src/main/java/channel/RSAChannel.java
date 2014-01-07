package channel;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

import util.EncryptionUtil;

public class RSAChannel extends ChannelDecorator
{
	private PublicKey publicKey;
	private PrivateKey privateKey;

	public RSAChannel(Channel channel, PublicKey publicKey, PrivateKey privateKey) throws IOException
	{
		super(channel);
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	@Override
	public void close()
	{
		publicKey = null;
		privateKey = null;
	}

	@Override
	public byte[] receiveBytes() throws IOException
	{
		byte[] response = super.receiveBytes();
		return EncryptionUtil.decryptRSA(response, privateKey);
	}

	@Override
	public void sendBytes(byte[] request) throws IOException
	{
		byte[] base64Message = EncryptionUtil.encryptRSA(request, publicKey);
		super.sendBytes(base64Message);

	}

	public void setPublicKey(PublicKey publicKey)
	{
		this.publicKey = publicKey;
	}
}
