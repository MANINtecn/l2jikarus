package net.sf.l2jdev.gameserver.network.serverpackets.relics;

import java.util.ArrayList;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRelicsCombination extends ServerPacket
{
	private final ArrayList<Integer> _successCompoundIds;
	private final ArrayList<Integer> _failCompoundIds;

	public ExRelicsCombination(Player player, ArrayList<Integer> successCompoundIds, ArrayList<Integer> failCompoundIds)
	{
		this._successCompoundIds = successCompoundIds;
		this._failCompoundIds = failCompoundIds;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RELICS_COMBINATION.writeId(this, buffer);
		buffer.writeByte(1);
		buffer.writeInt(this._successCompoundIds.size() + this._failCompoundIds.size());

		for (int receivedRelicId : this._successCompoundIds)
		{
			buffer.writeInt(receivedRelicId);
		}

		for (int receivedRelicId : this._failCompoundIds)
		{
			buffer.writeInt(receivedRelicId);
		}

		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeLong(0L);
	}
}
