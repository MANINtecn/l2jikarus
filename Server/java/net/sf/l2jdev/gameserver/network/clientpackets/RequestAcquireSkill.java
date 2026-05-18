package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.List;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.data.xml.SkillTreeData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.SkillLearn;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.enums.player.IllegalActionPunishmentType;
import net.sf.l2jdev.gameserver.model.actor.holders.player.SubClassHolder;
import net.sf.l2jdev.gameserver.model.actor.instance.Fisherman;
import net.sf.l2jdev.gameserver.model.actor.instance.VillageMaster;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerSkillLearn;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.script.QuestState;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.AcquireSkillType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.variables.PlayerVariables;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.AcquireSkillDone;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAcquirableSkillListByClass;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAcquireSkillResult;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAlchemySkillList;
import net.sf.l2jdev.gameserver.network.serverpackets.ExBasicActionList;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeSkillList;
import net.sf.l2jdev.gameserver.network.serverpackets.ShortcutInit;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.UserInfo;

public class RequestAcquireSkill extends ClientPacket
{
	private int _id;
	private int _level;
	private AcquireSkillType _skillType;
	private int _subType;

	@Override
	protected void readImpl()
	{
		this._id = this.readInt();
		this._level = this.readShort();
		this.readShort();
		this._skillType = AcquireSkillType.getAcquireSkillType(this.readInt());
		if (this._skillType == AcquireSkillType.SUBPLEDGE)
		{
			this._subType = this.readInt();
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.isCastingNow())
			{
				player.sendPacket(SystemMessageId.THE_OPTION_IS_UNAVAILABLE_WHEN_USING_SKILLS);
			}
			else if (player.isTransformed() || player.isMounted())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_STATE_YOU_CAN_ENHANCE_SKILLS_WHEN_NOT_IN_BATTLE_AND_CANNOT_USE_THE_FUNCTION_WHILE_TRANSFORMED_IN_BATTLE_ON_A_MOUNT_OR_WHILE_THE_SKILL_IS_ON_COOLDOWN);
			}
			else if (this._level >= 1 && this._level <= 1000 && this._id >= 1)
			{
				Npc trainer = player.getLastFolkNPC();
				if (this._skillType == AcquireSkillType.CLASS || trainer != null && trainer.isNpc() && (trainer.canInteract(player) || player.isGM()))
				{
					int skillId = player.getReplacementSkill(this._id);
					Skill existingSkill = player.getKnownSkill(skillId);
					Skill skill = SkillData.getInstance().getSkill(skillId, this._level, existingSkill == null ? 0 : existingSkill.getSubLevel());
					if (skill == null)
					{
						PacketLogger.warning(RequestAcquireSkill.class.getSimpleName() + ": " + player + " is trying to learn a null skill Id: " + this._id + " level: " + this._level + "!");
					}
					else
					{
						int prevSkillLevel = player.getSkillLevel(skillId);
						if (this._skillType != AcquireSkillType.TRANSFER && this._skillType != AcquireSkillType.SUBPLEDGE)
						{
							if (prevSkillLevel == this._level)
							{
								return;
							}

							if (prevSkillLevel != this._level - 1)
							{
								player.sendPacket(SystemMessageId.THE_PREVIOUS_LEVEL_SKILL_HAS_NOT_BEEN_LEARNED);
								PunishmentManager.handleIllegalPlayerAction(player, player + " is requesting skill Id: " + this._id + " level " + this._level + " without knowing it's previous level!", IllegalActionPunishmentType.NONE);
								return;
							}
						}

						SkillLearn s = SkillTreeData.getInstance().getSkillLearn(this._skillType, player.getOriginalSkill(this._id), this._level, player);
						if (s != null)
						{
							switch (this._skillType)
							{
								case CLASS:
									if (this.checkPlayerSkill(player, trainer, s))
									{
										this.giveSkill(player, trainer, skill);
									}
									break;
								case TRANSFORM:
									if (!canTransform(player))
									{
										player.sendPacket(SystemMessageId.YOU_HAVE_NOT_COMPLETED_THE_NECESSARY_QUEST_FOR_SKILL_ACQUISITION);
										PunishmentManager.handleIllegalPlayerAction(player, player + " is requesting skill Id: " + this._id + " level " + this._level + " without required quests!", IllegalActionPunishmentType.NONE);
										return;
									}

									if (this.checkPlayerSkill(player, trainer, s))
									{
										this.giveSkill(player, trainer, skill);
									}
									break;
								case FISHING:
									if (this.checkPlayerSkill(player, trainer, s))
									{
										this.giveSkill(player, trainer, skill);
									}
									break;
								case PLEDGE:
									if (!player.isClanLeader())
									{
										return;
									}

									Clan clan = player.getClan();
									int repCost = (int) s.getLevelUpSp();
									if (clan.getReputationScore() >= repCost)
									{
										if (PlayerConfig.LIFE_CRYSTAL_NEEDED)
										{
											int count = 0;
											long playerItemCount = 0L;

											for (List<ItemHolder> items : s.getRequiredItems())
											{
												count = 0;

												for (ItemHolder item : items)
												{
													count++;
													playerItemCount = player.getInventory().getInventoryItemCount(item.getId(), -1);
													if (playerItemCount >= item.getCount() && player.destroyItemByItemId(ItemProcessType.FEE, item.getId(), item.getCount(), trainer, true))
													{
														break;
													}

													if (count == items.size())
													{
														player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS_TO_LEARN_THE_SKILL);
														VillageMaster.showPledgeSkillList(player);
														return;
													}
												}
											}
										}

										clan.takeReputationScore(repCost);
										SystemMessage cr = new SystemMessage(SystemMessageId.CLAN_REPUTATION_POINTS_S1_2);
										cr.addInt(repCost);
										player.sendPacket(cr);
										clan.addNewSkill(skill);
										clan.broadcastToOnlineMembers(new PledgeSkillList(clan));
										player.sendPacket(new AcquireSkillDone());
										VillageMaster.showPledgeSkillList(player);
									}
									else
									{
										player.sendPacket(SystemMessageId.THE_ATTEMPT_TO_ACQUIRE_THE_SKILL_HAS_FAILED_BECAUSE_OF_AN_INSUFFICIENT_CLAN_REPUTATION);
										VillageMaster.showPledgeSkillList(player);
									}
									break;
								case SUBPLEDGE:
									if (!player.isClanLeader() || !player.hasAccess(ClanAccess.MEMBER_FAME))
									{
										return;
									}

									Clan subClan = player.getClan();
									if (subClan.getFortId() == 0 && subClan.getCastleId() == 0)
									{
										return;
									}

									if (!subClan.isLearnableSubPledgeSkill(skill, this._subType))
									{
										player.sendPacket(SystemMessageId.THIS_SQUAD_SKILL_HAS_ALREADY_BEEN_LEARNED);
										PunishmentManager.handleIllegalPlayerAction(player, player + " is requesting skill Id: " + this._id + " level " + this._level + " without knowing it's previous level!", IllegalActionPunishmentType.NONE);
										return;
									}

									int subRepCost = (int) s.getLevelUpSp();
									if (subClan.getReputationScore() < subRepCost)
									{
										player.sendPacket(SystemMessageId.THE_ATTEMPT_TO_ACQUIRE_THE_SKILL_HAS_FAILED_BECAUSE_OF_AN_INSUFFICIENT_CLAN_REPUTATION);
										return;
									}

									int count = 0;
									long playerItemCount = 0L;

									for (List<ItemHolder> items : s.getRequiredItems())
									{
										count = 0;

										for (ItemHolder item : items)
										{
											count++;
											playerItemCount = player.getInventory().getInventoryItemCount(item.getId(), -1);
											if (playerItemCount >= item.getCount() && player.destroyItemByItemId(ItemProcessType.FEE, item.getId(), item.getCount(), trainer, true))
											{
												break;
											}

											if (count == items.size())
											{
												player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS_TO_LEARN_THE_SKILL);
												return;
											}
										}
									}

									if (subRepCost > 0)
									{
										subClan.takeReputationScore(subRepCost);
										SystemMessage cr = new SystemMessage(SystemMessageId.CLAN_REPUTATION_POINTS_S1_2);
										cr.addInt(subRepCost);
										player.sendPacket(cr);
									}

									subClan.addNewSkill(skill, this._subType);
									subClan.broadcastToOnlineMembers(new PledgeSkillList(subClan));
									player.sendPacket(new AcquireSkillDone());
									showSubUnitSkillList(player);
									break;
								case TRANSFER:
									if (this.checkPlayerSkill(player, trainer, s))
									{
										this.giveSkill(player, trainer, skill);
									}

									List<SkillLearn> skills = SkillTreeData.getInstance().getAvailableTransferSkills(player);
									if (skills.isEmpty())
									{
										player.sendPacket(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
									}
									else
									{
										player.sendPacket(new ExAcquirableSkillListByClass(skills, AcquireSkillType.TRANSFER));
									}
									break;
								case SUBCLASS:
									if (player.isSubClassActive())
									{
										player.sendPacket(SystemMessageId.THIS_SKILL_CANNOT_BE_LEARNED_WHILE_IN_THE_SUBCLASS_STATE_PLEASE_TRY_AGAIN_AFTER_CHANGING_TO_THE_MAIN_CLASS);
										PunishmentManager.handleIllegalPlayerAction(player, player + " is requesting skill Id: " + this._id + " level " + this._level + " while Sub-Class is active!", IllegalActionPunishmentType.NONE);
										return;
									}

									if (this.checkPlayerSkill(player, trainer, s))
									{
										PlayerVariables vars = player.getVariables();
										String list = vars.getString("SubSkillList", "");
										if (prevSkillLevel > 0 && list.contains(this._id + "-" + prevSkillLevel))
										{
											list = list.replace(this._id + "-" + prevSkillLevel, this._id + "-" + this._level);
										}
										else
										{
											if (!list.isEmpty())
											{
												list = list + ";";
											}

											list = list + this._id + "-" + this._level;
										}

										vars.set("SubSkillList", list);
										this.giveSkill(player, trainer, skill, false);
									}
									break;
								case DUALCLASS:
									if (player.isSubClassActive())
									{
										player.sendPacket(SystemMessageId.THIS_SKILL_CANNOT_BE_LEARNED_WHILE_IN_THE_SUBCLASS_STATE_PLEASE_TRY_AGAIN_AFTER_CHANGING_TO_THE_MAIN_CLASS);
										PunishmentManager.handleIllegalPlayerAction(player, player + " is requesting skill Id: " + this._id + " level " + this._level + " while Sub-Class is active!", IllegalActionPunishmentType.NONE);
										return;
									}

									if (this.checkPlayerSkill(player, trainer, s))
									{
										PlayerVariables vars = player.getVariables();
										String list = vars.getString("DualSkillList", "");
										if (prevSkillLevel > 0 && list.contains(this._id + "-" + prevSkillLevel))
										{
											list = list.replace(this._id + "-" + prevSkillLevel, this._id + "-" + this._level);
										}
										else
										{
											if (!list.isEmpty())
											{
												list = list + ";";
											}

											list = list + this._id + "-" + this._level;
										}

										vars.set("DualSkillList", list);
										this.giveSkill(player, trainer, skill, false);
									}
									break;
								case COLLECT:
									if (this.checkPlayerSkill(player, trainer, s))
									{
										this.giveSkill(player, trainer, skill);
									}
									break;
								case ALCHEMY:
									if (player.getRace() != Race.ERTHEIA)
									{
										return;
									}

									if (this.checkPlayerSkill(player, trainer, s))
									{
										this.giveSkill(player, trainer, skill);
										player.sendPacket(new AcquireSkillDone());
										player.sendPacket(new ExAlchemySkillList(player));
										List<SkillLearn> alchemySkills = SkillTreeData.getInstance().getAvailableAlchemySkills(player);
										if (alchemySkills.isEmpty())
										{
											player.sendPacket(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
										}
										else
										{
											player.sendPacket(new ExAcquirableSkillListByClass(alchemySkills, AcquireSkillType.ALCHEMY));
										}
									}
								case REVELATION:
								case REVELATION_DUALCLASS:
									break;
								default:
									PacketLogger.warning("Recived Wrong Packet Data in Aquired Skill, unknown skill type:" + this._skillType);
							}
						}
					}
				}
			}
			else
			{
				PunishmentManager.handleIllegalPlayerAction(player, "Wrong Packet Data in Aquired Skill", GeneralConfig.DEFAULT_PUNISH);
				PacketLogger.warning("Recived Wrong Packet Data in Aquired Skill - id: " + this._id + " level: " + this._level + " for " + player);
			}
		}
	}

	public static void showSubUnitSkillList(Player player)
	{
		List<SkillLearn> skills = SkillTreeData.getInstance().getAvailableSubPledgeSkills(player.getClan());
		if (skills.isEmpty())
		{
			player.sendPacket(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
		else
		{
			player.sendPacket(new ExAcquirableSkillListByClass(skills, AcquireSkillType.SUBPLEDGE));
		}
	}

	public static void showSubSkillList(Player player)
	{
		List<SkillLearn> skills = SkillTreeData.getInstance().getAvailableSubClassSkills(player);
		if (!skills.isEmpty())
		{
			player.sendPacket(new ExAcquirableSkillListByClass(skills, AcquireSkillType.SUBCLASS));
		}
		else
		{
			player.sendPacket(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
	}

	public static void showDualSkillList(Player player)
	{
		List<SkillLearn> skills = SkillTreeData.getInstance().getAvailableDualClassSkills(player);
		if (!skills.isEmpty())
		{
			player.sendPacket(new ExAcquirableSkillListByClass(skills, AcquireSkillType.DUALCLASS));
		}
		else
		{
			player.sendPacket(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
	}

	private boolean checkPlayerSkill(Player player, Npc trainer, SkillLearn skillLearn)
	{
		if (skillLearn == null || skillLearn.getSkillLevel() != this._level)
		{
			return false;
		}
		else if (skillLearn.getGetLevel() > player.getLevel())
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_SKILL_LEVEL_REQUIREMENTS);
			PunishmentManager.handleIllegalPlayerAction(player, player + ", level " + player.getLevel() + " is requesting skill Id: " + this._id + " level " + this._level + " without having minimum required level, " + skillLearn.getGetLevel() + "!", IllegalActionPunishmentType.NONE);
			return false;
		}
		else
		{
			if (skillLearn.getDualClassLevel() > 0)
			{
				SubClassHolder playerDualClass = player.getDualClass();
				if (playerDualClass == null || playerDualClass.getLevel() < skillLearn.getDualClassLevel())
				{
					return false;
				}
			}

			long levelUpSp = skillLearn.getLevelUpSp();
			if (levelUpSp > 0L && levelUpSp > player.getSp())
			{
				player.sendPacket(new ExAcquireSkillResult(skillLearn.getSkillId(), skillLearn.getSkillLevel(), false, SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_THIS_SKILL));
				this.showSkillList(trainer, player);
				return false;
			}
			else if (!PlayerConfig.DIVINE_SP_BOOK_NEEDED && this._id == CommonSkill.DIVINE_INSPIRATION.getId())
			{
				return true;
			}
			else
			{
				if (!skillLearn.getPreReqSkills().isEmpty())
				{
					for (SkillHolder skill : skillLearn.getPreReqSkills())
					{
						if (player.getSkillLevel(skill.getSkillId()) < skill.getSkillLevel())
						{
							if (skill.getSkillId() == CommonSkill.ONYX_BEAST_TRANSFORMATION.getId())
							{
								player.sendPacket(new ExAcquireSkillResult(skillLearn.getSkillId(), skillLearn.getSkillLevel(), false, SystemMessageId.YOU_MUST_LEARN_THE_ONYX_BEAST_SKILL_BEFORE_YOU_CAN_LEARN_FURTHER_SKILLS));
							}
							else
							{
								player.sendPacket(new ExAcquireSkillResult(skillLearn.getSkillId(), skillLearn.getSkillLevel(), false, SystemMessageId.NOT_ENOUGH_ITEMS_TO_LEARN_THE_SKILL));
							}

							return false;
						}
					}
				}

				if (!skillLearn.getRequiredItems().isEmpty())
				{
					int count = 0;
					long playerItemCount = 0L;

					for (List<ItemHolder> items : skillLearn.getRequiredItems())
					{
						count = 0;

						for (ItemHolder item : items)
						{
							count++;
							playerItemCount = player.getInventory().getInventoryItemCount(item.getId(), -1);
							if (playerItemCount >= item.getCount())
							{
								break;
							}

							if (count == items.size())
							{
								player.sendPacket(new ExAcquireSkillResult(skillLearn.getSkillId(), skillLearn.getSkillLevel(), false, SystemMessageId.NOT_ENOUGH_ITEMS_TO_LEARN_THE_SKILL));
								this.showSkillList(trainer, player);
								return false;
							}
						}
					}

					for (List<ItemHolder> items : skillLearn.getRequiredItems())
					{
						count = 0;

						for (ItemHolder item : items)
						{
							count++;
							playerItemCount = player.getInventory().getInventoryItemCount(item.getId(), -1);
							if (playerItemCount >= item.getCount() && player.destroyItemByItemId(ItemProcessType.FEE, item.getId(), item.getCount(), trainer, true))
							{
								break;
							}

							if (count == items.size())
							{
								PunishmentManager.handleIllegalPlayerAction(player, "Somehow " + player + ", level " + player.getLevel() + " lose required item Id: " + item.getId() + " to learn skill while learning skill Id: " + this._id + " level " + this._level + "!", IllegalActionPunishmentType.NONE);
							}
						}
					}
				}

				if (!skillLearn.getRemoveSkills().isEmpty())
				{
					skillLearn.getRemoveSkills().forEach(skillId -> {
						Skill skillToRemove = player.getKnownSkill(skillId);
						if (skillToRemove != null)
						{
							player.removeSkill(skillToRemove, true);
						}
					});
				}

				if (levelUpSp > 0L)
				{
					player.setSp(player.getSp() - levelUpSp);
					UserInfo ui = new UserInfo(player);
					ui.addComponentType(UserInfoType.CURRENT_HPMPCP_EXP_SP);
					player.sendPacket(ui);
				}

				return true;
			}
		}
	}

	private void giveSkill(Player player, Npc trainer, Skill skill)
	{
		this.giveSkill(player, trainer, skill, true);
	}

	private void giveSkill(Player player, Npc trainer, Skill skill, boolean store)
	{
		player.addSkill(skill, store);
		player.sendItemList();
		player.updateShortcuts(this._id, skill.getLevel(), skill.getSubLevel());
		player.sendPacket(new ShortcutInit(player));
		player.sendPacket(ExBasicActionList.STATIC_PACKET);
		player.sendPacket(new ExAcquireSkillResult(skill.getId(), skill.getLevel(), true, SystemMessageId.YOU_HAVE_LEARNED_THE_SKILL_S1));
		player.sendSkillList(skill.getId());
		if (this._id >= 1368 && this._id <= 1372)
		{
			player.sendStorageMaxCount();
		}

		if (trainer != null)
		{
			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_SKILL_LEARN, trainer))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerSkillLearn(trainer, player, skill, this._skillType), trainer);
			}
		}
		else if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_SKILL_LEARN, player))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerSkillLearn(trainer, player, skill, this._skillType), player);
		}

		player.restoreAutoShortcutVisual();
	}

	private void showSkillList(Npc trainer, Player player)
	{
		if (this._skillType == AcquireSkillType.SUBCLASS)
		{
			showSubSkillList(player);
		}
		else if (this._skillType == AcquireSkillType.DUALCLASS)
		{
			showDualSkillList(player);
		}
		else if (trainer instanceof Fisherman)
		{
			Fisherman.showFishSkillList(player);
		}
	}

	public static boolean canTransform(Player player)
	{
		if (PlayerConfig.ALLOW_TRANSFORM_WITHOUT_QUEST)
		{
			return true;
		}
		QuestState qs = player.getQuestState("Q00136_MoreThanMeetsTheEye");
		return qs != null && qs.isCompleted();
	}
}
