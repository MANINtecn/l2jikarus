package net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.ElementalSpirit;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;

public abstract class UpdateElementalSpiritPacket extends AbstractElementalSpiritPacket
{
	private final Player _player;
	private final byte _type;
	private final boolean _update;

	UpdateElementalSpiritPacket(Player player, byte type, boolean update)
	{
		this._player = player;
		this._type = type;
		this._update = update;
	}

	protected void writeUpdate(WritableBuffer buffer)
	{
		buffer.writeByte(this._update);
		buffer.writeByte(this._type);
		if (this._update)
		{
			ElementalSpirit spirit = this._player.getElementalSpirit(ElementalSpiritType.of(this._type));
			if (spirit == null)
			{
				return;
			}

			buffer.writeByte(this._type);
			this.writeSpiritInfo(buffer, spirit);
		}
	}
}
