package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.xml.SecondaryAuthData;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.serverpackets.Ex2ndPasswordAck;
import net.sf.l2jdev.gameserver.security.SecondaryPasswordAuth;

public class RequestEx2ndPasswordReq extends ClientPacket
{
	private int _changePass;
	private String _password;
	private String _newPassword;

	@Override
	protected void readImpl()
	{
		this._changePass = this.readByte();
		this._password = this.readString();
		if (this._changePass == 2)
		{
			this._newPassword = this.readString();
		}
	}

	@Override
	protected void runImpl()
	{
		if (SecondaryAuthData.getInstance().isEnabled())
		{
			GameClient client = this.getClient();
			SecondaryPasswordAuth secondAuth = client.getSecondaryAuth();
			boolean success = false;
			if (this._changePass == 0 && !secondAuth.passwordExist())
			{
				success = secondAuth.savePassword(this._password);
			}
			else if (this._changePass == 2 && secondAuth.passwordExist())
			{
				success = secondAuth.changePassword(this._password, this._newPassword);
			}

			if (success)
			{
				client.sendPacket(new Ex2ndPasswordAck(this._changePass, 0));
			}
		}
	}
}
