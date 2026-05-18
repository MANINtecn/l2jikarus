package net.sf.l2jdev.loginserver;

import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.loginserver.config.LoginConfig;

public class SessionKey
{
	private static final boolean CHECK_LOGIN_PAIR = LoginConfig.SHOW_LICENCE;
	private final int _playOkID1;
	private final int _playOkID2;
	private final int _loginOkID1;
	private final int _loginOkID2;
	
	public SessionKey(int loginOk1, int loginOk2, int playOk1, int playOk2)
	{
		this._playOkID1 = playOk1;
		this._playOkID2 = playOk2;
		this._loginOkID1 = loginOk1;
		this._loginOkID2 = loginOk2;
	}
	
	@Override
	public String toString()
	{
		return StringUtil.concat("PlayOk: " + this._playOkID1 + " " + this._playOkID2 + " LoginOk:" + this._loginOkID1 + " " + this._loginOkID2);
	}
	
	public boolean checkLoginPair(int loginOk1, int loginOk2)
	{
		return this._loginOkID1 == loginOk1 && this._loginOkID2 == loginOk2;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if (this == object)
		{
			return true;
		}
		else if (!(object instanceof SessionKey key))
		{
			return false;
		}
		else if (this._playOkID1 != key._playOkID1 || this._playOkID2 != key._playOkID2)
		{
			return false;
		}
		else
		{
			return !CHECK_LOGIN_PAIR ? true : this._loginOkID1 == key._loginOkID1 && this._loginOkID2 == key._loginOkID2;
		}
	}
	
	@Override
	public int hashCode()
	{
		int h = 17;
		h = 31 * h + this._playOkID1;
		h = 31 * h + this._playOkID2;
		if (CHECK_LOGIN_PAIR)
		{
			h = 31 * h + this._loginOkID1;
			h = 31 * h + this._loginOkID2;
		}
		
		return h;
	}
	
	public int getPlayOkID1()
	{
		return this._playOkID1;
	}
	
	public int getPlayOkID2()
	{
		return this._playOkID2;
	}
	
	public int getLoginOkID1()
	{
		return this._loginOkID1;
	}
	
	public int getLoginOkID2()
	{
		return this._loginOkID2;
	}
}
