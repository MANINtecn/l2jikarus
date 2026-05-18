package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.script.QuestType;

public class OnPlayerQuestComplete implements IBaseEvent
{
	private final Player _player;
	private final int _questId;
	private final QuestType _questType;

	public OnPlayerQuestComplete(Player player, int questId, QuestType questType)
	{
		this._player = player;
		this._questId = questId;
		this._questType = questType;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public int getQuestId()
	{
		return this._questId;
	}

	public QuestType getQuestType()
	{
		return this._questType;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_QUEST_COMPLETE;
	}
}
