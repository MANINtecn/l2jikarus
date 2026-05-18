package net.sf.l2jdev.gameserver.network.clientpackets.balok;

import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExBalrogWarTeleport extends ClientPacket
{
	private static final Location BALOK_LOCATION = new Location(-18414, 180442, -3862);
 

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
			if (player.isDead())
			{
				player.sendPacket(SystemMessageId.DEAD_CHARACTERS_CANNOT_USE_TELEPORTATION);
			}
			else if (player.getMovieHolder() != null || player.isFishing() || player.isInInstance() || player.isOnEvent() || player.isInOlympiadMode() || player.inObserverMode() || player.isInTraingCamp() || player.isInTimedHuntingZone())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
			}
			else if (player.isInCombat() || player.isCastingNow())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_COMBAT);
			}
			else if (player.isAffected(EffectFlag.CANNOT_ESCAPE))
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
			}
			else if (!player.destroyItemByItemId(ItemProcessType.FEE, 57, 50000L, player, true))
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_MONEY_TO_USE_THE_FUNCTION);
			}
			else
			{
				player.abortCast();
				player.stopMove(null);
				player.setTeleportLocation(BALOK_LOCATION);
				player.castTeleportSkill();
			}
		}
	}
}
