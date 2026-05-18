package net.sf.l2jdev.gameserver.model.clan.enums;

public enum ClanHallGrade
{
	GRADE_S(50),
	GRADE_A(40),
	GRADE_B(30),
	GRADE_C(20),
	GRADE_D(10),
	GRADE_NONE(0);

	private final int _gradeValue;

	private ClanHallGrade(int gradeValue)
	{
		this._gradeValue = gradeValue;
	}

	public int getGradeValue()
	{
		return this._gradeValue;
	}
}
