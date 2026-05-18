package net.sf.l2jdev.gameserver.network.clientpackets.settings;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.settings.ExItemAnnounceSetting;

public class ExSaveItemAnnounceSetting extends ClientPacket
{
	private boolean _announceType;

	@Override
	protected void readImpl()
	{
		this._announceType = this.readByte() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.getClientSettings().setAnnounceEnabled(this._announceType);
			player.sendPacket(new ExItemAnnounceSetting(this._announceType));
		}
	}
}
