package net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExElementalSpiritAttackType extends ServerPacket
{
	private final Player _player;

	public ExElementalSpiritAttackType(Player player)
	{
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ELEMENTAL_SPIRIT_ATTACK_TYPE.writeId(this, buffer);
		byte elementalId = this._player.getActiveElementalSpiritType();
		if (elementalId == ElementalSpiritType.WIND.getId())
		{
			buffer.writeByte(4);
		}
		else if (elementalId == ElementalSpiritType.EARTH.getId())
		{
			buffer.writeByte(8);
		}
		else
		{
			buffer.writeByte(elementalId);
		}
	}
}
