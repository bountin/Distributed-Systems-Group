package message.request;

import message.Request;

/**
 * Lists all file infos available on a servers.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !list}<br/>
 * <b>Response:</b><br/>
 * {@code No files found.}<br/>
 * or<br/>
 * {@code &lt;filename1&gt;}<br/>
 * {@code &lt;filename2&gt;}<br/>
 * {@code ...}<br/>
 * 
 * @see message.response.FileInfoListResponse
 */
public class FileInfoListRequest implements Request
{
	private static final long serialVersionUID = -6838640521088466915L;

	@Override
	public String toString()
	{
		return "!list";
	}
}
