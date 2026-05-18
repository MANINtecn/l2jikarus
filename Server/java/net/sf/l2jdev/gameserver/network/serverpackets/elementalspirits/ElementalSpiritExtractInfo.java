package net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.ElementalSpiritData;
import net.sf.l2jdev.gameserver.model.ElementalSpirit;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ElementalSpiritExtractInfo extends ServerPacket
{
	private final Player _player;
	private final byte _type;

	public ElementalSpiritExtractInfo(Player player, byte type)
	{
		this._player = player;
		this._type = type;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ELEMENTAL_SPIRIT_EXTRACT_INFO.writeId(this, buffer);
		ElementalSpirit spirit = this._player.getElementalSpirit(ElementalSpiritType.of(this._type));
		if (spirit == null)
		{
			buffer.writeByte(0);
			buffer.writeByte(0);
		}
		else
		{
			buffer.writeByte(this._type);
			buffer.writeByte(1);
			buffer.writeByte(1);
			buffer.writeInt(57);
			buffer.writeInt(ElementalSpiritData.EXTRACT_FEES[spirit.getStage() - 1]);
			buffer.writeInt(spirit.getExtractItem());
			buffer.writeInt(spirit.getExtractAmount());
		}
	}
}
