package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerActiveEffectId extends Condition
{
	private final int _effectId;
	private final int _effectLvl;

	public ConditionPlayerActiveEffectId(int effectId)
	{
		this._effectId = effectId;
		this._effectLvl = -1;
	}

	public ConditionPlayerActiveEffectId(int effectId, int effectLevel)
	{
		this._effectId = effectId;
		this._effectLvl = effectLevel;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		BuffInfo info = effector.getEffectList().getBuffInfoBySkillId(this._effectId);
		return info != null && (this._effectLvl == -1 || this._effectLvl <= info.getSkill().getLevel());
	}
}
