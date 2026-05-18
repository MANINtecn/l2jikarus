package net.sf.l2jdev.gameserver.ai;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public abstract class PlayableAI extends CreatureAI
{
	protected PlayableAI(Playable playable)
	{
		super(playable);
	}

	@Override
	protected void onIntentionAttack(Creature target)
	{
		if (target != null && target.isPlayable())
		{
			Player player = this._actor.asPlayer();
			Player targetPlayer = target.asPlayer();
			if (targetPlayer.isProtectionBlessingAffected() && player.getLevel() - targetPlayer.getLevel() >= 10 && player.getReputation() < 0 && !target.isInsideZone(ZoneId.PVP))
			{
				player.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
				this.clientActionFailed();
				return;
			}

			if (player.isProtectionBlessingAffected() && targetPlayer.getLevel() - player.getLevel() >= 10 && targetPlayer.getReputation() < 0 && !target.isInsideZone(ZoneId.PVP))
			{
				player.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
				this.clientActionFailed();
				return;
			}

			if ((targetPlayer.isCursedWeaponEquipped() && player.getLevel() <= 20) || (player.isCursedWeaponEquipped() && targetPlayer.getLevel() <= 20))
			{
				player.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
				this.clientActionFailed();
				return;
			}
		}

		super.onIntentionAttack(target);
	}

	@Override
	protected void onIntentionCast(Skill skill, WorldObject target, Item item, boolean forceUse, boolean dontMove)
	{
		if (target != null && target.isPlayable() && skill.hasNegativeEffect())
		{
			Player player = this._actor.asPlayer();
			Player targetPlayer = target.asPlayer();
			if (targetPlayer.isProtectionBlessingAffected() && player.getLevel() - targetPlayer.getLevel() >= 10 && player.getReputation() < 0 && !target.isInsideZone(ZoneId.PVP))
			{
				player.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
				this.clientActionFailed();
				return;
			}

			if (player.isProtectionBlessingAffected() && targetPlayer.getLevel() - player.getLevel() >= 10 && targetPlayer.getReputation() < 0 && !target.isInsideZone(ZoneId.PVP))
			{
				player.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
				this.clientActionFailed();
				return;
			}

			if (targetPlayer.isCursedWeaponEquipped() && (player.getLevel() <= 20 || targetPlayer.getLevel() <= 20))
			{
				player.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
				this.clientActionFailed();
				return;
			}
		}

		super.onIntentionCast(skill, target, item, forceUse, dontMove);
	}
}
