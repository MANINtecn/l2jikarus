package net.sf.l2jdev.gameserver.model.instancezone.conditions;

import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceTemplate;
import net.sf.l2jdev.gameserver.model.script.Quest;
import net.sf.l2jdev.gameserver.model.script.QuestState;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class ConditionQuest extends Condition
{
	public ConditionQuest(InstanceTemplate template, StatSet parameters, boolean onlyLeader, boolean showMessageAndHtml)
	{
		super(template, parameters, onlyLeader, showMessageAndHtml);
		this.setSystemMessage(SystemMessageId.C1_DOES_NOT_MEET_QUEST_REQUIREMENTS_AND_CANNOT_ENTER, (message, player) -> message.addString(player.getName()));
	}

	@Override
	protected boolean test(Player player, Npc npc)
	{
		int id = this.getParameters().getInt("id");
		Quest q = ScriptManager.getInstance().getQuest(id);
		if (q == null)
		{
			return false;
		}
		QuestState qs = player.getQuestState(q.getName());
		if (qs == null)
		{
			return false;
		}
		int cond = this.getParameters().getInt("cond", -1);
		return cond == -1 || qs.isCond(cond);
	}
}
