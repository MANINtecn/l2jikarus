package net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits.ElementalSpiritEvolutionInfo;

public class ExElementalSpiritEvolutionInfo extends ClientPacket
{
	private byte _id;

	@Override
	protected void readImpl()
	{
		this._id = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ElementalSpiritEvolutionInfo(player, this._id));
		}
	}
}
