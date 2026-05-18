package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.config.NpcConfig;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class ConditionPlayerCanSweep extends Condition
{
	private final boolean _value;

	public ConditionPlayerCanSweep(boolean value)
	{
		this._value = value;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		boolean canSweep = false;
		if (effector.isPlayer() && skill != null)
		{
			Player sweeper = effector.asPlayer();

			for (WorldObject wo : skill.getTargetsAffected(sweeper, effected))
			{
				if (wo != null && wo.isAttackable())
				{
					Attackable attackable = wo.asAttackable();
					if (attackable.isDead())
					{
						if (attackable.isSpoiled())
						{
							canSweep = attackable.checkSpoilOwner(sweeper, true);
							if (canSweep)
							{
								canSweep = !attackable.isOldCorpse(sweeper, NpcConfig.CORPSE_CONSUME_SKILL_ALLOWED_TIME_BEFORE_DECAY, true);
							}

							if (canSweep)
							{
								canSweep = sweeper.getInventory().checkInventorySlotsAndWeight(attackable.getSpoilLootItems(), true, true);
							}
						}
						else
						{
							sweeper.sendPacket(SystemMessageId.THE_SWEEPER_HAS_FAILED_AS_THE_TARGET_IS_NOT_SPOILED);
						}
					}
				}
			}
		}

		return this._value == canSweep;
	}
}
