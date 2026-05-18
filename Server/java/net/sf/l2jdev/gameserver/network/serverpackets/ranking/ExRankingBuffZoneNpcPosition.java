package net.sf.l2jdev.gameserver.network.serverpackets.ranking;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.GlobalVariablesManager;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRankingBuffZoneNpcPosition extends ServerPacket
{
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RANKING_CHAR_BUFFZONE_NPC_POSITION.writeId(this, buffer);
		if (GlobalVariablesManager.getInstance().getLong("RANKING_POWER_COOLDOWN", 0L) > System.currentTimeMillis())
		{
			List<Integer> location = GlobalVariablesManager.getInstance().getIntegerList("RANKING_POWER_LOCATION");
			buffer.writeByte(1);
			buffer.writeInt(location.get(0));
			buffer.writeInt(location.get(1));
			buffer.writeInt(location.get(2));
		}
		else
		{
			buffer.writeByte(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
	}
}
