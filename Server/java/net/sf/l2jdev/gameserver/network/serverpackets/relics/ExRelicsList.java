package net.sf.l2jdev.gameserver.network.serverpackets.relics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.PlayerRelicData;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRelicsList extends ServerPacket
{
	private final Player _player;
	private final Collection<PlayerRelicData> _relics;
	private final List<Integer> _confirmedRelics;

	public ExRelicsList(Player player)
	{
		this._player = player;
		this._relics = this._player.getRelics();
		List<Integer> confirmedRelics = new ArrayList<>();

		for (PlayerRelicData relic : this._relics)
		{
			if (relic.getRelicIndex() < 300)
			{
				confirmedRelics.add(relic.getRelicId());
			}
		}

		this._confirmedRelics = confirmedRelics;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RELICS_LIST.writeId(this, buffer);
		buffer.writeInt(1);
		buffer.writeInt(140);
		buffer.writeInt(this._confirmedRelics.size());

		for (PlayerRelicData relic : this._relics)
		{
			if (relic.getRelicIndex() < 300)
			{
				buffer.writeInt(relic.getRelicId());
				buffer.writeInt(relic.getRelicLevel());
				buffer.writeInt(relic.getRelicCount());
			}
		}
	}
}
