package net.sf.l2jdev.loginserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.commons.network.WritablePacket;
import net.sf.l2jdev.loginserver.network.LoginClient;

public abstract class LoginServerPacket extends WritablePacket<LoginClient>
{
	@Override
	protected boolean write(LoginClient client, WritableBuffer buffer)
	{
		try
		{
			this.writeImpl(client, buffer);
			return true;
		}
		catch (Exception var4)
		{
			return false;
		}
	}
	
	protected abstract void writeImpl(LoginClient var1, WritableBuffer var2);
}
