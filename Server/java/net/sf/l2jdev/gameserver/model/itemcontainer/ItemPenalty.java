package net.sf.l2jdev.gameserver.model.itemcontainer;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;

public class ItemPenalty extends ItemContainer
{
	private final Player _player;

	public ItemPenalty(Player player)
	{
		this._player = player;
		this.restore();
	}

	@Override
	public Player getOwner()
	{
		return this._player;
	}

	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.PENALTY;
	}
}
