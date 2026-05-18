package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionSiegeZone extends Condition
{
	public static final int COND_NOT_ZONE = 1;
	public static final int COND_CAST_ATTACK = 2;
	public static final int COND_CAST_DEFEND = 4;
	public static final int COND_CAST_NEUTRAL = 8;
	public static final int COND_FORT_ATTACK = 16;
	public static final int COND_FORT_DEFEND = 32;
	public static final int COND_FORT_NEUTRAL = 64;
	private final int _value;
	private final boolean _self;

	public ConditionSiegeZone(int value, boolean self)
	{
		this._value = value;
		this._self = self;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		Creature target = this._self ? effector : effected;
		Castle castle = CastleManager.getInstance().getCastle(target);
		Fort fort = FortManager.getInstance().getFort(target);
		if (castle == null && fort == null)
		{
			return (this._value & 1) != 0;
		}
		return castle != null ? checkIfOk(target, castle, this._value) : checkIfOk(target, fort, this._value);
	}

	public static boolean checkIfOk(Creature creature, Castle castle, int value)
	{
		if (creature != null && creature.isPlayer())
		{
			Player player = creature.asPlayer();
			if (castle != null && castle.getResidenceId() > 0)
			{
				if (!castle.getZone().isActive())
				{
					if ((value & 1) != 0)
					{
						return true;
					}
				}
				else
				{
					if ((value & 2) != 0 && player.isRegisteredOnThisSiegeField(castle.getResidenceId()) && player.getSiegeState() == 1)
					{
						return true;
					}

					if ((value & 4) != 0 && player.isRegisteredOnThisSiegeField(castle.getResidenceId()) && player.getSiegeState() == 2)
					{
						return true;
					}

					if ((value & 8) != 0 && player.getSiegeState() == 0)
					{
						return true;
					}
				}
			}
			else if ((value & 1) != 0)
			{
				return true;
			}

			return false;
		}
		return false;
	}

	public static boolean checkIfOk(Creature creature, Fort fort, int value)
	{
		if (creature != null && creature.isPlayer())
		{
			Player player = creature.asPlayer();
			if (fort != null && fort.getResidenceId() > 0)
			{
				if (!fort.getZone().isActive())
				{
					if ((value & 1) != 0)
					{
						return true;
					}
				}
				else
				{
					if ((value & 16) != 0 && player.isRegisteredOnThisSiegeField(fort.getResidenceId()) && player.getSiegeState() == 1)
					{
						return true;
					}

					if ((value & 32) != 0 && player.isRegisteredOnThisSiegeField(fort.getResidenceId()) && player.getSiegeState() == 2)
					{
						return true;
					}

					if ((value & 64) != 0 && player.getSiegeState() == 0)
					{
						return true;
					}
				}
			}
			else if ((value & 1) != 0)
			{
				return true;
			}

			return false;
		}
		return false;
	}
}
