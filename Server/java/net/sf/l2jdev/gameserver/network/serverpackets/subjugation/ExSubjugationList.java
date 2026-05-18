package net.sf.l2jdev.gameserver.network.serverpackets.subjugation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.holders.player.PlayerPurgeHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExSubjugationList extends ServerPacket
{
	private final List<Entry<Integer, PlayerPurgeHolder>> _playerHolder;

	public ExSubjugationList(Map<Integer, PlayerPurgeHolder> playerHolder)
	{
		this._playerHolder = playerHolder.entrySet().stream().filter(it -> it.getValue() != null).collect(Collectors.toList());
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SUBJUGATION_LIST.writeId(this, buffer);
		buffer.writeInt(this._playerHolder.size());

		for (Entry<Integer, PlayerPurgeHolder> integerPlayerPurgeHolderEntry : this._playerHolder)
		{
			buffer.writeInt(integerPlayerPurgeHolderEntry.getKey());
			buffer.writeInt(integerPlayerPurgeHolderEntry.getValue() != null ? integerPlayerPurgeHolderEntry.getValue().getPoints() : 0);
			buffer.writeInt(integerPlayerPurgeHolderEntry.getValue() != null ? integerPlayerPurgeHolderEntry.getValue().getKeys() : 0);
			buffer.writeInt(integerPlayerPurgeHolderEntry.getValue() != null ? integerPlayerPurgeHolderEntry.getValue().getRemainingKeys() : 40);
		}
	}
}
