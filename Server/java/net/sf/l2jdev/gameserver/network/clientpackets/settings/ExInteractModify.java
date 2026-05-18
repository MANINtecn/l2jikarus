package net.sf.l2jdev.gameserver.network.clientpackets.settings;

import net.sf.l2jdev.gameserver.model.ClientSettings;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExInteractModify extends ClientPacket
{
	private int _type;
	private int _settings;

	@Override
	protected void readImpl()
	{
		this._type = this.readByte();
		this._settings = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			ClientSettings clientSettings = player.getClientSettings();
			switch (this._type)
			{
				case 0:
					clientSettings.setPartyRequestRestrictedFromOthers((this._settings & 1) == 1);
					clientSettings.setPartyRequestRestrictedFromClan((this._settings & 2) == 2);
					clientSettings.setPartyRequestRestrictedFromFriends((this._settings & 4) == 4);
					clientSettings.storeSettings();
					break;
				case 1:
					clientSettings.setFriendRequestRestrictedFromOthers((this._settings & 1) == 1);
					clientSettings.setFriendRequestRestrictionFromClan((this._settings & 2) == 2);
					clientSettings.storeSettings();
			}
		}
	}
}
