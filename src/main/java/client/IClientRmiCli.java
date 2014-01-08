package client;

import message.response.MessageResponse;

/**
 * RMI Commands
 */
public interface IClientRmiCli {

	MessageResponse readQuorum();
	MessageResponse writeQuorum();
	MessageResponse topThreeDownloads();
	MessageResponse getProxyPublicKey();
	MessageResponse setUserPublicKey(String username);
	MessageResponse subscribe(String filename, int count);
}
