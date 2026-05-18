package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class RequestRecordInfo extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.updateUserInfo();
			World.getInstance().forEachVisibleObject(player, WorldObject.class, object -> {
				if (object.isVisibleFor(player))
				{
					object.sendInfo(player);
					if (object.isCreature())
					{
						Creature creature = object.asCreature();
						if (creature.hasAI())
						{
							creature.getAI().describeStateToPlayer(player);
						}
					}
				}
			});
		}
	}
}
