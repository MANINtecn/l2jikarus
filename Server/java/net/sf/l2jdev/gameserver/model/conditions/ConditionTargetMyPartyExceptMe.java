package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class ConditionTargetMyPartyExceptMe extends Condition
{
	private final boolean _value;

	public ConditionTargetMyPartyExceptMe(boolean value)
	{
		this._value = value;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		boolean isPartyMember = true;
		Player player = effector.asPlayer();
		if (player == null || effected == null || !effected.isPlayer())
		{
			isPartyMember = false;
		}
		else if (player == effected)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_ON_YOURSELF);
			isPartyMember = false;
		}
		else if (!player.isInParty() || !player.getParty().equals(effected.getParty()))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
			sm.addSkillName(skill);
			player.sendPacket(sm);
			isPartyMember = false;
		}

		return this._value == isPartyMember;
	}
}
