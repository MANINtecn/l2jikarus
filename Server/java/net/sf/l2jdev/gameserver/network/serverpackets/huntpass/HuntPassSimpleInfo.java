package net.sf.l2jdev.gameserver.network.serverpackets.huntpass;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.HuntPass;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class HuntPassSimpleInfo extends ServerPacket
{
	private final HuntPass _huntPassInfo;

	public HuntPassSimpleInfo(Player player)
	{
		this._huntPassInfo = player.getHuntPass();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_L2PASS_SIMPLE_INFO.writeId(this, buffer);
		buffer.writeInt(1);
		buffer.writeByte(0);
		buffer.writeByte(1);
		buffer.writeByte(this._huntPassInfo.rewardAlert());
		buffer.writeInt(0);
	}
}
