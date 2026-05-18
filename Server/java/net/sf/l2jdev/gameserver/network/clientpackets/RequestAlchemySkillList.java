package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAlchemySkillList;

public class RequestAlchemySkillList extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && player.getRace() == Race.ERTHEIA)
		{
			player.sendPacket(new ExAlchemySkillList(player));
		}
	}
}
