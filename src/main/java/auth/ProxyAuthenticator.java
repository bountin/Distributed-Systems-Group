package auth;

import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import proxy.ProxyConfig;
import proxy.ProxyManager;
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

public class ProxyAuthenticator
{
	private final static String B64 = "a-zA-Z0-9/+";

	public static ObjectChannel authenticate(Socket socket, ProxyManager proxyManager, ProxyConfig proxyConfig) throws Exception
	{
		Channel tcpChannel = new TCPChannel(socket);
		Channel base64Channel = new Base64Channel(tcpChannel);
		RSAChannel rsaChannel = new RSAChannel(base64Channel, null, proxyConfig.getPrivateKey());
		ObjectChannel objectRSAChannel = new ObjectByteArrayConverterChannel(rsaChannel);

		// !login <username> <client-challenge>
		AuthClientChallenge loginRequest = (AuthClientChallenge)objectRSAChannel.receiveObject();
		assert loginRequest.toString().matches("!login \\w+ [" + B64 + "]{43}=") : "1st message";
		rsaChannel.setPublicKey(proxyConfig.getUserPublicKeys().getPublicKey(loginRequest.getUsername()));

		// !ok <client-challenge> <proxy-challenge> <secret-key> <iv-parameter>
		AuthProxyChallenge loginResponse = initializeLoginResponse(loginRequest);
		assert loginResponse.toString().matches("!ok [" + B64 + "]{43}= [" + B64 + "]{43}= [" + B64 + "]{43}= [" + B64 + "]{22}==") : "2nd message";
		objectRSAChannel.sendObject(loginResponse);

		// <proxy-challenge>
		Channel aesChannel = new AESChannel(base64Channel, loginResponse.getSecretKey(), loginResponse.getIvParameter());
		ObjectChannel objectAESChannel = new ObjectByteArrayConverterChannel(aesChannel);

		AuthSuccess loginSuccess = (AuthSuccess)objectAESChannel.receiveObject();
		assert loginSuccess.toString().matches("[" + B64 + "]{43}=") : "3rd message";
		if(!Arrays.equals(loginResponse.getProxyChallenge(), (loginSuccess.getProxyChallenge())))
		{
			throw new AuthenticationException("client could not prove it's identity");
		}

		proxyManager.login(loginRequest.getUsername());
		return objectAESChannel;
	}

	private static AuthProxyChallenge initializeLoginResponse(AuthClientChallenge loginRequest) throws NoSuchAlgorithmException
	{
		byte[] clientChallenge = loginRequest.getClientChallenge();
		byte[] proxyChallenge = EncryptionUtil.generateRandomNumber(32);
		byte[] secretKey = EncryptionUtil.generateSecretKey(256).getEncoded();
		byte[] ivParameter = EncryptionUtil.generateRandomNumber(16);

		return new AuthProxyChallenge(clientChallenge, proxyChallenge, secretKey, ivParameter);
	}
}
