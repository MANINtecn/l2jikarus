package net.sf.l2jdev.gameserver.model.actor.instance;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.TrapAction;
import net.sf.l2jdev.gameserver.model.actor.tasks.npc.trap.TrapTask;
import net.sf.l2jdev.gameserver.model.actor.tasks.npc.trap.TrapTriggerTask;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnTrapAction;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExDamagePopUp;
import net.sf.l2jdev.gameserver.network.serverpackets.ExMagicAttackInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.taskmanagers.DecayTaskManager;

public class Trap extends Npc
{
	public static final int TICK = 1000;
	private boolean _hasLifeTime;
	private boolean _isInArena = false;
	private boolean _isTriggered;
	private final int _lifeTime;
	private Player _owner;
	private final Set<Integer> _playersWhoDetectedMe = new HashSet<>();
	private final SkillHolder _skill;
	private int _remainingTime;
	private ScheduledFuture<?> _trapTask = null;

	public Trap(NpcTemplate template, int instanceId, int lifeTime)
	{
		super(template);
		this.setInstanceType(InstanceType.Trap);
		this.setInstanceById(instanceId);
		this.setName(template.getName());
		this.setInvul(false);
		this._owner = null;
		this._isTriggered = false;
		this._skill = this.getParameters().getObject("trap_skill", SkillHolder.class);
		this._hasLifeTime = lifeTime >= 0;
		this._lifeTime = lifeTime != 0 ? lifeTime : 30000;
		this._remainingTime = this._lifeTime;
		if (this._skill != null)
		{
			this._trapTask = ThreadPool.scheduleAtFixedRate(new TrapTask(this), 1000L, 1000L);
		}
	}

	public Trap(NpcTemplate template, Player owner, int lifeTime)
	{
		this(template, owner.getInstanceId(), lifeTime);
		this._owner = owner;
	}

	@Override
	public void broadcastPacket(ServerPacket packet, boolean includeSelf)
	{
		World.getInstance().forEachVisibleObject(this, Player.class, player -> {
			if (this._isTriggered || this.canBeSeen(player))
			{
				player.sendPacket(packet);
			}
		});
	}

	public boolean canBeSeen(Creature creature)
	{
		if (creature != null && this._playersWhoDetectedMe.contains(creature.getObjectId()))
		{
			return true;
		}
		else if (this._owner == null || creature == null)
		{
			return false;
		}
		else if (creature == this._owner)
		{
			return true;
		}
		else
		{
			if (creature.isPlayer())
			{
				if (creature.asPlayer().inObserverMode() || (this._owner.isInOlympiadMode() && creature.asPlayer().isInOlympiadMode() && creature.asPlayer().getOlympiadSide() != this._owner.getOlympiadSide()))
				{
					return false;
				}
			}

			return this._isInArena ? true : this._owner.isInParty() && creature.isInParty() && this._owner.getParty().getLeaderObjectId() == creature.getParty().getLeaderObjectId();
		}
	}

	@Override
	public boolean deleteMe()
	{
		this._owner = null;
		return super.deleteMe();
	}

	@Override
	public Player asPlayer()
	{
		return this._owner;
	}

	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public int getReputation()
	{
		return this._owner != null ? this._owner.getReputation() : 0;
	}

	public Player getOwner()
	{
		return this._owner;
	}

	@Override
	public byte getPvpFlag()
	{
		return this._owner != null ? this._owner.getPvpFlag() : 0;
	}

	@Override
	public Item getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	public Skill getSkill()
	{
		return this._skill == null ? null : this._skill.getSkill();
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return !this.canBeSeen(attacker);
	}

	@Override
	public boolean isTrap()
	{
		return true;
	}

	public boolean isTriggered()
	{
		return this._isTriggered;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		this._isInArena = this.isInsideZone(ZoneId.PVP) && !this.isInsideZone(ZoneId.SIEGE);
		this._playersWhoDetectedMe.clear();
	}

	@Override
	public void doAttack(double damage, Creature target, Skill skill, boolean isDOT, boolean directlyToHp, boolean critical, boolean reflect)
	{
		super.doAttack(damage, target, skill, isDOT, directlyToHp, critical, reflect);
		this.sendDamageMessage(target, skill, (int) damage, 0.0, critical, false, false);
	}

	@Override
	public void sendDamageMessage(Creature target, Skill skill, int damage, double elementalDamage, boolean crit, boolean miss, boolean elementalCrit)
	{
		if (!miss && this._owner != null)
		{
			if (this._owner.isInOlympiadMode() && target.isPlayer() && target.asPlayer().isInOlympiadMode() && target.asPlayer().getOlympiadGameId() == this._owner.getOlympiadGameId())
			{
				OlympiadGameManager.getInstance().notifyCompetitorDamage(this.getOwner(), damage);
			}

			if (target.isHpBlocked() && !target.isNpc())
			{
				if (skill == null)
				{
					this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), 0, (byte) 12));
				}
				else
				{
					this.sendPacket(new ExMagicAttackInfo(this.getObjectId(), target.getObjectId(), 4));
				}

				this._owner.sendPacket(SystemMessageId.THE_ATTACK_HAS_BEEN_BLOCKED);
			}
			else
			{
				if (crit)
				{
					this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 3));
					target.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 3));
				}
				else if (skill != null)
				{
					this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 13));
					target.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 13));
				}
				else
				{
					this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 1));
					target.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), damage, (byte) 1));
				}

				SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_DEALT_S3_DAMAGE_TO_C2);
				sm.addString(this.getName());
				sm.addString(target.getName());
				sm.addInt(damage);
				sm.addPopup(target.getObjectId(), this.getObjectId(), damage * -1);
				this._owner.sendPacket(sm);
			}
		}
		else
		{
			this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), 0, (byte) 11));
		}
	}

	@Override
	public void sendInfo(Player player)
	{
		if (this._isTriggered || this.canBeSeen(player))
		{
			player.sendPacket(new NpcInfo(this));
		}
	}

	public void setDetected(Creature detector)
	{
		if (this._isInArena)
		{
			if (detector.isPlayable())
			{
				this.sendInfo(detector.asPlayer());
			}
		}
		else if (this._owner == null || this._owner.getPvpFlag() != 0 || this._owner.getReputation() < 0)
		{
			this._playersWhoDetectedMe.add(detector.getObjectId());
			if (EventDispatcher.getInstance().hasListener(EventType.ON_TRAP_ACTION, this))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnTrapAction(this, detector, TrapAction.TRAP_DETECTED), this);
			}

			if (detector.isPlayable())
			{
				this.sendInfo(detector.asPlayer());
			}
		}
	}

	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancel(this);
	}

	public void triggerTrap(Creature target)
	{
		if (this._trapTask != null)
		{
			this._trapTask.cancel(true);
			this._trapTask = null;
		}

		this._isTriggered = true;
		this.broadcastPacket(new NpcInfo(this));
		this.setTarget(target);
		if (EventDispatcher.getInstance().hasListener(EventType.ON_TRAP_ACTION, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnTrapAction(this, target, TrapAction.TRAP_TRIGGERED), this);
		}

		ThreadPool.schedule(new TrapTriggerTask(this), 500L);
	}

	public void unSummon()
	{
		if (this._trapTask != null)
		{
			this._trapTask.cancel(true);
			this._trapTask = null;
		}

		this._owner = null;
		if (this.isSpawned() && !this.isDead())
		{
			ZoneManager.getInstance().getRegion(this).removeFromZones(this);
			this.deleteMe();
		}
	}

	public boolean hasLifeTime()
	{
		return this._hasLifeTime;
	}

	public void setHasLifeTime(boolean value)
	{
		this._hasLifeTime = value;
	}

	public int getRemainingTime()
	{
		return this._remainingTime;
	}

	public void setRemainingTime(int time)
	{
		this._remainingTime = time;
	}

	public int getLifeTime()
	{
		return this._lifeTime;
	}
}
