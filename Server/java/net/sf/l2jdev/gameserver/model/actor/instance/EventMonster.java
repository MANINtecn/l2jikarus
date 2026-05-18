package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;

public class EventMonster extends Monster
{
	public boolean block_skill_attack = false;
	public boolean drop_on_ground = false;

	public EventMonster(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.EventMob);
	}

	public void eventSetBlockOffensiveSkills(boolean value)
	{
		this.block_skill_attack = value;
	}

	public void eventSetDropOnGround(boolean value)
	{
		this.drop_on_ground = value;
	}

	public boolean eventDropOnGround()
	{
		return this.drop_on_ground;
	}

	public boolean eventSkillAttackBlocked()
	{
		return this.block_skill_attack;
	}
}
