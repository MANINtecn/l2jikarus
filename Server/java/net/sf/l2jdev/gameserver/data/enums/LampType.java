package net.sf.l2jdev.gameserver.data.enums;

public enum LampType
{
	RED(1),
	PURPLE(2),
	BLUE(3),
	GREEN(4);

	private int _grade;

	private LampType(int grade)
	{
		this._grade = grade;
	}

	public int getGrade()
	{
		return this._grade;
	}
}
