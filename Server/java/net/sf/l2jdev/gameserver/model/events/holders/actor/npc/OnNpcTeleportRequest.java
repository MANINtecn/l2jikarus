package net.sf.l2jdev.gameserver.model.events.holders.actor.npc;

import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.teleporter.TeleportLocation;

public class OnNpcTeleportRequest implements IBaseEvent
{
	private final Player _player;
	private final Npc _npc;
	private final TeleportLocation _loc;

	public OnNpcTeleportRequest(Player player, Npc npc, TeleportLocation loc)
	{
		this._player = player;
		this._npc = npc;
		this._loc = loc;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public Npc getNpc()
	{
		return this._npc;
	}

	public TeleportLocation getLocation()
	{
		return this._loc;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_NPC_TELEPORT_REQUEST;
	}
}
