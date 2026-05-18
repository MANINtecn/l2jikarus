package net.sf.l2jdev.gameserver.network.serverpackets.equipmentupgrade;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractItemPacket;

public class ExShowUpgradeSystem extends AbstractItemPacket
{
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_UPGRADE_SYSTEM.writeId(this, buffer);
		buffer.writeShort(1);
		buffer.writeShort(100);
		buffer.writeInt(0);
		buffer.writeInt(0);
	}
}
