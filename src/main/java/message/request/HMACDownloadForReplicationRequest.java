package message.request;

import util.HMACException;

public class HMACDownloadForReplicationRequest extends AbstractHMACRequest {
	private static final long serialVersionUID = -5242058496300076478L;

	public HMACDownloadForReplicationRequest(DownloadForReplicationRequest request, String keyPath) throws HMACException {
		super(request, keyPath);
	}
}
