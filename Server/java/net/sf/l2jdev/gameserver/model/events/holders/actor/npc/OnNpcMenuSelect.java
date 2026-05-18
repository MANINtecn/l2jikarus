package net.sf.l2jdev.gameserver.model.events.holders.actor.npc;

import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnNpcMenuSelect implements IBaseEvent
{
	private final Player _player;
	private final Npc _npc;
	private final int _ask;
	private final int _reply;

	public OnNpcMenuSelect(Player player, Npc npc, int ask, int reply)
	{
		this._player = player;
		this._npc = npc;
		this._ask = ask;
		this._reply = reply;
	}

	public Player getTalker()
	{
		return this._player;
	}

	public Npc getNpc()
	{
		return this._npc;
	}

	public int getAsk()
	{
		return this._ask;
	}

	public int getReply()
	{
		return this._reply;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_NPC_MENU_SELECT;
	}
}
