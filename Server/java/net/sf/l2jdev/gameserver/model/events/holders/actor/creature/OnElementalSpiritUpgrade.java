package net.sf.l2jdev.gameserver.model.events.holders.actor.creature;

import net.sf.l2jdev.gameserver.model.ElementalSpirit;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnElementalSpiritUpgrade implements IBaseEvent
{
	private final ElementalSpirit _spirit;
	private final Player _player;

	public OnElementalSpiritUpgrade(Player player, ElementalSpirit spirit)
	{
		this._player = player;
		this._spirit = spirit;
	}

	public ElementalSpirit getSpirit()
	{
		return this._spirit;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_ELEMENTAL_SPIRIT_UPGRADE;
	}
}
