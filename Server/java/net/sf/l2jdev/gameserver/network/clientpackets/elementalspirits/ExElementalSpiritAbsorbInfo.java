package net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits.ElementalSpiritAbsorbInfo;

public class ExElementalSpiritAbsorbInfo extends ClientPacket
{
	private byte _type;

	@Override
	protected void readImpl()
	{
		this.readByte();
		this._type = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ElementalSpiritAbsorbInfo(player, this._type));
		}
	}
}
