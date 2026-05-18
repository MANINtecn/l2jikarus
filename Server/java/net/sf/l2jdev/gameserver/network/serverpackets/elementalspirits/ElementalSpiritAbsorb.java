package net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ElementalSpiritAbsorb extends UpdateElementalSpiritPacket
{
	public ElementalSpiritAbsorb(Player player, byte type, boolean absorbed)
	{
		super(player, type, absorbed);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ELEMENTAL_SPIRIT_ABSORB.writeId(this, buffer);
		this.writeUpdate(buffer);
	}
}
