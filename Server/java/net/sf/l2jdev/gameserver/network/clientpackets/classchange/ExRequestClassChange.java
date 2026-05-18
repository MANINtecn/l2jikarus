package net.sf.l2jdev.gameserver.network.clientpackets.classchange;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.data.xml.CategoryData;
import net.sf.l2jdev.gameserver.model.ElementalSpirit;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.PlaySound;
import net.sf.l2jdev.gameserver.network.serverpackets.UserInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.classchange.ExClassChangeSetAlarm;
import net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits.ElementalSpiritInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits.ExElementalSpiritAttackType;

public class ExRequestClassChange extends ClientPacket
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
			boolean canChange = false;

			for (PlayerClass cId : player.getPlayerClass().getNextClasses())
			{
				if (cId.getId() == this._classId)
				{
					canChange = true;
					break;
				}
			}

			if (!canChange)
			{
				PacketLogger.warning(player + " tried to change class from " + player.getPlayerClass() + " to " + PlayerClass.getPlayerClass(this._classId) + "!");
			}
			else
			{
				canChange = false;
				int playerLevel = player.getLevel();
				if (player.isInCategory(CategoryType.FIRST_CLASS_GROUP) && playerLevel >= 18)
				{
					canChange = CategoryData.getInstance().isInCategory(CategoryType.SECOND_CLASS_GROUP, this._classId);
				}
				else if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP) && playerLevel >= 38)
				{
					canChange = CategoryData.getInstance().isInCategory(CategoryType.THIRD_CLASS_GROUP, this._classId);
				}
				else if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP) && playerLevel >= 76)
				{
					canChange = CategoryData.getInstance().isInCategory(CategoryType.FOURTH_CLASS_GROUP, this._classId);
				}

				if (canChange)
				{
					player.setPlayerClass(this._classId);
					if (player.isSubClassActive())
					{
						player.getSubClasses().get(player.getClassIndex()).setPlayerClass(player.getActiveClass());
					}
					else
					{
						player.setBaseClass(player.getActiveClass());
					}

					if (!PlayerConfig.DISABLE_TUTORIAL)
					{
						switch (player.getPlayerClass())
						{
							case KNIGHT:
							case ELVEN_KNIGHT:
							case PALUS_KNIGHT:
							case DEATH_BLADE_HUMAN:
							case DEATH_BLADE_ELF:
							case DEATH_BLADE_DARK_ELF:
							case DIVINE_TEMPLAR_1:
								player.addItem(ItemProcessType.REWARD, 93028, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93493, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93496, 1L, player, true);
								break;
							case WARRIOR:
								player.addItem(ItemProcessType.REWARD, 93028, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93034, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93493, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93496, 1L, player, true);
								break;
							case ROGUE:
							case ELVEN_SCOUT:
							case ASSASSIN:
								player.addItem(ItemProcessType.REWARD, 93029, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93030, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 1341, 2000L, player, true);
								player.addItem(ItemProcessType.REWARD, 93494, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93496, 1L, player, true);
								break;
							case WARG_1:
								player.addItem(ItemProcessType.REWARD, 93035, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93494, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93496, 1L, player, true);
								break;
							case WIZARD:
							case CLERIC:
							case ELVEN_WIZARD:
							case ORACLE:
							case DARK_WIZARD:
							case SHILLIEN_ORACLE:
							case ORC_SHAMAN:
							case ELEMENT_WEAVER_1:
								player.addItem(ItemProcessType.REWARD, 93033, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93495, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93496, 1L, player, true);
								break;
							case ORC_RAIDER:
								player.addItem(ItemProcessType.REWARD, 93032, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93493, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93497, 1L, player, true);
								break;
							case ORC_MONK:
								player.addItem(ItemProcessType.REWARD, 93035, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93493, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93497, 1L, player, true);
								break;
							case ARTISAN:
							case SCAVENGER:
								player.addItem(ItemProcessType.REWARD, 93031, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93034, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93493, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93496, 1L, player, true);
								break;
							case TROOPER:
								player.addItem(ItemProcessType.REWARD, 93037, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93494, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93496, 1L, player, true);
								break;
							case WARDER:
								player.addItem(ItemProcessType.REWARD, 93030, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 1341, 2000L, player, true);
								player.addItem(ItemProcessType.REWARD, 93494, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93496, 1L, player, true);
								break;
							case SOUL_FINDER:
								player.addItem(ItemProcessType.REWARD, 93036, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93494, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93496, 1L, player, true);
								break;
							case SHARPSHOOTER:
								player.addItem(ItemProcessType.REWARD, 94892, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 94897, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93494, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93496, 1L, player, true);
								break;
							case RIDER:
								player.addItem(ItemProcessType.REWARD, 93034, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93493, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93496, 1L, player, true);
								break;
							case ASSASSIN_MALE_1:
							case ASSASSIN_FEMALE_1:
								player.addItem(ItemProcessType.REWARD, 94998, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93494, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93496, 1L, player, true);
								break;
							case ASSASSIN_FEMALE_3:
							case ASSASSIN_MALE_3:
								player.setAssassinationPoints(1);
								break;
							case BLOOD_ROSE_1:
								player.addItem(ItemProcessType.REWARD, 94998, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93495, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93496, 1L, player, true);
								break;
							case HATAMOTO:
								player.addItem(ItemProcessType.REWARD, 129, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93493, 1L, player, true);
								player.addItem(ItemProcessType.REWARD, 93496, 1L, player, true);
						}
					}

					if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP))
					{
						if (player.getSpirits() == null)
						{
							player.initElementalSpirits();
						}

						for (ElementalSpirit spirit : player.getSpirits())
						{
							if (spirit.getStage() == 0)
							{
								spirit.upgrade();
							}
						}

						UserInfo userInfo = new UserInfo(player);
						userInfo.addComponentType(UserInfoType.ATT_SPIRITS);
						player.sendPacket(userInfo);
						player.sendPacket(new ElementalSpiritInfo(player, (byte) 0));
						player.sendPacket(new ExElementalSpiritAttackType(player));
					}

					if (PlayerConfig.AUTO_LEARN_SKILLS)
					{
						player.giveAvailableSkills(PlayerConfig.AUTO_LEARN_FS_SKILLS, true, PlayerConfig.AUTO_LEARN_SKILLS_WITHOUT_ITEMS);
					}

					player.store(false);
					player.broadcastUserInfo();
					player.sendSkillList();
					player.sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
					if (PlayerConfig.DISABLE_TUTORIAL && !player.isInCategory(CategoryType.FOURTH_CLASS_GROUP) && (player.isInCategory(CategoryType.SECOND_CLASS_GROUP) && playerLevel >= 38 || player.isInCategory(CategoryType.THIRD_CLASS_GROUP) && playerLevel >= 76))
					{
						player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
					}
				}
			}
		}
	}
}
