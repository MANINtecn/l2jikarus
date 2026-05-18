package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerProfessionCancel implements IBaseEvent
{
	private final Player _player;
	private final int _classId;

	public OnPlayerProfessionCancel(Player player, int classId)
	{
		this._player = player;
		this._classId = classId;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public int getClassId()
	{
		return this._classId;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_PROFESSION_CANCEL;
	}
}
