package message.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import message.Response;
import proxy.FileInfo;

/**
 * Lists all files available on all file servers.
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
 * @see message.request.ListRequest
 */
public class FileInfoListResponse implements Response
{
	private static final long serialVersionUID = 8715586306914469865L;
	private final List<FileInfo> fileInfos;

	public FileInfoListResponse(Collection<FileInfo> fileInfos)
	{
		this.fileInfos = Collections.unmodifiableList(new ArrayList<FileInfo>(fileInfos));
	}

	public List<FileInfo> getFileInfos()
	{
		return fileInfos;
	}

	@Override
	public String toString()
	{
		if(getFileInfos().isEmpty())
		{
			return "No files found.";
		}

		StringBuilder sb = new StringBuilder();
		for(FileInfo fileInfo : getFileInfos())
		{
			sb.append(fileInfo.toString()).append("\n");
		}
		return sb.toString();
	}
}
