package net.sf.l2jdev.loginserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.loginserver.enums.LoginFailReason;
import net.sf.l2jdev.loginserver.network.ConnectionState;
import net.sf.l2jdev.loginserver.network.serverpackets.GGAuth;

public class AuthGameGuard extends LoginClientPacket
{
	private static final Logger LOGGER = Logger.getLogger(AuthGameGuard.class.getName());
	private int _receivedSessionId;
	
	@Override
	protected boolean readImpl()
	{
		int remainingBytes = this.remaining();
		if (remainingBytes < 20)
		{
			if (LOGGER.isLoggable(Level.FINE))
			{
				LOGGER.fine("AuthGameGuard: Invalid payload length " + remainingBytes + " (expected 20).");
			}
			
			return false;
		}
		this._receivedSessionId = this.readInt();
		
		for (int i = 0; i < 4; i++)
		{
			this.readInt();
		}
		
		return true;
	}
	
	@Override
	public void run()
	{
		int clientSessionId = this.getClient().getSessionId();
		if (this._receivedSessionId != clientSessionId)
		{
			if (LOGGER.isLoggable(Level.FINE))
			{
				LOGGER.fine("AuthGameGuard: Session mismatch. Received=" + this._receivedSessionId + ", expected=" + clientSessionId + ".");
			}
			
			this.getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
		}
		else
		{
			this.getClient().setConnectionState(ConnectionState.AUTHED_GG);
			this.getClient().sendPacket(new GGAuth(clientSessionId));
		}
	}
}
