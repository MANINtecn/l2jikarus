package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.henna.HennaPoten;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class GMHennaInfo extends ServerPacket
{
	private final Player _player;
	private final List<HennaPoten> _hennas = new ArrayList<>();

	public GMHennaInfo(Player player)
	{
		this._player = player;

		for (HennaPoten henna : this._player.getHennaPotenList())
		{
			if (henna != null)
			{
				this._hennas.add(henna);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.GM_HENNA_INFO.writeId(this, buffer);
		buffer.writeShort(this._player.getHennaValue(BaseStat.INT));
		buffer.writeShort(this._player.getHennaValue(BaseStat.STR));
		buffer.writeShort(this._player.getHennaValue(BaseStat.CON));
		buffer.writeShort(this._player.getHennaValue(BaseStat.MEN));
		buffer.writeShort(this._player.getHennaValue(BaseStat.DEX));
		buffer.writeShort(this._player.getHennaValue(BaseStat.WIT));
		buffer.writeShort(0);
		buffer.writeShort(0);
		buffer.writeInt(3);
		buffer.writeInt(this._hennas.size());

		for (HennaPoten henna : this._hennas)
		{
			buffer.writeInt(henna.getPotenId());
			buffer.writeInt(1);
		}

		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
	}
}
