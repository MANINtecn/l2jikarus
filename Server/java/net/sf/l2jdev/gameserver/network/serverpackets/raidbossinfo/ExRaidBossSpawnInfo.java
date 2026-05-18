package net.sf.l2jdev.gameserver.network.serverpackets.raidbossinfo;

import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.enums.npc.RaidBossStatus;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRaidBossSpawnInfo extends ServerPacket
{
	private final Map<Integer, RaidBossStatus> _statuses;

	public ExRaidBossSpawnInfo(Map<Integer, RaidBossStatus> statuses)
	{
		this._statuses = statuses;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RAID_BOSS_SPAWN_INFO.writeId(this, buffer);
		buffer.writeInt(0);
		buffer.writeInt(this._statuses.size());

		for (Entry<Integer, RaidBossStatus> entry : this._statuses.entrySet())
		{
			buffer.writeInt(entry.getKey());
			buffer.writeInt(entry.getValue().ordinal());
			buffer.writeInt(0);
		}
	}
}
