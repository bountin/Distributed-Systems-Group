package message.request;

import util.HMACException;

/**
 * Lists all files available on all file servers.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code &lt;HMAC&gt; !list}<br/>
 * <b>Response:</b><br/>
 * {@code No files found.}<br/>
 * or<br/>
 * {@code &lt;filename1&gt;}<br/>
 * {@code &lt;filename2&gt;}<br/>
 * {@code ...}<br/>
 *
 * @see message.response.ListResponse
 */
public class HMACListRequest extends AbstractHMACRequest {
	private static final long serialVersionUID = 5390727304804613029L;

	public HMACListRequest(ListRequest listRequest, String keyPath) throws HMACException {
		super(listRequest, keyPath);
	}
}
