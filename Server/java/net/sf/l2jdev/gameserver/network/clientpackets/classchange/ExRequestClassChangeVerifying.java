package net.sf.l2jdev.gameserver.network.clientpackets.classchange;

import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.script.Quest;
import net.sf.l2jdev.gameserver.model.script.QuestState;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.classchange.ExClassChangeSetAlarm;

public class ExRequestClassChangeVerifying extends ClientPacket
{
	private int _classId;

	@Override
	protected void readImpl()
	{
		this._classId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._classId == player.getPlayerClass().getId())
			{
				if (!player.isInCategory(CategoryType.FOURTH_CLASS_GROUP))
				{
					if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP))
					{
						if (!this.thirdClassCheck(player))
						{
							return;
						}
					}
					else if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP))
					{
						if (!this.secondClassCheck(player))
						{
							return;
						}
					}
					else if (player.isInCategory(CategoryType.FIRST_CLASS_GROUP) && !this.firstClassCheck(player))
					{
						return;
					}

					player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
				}
			}
		}
	}

	protected boolean firstClassCheck(Player player)
	{
		QuestState qs = null;
		if (player.isDeathKnight())
		{
			Quest quest = ScriptManager.getInstance().getQuest(10101);
			qs = player.getQuestState(quest.getName());
		}
		else if (player.isAssassin())
		{
			Quest quest = ScriptManager.getInstance().getQuest(10123);
			qs = player.getQuestState(quest.getName());
		}
		else if (player.isWarg())
		{
			Quest quest = ScriptManager.getInstance().getQuest(10144);
			qs = player.getQuestState(quest.getName());
		}
		else
		{
			switch (player.getRace())
			{
				case HUMAN:
					if (player.getPlayerClass() == PlayerClass.FIGHTER)
					{
						Quest questx = ScriptManager.getInstance().getQuest(10009);
						qs = player.getQuestState(questx.getName());
					}
					else
					{
						Quest questx = ScriptManager.getInstance().getQuest(10020);
						qs = player.getQuestState(questx.getName());
					}
					break;
				case ELF:
				{
					Quest quest = ScriptManager.getInstance().getQuest(10033);
					qs = player.getQuestState(quest.getName());
					break;
				}
				case DARK_ELF:
				{
					Quest quest = ScriptManager.getInstance().getQuest(10046);
					qs = player.getQuestState(quest.getName());
					break;
				}
				case ORC:
				{
					Quest quest = ScriptManager.getInstance().getQuest(10057);
					qs = player.getQuestState(quest.getName());
					break;
				}
				case DWARF:
				{
					Quest quest = ScriptManager.getInstance().getQuest(10079);
					qs = player.getQuestState(quest.getName());
					break;
				}
				case KAMAEL:
				{
					Quest quest = ScriptManager.getInstance().getQuest(10090);
					qs = player.getQuestState(quest.getName());
					break;
				}
				case SYLPH:
				{
					Quest quest = ScriptManager.getInstance().getQuest(10112);
					qs = player.getQuestState(quest.getName());
					break;
				}
				case HIGH_ELF:
				{
					Quest quest = ScriptManager.getInstance().getQuest(10123);
					qs = player.getQuestState(quest.getName());
				}
			}
		}

		return qs != null && qs.isCompleted();
	}

	protected boolean secondClassCheck(Player player)
	{
		return player.getLevel() >= 40;
	}

	protected boolean thirdClassCheck(Player player)
	{
		return player.getLevel() >= 76;
	}
}
