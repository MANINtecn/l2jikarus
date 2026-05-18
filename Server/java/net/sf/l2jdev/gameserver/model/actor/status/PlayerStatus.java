package net.sf.l2jdev.gameserver.model.actor.status;

import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.config.custom.MultilingualSupportConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflineTradeConfig;
import net.sf.l2jdev.gameserver.data.xml.NpcNameLocalisationData;
import net.sf.l2jdev.gameserver.managers.DuelManager;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PrivateStoreType;
import net.sf.l2jdev.gameserver.model.actor.instance.Guardian;
import net.sf.l2jdev.gameserver.model.actor.stat.PlayerStat;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerCheatDeath;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;
import net.sf.l2jdev.gameserver.model.stats.Formulas;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class PlayerStatus extends PlayableStatus
{
	private double _currentCp = 0.0;

	public PlayerStatus(Player player)
	{
		super(player);
	}

	@Override
	public void reduceCp(int value)
	{
		if (this._currentCp > value)
		{
			this.setCurrentCp(this._currentCp - value);
		}
		else
		{
			this.setCurrentCp(0.0);
		}
	}

	@Override
	public void reduceHp(double value, Creature attacker)
	{
		this.reduceHp(value, attacker, null, true, false, false, false);
	}

	@Override
	public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption)
	{
		this.reduceHp(value, attacker, null, awake, isDOT, isHPConsumption, false);
	}

	public void reduceHp(double value, Creature attacker, Skill skill, boolean awake, boolean isDOT, boolean isHPConsumption, boolean ignoreCP)
	{
		Player player = this.getActiveChar();
		if (!player.isDead())
		{
			if (!OfflineTradeConfig.OFFLINE_MODE_NO_DAMAGE || player.getClient() == null || !player.getClient().isDetached() || (!OfflineTradeConfig.OFFLINE_TRADE_ENABLE || player.getPrivateStoreType() != PrivateStoreType.SELL && player.getPrivateStoreType() != PrivateStoreType.BUY) && (!OfflineTradeConfig.OFFLINE_CRAFT_ENABLE || !player.isCrafting() && player.getPrivateStoreType() != PrivateStoreType.MANUFACTURE))
			{
				if (!player.isHpBlocked() || isDOT || isHPConsumption)
				{
					if (!player.isAffected(EffectFlag.DUELIST_FURY) || attacker.isAffected(EffectFlag.FACEOFF))
					{
						if (player.hasAbnormalType(AbnormalType.BLAZING_BEAST) && player.getBeastPoints() == 0)
						{
							player.getEffectList().stopEffects(AbnormalType.BLAZING_BEAST);
							player.getEffectList().stopEffects(AbnormalType.PAAGRIO_GRACE);
						}

						if (!isHPConsumption)
						{
							if (awake)
							{
								player.stopEffectsOnDamage();
							}

							if (player.isCrafting() || player.isInStoreMode())
							{
								player.setPrivateStoreType(PrivateStoreType.NONE);
								player.standUp();
								player.broadcastUserInfo();
							}
							else if (player.isSitting())
							{
								player.standUp();
							}

							if (!isDOT)
							{
								if (Formulas.calcStunBreak(player))
								{
									player.stopStunning(true);
								}

								if (Formulas.calcRealTargetBreak())
								{
									player.getEffectList().stopEffects(AbnormalType.REAL_TARGET);
								}
							}
						}

						double amount = value;
						int fullValue = (int) value;
						int tDmg = 0;
						int mpDam = 0;
						if (attacker != null && attacker != player)
						{
							Player attackerPlayer = attacker.asPlayer();
							if (attackerPlayer != null)
							{
								if (attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage())
								{
									return;
								}

								if (player.isInDuel())
								{
									if ((player.getDuelState() == 2) || (player.getDuelState() == 3))
									{
										return;
									}

									if (attackerPlayer.getDuelId() != player.getDuelId())
									{
										player.setDuelState(4);
									}
								}
							}

							PlayerStat stat = player.getStat();
							Summon summon = player.getFirstServitor();
							if (summon != null && LocationUtil.checkIfInRange(1000, player, summon, true))
							{
								tDmg = (int) value * (int) stat.getValue(Stat.TRANSFER_DAMAGE_SUMMON_PERCENT, 0.0) / 100;
								tDmg = Math.min((int) summon.getCurrentHp() - 1, tDmg);
								if (tDmg > 0)
								{
									summon.reduceCurrentHp(tDmg, attacker, null);
									amount = value - tDmg;
									fullValue = (int) amount;
								}
							}

							mpDam = (int) amount * (int) stat.getValue(Stat.MANA_SHIELD_PERCENT, 0.0) / 100;
							if (mpDam > 0)
							{
								mpDam = (int) (amount - mpDam);
								if (!(mpDam > player.getCurrentMp()))
								{
									player.reduceCurrentMp(mpDam);
									SystemMessage smsg = new SystemMessage(SystemMessageId.ARCANE_SHIELD_S1_DECREASED_YOUR_MP_INSTEAD_OF_HP);
									smsg.addInt(mpDam);
									player.sendPacket(smsg);
									return;
								}

								player.sendPacket(SystemMessageId.MP_BECAME_0_AND_THE_ARCANE_SHIELD_IS_DISAPPEARING);
								player.stopSkillEffects(SkillFinishType.REMOVED, 1556);
								amount = mpDam - player.getCurrentMp();
								player.setCurrentMp(0.0);
							}

							Player caster = player.getTransferingDamageTo();
							if (caster != null && player.getParty() != null && LocationUtil.checkIfInRange(1000, player, caster, true) && !caster.isDead() && player != caster && player.getParty().getMembers().contains(caster))
							{
								int transferDmg = 0;
								transferDmg = (int) amount * (int) stat.getValue(Stat.TRANSFER_DAMAGE_TO_PLAYER, 0.0) / 100;
								transferDmg = Math.min((int) caster.getCurrentHp() - 1, transferDmg);
								if (transferDmg > 0)
								{
									int membersInRange = 0;

									for (Player member : caster.getParty().getMembers())
									{
										if (LocationUtil.checkIfInRange(1000, member, caster, false) && member != caster)
										{
											membersInRange++;
										}
									}

									if ((attacker.isPlayable() || attacker.isFakePlayer()) && caster.getCurrentCp() > 0.0)
									{
										if (caster.getCurrentCp() > transferDmg)
										{
											caster.getStatus().reduceCp(transferDmg);
										}
										else
										{
											transferDmg = (int) (transferDmg - caster.getCurrentCp());
											caster.getStatus().reduceCp((int) caster.getCurrentCp());
										}
									}

									if (membersInRange > 0)
									{
										caster.reduceCurrentHp(transferDmg / membersInRange, attacker, null);
										amount -= transferDmg;
										fullValue = (int) amount;
									}
								}
							}

							if (!ignoreCP && (attacker.isPlayable() || attacker.isFakePlayer()) || attacker instanceof Guardian)
							{
								if (this._currentCp >= amount)
								{
									this.setCurrentCp(this._currentCp - amount);
									amount = 0.0;
								}
								else
								{
									amount -= this._currentCp;
									this.setCurrentCp(0.0, false);
								}
							}

							if (fullValue > 0 && !isDOT)
							{
								SystemMessage smsg = new SystemMessage(SystemMessageId.C1_HAS_RECEIVED_S3_DAMAGE_FROM_C2);
								smsg.addString(player.getName());
								String targetName = attacker.getName();
								if (MultilingualSupportConfig.MULTILANG_ENABLE && attacker.isNpc())
								{
									String[] localisation = NpcNameLocalisationData.getInstance().getLocalisation(player.getLang(), attacker.getId());
									if (localisation != null)
									{
										targetName = localisation[0];
									}
								}

								smsg.addString(targetName);
								smsg.addInt(fullValue);
								smsg.addPopup(player.getObjectId(), attacker.getObjectId(), -fullValue);
								player.sendPacket(smsg);
								if (tDmg > 0 && summon != null && attackerPlayer != null)
								{
									smsg = new SystemMessage(SystemMessageId.YOU_VE_DEALT_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_THEIR_SERVITOR);
									smsg.addInt(fullValue);
									smsg.addInt(tDmg);
									attackerPlayer.sendPacket(smsg);
								}
							}
						}

						if (amount > 0.0)
						{
							double newHp = Math.max(this.getCurrentHp() - amount, player.isUndying() ? 1.0 : 0.0);
							if (newHp <= 0.0)
							{
								if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CHEAT_DEATH, player) && !player.hasAbnormalType(AbnormalType.BLOCK_RESURRECTION) && !player.hasAbnormalType(AbnormalType.RESIST_CHEAT_DEATH))
								{
									EventDispatcher.getInstance().notifyEventAsync(new OnPlayerCheatDeath(player), player);
									return;
								}

								if (player.isInDuel())
								{
									player.disableAllSkills();
									this.stopHpMpRegeneration();
									if (attacker != null)
									{
										attacker.getAI().setIntention(Intention.ACTIVE);
										attacker.sendPacket(ActionFailed.STATIC_PACKET);
									}

									DuelManager.getInstance().onPlayerDefeat(player);
									newHp = 1.0;
								}
								else
								{
									newHp = 0.0;
								}
							}

							this.setCurrentHp(newHp);
						}

						if (player.getCurrentHp() < 0.5 && !isHPConsumption && !player.isUndying())
						{
							player.abortAttack();
							player.abortCast();
							if (player.isInOlympiadMode())
							{
								this.stopHpMpRegeneration();
								player.setDead(true);
								player.setIsPendingRevive(true);
								Summon pet = player.getPet();
								if (pet != null)
								{
									pet.getAI().setIntention(Intention.IDLE);
								}

								player.getServitors().values().forEach(s -> s.getAI().setIntention(Intention.IDLE));
								return;
							}

							player.doDie(attacker);
						}
					}
				}
			}
		}
	}

	@Override
	public double getCurrentCp()
	{
		return this._currentCp;
	}

	@Override
	public void setCurrentCp(double newCp)
	{
		this.setCurrentCp(newCp, true);
	}

	@Override
	public void setCurrentCp(double value, boolean broadcastPacket)
	{
		int currentCp = (int) this._currentCp;
		Player player = this.getActiveChar();
		int maxCp = player.getStat().getMaxCp();
		synchronized (this)
		{
			if (player.isDead())
			{
				return;
			}

			double newCp = Math.max(0.0, value);
			if (newCp >= maxCp)
			{
				this._currentCp = maxCp;
				this._flagsRegenActive &= -5;
				if (this._flagsRegenActive == 0)
				{
					this.stopHpMpRegeneration();
				}
			}
			else
			{
				this._currentCp = newCp;
				this._flagsRegenActive = (byte) (this._flagsRegenActive | 4);
				this.startHpMpRegeneration();
			}
		}

		if (currentCp != this._currentCp && broadcastPacket)
		{
			player.broadcastStatusUpdate();
		}
	}

	@Override
	protected void doRegeneration()
	{
		Player player = this.getActiveChar();
		PlayerStat stat = player.getStat();
		if (this._currentCp < stat.getMaxRecoverableCp())
		{
			this.setCurrentCp(this._currentCp + stat.getValue(Stat.REGENERATE_CP_RATE), false);
		}

		if (this.getCurrentHp() < stat.getMaxRecoverableHp())
		{
			this.setCurrentHp(this.getCurrentHp() + stat.getValue(Stat.REGENERATE_HP_RATE), false);
		}

		if (this.getCurrentMp() < stat.getMaxRecoverableMp())
		{
			this.setCurrentMp(this.getCurrentMp() + stat.getValue(Stat.REGENERATE_MP_RATE), false);
		}

		player.broadcastStatusUpdate();
	}

	@Override
	public Player getActiveChar()
	{
		return super.getActiveChar().asPlayer();
	}
}
