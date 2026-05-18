package net.sf.l2jdev.gameserver.network.serverpackets.ranking;

import java.util.concurrent.TimeUnit;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.GlobalVariablesManager;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRankingBuffZoneNpcInfo extends ServerPacket
{
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RANKING_CHAR_BUFFZONE_NPC_INFO.writeId(this, buffer);
		long cooldown = GlobalVariablesManager.getInstance().getLong("RANKING_POWER_COOLDOWN", 0L);
		long currentTime = System.currentTimeMillis();
		if (cooldown > currentTime)
		{
			long reuseTime = TimeUnit.MILLISECONDS.toSeconds(cooldown - currentTime);
			buffer.writeInt((int) reuseTime);
		}
		else
		{
			buffer.writeInt(0);
		}
	}
}
