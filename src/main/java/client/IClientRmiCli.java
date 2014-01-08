package client;

import message.response.MessageResponse;

/**
 * RMI Commands
 */
public interface IClientRmiCli {

	MessageResponse readQuorum();
	MessageResponse writeQuorum();
}
