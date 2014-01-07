package channel;

import java.io.IOException;

import javax.crypto.Cipher;

import util.EncryptionUtil;

public class AESChannel extends ChannelDecorator
{
	private Cipher decrypter;
	private Cipher encrypter;

	public AESChannel(Channel channel, byte[] secretKey, byte[] ivParameter) throws IOException
	{
		super(channel);
		this.decrypter = EncryptionUtil.initializeAESCipher(Cipher.DECRYPT_MODE, secretKey, ivParameter);
		this.encrypter = EncryptionUtil.initializeAESCipher(Cipher.ENCRYPT_MODE, secretKey, ivParameter);
	}

	@Override
	public void close()
	{
		decrypter = null;
		encrypter = null;
	}

	@Override
	public byte[] receiveBytes() throws IOException
	{
		byte[] response = super.receiveBytes();
		return EncryptionUtil.crypt(decrypter, response);
	}

	@Override
	public void sendBytes(byte[] request) throws IOException
	{
		byte[] base64Message = EncryptionUtil.crypt(encrypter, request);
		super.sendBytes(base64Message);
	}
}
