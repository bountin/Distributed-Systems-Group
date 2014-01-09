package auth;

import java.net.Socket;
import java.security.PrivateKey;
import java.util.Arrays;

import util.EncryptionUtil;
import auth.message.AuthClientChallenge;
import auth.message.AuthProxyChallenge;
import auth.message.AuthSuccess;
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
	private final static String B64 = "a-zA-Z0-9/+";

	public static ObjectChannel authenticate(String username, ClientConfig clientConfig, Socket proxySocket, String password) throws Exception
	{
		if(clientConfig.getPublicProxyKey() == null)
		{
			throw new Exception("Operations are limited to RMI since the proxy's public key is unavailable.");
		}
		PrivateKey userPrivateKey = clientConfig.getPrivateKeyDir().getPrivateKey(username, password);

		Channel tcpChannel = new TCPChannel(proxySocket);
		Channel base64Channel = new Base64Channel(tcpChannel);
		Channel rsaChannel = new RSAChannel(base64Channel, clientConfig.getPublicProxyKey(), userPrivateKey);
		ObjectChannel objectRSAChannel = new ObjectByteArrayConverterChannel(rsaChannel);

		// !login <username> <client-challenge>
		AuthClientChallenge authRequest = new AuthClientChallenge(username, EncryptionUtil.generateRandomNumber(32));
		assert authRequest.toString().matches("!login \\w+ [" + B64 + "]{43}=") : "1st message";
		objectRSAChannel.sendObject(authRequest);

		// !ok <client-challenge> <proxy-challenge> <secret-key> <iv-parameter>
		AuthProxyChallenge authResponse = (AuthProxyChallenge)objectRSAChannel.receiveObject();
		assert authResponse.toString().matches("!ok [" + B64 + "]{43}= [" + B64 + "]{43}= [" + B64 + "]{43}= [" + B64 + "]{22}==") : "2nd message";
		if(!Arrays.equals(authRequest.getClientChallenge(), authResponse.getClientChallenge()))
		{
			throw new AuthenticationException("proxy could not prove it's identity");
		}

		// <proxy-challenge>
		Channel aesChannel = new AESChannel(base64Channel, authResponse.getSecretKey(), authResponse.getIvParameter());
		ObjectChannel objectAESChannel = new ObjectByteArrayConverterChannel(aesChannel);

		AuthSuccess authSuccess = new AuthSuccess(authResponse.getProxyChallenge());
		assert authSuccess.toString().matches("[" + B64 + "]{43}=") : "3rd message";
		objectAESChannel.sendObject(authSuccess);

		return objectAESChannel;
	}

}
