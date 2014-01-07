package message.request;

import util.HMACException;

/**
 * Uploads the file with the given name.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code &lt;HMAC&gt; !upload &lt;filename&gt; &lt;content&gt;}<br/>
 * <b>Response:</b><br/>
 * {@code !upload &lt;message&gt;}<br/>
 */
public class HMACUploadRequest extends AbstractHMACRequest {
	private static final long serialVersionUID = -68213123L;

	public HMACUploadRequest(UploadRequest uploadRequest, String keyPath) throws HMACException {
		super(uploadRequest, keyPath);
	}
}
