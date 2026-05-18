package net.sf.l2jdev.gameserver.model.actor.transform;

import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class AdditionalItemHolder extends ItemHolder
{
	private final boolean _allowed;

	public AdditionalItemHolder(int id, boolean allowed)
	{
		super(id, 0L);
		this._allowed = allowed;
	}

	public boolean isAllowedToUse()
	{
		return this._allowed;
	}
}
