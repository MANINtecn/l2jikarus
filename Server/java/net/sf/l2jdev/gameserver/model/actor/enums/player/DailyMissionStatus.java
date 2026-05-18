package net.sf.l2jdev.gameserver.model.actor.enums.player;

public enum DailyMissionStatus
{
	AVAILABLE(1),
	NOT_AVAILABLE(2),
	COMPLETED(3);

	private final int _clientId;

	private DailyMissionStatus(int clientId)
	{
		this._clientId = clientId;
	}

	public int getClientId()
	{
		return this._clientId;
	}

	public static DailyMissionStatus valueOf(int clientId)
	{
		for (DailyMissionStatus type : values())
		{
			if (type.getClientId() == clientId)
			{
				return type;
			}
		}

		return null;
	}
}
