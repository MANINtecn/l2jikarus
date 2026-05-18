package net.sf.l2jdev.gameserver.model.script.newquestdata;

public class NewQuestLocation
{
	private final int _startLocationId;
	private final int _endLocationId;
	private final int _questLocationId;

	public NewQuestLocation(int startLocationId, int endLocationId, int questLocationId)
	{
		this._startLocationId = startLocationId;
		this._endLocationId = endLocationId;
		this._questLocationId = questLocationId;
	}

	public int getStartLocationId()
	{
		return this._startLocationId;
	}

	public int getEndLocationId()
	{
		return this._endLocationId;
	}

	public int getQuestLocationId()
	{
		return this._questLocationId;
	}
}
