package net.sf.l2jdev.loginserver.model.data;

import java.util.Objects;

public class AccountInfo
{
	private final String _login;
	private final String _passwordHash;
	private final int _accessLevel;
	private final int _lastServer;
	
	public AccountInfo(String login, String passwordHash, int accessLevel, int lastServer)
	{
		Objects.requireNonNull(login, "login");
		Objects.requireNonNull(passwordHash, "passwordHash");
		this._login = requireNonEmpty(login, "login").toLowerCase();
		this._passwordHash = requireNonEmpty(passwordHash, "passwordHash");
		this._accessLevel = accessLevel;
		this._lastServer = lastServer;
	}
	
	private static String requireNonEmpty(String value, String argumentName)
	{
		if (value.isEmpty())
		{
			throw new IllegalArgumentException(argumentName);
		}
		return value;
	}
	
	public boolean checkPassHash(String passwordHash)
	{
		return this._passwordHash.equals(passwordHash);
	}
	
	public String getLogin()
	{
		return this._login;
	}
	
	public int getAccessLevel()
	{
		return this._accessLevel;
	}
	
	public int getLastServer()
	{
		return this._lastServer;
	}
	
	@Override
	public String toString()
	{
		return "AccountInfo[login=" + this._login + ", accessLevel=" + this._accessLevel + ", lastServer=" + this._lastServer + "]";
	}
}
