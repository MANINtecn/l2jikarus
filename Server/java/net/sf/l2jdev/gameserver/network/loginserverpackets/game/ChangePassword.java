package net.sf.l2jdev.gameserver.network.loginserverpackets.game;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;

public class ChangePassword extends BaseWritablePacket
{
	 
	private final String _accountName;
	private final String _characterName;
	private final String _oldPass;
	private final String _newPass;

	public ChangePassword(String accountName, String characterName, String oldPass, String newPass)
	{
		this._accountName = accountName != null ? accountName : "";
		this._characterName = characterName != null ? characterName : "";
		this._oldPass = oldPass != null ? oldPass : "";
		this._newPass = newPass != null ? newPass : "";
	}

	@Override
	public void write()
	{
		this.writeByte(11);
		this.writeString(this._accountName);
		this.writeString(this._characterName);
		this.writeString(this._oldPass);
		this.writeString(this._newPass);
	}
}
