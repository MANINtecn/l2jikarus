package net.sf.l2jdev.gameserver.model.options;

public class VariationFee
{
	private final int _itemId;
	private final long _itemCount;
	private final long _adenaFee;
	private final long _cancelFee;

	public VariationFee(int itemId, long itemCount, long adenaFee, long cancelFee)
	{
		this._itemId = itemId;
		this._itemCount = itemCount;
		this._adenaFee = adenaFee;
		this._cancelFee = cancelFee;
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public long getItemCount()
	{
		return this._itemCount;
	}

	public long getAdenaFee()
	{
		return this._adenaFee;
	}

	public long getCancelFee()
	{
		return this._cancelFee;
	}
}
