package net.sf.l2jdev.gameserver.model.itemauction;

public enum ItemAuctionState
{
	CREATED((byte) 0),
	STARTED((byte) 1),
	FINISHED((byte) 2);

	private final byte _stateId;

	private ItemAuctionState(byte stateId)
	{
		this._stateId = stateId;
	}

	public byte getStateId()
	{
		return this._stateId;
	}

	public static ItemAuctionState stateForStateId(byte stateId)
	{
		for (ItemAuctionState state : values())
		{
			if (state.getStateId() == stateId)
			{
				return state;
			}
		}

		return null;
	}
}
