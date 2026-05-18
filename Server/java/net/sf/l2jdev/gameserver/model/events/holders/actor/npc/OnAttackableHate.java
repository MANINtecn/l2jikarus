package net.sf.l2jdev.gameserver.model.events.holders.actor.npc;

import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnAttackableHate implements IBaseEvent
{
	private final Attackable _npc;
	private final Player _player;
	private final boolean _isSummon;

	public OnAttackableHate(Attackable npc, Player player, boolean isSummon)
	{
		this._npc = npc;
		this._player = player;
		this._isSummon = isSummon;
	}

	public Attackable getNpc()
	{
		return this._npc;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public boolean isSummon()
	{
		return this._isSummon;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_NPC_HATE;
	}
}
