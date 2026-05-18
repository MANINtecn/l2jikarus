package net.sf.l2jdev.gameserver.network.serverpackets.revenge;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.RevengeType;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPvpBookShareRevengeNewRevengeInfo extends ServerPacket
{
	private final String _victimName;
	private final String _killerName;
	private final RevengeType _type;

	public ExPvpBookShareRevengeNewRevengeInfo(String victimName, String killerName, RevengeType type)
	{
		this._victimName = victimName;
		this._killerName = killerName;
		this._type = type;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PVPBOOK_SHARE_REVENGE_NEW_REVENGEINFO.writeId(this, buffer);
		buffer.writeInt(this._type.ordinal());
		buffer.writeSizedString(this._victimName);
		buffer.writeSizedString(this._killerName);
	}
}
