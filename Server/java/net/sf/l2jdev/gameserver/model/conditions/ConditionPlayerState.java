package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerState;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerState extends Condition
{
	private final PlayerState _check;
	private final boolean _required;

	public ConditionPlayerState(PlayerState check, boolean required)
	{
		this._check = check;
		this._required = required;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		Player player = effector.asPlayer();
		switch (this._check)
		{
			case RESTING:
				if (player != null)
				{
					return player.isSitting() == this._required;
				}

				return !this._required;
			case MOVING:
				return effector.isMoving() == this._required;
			case RUNNING:
				return effector.isMoving() == this._required && effector.isRunning() == this._required;
			case STANDING:
				if (player == null)
				{
					return this._required != effector.isMoving();
				}

				return this._required != (player.isSitting() || player.isMoving());
			case FLYING:
				return effector.isFlying() == this._required;
			case BEHIND:
				return effector.isBehind(effected) == this._required;
			case FRONT:
				return effector.isInFrontOf(effected) == this._required;
			case CHAOTIC:
				if (player != null)
				{
					return player.getReputation() < 0 == this._required;
				}

				return !this._required;
			case OLYMPIAD:
				if (player != null)
				{
					return player.isInOlympiadMode() == this._required;
				}

				return !this._required;
			default:
				return !this._required;
		}
	}
}
