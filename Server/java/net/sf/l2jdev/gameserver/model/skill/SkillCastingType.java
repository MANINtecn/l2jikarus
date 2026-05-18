package net.sf.l2jdev.gameserver.model.skill;

public enum SkillCastingType
{
	SIMULTANEOUS(-1),
	NORMAL(0),
	NORMAL_SECOND(1),
	BLUE(2),
	GREEN(3),
	RED(4);

	private final int _clientBarId;

	private SkillCastingType(int clientBarId)
	{
		this._clientBarId = clientBarId;
	}

	public int getClientBarId()
	{
		return this._clientBarId;
	}
}
