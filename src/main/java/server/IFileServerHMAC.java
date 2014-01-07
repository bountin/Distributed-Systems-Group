package server;

import message.Response;
import message.request.*;
import message.response.MessageResponse;

import java.io.IOException;

/**
 * This interface defines the functionality for the HMAC enabled file server.
 */
public interface IFileServerHMAC {
	/**
	 * @see server.IFileServer list
	 */
	Response listHMAC(HMACListRequest request) throws IOException;

	/**
	 * @see model.DownloadTicket
	 */
	Response downloadForReplicationHMAC(HMACDownloadForReplicationRequest request) throws IOException;

	/**
	 * @see server.IFileServer upload
	 */
	MessageResponse uploadHMAC(HMACUploadRequest request) throws IOException;
}
