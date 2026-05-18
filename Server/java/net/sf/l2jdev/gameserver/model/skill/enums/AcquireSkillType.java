package net.sf.l2jdev.gameserver.model.skill.enums;

public enum AcquireSkillType
{
	CLASS(0),
	DUMMY(1),
	PLEDGE(2),
	SUBPLEDGE(3),
	TRANSFORM(4),
	TRANSFER(5),
	SUBCLASS(6),
	COLLECT(7),
	DUMMY2(8),
	DUMMY3(9),
	FISHING(10),
	REVELATION(11),
	REVELATION_DUALCLASS(12),
	DUALCLASS(13),
	ALCHEMY(140);

	private final int _id;

	private AcquireSkillType(int id)
	{
		this._id = id;
	}

	public int getId()
	{
		return this._id;
	}

	public static AcquireSkillType getAcquireSkillType(int id)
	{
		for (AcquireSkillType type : values())
		{
			if (type.getId() == id)
			{
				return type;
			}
		}

		return null;
	}
}
