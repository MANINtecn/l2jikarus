package net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.ElementalSpirit;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ElementalSpiritInfo extends AbstractElementalSpiritPacket
{
	private final Player _player;
	private final byte _type;

	public ElementalSpiritInfo(Player player, byte packetType)
	{
		this._player = player;
		this._type = packetType;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ELEMENTAL_SPIRIT_INFO.writeId(this, buffer);
		ElementalSpirit[] spirits = this._player.getSpirits();
		if (spirits == null)
		{
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeByte(0);
		}
		else
		{
			buffer.writeByte(this._type);
			buffer.writeByte(spirits.length);

			for (ElementalSpirit spirit : spirits)
			{
				buffer.writeByte(spirit.getType());
				buffer.writeByte(1);
				this.writeSpiritInfo(buffer, spirit);
			}

			buffer.writeInt(1);

			for (int i = 0; i < 1; i++)
			{
				buffer.writeInt(57);
				buffer.writeLong(50000L);
			}
		}
	}
}
