package net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.ElementalSpirit;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.model.item.holders.ElementalSpiritAbsorbItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ElementalSpiritAbsorbInfo extends ServerPacket
{
	private final Player _player;
	private final byte _type;

	public ElementalSpiritAbsorbInfo(Player player, byte type)
	{
		this._player = player;
		this._type = type;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ELEMENTAL_SPIRIT_ABSORB_INFO.writeId(this, buffer);
		ElementalSpirit spirit = this._player.getElementalSpirit(ElementalSpiritType.of(this._type));
		if (spirit == null)
		{
			buffer.writeByte(0);
			buffer.writeByte(0);
		}
		else
		{
			buffer.writeByte(1);
			buffer.writeByte(this._type);
			buffer.writeByte(spirit.getStage());
			buffer.writeLong(spirit.getExperience());
			buffer.writeLong(spirit.getExperienceToNextLevel());
			buffer.writeLong(spirit.getExperienceToNextLevel());
			buffer.writeInt(spirit.getLevel());
			buffer.writeInt(spirit.getMaxLevel());
			List<ElementalSpiritAbsorbItemHolder> absorbItems = spirit.getAbsorbItems();
			buffer.writeInt(absorbItems.size());

			for (ElementalSpiritAbsorbItemHolder absorbItem : absorbItems)
			{
				buffer.writeInt(absorbItem.getId());
				Item item = this._player.getInventory().getItemByItemId(absorbItem.getId());
				int itemCount = item != null ? (int) item.getCount() : 0;
				buffer.writeInt(itemCount);
				buffer.writeInt(absorbItem.getExperience());
			}
		}
	}
}
