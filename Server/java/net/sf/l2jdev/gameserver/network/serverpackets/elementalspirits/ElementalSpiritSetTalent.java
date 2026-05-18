package net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ElementalSpiritSetTalent extends UpdateElementalSpiritPacket
{
	public ElementalSpiritSetTalent(Player player, byte type, boolean result)
	{
		super(player, type, result);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ELEMENTAL_SPIRIT_SET_TALENT.writeId(this, buffer);
		this.writeUpdate(buffer);
	}
}
