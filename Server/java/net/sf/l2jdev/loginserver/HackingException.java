package net.sf.l2jdev.loginserver;

public class HackingException extends Exception
{
	private static final long serialVersionUID = 1L;
	private final String _ip;
	private final int _connects;
	
	public HackingException(String ip, int connects)
	{
		this._ip = ip;
		this._connects = connects;
	}
	
	public String getIP()
	{
		return this._ip;
	}
	
	public int getConnects()
	{
		return this._connects;
	}
}
