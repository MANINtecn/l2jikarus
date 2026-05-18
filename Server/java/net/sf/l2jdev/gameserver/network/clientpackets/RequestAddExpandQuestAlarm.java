package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.script.Quest;

public class RequestAddExpandQuestAlarm extends ClientPacket
{
	private int _questId;

	@Override
	protected void readImpl()
	{
		this._questId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Quest quest = ScriptManager.getInstance().getQuest(this._questId);
			if (quest != null)
			{
				quest.sendNpcLogList(player);
			}
		}
	}
}
