package net.sf.l2jdev.gameserver.network.serverpackets.relics;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRelicsAnnounce extends ServerPacket
{
	private final String _announceName;
	private final int _relicId;

	public ExRelicsAnnounce(Player player, int relicId)
	{
		this._relicId = relicId;
		if (!player.getClientSettings().isAnnounceDisabled())
		{
			this._announceName = player.getName();
		}
		else if ("ru".equals(player.getLang()))
		{
			this._announceName = "Некто";
		}
		else
		{
			this._announceName = "Someone";
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RELICS_ANNOUNCE.writeId(this, buffer);
		buffer.writeSizedString(this._announceName);
		buffer.writeInt(this._relicId);
	}
}
