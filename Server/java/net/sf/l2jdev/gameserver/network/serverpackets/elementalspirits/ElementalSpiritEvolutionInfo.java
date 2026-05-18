package net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.ElementalSpirit;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ElementalSpiritEvolutionInfo extends ServerPacket
{
	private final Player _player;
	private final byte _type;

	public ElementalSpiritEvolutionInfo(Player player, byte type)
	{
		this._player = player;
		this._type = type;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ELEMENTAL_SPIRIT_EVOLUTION_INFO.writeId(this, buffer);
		ElementalSpirit spirit = this._player.getElementalSpirit(ElementalSpiritType.of(this._type));
		if (spirit == null)
		{
			buffer.writeByte(0);
			buffer.writeInt(0);
		}
		else
		{
			buffer.writeByte(this._type);
			buffer.writeInt(spirit.getNpcId());
			buffer.writeInt(1);
			buffer.writeInt(spirit.getStage());
			buffer.writeDouble(100.0);
			List<ItemHolder> items = spirit.getItemsToEvolve();
			buffer.writeInt(items.size());

			for (ItemHolder item : items)
			{
				buffer.writeInt(item.getId());
				buffer.writeLong(item.getCount());
			}
		}
	}
}
