package net.sf.l2jdev.gameserver.model.events.holders.actor.npc;

import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnNpcManorBypass implements IBaseEvent
{
	private final Player _player;
	private final Npc _target;
	private final int _request;
	private final int _manorId;
	private final boolean _nextPeriod;

	public OnNpcManorBypass(Player player, Npc target, int request, int manorId, boolean nextPeriod)
	{
		this._player = player;
		this._target = target;
		this._request = request;
		this._manorId = manorId;
		this._nextPeriod = nextPeriod;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public Npc getTarget()
	{
		return this._target;
	}

	public int getRequest()
	{
		return this._request;
	}

	public int getManorId()
	{
		return this._manorId;
	}

	public boolean isNextPeriod()
	{
		return this._nextPeriod;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_NPC_MANOR_BYPASS;
	}
}
