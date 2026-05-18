package net.sf.l2jdev.gameserver.model.skill.enums;

public enum SkillConditionPercentType
{
	MORE
	{
		@Override
		public boolean test(int x1, int x2)
		{
			return x1 >= x2;
		}
	},
	LESS
	{
		@Override
		public boolean test(int x1, int x2)
		{
			return x1 <= x2;
		}
	};

	public abstract boolean test(int var1, int var2);
}
