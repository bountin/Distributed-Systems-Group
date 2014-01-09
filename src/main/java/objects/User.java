package objects;

public class User
{
	private String username;
	private String password;
	private boolean online;
	private long credits;

	public User()
	{

	}

	public User(String username, String password, boolean online, long credits)
	{
		super();
		this.username = username;
		this.password = password;
		this.online = online;
		this.credits = credits;
	}

	public long getCredits()
	{
		return credits;
	}

	public String getPassword()
	{
		return password;
	}

	public String getUsername()
	{
		return username;
	}

	public boolean isOnline()
	{
		return online;
	}

	public void setCredits(long credits)
	{
		this.credits = credits;
	}

	public void setOnline(boolean online)
	{
		this.online = online;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

}
