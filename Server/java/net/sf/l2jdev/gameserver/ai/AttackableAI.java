package net.sf.l2jdev.gameserver.ai;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.NpcConfig;
import net.sf.l2jdev.gameserver.config.custom.ChampionMonstersConfig;
import net.sf.l2jdev.gameserver.config.custom.FakePlayersConfig;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.managers.ItemsOnGroundManager;
import net.sf.l2jdev.gameserver.model.AggroInfo;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.WorldRegion;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.npc.AISkillScope;
import net.sf.l2jdev.gameserver.model.actor.enums.npc.AIType;
import net.sf.l2jdev.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2jdev.gameserver.model.actor.instance.Guard;
import net.sf.l2jdev.gameserver.model.actor.instance.Monster;
import net.sf.l2jdev.gameserver.model.actor.instance.RaidBoss;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.effects.EffectType;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableFactionCall;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableHate;
import net.sf.l2jdev.gameserver.model.events.returns.TerminateReturn;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillCaster;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.taskmanagers.AttackableThinkTaskManager;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class AttackableAI extends CreatureAI
{
	public static final int RANDOM_WALK_RATE = 30;
	public static final int MAX_ATTACK_TIMEOUT = 1200;
	private int _attackTimeout;
	private int _globalAggro;
	private boolean _thinking;
	private int _chaosTime = 0;

	public AttackableAI(Attackable attackable)
	{
		super(attackable);
		this._attackTimeout = Integer.MAX_VALUE;
		this._globalAggro = -10;
	}

	private boolean isAggressiveTowards(Creature target)
	{
		if (target == null || this.getActiveChar() == null)
		{
			return false;
		}
		else if (target.isInvul())
		{
			return false;
		}
		else if (target.isDoor())
		{
			return false;
		}
		else if (target.isAlikeDead())
		{
			return false;
		}
		else
		{
			Attackable me = this.getActiveChar();
			if (target.isPlayable() && !me.isRaid() && !me.canSeeThroughSilentMove() && target.asPlayable().isSilentMovingAffected())
			{
				return false;
			}
			Player player = target.asPlayer();
			if (player != null)
			{
				if (!player.getAccessLevel().canTakeAggro() || player.isRecentFakeDeath())
				{
					return false;
				}

				if (me instanceof Guard)
				{
					World.getInstance().forEachVisibleObjectInRange(me, Guard.class, 500, guard -> {
						if (guard.isAttackingNow() && guard.getTarget() == player)
						{
							me.getAI().startFollow(player);
							me.addDamageHate(player, 0L, 10L);
						}
					});
					if (player.getReputation() < 0)
					{
						return true;
					}
				}
			}
			else if (me.isMonster())
			{
				if ((!NpcConfig.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsideZone(ZoneId.PEACE) && target.isInsideZone(ZoneId.NO_PVP)) || !me.isAggressive())
				{
					return false;
				}
			}

			return me.isChampion() && ChampionMonstersConfig.CHAMPION_PASSIVE ? false : target.isAutoAttackable(me) && GeoEngine.getInstance().canSeeTarget(me, target);
		}
	}

	public void startAITask()
	{
		AttackableThinkTaskManager.getInstance().add(this.getActiveChar());
	}

	@Override
	public void stopAITask()
	{
		AttackableThinkTaskManager.getInstance().remove(this.getActiveChar());
		super.stopAITask();
	}

	@Override
	synchronized void changeIntention(Intention newIntention, Object... args)
	{
		Intention intention = newIntention;
		if (newIntention == Intention.IDLE || newIntention == Intention.ACTIVE)
		{
			Attackable npc = this.getActiveChar();
			if (!npc.isAlikeDead())
			{
				if (!World.getInstance().getVisibleObjects(npc, Player.class).isEmpty())
				{
					intention = Intention.ACTIVE;
				}
				else if (npc.getSpawn() != null && !npc.isInsideRadius3D(npc.getSpawn(), NpcConfig.MAX_DRIFT_RANGE + NpcConfig.MAX_DRIFT_RANGE))
				{
					intention = Intention.ACTIVE;
				}
			}

			if (intention == Intention.IDLE)
			{
				super.changeIntention(Intention.IDLE);
				this.stopAITask();
				this._actor.detachAI();
				return;
			}
		}

		super.changeIntention(intention, args);
		this.startAITask();
	}

	@Override
	protected void changeIntentionToCast(Skill skill, WorldObject target, Item item, boolean forceUse, boolean dontMove)
	{
		this.setTarget(target);
		super.changeIntentionToCast(skill, target, item, forceUse, dontMove);
	}

	@Override
	protected void onIntentionAttack(Creature target)
	{
		this._attackTimeout = 1200 + GameTimeTaskManager.getInstance().getGameTicks();
		super.onIntentionAttack(target);
	}

	protected void thinkCast()
	{
		WorldObject target = this._skill.getTarget(this._actor, this.getTarget(), this._forceUse, this._dontMove, false);
		if (this.checkTargetLost(target))
		{
			this.setCastTarget(null);
		}
		else if (!this.maybeMoveToPawn(target, this._actor.getMagicalAttackRange(this._skill)))
		{
			this.setIntention(Intention.ACTIVE);
			this._actor.doCast(this._skill, this._item, this._forceUse, this._dontMove);
		}
	}

	protected void thinkActive()
	{
		WorldRegion region = this._actor.getWorldRegion();
		if (region != null && region.areNeighborsActive())
		{
			Attackable npc = this.getActiveChar();
			WorldObject target = this.getTarget();
			if (this._globalAggro != 0)
			{
				if (this._globalAggro < 0)
				{
					this._globalAggro++;
				}
				else
				{
					this._globalAggro--;
				}
			}

			if (this._globalAggro >= 0)
			{
				if (npc.isFakePlayer() && npc.isAggressive())
				{
					List<Item> droppedItems = npc.getFakePlayerDrops();
					if (droppedItems.isEmpty())
					{
						Creature nearestTarget = null;
						double closestDistance = Double.MAX_VALUE;

						for (Creature t : World.getInstance().getVisibleObjectsInRange(npc, Creature.class, npc.getAggroRange()))
						{
							if (t != this._actor && t != null && !t.isDead() && (FakePlayersConfig.FAKE_PLAYER_AGGRO_FPC && t.isFakePlayer() || FakePlayersConfig.FAKE_PLAYER_AGGRO_MONSTERS && t.isMonster() && !t.isFakePlayer() || FakePlayersConfig.FAKE_PLAYER_AGGRO_PLAYERS && t.isPlayer()))
							{
								long hating = npc.getHating(t);
								double distance = npc.calculateDistance2D(t);
								if (hating == 0L && closestDistance > distance)
								{
									nearestTarget = t;
									closestDistance = distance;
								}
							}
						}

						if (nearestTarget != null)
						{
							npc.addDamageHate(nearestTarget, 0L, 1L);
						}
					}
					else if (!npc.isInCombat())
					{
						int itemIndex = npc.getFakePlayerDrops().size() - 1;
						Item droppedItem = npc.getFakePlayerDrops().get(itemIndex);
						if (droppedItem != null && droppedItem.isSpawned())
						{
							if (npc.calculateDistance2D(droppedItem) > 50.0)
							{
								this.moveTo(droppedItem);
							}
							else
							{
								npc.getFakePlayerDrops().remove(itemIndex);
								droppedItem.pickupMe(npc);
								if (GeneralConfig.SAVE_DROPPED_ITEM)
								{
									ItemsOnGroundManager.getInstance().removeObject(droppedItem);
								}

								if (droppedItem.getTemplate().hasExImmediateEffect())
								{
									for (SkillHolder skillHolder : droppedItem.getTemplate().getAllSkills())
									{
										SkillCaster.triggerCast(npc, null, skillHolder.getSkill(), null, false);
									}

									npc.broadcastInfo();
								}
							}
						}
						else
						{
							npc.getFakePlayerDrops().remove(itemIndex);
						}

						npc.setRunning();
					}
				}
				else if (npc.isAggressive() || npc instanceof Guard)
				{
					int range = npc instanceof Guard ? 500 : npc.getAggroRange();
					World.getInstance().forEachVisibleObjectInRange(npc, Creature.class, range, tx -> {
						if (this.isAggressiveTowards(tx))
						{
							if (tx.isFakePlayer())
							{
								if (!npc.isFakePlayer() || npc.isFakePlayer() && FakePlayersConfig.FAKE_PLAYER_AGGRO_FPC)
								{
									long hatingxx = npc.getHating(tx);
									if (hatingxx == 0L)
									{
										npc.addDamageHate(tx, 0L, 0L);
									}
								}
							}
							else if (tx.isPlayable() || tx.isMonster() && NpcConfig.GUARD_ATTACK_AGGRO_MOB && tx.isAutoAttackable(npc))
							{
								if (EventDispatcher.getInstance().hasListener(EventType.ON_NPC_HATE, this.getActiveChar()))
								{
									TerminateReturn term = EventDispatcher.getInstance().notifyEvent(new OnAttackableHate(this.getActiveChar(), tx.asPlayer(), tx.isSummon()), this.getActiveChar(), TerminateReturn.class);
									if (term != null && term.terminate())
									{
										return;
									}
								}

								long hatingx = npc.getHating(tx);
								if (hatingx == 0L)
								{
									npc.addDamageHate(tx, 0L, 0L);
								}

								if (npc instanceof Guard)
								{
									World.getInstance().forEachVisibleObjectInRange(npc, Guard.class, 500, guard -> guard.addDamageHate(tx, 0L, 10L));
								}
							}
						}
					});
				}

				Creature hated;
				if (npc.isConfused() && target != null && target.isCreature())
				{
					hated = target.asCreature();
				}
				else
				{
					hated = npc.getMostHated();
				}

				if (hated != null && !npc.isCoreAIDisabled())
				{
					long aggro = npc.getHating(hated);
					if (aggro + this._globalAggro > 0L)
					{
						if (!npc.isRunning())
						{
							npc.setRunning();
						}

						this.setIntention(Intention.ATTACK, hated);
					}

					return;
				}
			}

			if (npc.getCurrentHp() == npc.getMaxHp() && npc.getCurrentMp() == npc.getMaxMp() && !npc.getAttackByList().isEmpty() && Rnd.get(500) == 0)
			{
				npc.clearAggroList();
				npc.getAttackByList().clear();
			}

			if (npc.canReturnToSpawnPoint())
			{
				if (npc.isWalker() || npc.getSpawn() == null || !(npc.calculateDistance2D(npc.getSpawn()) > NpcConfig.MAX_DRIFT_RANGE) || this.getTarget() != null && !this.getTarget().isInvisible() && (!this.getTarget().isPlayer() || NpcConfig.ATTACKABLES_CAMP_PLAYER_CORPSES || !this.getTarget().asPlayer().isAlikeDead()))
				{
					if (this.getTarget() == null || !this.getTarget().isPlayer() || !this.getTarget().asPlayer().isAlikeDead())
					{
						Creature leader = npc.getLeader();
						if (leader != null && !leader.isAlikeDead())
						{
							int offset;
							if (npc.isRaidMinion())
							{
								offset = 500;
							}
							else
							{
								offset = 200;
							}

							if (leader.isRunning())
							{
								npc.setRunning();
							}
							else
							{
								npc.setWalking();
							}

							if (npc.calculateDistance2D(leader) > offset)
							{
								int x1 = Rnd.get(60, offset * 2);
								int y1 = Rnd.get(x1, offset * 2);
								y1 = (int) Math.sqrt(y1 * y1 - x1 * x1);
								if (x1 > offset + 30)
								{
									x1 = leader.getX() + x1 - offset;
								}
								else
								{
									x1 = leader.getX() - x1 + 30;
								}

								if (y1 > offset + 30)
								{
									y1 = leader.getY() + y1 - offset;
								}
								else
								{
									y1 = leader.getY() - y1 + 30;
								}

								this.moveTo(x1, y1, leader.getZ());
							}
							else if (Rnd.get(30) == 0)
							{
								for (Skill sk : npc.getTemplate().getAISkills(AISkillScope.BUFF))
								{
									WorldObject var15 = this.skillTargetReconsider(sk, true);
									if (var15 != null)
									{
										this.setTarget(var15);
										npc.doCast(sk);
									}
								}
							}
						}
						else if (npc.getSpawn() != null && Rnd.get(30) == 0 && npc.isRandomWalkingEnabled())
						{
							for (Skill skx : npc.getTemplate().getAISkills(AISkillScope.BUFF))
							{
								WorldObject var14 = this.skillTargetReconsider(skx, true);
								if (var14 != null)
								{
									this.setTarget(var14);
									npc.doCast(skx);
									return;
								}
							}

							int x1x = npc.getSpawn().getX();
							int y1x = npc.getSpawn().getY();
							int z1 = npc.getSpawn().getZ();
							if (npc.isInsideRadius2D(x1x, y1x, 0, NpcConfig.MAX_DRIFT_RANGE))
							{
								int deltaX = Rnd.get(NpcConfig.MAX_DRIFT_RANGE * 2);
								int deltaY = Rnd.get(deltaX, NpcConfig.MAX_DRIFT_RANGE * 2);
								deltaY = (int) Math.sqrt(deltaY * deltaY - deltaX * deltaX);
								x1x = deltaX + x1x - NpcConfig.MAX_DRIFT_RANGE;
								y1x = deltaY + y1x - NpcConfig.MAX_DRIFT_RANGE;
								z1 = npc.getZ();
							}

							Location moveLoc = this._actor.isFlying() ? new Location(x1x, y1x, z1) : GeoEngine.getInstance().getValidLocation(npc.getX(), npc.getY(), npc.getZ(), x1x, y1x, z1, npc.getInstanceWorld());
							if (LocationUtil.calculateDistance(npc.getSpawn(), moveLoc, false, false) <= NpcConfig.MAX_DRIFT_RANGE)
							{
								this.moveTo(moveLoc.getX(), moveLoc.getY(), moveLoc.getZ());
							}
						}
					}
				}
				else
				{
					npc.setWalking();
					npc.returnHome();
				}
			}
		}
	}

	protected void thinkAttack()
	{
		Attackable npc = this.getActiveChar();
		if (npc != null && !npc.isCastingNow())
		{
			if (NpcConfig.AGGRO_DISTANCE_CHECK_ENABLED && npc.isMonster() && !npc.isWalker() && !(npc instanceof GrandBoss))
			{
				Spawn spawn = npc.getSpawn();
				if (spawn != null && npc.calculateDistance2D(spawn.getLocation()) > (spawn.getChaseRange() > 0 ? Math.max(NpcConfig.MAX_DRIFT_RANGE, spawn.getChaseRange()) : (npc.isRaid() ? NpcConfig.AGGRO_DISTANCE_CHECK_RAID_RANGE : NpcConfig.AGGRO_DISTANCE_CHECK_RANGE)) && (NpcConfig.AGGRO_DISTANCE_CHECK_RAIDS || !npc.isRaid()) && (NpcConfig.AGGRO_DISTANCE_CHECK_INSTANCES || !npc.isInInstance()))
				{
					if (NpcConfig.AGGRO_DISTANCE_CHECK_RESTORE_LIFE)
					{
						npc.setCurrentHp(npc.getMaxHp());
						npc.setCurrentMp(npc.getMaxMp());
					}

					npc.abortAttack();
					npc.clearAggroList();
					npc.getAttackByList().clear();
					if (npc.hasAI())
					{
						npc.getAI().setIntention(Intention.MOVE_TO, spawn.getLocation());
					}
					else
					{
						npc.teleToLocation(spawn.getLocation(), true);
					}

					if (this._actor.asMonster().hasMinions())
					{
						for (Monster minion : this._actor.asMonster().getMinionList().getSpawnedMinions())
						{
							if (NpcConfig.AGGRO_DISTANCE_CHECK_RESTORE_LIFE)
							{
								minion.setCurrentHp(minion.getMaxHp());
								minion.setCurrentMp(minion.getMaxMp());
							}

							minion.abortAttack();
							minion.clearAggroList();
							minion.getAttackByList().clear();
							if (minion.hasAI())
							{
								minion.getAI().setIntention(Intention.MOVE_TO, spawn.getLocation());
							}
							else
							{
								minion.teleToLocation(spawn.getLocation(), true);
							}
						}
					}

					return;
				}
			}

			if (!npc.isCoreAIDisabled())
			{
				Creature target = npc.getMostHated();
				if (target == null)
				{
					this.setIntention(Intention.ACTIVE);
				}
				else
				{
					if (this.getTarget() != target)
					{
						this.setTarget(target);
					}

					if (target.isAlikeDead())
					{
						npc.stopHating(target);
					}
					else if (this._attackTimeout < GameTimeTaskManager.getInstance().getGameTicks())
					{
						this.setIntention(Intention.ACTIVE);
						if (!this._actor.isFakePlayer())
						{
							npc.setWalking();
						}

						if (npc.isMonster() && npc.getSpawn() != null && !npc.isInInstance() && (npc.isInCombat() || World.getInstance().getVisibleObjects(npc, Player.class).isEmpty()))
						{
							npc.teleToLocation(npc.getSpawn(), false);
						}
					}
					else if (!GeoEngine.getInstance().canSeeTarget(this._actor, target))
					{
						if (this._actor.calculateDistance3D(target) < 6000.0)
						{
							this.moveTo(target);
						}
					}
					else
					{
						NpcTemplate template = npc.getTemplate();
						int collision = template.getCollisionRadius();
						List<Skill> aiSuicideSkills = template.getAISkills(AISkillScope.SUICIDE);
						if (!aiSuicideSkills.isEmpty() && (int) (npc.getCurrentHp() / npc.getMaxHp() * 100.0) < 30 && npc.hasSkillChance())
						{
							Skill skill = aiSuicideSkills.get(Rnd.get(aiSuicideSkills.size()));
							if (SkillCaster.checkUseConditions(npc, skill) && this.checkSkillTarget(skill, target))
							{
								npc.doCast(skill);
								return;
							}
						}

						int combinedCollision = collision + target.getTemplate().getCollisionRadius();
						if (!npc.isMovementDisabled() && Rnd.get(100) <= 3)
						{
							for (Attackable nearby : World.getInstance().getVisibleObjects(npc, Attackable.class))
							{
								if (npc.isInsideRadius2D(nearby, collision) && nearby != target)
								{
									int newX = combinedCollision + Rnd.get(40);
									if (Rnd.nextBoolean())
									{
										newX += target.getX();
									}
									else
									{
										newX = target.getX() - newX;
									}

									int newY = combinedCollision + Rnd.get(40);
									if (Rnd.nextBoolean())
									{
										newY += target.getY();
									}
									else
									{
										newY = target.getY() - newY;
									}

									if (!npc.isInsideRadius2D(newX, newY, 0, collision))
									{
										int newZ = npc.getZ() + 30;
										this.moveTo(GeoEngine.getInstance().getValidLocation(npc.getX(), npc.getY(), npc.getZ(), newX, newY, newZ, npc.getInstanceWorld()));
									}

									return;
								}
							}
						}

						if (!npc.isMovementDisabled() && npc.getAiType() == AIType.ARCHER && Rnd.get(100) < 15)
						{
							double distance = npc.calculateDistance2D(target);
							if (distance <= 60 + combinedCollision)
							{
								int posX = npc.getX();
								int posY = npc.getY();
								int posZ = npc.getZ() + 30;
								if (target.getX() < posX)
								{
									posX += 300;
								}
								else
								{
									posX -= 300;
								}

								if (target.getY() < posY)
								{
									posY += 300;
								}
								else
								{
									posY -= 300;
								}

								if (GeoEngine.getInstance().canMoveToTarget(npc.getX(), npc.getY(), npc.getZ(), posX, posY, posZ, npc.getInstanceWorld()))
								{
									this.setIntention(Intention.MOVE_TO, new Location(posX, posY, posZ, 0));
								}

								return;
							}
						}

						if (npc.isRaid() || npc.isRaidMinion())
						{
							this._chaosTime++;
							boolean changeTarget = false;
							if (npc instanceof RaidBoss && this._chaosTime > NpcConfig.RAID_CHAOS_TIME)
							{
								double multiplier = npc.asMonster().hasMinions() ? 200.0 : 100.0;
								changeTarget = Rnd.get(100) <= 100.0 - npc.getCurrentHp() * multiplier / npc.getMaxHp();
							}
							else if (npc instanceof GrandBoss && this._chaosTime > NpcConfig.GRAND_CHAOS_TIME)
							{
								double chaosRate = 100.0 - npc.getCurrentHp() * 300.0 / npc.getMaxHp();
								changeTarget = chaosRate <= 10.0 && Rnd.get(100) <= 10 || chaosRate > 10.0 && Rnd.get(100) <= chaosRate;
							}
							else if (this._chaosTime > NpcConfig.MINION_CHAOS_TIME)
							{
								changeTarget = Rnd.get(100) <= 100.0 - npc.getCurrentHp() * 200.0 / npc.getMaxHp();
							}

							if (changeTarget)
							{
								target = this.targetReconsider(true);
								if (target != null)
								{
									this.setTarget(target);
									this._chaosTime = 0;
									return;
								}
							}
						}

						if (target == null)
						{
							target = this.targetReconsider(false);
							if (target == null)
							{
								return;
							}

							this.setTarget(target);
						}

						if (!npc.isMoving() && npc.hasSkillChance() || npc.getAiType() == AIType.MAGE)
						{
							if (!template.getAISkills(AISkillScope.HEAL).isEmpty())
							{
								Skill healSkill = template.getAISkills(AISkillScope.HEAL).get(Rnd.get(template.getAISkills(AISkillScope.HEAL).size()));
								if (SkillCaster.checkUseConditions(npc, healSkill))
								{
									Creature healTarget = this.skillTargetReconsider(healSkill, false);
									if (healTarget != null)
									{
										double healChance = (100 - healTarget.getCurrentHpPercent()) * 1.5;
										if (Rnd.get(100) < healChance && this.checkSkillTarget(healSkill, healTarget))
										{
											this.setTarget(healTarget);
											npc.doCast(healSkill);
											return;
										}
									}
								}
							}

							if (!template.getAISkills(AISkillScope.BUFF).isEmpty())
							{
								Skill buffSkill = template.getAISkills(AISkillScope.BUFF).get(Rnd.get(template.getAISkills(AISkillScope.BUFF).size()));
								if (SkillCaster.checkUseConditions(npc, buffSkill))
								{
									Creature buffTarget = this.skillTargetReconsider(buffSkill, true);
									if (this.checkSkillTarget(buffSkill, buffTarget))
									{
										this.setTarget(buffTarget);
										npc.doCast(buffSkill);
										return;
									}
								}
							}

							if (target.isMoving() && !template.getAISkills(AISkillScope.IMMOBILIZE).isEmpty())
							{
								Skill immobolizeSkill = template.getAISkills(AISkillScope.IMMOBILIZE).get(Rnd.get(template.getAISkills(AISkillScope.IMMOBILIZE).size()));
								if (SkillCaster.checkUseConditions(npc, immobolizeSkill) && this.checkSkillTarget(immobolizeSkill, target))
								{
									npc.doCast(immobolizeSkill);
									return;
								}
							}

							if (target.isCastingNow() && !template.getAISkills(AISkillScope.COT).isEmpty())
							{
								Skill muteSkill = template.getAISkills(AISkillScope.COT).get(Rnd.get(template.getAISkills(AISkillScope.COT).size()));
								if (SkillCaster.checkUseConditions(npc, muteSkill) && this.checkSkillTarget(muteSkill, target))
								{
									npc.doCast(muteSkill);
									return;
								}
							}

							if (!npc.getShortRangeSkills().isEmpty() && npc.calculateDistance2D(target) <= 150.0)
							{
								Skill shortRangeSkill = npc.getShortRangeSkills().get(Rnd.get(npc.getShortRangeSkills().size()));
								if (SkillCaster.checkUseConditions(npc, shortRangeSkill) && this.checkSkillTarget(shortRangeSkill, target))
								{
									npc.doCast(shortRangeSkill);
									return;
								}
							}

							if (!npc.getLongRangeSkills().isEmpty())
							{
								Skill longRangeSkill = npc.getLongRangeSkills().get(Rnd.get(npc.getLongRangeSkills().size()));
								if (SkillCaster.checkUseConditions(npc, longRangeSkill) && this.checkSkillTarget(longRangeSkill, target))
								{
									npc.doCast(longRangeSkill);
									return;
								}
							}

							if (!template.getAISkills(AISkillScope.GENERAL).isEmpty())
							{
								Skill generalSkill = template.getAISkills(AISkillScope.GENERAL).get(Rnd.get(template.getAISkills(AISkillScope.GENERAL).size()));
								if (SkillCaster.checkUseConditions(npc, generalSkill) && this.checkSkillTarget(generalSkill, target))
								{
									npc.doCast(generalSkill);
									return;
								}
							}
						}

						int range = npc.getPhysicalAttackRange() + combinedCollision;
						if (npc.isMoving())
						{
							range *= 2;
						}

						if (npc.getAiType() == AIType.ARCHER)
						{
							range = 850 + combinedCollision;
						}

						if (npc.calculateDistance2D(target) > range)
						{
							if (this.checkTarget(target))
							{
								this.moveToPawn(target, range);
								return;
							}

							target = this.targetReconsider(false);
							if (target == null)
							{
								return;
							}

							this.setTarget(target);
						}

						this._actor.doAutoAttack(target);
					}
				}
			}
		}
	}

	private boolean checkSkillTarget(Skill skill, WorldObject target)
	{
		if (target == null)
		{
			return false;
		}
		Attackable attackable = this.getActiveChar();
		if (skill.getTarget(attackable, target, false, attackable.isMovementDisabled(), false) == null)
		{
			return false;
		}
		else if (!LocationUtil.checkIfInRange(skill.getCastRange(), attackable, target, true))
		{
			return false;
		}
		else
		{
			if (target.isCreature())
			{
				if (skill.isContinuous())
				{
					if (target.asCreature().getEffectList().hasAbnormalType(skill.getAbnormalType(), i -> i.getSkill().getAbnormalLevel() >= skill.getAbnormalLevel()) || ((!skill.isDebuff() || !skill.hasNegativeEffect()) && target.isAutoAttackable(attackable)))
					{
						return false;
					}
				}

				if (skill.hasEffectType(EffectType.DISPEL, EffectType.DISPEL_BY_SLOT))
				{
					if (skill.hasNegativeEffect())
					{
						if (target.asCreature().getEffectList().getBuffCount() == 0)
						{
							return false;
						}
					}
					else if (target.asCreature().getEffectList().getDebuffCount() == 0)
					{
						return false;
					}
				}

				if (target.asCreature().getCurrentHp() == target.asCreature().getMaxHp() && skill.hasEffectType(EffectType.HEAL))
				{
					return false;
				}
			}

			return true;
		}
	}

	private boolean checkTarget(WorldObject target)
	{
		if (target == null)
		{
			return false;
		}
		Attackable npc = this.getActiveChar();
		if (target.isCreature())
		{
			if (target.asCreature().isDead())
			{
				return false;
			}

			if (npc.isMovementDisabled())
			{
				if (!npc.isInsideRadius2D(target, npc.getPhysicalAttackRange() + npc.getTemplate().getCollisionRadius() + target.asCreature().getTemplate().getCollisionRadius()) || !GeoEngine.getInstance().canSeeTarget(npc, target))
				{
					return false;
				}
			}

			if (!target.isAutoAttackable(npc))
			{
				return false;
			}
		}

		return true;
	}

	private Creature skillTargetReconsider(Skill skill, boolean insideCastRange)
	{
		Attackable npc = this.getActiveChar();
		if (!SkillCaster.checkUseConditions(npc, skill))
		{
			return null;
		}
		boolean hasNegativeEffect = skill.isContinuous() ? skill.isDebuff() : skill.hasNegativeEffect();
		int range = insideCastRange ? skill.getCastRange() + this.getActiveChar().getTemplate().getCollisionRadius() : 2000;
		List<Creature> result = new LinkedList<>();
		if (hasNegativeEffect)
		{
			for (AggroInfo aggro : npc.getAggroList().values())
			{
				if (this.checkSkillTarget(skill, aggro.getAttacker()))
				{
					result.add(aggro.getAttacker());
				}
			}
		}
		else
		{
			for (Creature creature : World.getInstance().getVisibleObjectsInRange(npc, Creature.class, range))
			{
				if (this.checkSkillTarget(skill, creature))
				{
					result.add(creature);
				}
			}

			if (this.checkSkillTarget(skill, npc))
			{
				result.add(npc);
			}

			if (skill.hasEffectType(EffectType.HEAL))
			{
				int searchValue = Integer.MAX_VALUE;
				Creature creaturex = null;

				for (Creature c : result)
				{
					int hpPer = c.getCurrentHpPercent();
					if (hpPer < searchValue)
					{
						searchValue = hpPer;
						creaturex = c;
					}
				}

				if (creaturex != null)
				{
					return creaturex;
				}
			}
		}

		return !result.isEmpty() ? result.get(Rnd.get(result.size())) : null;
	}

	private Creature targetReconsider(boolean randomTarget)
	{
		Attackable npc = this.getActiveChar();
		if (randomTarget)
		{
			List<Creature> result = new LinkedList<>();

			for (AggroInfo aggro : npc.getAggroList().values())
			{
				if (this.checkTarget(aggro.getAttacker()))
				{
					result.add(aggro.getAttacker());
				}
			}

			if (npc.isAggressive())
			{
				for (Creature creature : World.getInstance().getVisibleObjectsInRange(npc, Creature.class, npc.getAggroRange()))
				{
					if (this.checkTarget(creature))
					{
						result.add(creature);
					}
				}
			}

			if (!result.isEmpty())
			{
				return result.get(Rnd.get(result.size()));
			}
		}

		long searchValue = Long.MIN_VALUE;
		Creature creaturex = null;

		for (AggroInfo aggrox : npc.getAggroList().values())
		{
			if (this.checkTarget(aggrox.getAttacker()) && aggrox.getHate() > searchValue)
			{
				searchValue = aggrox.getHate();
				creaturex = aggrox.getAttacker();
			}
		}

		if (creaturex == null && npc.isAggressive())
		{
			for (Creature nearby : World.getInstance().getVisibleObjectsInRange(npc, Creature.class, npc.getAggroRange()))
			{
				if (this.checkTarget(nearby))
				{
					return nearby;
				}
			}
		}

		return null;
	}

	@Override
	public void onActionThink()
	{
		if (!this._thinking)
		{
			WorldRegion region = this._actor.getWorldRegion();
			if (region != null && region.areNeighborsActive())
			{
				if (!this.getActiveChar().isAllSkillsDisabled())
				{
					this._thinking = true;

					try
					{
						switch (this.getIntention())
						{
							case ACTIVE:
								this.thinkActive();
								break;
							case ATTACK:
								this.thinkAttack();
								break;
							case CAST:
								this.thinkCast();
						}
					}
					catch (Exception var6)
					{
					}
					finally
					{
						this._thinking = false;
					}
				}
			}
		}
	}

	@Override
	protected void onActionAttacked(Creature attacker)
	{
		Attackable me = this.getActiveChar();
		WorldObject target = this.getTarget();
		this._attackTimeout = 1200 + GameTimeTaskManager.getInstance().getGameTicks();
		if (this._globalAggro < 0)
		{
			this._globalAggro = 0;
		}

		if (!me.isInAggroList(attacker))
		{
			me.addDamageHate(attacker, 0L, 1L);
		}

		if (!me.isRunning())
		{
			me.setRunning();
		}

		if (!me.isCoreAIDisabled())
		{
			if (this.getIntention() != Intention.ATTACK)
			{
				this.setIntention(Intention.ATTACK, attacker);
			}
			else if (me.getMostHated() != target)
			{
				this.setIntention(Intention.ATTACK, attacker);
			}
		}

		if (me.isMonster())
		{
			Monster master = me.asMonster();
			if (master.hasMinions())
			{
				master.getMinionList().onAssist(me, attacker);
			}

			master = master.getLeader();
			if (master != null && master.hasMinions())
			{
				master.getMinionList().onAssist(me, attacker);
			}
		}

		NpcTemplate template = me.getTemplate();
		Set<Integer> clans = template.getClans();
		if (clans != null && !clans.isEmpty())
		{
			int collision = template.getCollisionRadius();
			int factionRange = template.getClanHelpRange() + collision;

			try
			{
				Creature finalTarget = attacker;
				boolean targetExistsInAttackByList = false;

				for (WeakReference<Creature> reference : me.getAttackByList())
				{
					if (reference.get() == finalTarget)
					{
						targetExistsInAttackByList = true;
						break;
					}
				}

				if (targetExistsInAttackByList)
				{
					World.getInstance().forEachVisibleObjectInRange(me, Attackable.class, factionRange, nearby -> {
						if (!nearby.isDead() && nearby.hasAI() && Math.abs(finalTarget.getZ() - nearby.getZ()) <= 600)
						{
							if (nearby.getAI()._intention == Intention.IDLE || nearby.getAI()._intention == Intention.ACTIVE)
							{
								NpcTemplate nearbytemplate = nearby.getTemplate();
								if (template.isClan(nearbytemplate.getClans()) && (!nearbytemplate.hasIgnoreClanNpcIds() || !nearbytemplate.getIgnoreClanNpcIds().contains(me.getId())))
								{
									if (finalTarget.isPlayable())
									{
										if (GeoEngine.getInstance().canSeeTarget(nearby, finalTarget))
										{
											nearby.getAI().notifyAction(Action.AGGRESSION, finalTarget, 1);
										}

										if (EventDispatcher.getInstance().hasListener(EventType.ON_ATTACKABLE_FACTION_CALL, nearby))
										{
											EventDispatcher.getInstance().notifyEventAsync(new OnAttackableFactionCall(nearby, me, finalTarget.asPlayer(), finalTarget.isSummon()), nearby);
										}
									}
									else if (nearby.getAI()._intention != Intention.ATTACK && GeoEngine.getInstance().canSeeTarget(nearby, finalTarget))
									{
										nearby.addDamageHate(finalTarget, 0L, me.getHating(finalTarget));
										nearby.getAI().setIntention(Intention.ATTACK, finalTarget);
									}
								}
							}
						}
					});
				}
			}
			catch (NullPointerException var12)
			{
			}
		}

		super.onActionAttacked(attacker);
	}

	@Override
	protected void onActionAggression(Creature target, int aggro)
	{
		Attackable me = this.getActiveChar();
		if (!me.isDead() && target != null)
		{
			me.addDamageHate(target, 0L, aggro);
			if (this.getIntention() != Intention.ATTACK)
			{
				if (!me.isRunning())
				{
					me.setRunning();
				}

				this.setIntention(Intention.ATTACK, target);
			}

			if (me.isMonster())
			{
				Monster master = me.asMonster();
				if (master.hasMinions())
				{
					master.getMinionList().onAssist(me, target);
				}

				master = master.getLeader();
				if (master != null && master.hasMinions())
				{
					master.getMinionList().onAssist(me, target);
				}
			}
		}
	}

	@Override
	protected void onIntentionActive()
	{
		this._attackTimeout = Integer.MAX_VALUE;
		super.onIntentionActive();
	}

	public void setGlobalAggro(int value)
	{
		this._globalAggro = value;
	}

	@Override
	public void setTarget(WorldObject target)
	{
		this._actor.setTarget(target);
	}

	@Override
	public WorldObject getTarget()
	{
		return this._actor.getTarget();
	}

	public Attackable getActiveChar()
	{
		return this._actor.asAttackable();
	}
}
