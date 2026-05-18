package net.sf.l2jdev.gameserver.taskmanagers;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.xml.ActionData;
import net.sf.l2jdev.gameserver.data.xml.PetSkillData;
import net.sf.l2jdev.gameserver.handler.IItemHandler;
import net.sf.l2jdev.gameserver.handler.IPlayerActionHandler;
import net.sf.l2jdev.gameserver.handler.ItemHandler;
import net.sf.l2jdev.gameserver.handler.PlayerActionHandler;
import net.sf.l2jdev.gameserver.model.ActionDataHolder;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.instance.Guard;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.actor.transform.Transform;
import net.sf.l2jdev.gameserver.model.actor.transform.TransformTemplate;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.effects.EffectType;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.item.OnItemUse;
import net.sf.l2jdev.gameserver.model.item.EtcItem;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemSkillType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemSkillHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.EffectScope;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.holders.AttachSkillHolder;
import net.sf.l2jdev.gameserver.model.skill.targets.AffectScope;
import net.sf.l2jdev.gameserver.model.skill.targets.TargetType;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class AutoUseTaskManager
{
	private static final Set<Set<Player>> POOLS = ConcurrentHashMap.newKeySet();
 

	protected AutoUseTaskManager()
	{
	}

	public synchronized void startAutoUseTask(Player player)
	{
		for (Set<Player> pool : POOLS)
		{
			if (pool.contains(player))
			{
				return;
			}
		}

		for (Set<Player> poolx : POOLS)
		{
			if (poolx.size() < 200)
			{
				poolx.add(player);
				return;
			}
		}

		Set<Player> poolxx = ConcurrentHashMap.newKeySet(200);
		poolxx.add(player);
		ThreadPool.schedulePriorityTaskAtFixedRate(new AutoUseTaskManager.AutoUse(poolxx), 300L, 300L);
		POOLS.add(poolxx);
	}

	public void stopAutoUseTask(Player player)
	{
		player.getAutoUseSettings().resetSkillOrder();
		if (player.getAutoUseSettings().isEmpty() || !player.isOnline() || player.isInOfflineMode() && !player.isOfflinePlay())
		{
			for (Set<Player> pool : POOLS)
			{
				if (pool.remove(player))
				{
					return;
				}
			}
		}
	}

	public void addAutoSupplyItem(Player player, int itemId)
	{
		player.getAutoUseSettings().getAutoSupplyItems().add(itemId);
		this.startAutoUseTask(player);
	}

	public void removeAutoSupplyItem(Player player, int itemId)
	{
		player.getAutoUseSettings().getAutoSupplyItems().remove(itemId);
		this.stopAutoUseTask(player);
	}

	public void setAutoPotionItem(Player player, int itemId)
	{
		player.getAutoUseSettings().setAutoPotionItem(itemId);
		this.startAutoUseTask(player);
	}

	public void removeAutoPotionItem(Player player)
	{
		player.getAutoUseSettings().setAutoPotionItem(0);
		this.stopAutoUseTask(player);
	}

	public void setAutoPetPotionItem(Player player, int itemId)
	{
		player.getAutoUseSettings().setAutoPetPotionItem(itemId);
		this.startAutoUseTask(player);
	}

	public void removeAutoPetPotionItem(Player player)
	{
		player.getAutoUseSettings().setAutoPetPotionItem(0);
		this.stopAutoUseTask(player);
	}

	public void addAutoBuff(Player player, int skillId)
	{
		player.getAutoUseSettings().getAutoBuffs().add(skillId);
		this.startAutoUseTask(player);
	}

	public void removeAutoBuff(Player player, int skillId)
	{
		player.getAutoUseSettings().getAutoBuffs().remove(skillId);
		this.stopAutoUseTask(player);
	}

	public void addAutoSkill(Player player, Integer skillId)
	{
		player.getAutoUseSettings().getAutoSkills().add(skillId);
		this.startAutoUseTask(player);
	}

	public void removeAutoSkill(Player player, Integer skillId)
	{
		player.getAutoUseSettings().getAutoSkills().remove(skillId);
		this.stopAutoUseTask(player);
	}

	public void addAutoAction(Player player, int actionId)
	{
		player.getAutoUseSettings().getAutoActions().add(actionId);
		this.startAutoUseTask(player);
	}

	public void removeAutoAction(Player player, int actionId)
	{
		player.getAutoUseSettings().getAutoActions().remove(actionId);
		this.stopAutoUseTask(player);
	}

	public static AutoUseTaskManager getInstance()
	{
		return AutoUseTaskManager.SingletonHolder.INSTANCE;
	}

	private class AutoUse implements Runnable
	{
		private final Set<Player> _players;

		public AutoUse(Set<Player> players)
		{
			Objects.requireNonNull(AutoUseTaskManager.this);
			super();
			this._players = players;
		}

		@Override
		public void run()
		{
			if (!this._players.isEmpty())
			{
				for (Player player : this._players)
				{
					if (player.getAutoUseSettings().isEmpty() || !player.isOnline() || player.isInOfflineMode() && !player.isOfflinePlay())
					{
						AutoUseTaskManager.this.stopAutoUseTask(player);
					}
					else if (!player.isSitting() && !player.hasBlockActions() && !player.isControlBlocked() && !player.isAlikeDead() && !player.isMounted())
					{
						Transform transform = player.getTransformation();
						if (transform == null || !transform.isRiding())
						{
							boolean isInPeaceZone = player.isInsideZone(ZoneId.PEACE) || player.isInsideZone(ZoneId.SAYUNE);
							if (GeneralConfig.ENABLE_AUTO_ITEM && !isInPeaceZone)
							{
								Pet pet = player.getPet();

								label309:
								for (Integer itemId : player.getAutoUseSettings().getAutoSupplyItems())
								{
									if (player.isTeleporting())
									{
										break;
									}

									Item item = player.getInventory().getItemByItemId(itemId);
									if (item == null)
									{
										player.getAutoUseSettings().getAutoSupplyItems().remove(itemId);
									}
									else
									{
										ItemTemplate template = item.getTemplate();
										if (template != null && template.checkCondition(player, player, false))
										{
											List<ItemSkillHolder> skills = template.getSkills(ItemSkillType.NORMAL);
											if (skills != null)
											{
												for (ItemSkillHolder itemSkillHolder : skills)
												{
													Skill skill = itemSkillHolder.getSkill();
													if (player.isAffectedBySkill(skill.getId()) || player.hasSkillReuse(skill.getReuseHashCode()) || !skill.checkCondition(player, player, false) || pet != null && !pet.isDead() && (pet.isAffectedBySkill(skill.getId()) || pet.hasSkillReuse(skill.getReuseHashCode()) || !skill.checkCondition(pet, pet, false)))
													{
														continue label309;
													}
												}
											}

											int reuseDelay = item.getReuseDelay();
											if (reuseDelay <= 0 || player.getItemRemainingReuseTime(item.getObjectId()) <= 0L)
											{
												IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
												if (handler != null && handler.onItemUse(player, item, false))
												{
													if (reuseDelay > 0)
													{
														player.addTimeStampItem(item, reuseDelay);
													}

													if (EventDispatcher.getInstance().hasListener(EventType.ON_ITEM_USE, template))
													{
														EventDispatcher.getInstance().notifyEventAsync(new OnItemUse(player, item), template);
													}
												}
											}
										}
									}
								}
							}

							if (GeneralConfig.ENABLE_AUTO_POTION && !isInPeaceZone && player.getCurrentHpPercent() < player.getAutoPlaySettings().getAutoPotionPercent())
							{
								int itemId = player.getAutoUseSettings().getAutoPotionItem();
								if (itemId > 0)
								{
									Item item = player.getInventory().getItemByItemId(itemId);
									if (item == null)
									{
										player.getAutoUseSettings().setAutoPotionItem(0);
									}
									else
									{
										int reuseDelay = item.getReuseDelay();
										if (reuseDelay <= 0 || player.getItemRemainingReuseTime(item.getObjectId()) <= 0L)
										{
											EtcItem etcItem = item.getEtcItem();
											IItemHandler handler = ItemHandler.getInstance().getHandler(etcItem);
											if (handler != null && handler.onItemUse(player, item, false))
											{
												if (reuseDelay > 0)
												{
													player.addTimeStampItem(item, reuseDelay);
												}

												if (EventDispatcher.getInstance().hasListener(EventType.ON_ITEM_USE, item.getTemplate()))
												{
													EventDispatcher.getInstance().notifyEventAsync(new OnItemUse(player, item), item.getTemplate());
												}
											}
										}
									}
								}
							}

							if (GeneralConfig.ENABLE_AUTO_PET_POTION && !isInPeaceZone)
							{
								Pet pet = player.getPet();
								if (pet != null && !pet.isDead())
								{
									int percent = pet.getCurrentHpPercent();
									if (percent < 100 && percent <= player.getAutoPlaySettings().getAutoPetPotionPercent())
									{
										int itemId = player.getAutoUseSettings().getAutoPetPotionItem();
										if (itemId > 0)
										{
											Item item = player.getInventory().getItemByItemId(itemId);
											if (item == null)
											{
												player.getAutoUseSettings().setAutoPetPotionItem(0);
											}
											else
											{
												int reuseDelay = item.getReuseDelay();
												if (reuseDelay <= 0 || player.getItemRemainingReuseTime(item.getObjectId()) <= 0L)
												{
													EtcItem etcItem = item.getEtcItem();
													IItemHandler handler = ItemHandler.getInstance().getHandler(etcItem);
													if (handler != null && handler.onItemUse(player, item, false) && reuseDelay > 0)
													{
														player.addTimeStampItem(item, reuseDelay);
													}
												}
											}
										}
									}
								}
							}

							if (GeneralConfig.ENABLE_AUTO_SKILL)
							{
								for (Integer skillId : player.getAutoUseSettings().getAutoBuffs())
								{
									if (isInPeaceZone || player.isCastingNow() || player.isAttackingNow() || player.isTeleporting())
									{
										break;
									}

									Playable pet = null;
									Skill skill = player.getKnownSkill(skillId);
									if (skill == null)
									{
										if (player.hasServitors())
										{
											for (Summon summon : player.getServitors().values())
											{
												skill = summon.getKnownSkill(skillId);
												if (skill != null)
												{
													pet = summon;
													break;
												}
											}
										}

										if (skill == null && player.hasPet())
										{
											pet = player.getPet();
											skill = pet.getKnownSkill(skillId);
										}

										if (skill == null)
										{
											player.getAutoUseSettings().getAutoBuffs().remove(skillId);
											continue;
										}
									}

									WorldObject target = player.getTarget();
									if (this.canCastBuff(player, target, skill))
									{
										for (AttachSkillHolder holder : skill.getAttachSkills())
										{
											if (player.isAffectedBySkill(holder.getRequiredSkillId()))
											{
												skill = holder.getSkill();
												break;
											}
										}

										Playable caster = pet != null ? pet : player;
										if (target != null && target.isPlayable())
										{
											Player targetPlayer = target.asPlayer();
											if ((targetPlayer.getPvpFlag() != 0 || targetPlayer.getReputation() < 0) && targetPlayer.getParty() != caster.getParty())
											{
												if (!caster.getEffectList().isAffectedBySkill(skill.getId()))
												{
													caster.setTarget(caster);
													caster.doCast(skill);
													caster.setTarget(target);
												}
											}
											else
											{
												caster.doCast(skill);
											}
										}
										else
										{
											caster.setTarget(caster);
											caster.doCast(skill);
											caster.setTarget(target);
										}
									}
								}

								if (player.isAutoPlaying())
								{
									int count = player.getAutoUseSettings().getAutoSkills().size();

									for (int i = 0; i < count && !player.isCastingNow() && !player.isTeleporting(); i++)
									{
										Playable petx = null;
										WorldObject target = player.getTarget();
										Integer skillId = player.getAutoUseSettings().getNextSkillId();
										Skill skillx = player.getKnownSkill(skillId);
										if (skillx == null)
										{
											if (player.hasServitors())
											{
												for (Summon summonx : player.getServitors().values())
												{
													skillx = summonx.getKnownSkill(skillId);
													if (skillx == null)
													{
														skillx = PetSkillData.getInstance().getKnownSkill(summonx, skillId);
													}

													if (skillx != null)
													{
														petx = summonx;
														summonx.setTarget(target);
														break;
													}
												}
											}

											if (skillx == null && player.hasPet())
											{
												petx = player.getPet();
												skillx = petx.getKnownSkill(skillId);
												if (skillx == null)
												{
													skillx = PetSkillData.getInstance().getKnownSkill(petx.asSummon(), skillId);
												}

												if (petx.isSkillDisabled(skillx))
												{
													player.getAutoUseSettings().incrementSkillOrder();
													break;
												}
											}

											if (skillx == null)
											{
												player.getAutoUseSettings().getAutoSkills().remove(skillId);
												player.getAutoUseSettings().resetSkillOrder();
												break;
											}
										}

										if (target == player)
										{
											break;
										}

										if (target == null || target.asCreature().isDead())
										{
											if (player.getQueuedSkill() != null)
											{
												player.setQueuedSkill(null, null, false, false);
											}
											break;
										}

										if (target.isInsideZone(ZoneId.PEACE) || !target.isAutoAttackable(player))
										{
											break;
										}

										if (target instanceof Guard)
										{
											int targetMode = player.getAutoPlaySettings().getNextTargetMode();
											if (targetMode != 3 && targetMode != 0)
											{
												break;
											}
										}

										player.getAutoUseSettings().incrementSkillOrder();
										Playable caster = petx != null ? petx : player;
										if (this.canUseMagic(caster, target, skillx))
										{
											caster.useMagic(skillx, null, true, false);
											break;
										}
									}

									for (Integer actionId : player.getAutoUseSettings().getAutoActions())
									{
										BuffInfo info = player.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.BOT_PENALTY);
										if (info != null)
										{
											for (AbstractEffect effect : info.getEffects())
											{
												if (!effect.checkCondition(actionId))
												{
													player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_ACTIONS_HAVE_BEEN_RESTRICTED);
													break;
												}
											}
										}

										if (transform != null)
										{
											TransformTemplate transformTemplate = transform.getTemplate(player);
											int[] allowedActions = transformTemplate.getBasicActionList();
											if (allowedActions == null || Arrays.binarySearch(allowedActions, actionId.intValue()) < 0)
											{
												continue;
											}
										}

										ActionDataHolder actionHolder = ActionData.getInstance().getActionData(actionId);
										if (actionHolder != null)
										{
											IPlayerActionHandler actionHandler = PlayerActionHandler.getInstance().getHandler(actionHolder.getHandler());
											if (actionHandler != null)
											{
												if (!actionHandler.isPetAction())
												{
													actionHandler.onAction(player, actionHolder, false, false);
												}
												else
												{
													Summon summonx = player.getAnyServitor();
													if (summonx != null && !summonx.isAlikeDead())
													{
														Skill skillxx = summonx.getKnownSkill(actionHolder.getOptionId());
														if (skillxx == null || this.canSummonCastSkill(player, summonx, skillxx))
														{
															actionHandler.onAction(player, actionHolder, false, false);
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		private boolean canCastBuff(Player player, WorldObject target, Skill skill)
		{
			if (skill.getAffectScope() == AffectScope.SUMMON_EXCEPT_MASTER || skill.getTargetType() == TargetType.SUMMON)
			{
				if (!player.hasServitors())
				{
					return false;
				}

				int occurrences = 0;

				for (Summon servitor : player.getServitors().values())
				{
					if (servitor.isAffectedBySkill(skill.getId()))
					{
						occurrences++;
					}
				}

				if (occurrences == player.getServitors().size())
				{
					return false;
				}
			}

			if (target != null && target.isCreature() && target.asCreature().isAlikeDead() && skill.getTargetType() != TargetType.SELF && skill.getTargetType() != TargetType.NPC_BODY && skill.getTargetType() != TargetType.PC_BODY)
			{
				return false;
			}
			Playable playableTarget = target != null && target.isPlayable() && skill.getTargetType() != TargetType.SELF ? target.asPlayable() : player;
			if (player != playableTarget && player.calculateDistance3D(playableTarget) > skill.getCastRange())
			{
				return false;
			}
			else if (!this.canUseMagic(player, playableTarget, skill))
			{
				return false;
			}
			else
			{
				BuffInfo buffInfo = playableTarget.getEffectList().getBuffInfoBySkillId(skill.getId());
				BuffInfo abnormalBuffInfo = playableTarget.getEffectList().getFirstBuffInfoByAbnormalType(skill.getAbnormalType());
				if (abnormalBuffInfo == null)
				{
					return buffInfo == null;
				}
				return buffInfo != null ? abnormalBuffInfo.getSkill().getId() == buffInfo.getSkill().getId() && (buffInfo.getTime() <= 3 || buffInfo.getSkill().getLevel() < skill.getLevel()) : abnormalBuffInfo.getSkill().getAbnormalLevel() < skill.getAbnormalLevel() || abnormalBuffInfo.isAbnormalType(AbnormalType.NONE);
			}
		}

		private boolean canUseMagic(Playable playable, WorldObject target, Skill skill)
		{
			if (skill.getItemConsumeCount() > 0 && playable.getInventory().getInventoryItemCount(skill.getItemConsumeId(), -1) < skill.getItemConsumeCount())
			{
				return false;
			}
			else if (playable.isPlayer() && playable.asPlayer().getCharges() < skill.getChargeConsumeCount())
			{
				return false;
			}
			else
			{
				int mpConsume = skill.getMpInitialConsume() + skill.getMpConsume();
				if (mpConsume > 0 && playable.getCurrentMp() < mpConsume)
				{
					return false;
				}
				else if (skill.getId() == 254 && target != null && target.isMonster() && target.asMonster().isSpoiled())
				{
					return false;
				}
				else
				{
					for (AttachSkillHolder holder : skill.getAttachSkills())
					{
						if (playable.isAffectedBySkill(holder.getRequiredSkillId()) && (playable.hasSkillReuse(holder.getSkill().getReuseHashCode()) || playable.isAffectedBySkill(holder)))
						{
							return false;
						}
					}

					return !playable.isSkillDisabled(skill) && skill.checkCondition(playable, target, false);
				}
			}
		}

		private boolean canSummonCastSkill(Player player, Summon summon, Skill skill)
		{
			if (skill.hasNegativeEffect() && player.getTarget() == null)
			{
				return false;
			}
			int mpConsume = skill.getMpConsume() + skill.getMpInitialConsume();
			if ((mpConsume == 0 || mpConsume <= (int) Math.floor(summon.getCurrentMp())) && (skill.getHpConsume() == 0 || skill.getHpConsume() <= (int) Math.floor(summon.getCurrentHp())))
			{
				if (summon.isSkillDisabled(skill))
				{
					return false;
				}
				else if ((player.getTarget() == null || skill.checkCondition(summon, player.getTarget(), false)) && (player.getTarget() != null || skill.checkCondition(summon, player, false)))
				{
					if (skill.getItemConsumeCount() > 0 && summon.getInventory().getInventoryItemCount(skill.getItemConsumeId(), -1) < skill.getItemConsumeCount())
					{
						return false;
					}
					else if (skill.getTargetType().equals(TargetType.SELF) || skill.getTargetType().equals(TargetType.SUMMON))
					{
						BuffInfo summonInfo = summon.getEffectList().getBuffInfoBySkillId(skill.getId());
						return summonInfo != null && summonInfo.getTime() >= 3;
					}
					else if (skill.getEffects(EffectScope.GENERAL) != null && skill.getEffects(EffectScope.GENERAL).stream().anyMatch(a -> a.getEffectType().equals(EffectType.MANAHEAL_BY_LEVEL)) && player.getCurrentMpPercent() > 80)
					{
						return false;
					}
					else
					{
						BuffInfo buffInfo = player.getEffectList().getBuffInfoBySkillId(skill.getId());
						BuffInfo abnormalBuffInfo = player.getEffectList().getFirstBuffInfoByAbnormalType(skill.getAbnormalType());
						if (abnormalBuffInfo == null)
						{
							return true;
						}
						return buffInfo != null ? abnormalBuffInfo.getSkill().getId() == buffInfo.getSkill().getId() && (buffInfo.getTime() <= 3 || buffInfo.getSkill().getLevel() < skill.getLevel()) : abnormalBuffInfo.getSkill().getAbnormalLevel() < skill.getAbnormalLevel() || abnormalBuffInfo.isAbnormalType(AbnormalType.NONE);
					}
				}
				else
				{
					return false;
				}
			}
			return false;
		}
	}

	private static class SingletonHolder
	{
		protected static final AutoUseTaskManager INSTANCE = new AutoUseTaskManager();
	}
}
