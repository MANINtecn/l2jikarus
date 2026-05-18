package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.taskmanagers.AttackStanceTaskManager;

public class ConditionPlayerCanSwitchSubclass extends Condition
{
	private final int _subIndex;

	public ConditionPlayerCanSwitchSubclass(int subIndex)
	{
		this._subIndex = subIndex;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		boolean canSwitchSub = true;
		Player player = effector.asPlayer();
		if (player != null && !player.isAlikeDead())
		{
			if ((this._subIndex == 0 || player.getSubClasses().get(this._subIndex) != null) && player.getClassIndex() != this._subIndex)
			{
				if (!player.isInventoryUnder90(true))
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_CREATE_OR_CHANGE_A_SUBCLASS_WHILE_YOU_HAVE_NO_FREE_SPACE_IN_YOUR_INVENTORY);
					canSwitchSub = false;
				}
				else if (player.getWeightPenalty() >= 2)
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_CREATE_OR_CHANGE_A_DUAL_CLASS_WHILE_YOU_HAVE_OVERWEIGHT);
					canSwitchSub = false;
				}
				else if (player.isRegisteredOnEvent())
				{
					player.sendMessage("You cannot change your subclass while registered in an event.");
					canSwitchSub = false;
				}
				else if (player.isAllSkillsDisabled())
				{
					canSwitchSub = false;
				}
				else if (player.isAffected(EffectFlag.MUTED))
				{
					canSwitchSub = false;
					player.sendPacket(SystemMessageId.YOU_CANNOT_CHANGE_THE_CLASS_BECAUSE_OF_IDENTITY_CRISIS);
				}
				else if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) || player.getPvpFlag() > 0 || player.isInInstance() || player.isTransformed() || player.isMounted())
				{
					canSwitchSub = false;
				}
			}
			else
			{
				canSwitchSub = false;
			}
		}
		else
		{
			canSwitchSub = false;
		}

		return canSwitchSub;
	}
}
