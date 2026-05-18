package net.sf.l2jdev.gameserver.model.skill.enums;

import net.sf.l2jdev.gameserver.model.actor.Player;

public enum SkillConditionAlignment
{
	LAWFUL
	{
		@Override
		public boolean test(Player player)
		{
			return player.getReputation() >= 0;
		}
	},
	CHAOTIC
	{
		@Override
		public boolean test(Player player)
		{
			return player.getReputation() < 0;
		}
	};

	public abstract boolean test(Player var1);
}
