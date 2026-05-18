package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.henna.Henna;
import net.sf.l2jdev.gameserver.model.item.henna.HennaPoten;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class HennaInfo extends ServerPacket
{
	private final Player _player;
	private final List<Henna> _hennas = new ArrayList<>();

	public HennaInfo(Player player)
	{
		this._player = player;

		for (HennaPoten hennaPoten : this._player.getHennaPotenList())
		{
			if (hennaPoten != null)
			{
				Henna henna = hennaPoten.getHenna();
				if (henna != null)
				{
					this._hennas.add(henna);
				}
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.HENNA_INFO.writeId(this, buffer);
		buffer.writeShort(this._player.getHennaValue(BaseStat.INT));
		buffer.writeShort(this._player.getHennaValue(BaseStat.STR));
		buffer.writeShort(this._player.getHennaValue(BaseStat.CON));
		buffer.writeShort(this._player.getHennaValue(BaseStat.MEN));
		buffer.writeShort(this._player.getHennaValue(BaseStat.DEX));
		buffer.writeShort(this._player.getHennaValue(BaseStat.WIT));
		buffer.writeShort(0);
		buffer.writeShort(0);
		buffer.writeInt(3 - this._player.getHennaEmptySlots());
		buffer.writeInt(this._hennas.size());

		for (Henna henna : this._hennas)
		{
			buffer.writeInt(henna.getDyeId());
			buffer.writeInt(henna.isAllowedClass(this._player));
		}

		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
	}
}
