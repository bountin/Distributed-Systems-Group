package auth;

import java.io.File;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.Arrays;

import auth.message.AuthClientChallenge;
import auth.message.AuthSuccess;
import auth.message.AuthProxyChallenge;
import util.EncryptionUtil;
import channel.AESChannel;
import channel.Base64Channel;
import channel.Channel;
import channel.ObjectByteArrayConverterChannel;
import channel.ObjectChannel;
import channel.RSAChannel;
import channel.TCPChannel;
import client.ClientConfig;

public class ClientAuthenticator
{
	public static ObjectChannel authenticate(String username, ClientConfig clientConfig, Socket proxySocket, String password) throws Exception
	{
		if (clientConfig.getPublicProxyKey() == null) {
			throw new Exception("Operations are limited to RMI since the proxy's public key is unavailable.");
		}
		Channel tcpChannel = new TCPChannel(proxySocket);
		Channel base64Channel = new Base64Channel(tcpChannel);
		Channel rsaChannel = new RSAChannel(base64Channel, clientConfig.getPublicProxyKey(), readUserPrivateKey(clientConfig, username, password));
		ObjectChannel objectRSAChannel = new ObjectByteArrayConverterChannel(rsaChannel);

		// !login <username> <client-challenge>
		AuthClientChallenge loginRequest = new AuthClientChallenge(username, EncryptionUtil.generateRandomNumber(32));
		objectRSAChannel.sendObject(loginRequest);

		// !ok <client-challenge> <proxy-challenge> <secret-key> <iv-parameter>
		AuthProxyChallenge loginAuthResponse = (AuthProxyChallenge)objectRSAChannel.receiveObject();
		if(!Arrays.equals(loginRequest.getClientChallenge(), loginAuthResponse.getClientChallenge()))
		{
			throw new AuthenticationException("proxy could not prove it's identity");
		}

		// <proxy-challenge>
		Channel aesChannel = new AESChannel(base64Channel, loginAuthResponse.getSecretKey(), loginAuthResponse.getIvParameter());
		ObjectChannel objectAESChannel = new ObjectByteArrayConverterChannel(aesChannel);

		objectAESChannel.sendObject(new AuthSuccess(loginAuthResponse.getProxyChallenge()));

		return objectAESChannel;
	}

	private static PrivateKey readUserPrivateKey(ClientConfig clientConfig, String username, String password) throws Exception
	{
		File userPrivateKeyFile = new File(clientConfig.getPrivateKeyDir(), username + ".pem");
		if(!userPrivateKeyFile.exists())
		{
			throw new AuthenticationException("user key file does not exist " + userPrivateKeyFile);
		}
		PrivateKey userPrivateKey = EncryptionUtil.getPrivateKeyFromFile(userPrivateKeyFile, password);

		return userPrivateKey;
	}
}
