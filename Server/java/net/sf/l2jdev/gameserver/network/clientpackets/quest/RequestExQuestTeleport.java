package net.sf.l2jdev.gameserver.network.clientpackets.quest;

import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.script.Quest;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestExQuestTeleport extends ClientPacket
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
		if (player != null && !player.isTeleporting() && !player.isCastingTeleportSkill())
		{
			if (player.isPrisoner())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_FUNCTION_IN_THE_UNDERGROUND_LABYRINTH);
			}
			else
			{
				Quest quest = ScriptManager.getInstance().getQuest(this._questId);
				if (quest != null)
				{
					quest.notifyEvent("TELEPORT", null, player);
				}
			}
		}
	}
}
