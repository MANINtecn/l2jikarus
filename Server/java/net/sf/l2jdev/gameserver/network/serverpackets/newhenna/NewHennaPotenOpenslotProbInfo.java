package net.sf.l2jdev.gameserver.network.serverpackets.newhenna;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class NewHennaPotenOpenslotProbInfo extends ServerPacket
{
	private final int _slot;

	public NewHennaPotenOpenslotProbInfo(Player player, int slot)
	{
		this._slot = slot;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_NEW_HENNA_POTEN_OPENSLOT_PROB_INFO.writeId(this, buffer);
		buffer.writeInt(this._slot);
		buffer.writeInt(10000);
		buffer.writeInt(0);
		buffer.writeInt(57);
		buffer.writeLong(10000L);
	}
}
