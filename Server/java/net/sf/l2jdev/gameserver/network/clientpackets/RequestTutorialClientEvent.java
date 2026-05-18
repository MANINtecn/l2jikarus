package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.script.QuestState;

public class RequestTutorialClientEvent extends ClientPacket
{
	int _eventId = 0;

	@Override
	protected void readImpl()
	{
		this._eventId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			QuestState qs = player.getQuestState("255_Tutorial");
			if (qs != null)
			{
				qs.getQuest().notifyEvent("CE" + this._eventId, null, player);
			}
		}
	}
}
