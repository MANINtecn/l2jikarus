package net.sf.l2jdev.gameserver.taskmanagers;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.instance.Monster;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.serverpackets.autoplay.ExAutoPlayDoMacro;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class AutoPlayTaskManager
{
	private static final Set<Set<Player>> POOLS = ConcurrentHashMap.newKeySet();
	private static final Map<Player, Integer> IDLE_COUNT = new ConcurrentHashMap<>();
 
	private static final Integer AUTO_ATTACK_ACTION = 2;
	private static final Integer PET_ATTACK_ACTION = 16;
	private static final Integer SUMMON_ATTACK_ACTION = 22;

	protected AutoPlayTaskManager()
	{
	}

	public synchronized void startAutoPlay(Player player)
	{
		for (Set<Player> pool : POOLS)
		{
			if (pool.contains(player))
			{
				return;
			}
		}

		player.setAutoPlaying(true);

		for (Set<Player> poolx : POOLS)
		{
			if (poolx.size() < 200)
			{
				player.onActionRequest();
				poolx.add(player);
				return;
			}
		}

		Set<Player> poolxx = ConcurrentHashMap.newKeySet(200);
		player.onActionRequest();
		poolxx.add(player);
		ThreadPool.schedulePriorityTaskAtFixedRate(new AutoPlayTaskManager.AutoPlay(poolxx), 700L, 700L);
		POOLS.add(poolxx);
	}

	public void stopAutoPlay(Player player)
	{
		for (Set<Player> pool : POOLS)
		{
			if (pool.remove(player))
			{
				player.setAutoPlaying(false);
				if (player.hasServitors())
				{
					for (Summon summon : player.getServitors().values())
					{
						summon.followOwner();
					}
				}

				if (player.hasPet())
				{
					player.getPet().followOwner();
				}

				IDLE_COUNT.remove(player);
				return;
			}
		}
	}

	public static AutoPlayTaskManager getInstance()
	{
		return AutoPlayTaskManager.SingletonHolder.INSTANCE;
	}

	private class AutoPlay implements Runnable
	{
		private final Set<Player> _players;

		public AutoPlay(Set<Player> players)
		{
			Objects.requireNonNull(AutoPlayTaskManager.this);
			super();
			this._players = players;
		}

		@Override
		public void run()
		{
			if (!this._players.isEmpty())
			{
				label321:
				for (Player player : this._players)
				{
					if (!player.isOnline() || player.isInOfflineMode() && !player.isOfflinePlay() || !GeneralConfig.ENABLE_AUTO_PLAY)
					{
						AutoPlayTaskManager.this.stopAutoPlay(player);
					}
					else if (!player.isSitting() && !player.isCastingNow() && player.getQueuedSkill() == null)
					{
						int targetMode = player.getAutoPlaySettings().getNextTargetMode();
						WorldObject target = player.getTarget();
						if (target != null && target.isCreature())
						{
							Creature creature = target.asCreature();
							if (!creature.isAlikeDead() && this.isTargetModeValid(targetMode, player, creature))
							{
								if (creature.getTarget() == player || creature.getTarget() == null)
								{
									if (!GeoEngine.getInstance().canSeeTarget(player, creature))
									{
										player.setTarget(null);
										continue;
									}

									Pet pet = player.getPet();
									if (pet != null && (player.isOfflinePlay() || player.getAutoUseSettings().getAutoActions().contains(AutoPlayTaskManager.PET_ATTACK_ACTION)) && pet.hasAI() && !pet.isMoving() && !pet.isDisabled() && pet.getAI().getIntention() != Intention.ATTACK && pet.getAI().getIntention() != Intention.CAST && creature.isAutoAttackable(player) && GeoEngine.getInstance().canSeeTarget(player, creature))
									{
										pet.getAI().setIntention(Intention.ATTACK, creature);
									}

									if (player.hasSummon() && (player.isOfflinePlay() || player.getAutoUseSettings().getAutoActions().contains(AutoPlayTaskManager.SUMMON_ATTACK_ACTION)))
									{
										for (Summon summon : player.getServitors().values())
										{
											if (summon.hasAI() && !summon.isMoving() && !summon.isDisabled() && summon.getAI().getIntention() != Intention.ATTACK && summon.getAI().getIntention() != Intention.CAST && creature.isAutoAttackable(player) && GeoEngine.getInstance().canSeeTarget(player, creature))
											{
												summon.getAI().setIntention(Intention.ATTACK, creature);
											}
										}
									}

									if (!this.isMageCaster(player) && player.hasAI() && !player.isAttackingNow() && !player.isCastingNow() && !player.isMoving() && !player.isDisabled())
									{
										if (player.getAI().getIntention() != Intention.ATTACK)
										{
											if (creature.isAutoAttackable(player))
											{
												if (!GeoEngine.getInstance().canSeeTarget(player, creature))
												{
													player.setTarget(null);
												}
												else
												{
													player.getAI().setIntention(Intention.ATTACK, creature);
												}
											}
										}
										else if (creature.hasAI() && !creature.getAI().isAutoAttacking())
										{
											Weapon weapon = player.getActiveWeaponItem();
											if (weapon != null)
											{
												int idleCount = AutoPlayTaskManager.IDLE_COUNT.getOrDefault(player, 0);
												if (idleCount > 10)
												{
													boolean ranged = weapon.getItemType().isRanged();
													double angle = LocationUtil.calculateHeadingFrom(player, creature);
													double radian = Math.toRadians(angle);
													double course = Math.toRadians(180.0);
													double distance = (ranged ? player.getCollisionRadius() : player.getCollisionRadius() + creature.getCollisionRadius()) * 2.0F;
													int x1 = (int) (Math.cos(Math.PI + radian + course) * distance);
													int y1 = (int) (Math.sin(Math.PI + radian + course) * distance);
													Location location;
													if (ranged)
													{
														location = new Location(player.getX() + x1, player.getY() + y1, player.getZ());
													}
													else
													{
														location = new Location(creature.getX() + x1, creature.getY() + y1, player.getZ());
													}

													player.getAI().setIntention(Intention.MOVE_TO, location);
													AutoPlayTaskManager.IDLE_COUNT.remove(player);
												}
												else
												{
													AutoPlayTaskManager.IDLE_COUNT.put(player, idleCount + 1);
												}
											}
										}
									}
									continue;
								}
							}
							else
							{
								if (creature.isMonster() && creature.isDead() && player.getAutoUseSettings().getAutoSkills().contains(254))
								{
									Skill sweeper = player.getKnownSkill(42);
									if (sweeper != null)
									{
										Monster monster = target.asMonster();
										if (monster.checkSpoilOwner(player, false))
										{
											if (player.calculateDistance2D(target) > 40.0)
											{
												if (!player.isMoving())
												{
													player.getAI().setIntention(Intention.MOVE_TO, target);
												}
											}
											else
											{
												player.doCast(sweeper);
											}
											continue;
										}
									}
								}

								player.setTarget(null);
							}
						}

						AutoPlayTaskManager.IDLE_COUNT.remove(player);
						if (player.getAutoPlaySettings().doPickup() && player.isInventoryUnder90(false))
						{
							for (Item droppedItem : World.getInstance().getVisibleObjectsInRange(player, Item.class, 200))
							{
								if (droppedItem != null && droppedItem.isSpawned() && !GeneralConfig.IGNORED_AUTO_PICK_ITEMS.contains(droppedItem.getId()) && GeoEngine.getInstance().canMoveToTarget(player.getX(), player.getY(), player.getZ(), droppedItem.getX(), droppedItem.getY(), droppedItem.getZ(), player.getInstanceWorld()))
								{
									if (player.calculateDistance2D(droppedItem) > 70.0)
									{
										if (!player.isMoving())
										{
											player.getAI().setIntention(Intention.MOVE_TO, droppedItem);
										}
										continue label321;
									}

									if (!droppedItem.isProtected() || droppedItem.getOwnerId() == player.getObjectId())
									{
										player.doPickupItem(droppedItem);
										continue label321;
									}
								}
							}
						}

						Creature creature = null;
						Party party = player.getParty();
						Player leader = party == null ? null : party.getLeader();
						if (GeneralConfig.ENABLE_AUTO_ASSIST && party != null && leader != null && leader != player && !leader.isDead())
						{
							if (leader.calculateDistance3D(player) < PlayerConfig.ALT_PARTY_RANGE * 2)
							{
								WorldObject leaderTarget = leader.getTarget();
								if (leaderTarget == null || !leaderTarget.isAttackable() && (!leaderTarget.isPlayable() || party.containsPlayer(leaderTarget.asPlayer())))
								{
									if (player.getAI().getIntention() != Intention.FOLLOW && !player.isDisabled())
									{
										player.getAI().setIntention(Intention.FOLLOW, leader);
									}
								}
								else
								{
									creature = leaderTarget.asCreature();
								}
							}
						}
						else
						{
							double closestDistance = Double.MAX_VALUE;

							for (Creature nearby : World.getInstance().getVisibleObjectsInRange(player, Creature.class, player.getAutoPlaySettings().isShortRange() && targetMode != 2 && targetMode != 4 ? 600 : 1400))
							{
								if (nearby != null && !nearby.isAlikeDead() && (!player.getAutoPlaySettings().isRespectfulHunting() || nearby.isPlayable() || nearby.getTarget() == null || nearby.getTarget() == player || player.getServitors().containsKey(nearby.getTarget().getObjectId())) && this.isTargetModeValid(targetMode, player, nearby) && Math.abs(player.getZ() - nearby.getZ()) < 800 && GeoEngine.getInstance().canSeeTarget(player, nearby) && GeoEngine.getInstance().canMoveToTarget(player.getX(), player.getY(), player.getZ(), nearby.getX(), nearby.getY(), nearby.getZ(), player.getInstanceWorld()))
								{
									double creatureDistance = player.calculateDistance2D(nearby);
									if (creatureDistance < closestDistance)
									{
										creature = nearby;
										closestDistance = creatureDistance;
									}
								}
							}
						}

						if (creature != null)
						{
							player.setTarget(creature);
							if (!this.isMageCaster(player))
							{
								player.sendPacket(ExAutoPlayDoMacro.STATIC_PACKET);
							}
						}
					}
				}
			}
		}

		private boolean isMageCaster(Player player)
		{
			return GeneralConfig.AUTO_PLAY_ATTACK_ACTION ? !player.getAutoUseSettings().getAutoActions().contains(AutoPlayTaskManager.AUTO_ATTACK_ACTION) : player.isMageClass() && player.getRace() != Race.ORC;
		}

		private boolean isTargetModeValid(int mode, Player player, Creature creature)
		{
			if (creature.isTargetable() && (!creature.isNpc() || !creature.isInvul() && creature.asNpc().isShowName()))
			{
				switch (mode)
				{
					case 1:
						return creature.isMonster() && !creature.isRaid() && creature.isAutoAttackable(player);
					case 2:
						return creature.isPlayable() && creature.isAutoAttackable(player);
					case 3:
						return creature.isNpc() && !creature.isMonster() && !creature.isInsideZone(ZoneId.PEACE);
					case 4:
						return creature.isMonster() || creature.isPlayer() && creature.getTarget() == player && creature.asPlayer().getEinhasadOverseeingLevel() >= 1;
					default:
						return creature.isNpc() && !creature.isInsideZone(ZoneId.PEACE) || creature.isPlayable() && creature.isAutoAttackable(player);
				}
			}
			return false;
		}
	}

	private static class SingletonHolder
	{
		protected static final AutoPlayTaskManager INSTANCE = new AutoPlayTaskManager();
	}
}
