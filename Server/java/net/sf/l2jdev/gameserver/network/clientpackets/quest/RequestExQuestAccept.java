package net.sf.l2jdev.gameserver.network.clientpackets.quest;

import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.script.Quest;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestExQuestAccept extends ClientPacket
{
	private int _questId;
	private boolean _isAccepted;

	@Override
	protected void readImpl()
	{
		this._questId = this.readInt();
		this._isAccepted = this.readBoolean();
	}

	@Override
	protected void runImpl()
	{
		if (this._isAccepted)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				Quest quest = ScriptManager.getInstance().getQuest(this._questId);
				if (quest != null)
				{
					quest.notifyEvent("ACCEPT", null, player);
				}
			}
		}
	}
}
