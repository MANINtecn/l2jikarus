package net.sf.l2jdev.gameserver.model.actor.instance;

import java.util.List;

import net.sf.l2jdev.gameserver.data.xml.SkillTreeData;
import net.sf.l2jdev.gameserver.model.SkillLearn;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.skill.enums.AcquireSkillType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAcquirableSkillListByClass;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class Fisherman extends Merchant
{
	public Fisherman(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Fisherman);
	}

	@Override
	public String getHtmlPath(int npcId, int value, Player player)
	{
		String pom = "";
		if (value == 0)
		{
			pom = Integer.toString(npcId);
		}
		else
		{
			pom = npcId + "-" + value;
		}

		return "data/html/fisherman/" + pom + ".htm";
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.equalsIgnoreCase("FishSkillList"))
		{
			showFishSkillList(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	public static void showFishSkillList(Player player)
	{
		List<SkillLearn> skills = SkillTreeData.getInstance().getAvailableFishingSkills(player);
		if (skills.isEmpty())
		{
			int minlLevel = SkillTreeData.getInstance().getMinLevelForNewSkill(player, SkillTreeData.getInstance().getFishingSkillTree());
			if (minlLevel > 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
				sm.addInt(minlLevel);
				player.sendPacket(sm);
			}
			else
			{
				player.sendPacket(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
			}
		}
		else
		{
			player.sendPacket(new ExAcquirableSkillListByClass(skills, AcquireSkillType.FISHING));
		}
	}
}
