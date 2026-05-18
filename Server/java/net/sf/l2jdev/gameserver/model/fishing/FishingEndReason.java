package net.sf.l2jdev.gameserver.model.fishing;

public enum FishingEndReason
{
	LOSE(0),
	WIN(1),
	STOP(2);

	private final int _reason;

	private FishingEndReason(int reason)
	{
		this._reason = reason;
	}

	public int getReason()
	{
		return this._reason;
	}
}
