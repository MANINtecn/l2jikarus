package net.sf.l2jdev.gameserver.model.actor.instance;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sf.l2jdev.gameserver.data.xml.SkillTreeData;
import net.sf.l2jdev.gameserver.model.SkillLearn;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.actor.status.FolkStatus;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.skill.enums.AcquireSkillType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAcquirableSkillListByClass;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class Folk extends Npc
{
	public Folk(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Folk);
		this.setInvul(false);
	}

	@Override
	public FolkStatus getStatus()
	{
		return (FolkStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		this.setStatus(new FolkStatus(this));
	}

	public static void showSkillList(Player player, Npc npc, PlayerClass playerClass)
	{
		int npcId = npc.getTemplate().getId();
		if (npcId == 32611)
		{
			List<SkillLearn> skills = SkillTreeData.getInstance().getAvailableCollectSkills(player);
			if (skills.isEmpty())
			{
				int minLevel = SkillTreeData.getInstance().getMinLevelForNewSkill(player, SkillTreeData.getInstance().getCollectSkillTree());
				if (minLevel > 0)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
					sm.addInt(minLevel);
					player.sendPacket(sm);
				}
				else
				{
					player.sendPacket(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
				}
			}
			else
			{
				player.sendPacket(new ExAcquirableSkillListByClass(skills, AcquireSkillType.COLLECT));
			}
		}
		else
		{
			Collection<SkillLearn> skills = SkillTreeData.getInstance().getAvailableSkills(player, playerClass, false, false);
			if (skills.isEmpty())
			{
				Map<Long, SkillLearn> skillTree = SkillTreeData.getInstance().getCompleteClassSkillTree(playerClass);
				int minLevel = SkillTreeData.getInstance().getMinLevelForNewSkill(player, skillTree);
				if (minLevel > 0)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
					sm.addInt(minLevel);
					player.sendPacket(sm);
				}
				else if (player.getPlayerClass().level() == 1)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN_PLEASE_COME_BACK_AFTER_S1ND_CLASS_CHANGE);
					sm.addInt(2);
					player.sendPacket(sm);
				}
				else
				{
					player.sendPacket(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
				}
			}
			else
			{
				player.sendPacket(new ExAcquirableSkillListByClass(skills, AcquireSkillType.CLASS));
			}
		}
	}
}
