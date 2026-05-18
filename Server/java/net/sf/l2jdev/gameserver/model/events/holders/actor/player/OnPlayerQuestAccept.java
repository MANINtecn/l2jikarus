package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnPlayerQuestAccept implements IBaseEvent
{
	private final Player _player;
	private final int _questId;

	public OnPlayerQuestAccept(Player player, int questId)
	{
		this._player = player;
		this._questId = questId;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public int getQuestId()
	{
		return this._questId;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_QUEST_ACCEPT;
	}
}
