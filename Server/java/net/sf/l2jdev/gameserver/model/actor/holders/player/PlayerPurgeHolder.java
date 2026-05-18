package net.sf.l2jdev.gameserver.model.actor.holders.player;

public class PlayerPurgeHolder
{
	private final int _points;
	private final int _keys;
	private int _remainingKeys;

	public PlayerPurgeHolder(int points, int keys, int remainingKeys)
	{
		this._points = points;
		this._keys = keys;
		this._remainingKeys = remainingKeys;
	}

	public int getPoints()
	{
		return this._remainingKeys == 0 ? 0 : this._points;
	}

	public int getKeys()
	{
		return this._keys;
	}

	public int getRemainingKeys()
	{
		if (this._keys == 0 && this._remainingKeys == 0)
		{
			this._remainingKeys = 40;
		}

		return this._remainingKeys;
	}
}
