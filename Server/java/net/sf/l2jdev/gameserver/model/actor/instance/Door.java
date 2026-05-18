package net.sf.l2jdev.gameserver.model.actor.instance;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.ai.CreatureAI;
import net.sf.l2jdev.gameserver.ai.DoorAI;
import net.sf.l2jdev.gameserver.data.xml.DoorData;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.DoorOpenType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.stat.DoorStat;
import net.sf.l2jdev.gameserver.model.actor.status.DoorStatus;
import net.sf.l2jdev.gameserver.model.actor.templates.DoorTemplate;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.DoorStatusUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.OnEventTrigger;
import net.sf.l2jdev.gameserver.network.serverpackets.StaticObjectInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class Door extends Creature
{
	boolean _open = false;
	private boolean _isAttackableDoor = false;
	private boolean _isInverted = false;
	private int _meshindex = 1;
	private Future<?> _autoCloseTask;

	public Door(DoorTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Door);
		this.setInvul(false);
		this.setLethalable(false);
		this._open = template.isOpenByDefault();
		this._isAttackableDoor = template.isAttackable();
		this._isInverted = template.isInverted();
		super.setTargetable(template.isTargetable());
		if (this.isOpenableByTime())
		{
			this.startTimerOpen();
		}
	}

	@Override
	protected CreatureAI initAI()
	{
		return new DoorAI(this);
	}

	@Override
	public void moveToLocation(int x, int y, int z, int offset)
	{
	}

	@Override
	public void stopMove(Location loc)
	{
	}

	@Override
	public void doAutoAttack(Creature target)
	{
	}

	@Override
	public void doCast(Skill skill)
	{
	}

	private void startTimerOpen()
	{
		int delay = this._open ? this.getTemplate().getOpenTime() : this.getTemplate().getCloseTime();
		if (this.getTemplate().getRandomTime() > 0)
		{
			delay += Rnd.get(this.getTemplate().getRandomTime());
		}

		ThreadPool.schedule(new Door.TimerOpen(), delay * 1000);
	}

	@Override
	public DoorTemplate getTemplate()
	{
		return (DoorTemplate) super.getTemplate();
	}

	@Override
	public DoorStatus getStatus()
	{
		return (DoorStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		this.setStatus(new DoorStatus(this));
	}

	@Override
	public void initCharStat()
	{
		this.setStat(new DoorStat(this));
	}

	@Override
	public DoorStat getStat()
	{
		return (DoorStat) super.getStat();
	}

	public boolean isOpenableBySkill()
	{
		return this.getTemplate().getOpenType() == DoorOpenType.BY_SKILL;
	}

	public boolean isOpenableByItem()
	{
		return this.getTemplate().getOpenType() == DoorOpenType.BY_ITEM;
	}

	public boolean isOpenableByClick()
	{
		return this.getTemplate().getOpenType() == DoorOpenType.BY_CLICK;
	}

	public boolean isOpenableByTime()
	{
		return this.getTemplate().getOpenType() == DoorOpenType.BY_TIME;
	}

	public boolean isOpenableByCycle()
	{
		return this.getTemplate().getOpenType() == DoorOpenType.BY_CYCLE;
	}

	@Override
	public int getLevel()
	{
		return this.getTemplate().getLevel();
	}

	@Override
	public int getId()
	{
		return this.getTemplate().getId();
	}

	public boolean isOpen()
	{
		return this._open;
	}

	public void setOpen(boolean open)
	{
		this._open = open;
		if (this.getChildId() > 0)
		{
			Door sibling = this.getSiblingDoor(this.getChildId());
			if (sibling != null)
			{
				sibling.notifyChildEvent(open);
			}
			else
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": cannot find child id: " + this.getChildId());
			}
		}
	}

	public boolean isAttackableDoor()
	{
		return this._isAttackableDoor;
	}

	public boolean isInverted()
	{
		return this._isInverted;
	}

	public boolean isShowHp()
	{
		return this.getTemplate().isShowHp();
	}

	public void setIsAttackableDoor(boolean value)
	{
		this._isAttackableDoor = value;
	}

	public int getDamage()
	{
		if (this.getCastle() == null && this.getFort() == null)
		{
			return 0;
		}
		int dmg = 6 - (int) Math.ceil(this.getCurrentHp() / this.getMaxHp() * 6.0);
		if (dmg > 6)
		{
			return 6;
		}
		return dmg < 0 ? 0 : dmg;
	}

	public Castle getCastle()
	{
		return CastleManager.getInstance().getCastle(this);
	}

	public Fort getFort()
	{
		return net.sf.l2jdev.gameserver.managers.FortManager.getInstance().getFort(this);
	}

	public boolean isEnemy()
	{
		return this.getCastle() != null && this.getCastle().getResidenceId() > 0 && this.getCastle().getZone().isActive() && this.isShowHp() ? true : this.getFort() != null && this.getFort().getResidenceId() > 0 && this.getFort().getZone().isActive() && this.isShowHp();
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		if (!attacker.isPlayable())
		{
			return false;
		}
		else if (this._isAttackableDoor)
		{
			return true;
		}
		else if (!this.isShowHp())
		{
			return false;
		}
		else
		{
			Player actingPlayer = attacker.asPlayer();
			boolean isCastle = this.getCastle() != null && this.getCastle().getResidenceId() > 0 && this.getCastle().getZone().isActive();
			boolean isFort = this.getFort() != null && this.getFort().getResidenceId() > 0 && this.getFort().getZone().isActive();
			if (isFort)
			{
				Clan clan = actingPlayer.getClan();
				if (clan != null && clan == this.getFort().getOwnerClan())
				{
					return false;
				}
			}
			else if (isCastle)
			{
				Clan clan = actingPlayer.getClan();
				if (clan != null && clan.getId() == this.getCastle().getOwnerId())
				{
					return false;
				}
			}

			return isCastle || isFort;
		}
	}

	@Override
	public Item getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
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

	@Override
	public void broadcastStatusUpdate(Creature caster)
	{
		Collection<Player> knownPlayers = World.getInstance().getVisibleObjects(this, Player.class);
		if (knownPlayers != null && !knownPlayers.isEmpty())
		{
			StaticObjectInfo su = new StaticObjectInfo(this, false);
			StaticObjectInfo targetableSu = new StaticObjectInfo(this, true);
			DoorStatusUpdate dsu = new DoorStatusUpdate(this);
			OnEventTrigger oe = null;
			if (this.getEmitter() > 0)
			{
				if (this._isInverted)
				{
					oe = new OnEventTrigger(this.getEmitter(), !this._open);
				}
				else
				{
					oe = new OnEventTrigger(this.getEmitter(), this._open);
				}
			}

			for (Player player : knownPlayers)
			{
				if (player != null && this.isVisibleFor(player))
				{
					if (!player.isGM() && (this.getCastle() == null || this.getCastle().getResidenceId() <= 0) && (this.getFort() == null || this.getFort().getResidenceId() <= 0))
					{
						player.sendPacket(su);
					}
					else
					{
						player.sendPacket(targetableSu);
					}

					player.sendPacket(dsu);
					if (oe != null)
					{
						player.sendPacket(oe);
					}
				}
			}
		}
	}

	public void openCloseMe(boolean open)
	{
		if (open)
		{
			this.openMe();
		}
		else
		{
			this.closeMe();
		}
	}

	public void openMe()
	{
		if (this.getGroupName() != null)
		{
			this.manageGroupOpen(true, this.getGroupName());
		}
		else
		{
			if (!this.isOpen())
			{
				this.setOpen(true);
				this.broadcastStatusUpdate();
				this.startAutoCloseTask();
			}
		}
	}

	public void closeMe()
	{
		Future<?> oldTask = this._autoCloseTask;
		if (oldTask != null)
		{
			this._autoCloseTask = null;
			oldTask.cancel(false);
		}

		if (this.getGroupName() != null)
		{
			this.manageGroupOpen(false, this.getGroupName());
		}
		else
		{
			if (this.isOpen())
			{
				this.setOpen(false);
				this.broadcastStatusUpdate();
			}
		}
	}

	private void manageGroupOpen(boolean open, String groupName)
	{
		Set<Integer> set = DoorData.getInstance().getDoorsByGroup(groupName);
		Door first = null;

		for (Integer id : set)
		{
			Door door = this.getSiblingDoor(id);
			if (first == null)
			{
				first = door;
			}

			if (door.isOpen() != open)
			{
				door.setOpen(open);
				door.broadcastStatusUpdate();
			}
		}

		if (first != null && open)
		{
			first.startAutoCloseTask();
		}
	}

	private void notifyChildEvent(boolean open)
	{
		byte openThis = open ? this.getTemplate().getMasterDoorOpen() : this.getTemplate().getMasterDoorClose();
		if (openThis == 1)
		{
			this.openMe();
		}
		else if (openThis == -1)
		{
			this.closeMe();
		}
	}

	@Override
	public String getName()
	{
		return this.getTemplate().getName();
	}

	public int getX(int i)
	{
		return this.getTemplate().getNodeX()[i];
	}

	public int getY(int i)
	{
		return this.getTemplate().getNodeY()[i];
	}

	public int getZMin()
	{
		return this.getTemplate().getNodeZ();
	}

	public int getZMax()
	{
		return this.getTemplate().getNodeZ() + this.getTemplate().getHeight();
	}

	public void setMeshIndex(int mesh)
	{
		this._meshindex = mesh;
	}

	public int getMeshIndex()
	{
		return this._meshindex;
	}

	public int getEmitter()
	{
		return this.getTemplate().getEmmiter();
	}

	public boolean isWall()
	{
		return this.getTemplate().isWall();
	}

	public String getGroupName()
	{
		return this.getTemplate().getGroupName();
	}

	public int getChildId()
	{
		return this.getTemplate().getChildDoorId();
	}

	@Override
	public void reduceCurrentHp(double value, Creature attacker, Skill skill, boolean isDOT, boolean directlyToHp, boolean critical, boolean reflect)
	{
		if (this.isWall() && !this.isInInstance())
		{
			if (!attacker.isServitor() || (attacker.getTemplate().getRace() != Race.SIEGE_WEAPON))
			{
				return;
			}
		}

		super.reduceCurrentHp(value, attacker, skill, isDOT, directlyToHp, critical, reflect);
	}

	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		boolean isFort = this.getFort() != null && this.getFort().getResidenceId() > 0 && this.getFort().getSiege().isInProgress();
		boolean isCastle = this.getCastle() != null && this.getCastle().getResidenceId() > 0 && this.getCastle().getSiege().isInProgress();
		if (!isFort && !isCastle)
		{
			this.openMe();
		}
		else
		{
			this.broadcastPacket(new SystemMessage(SystemMessageId.THE_CASTLE_GATE_HAS_BEEN_DESTROYED));
		}

		return true;
	}

	@Override
	public void sendInfo(Player player)
	{
		if (this.isVisibleFor(player))
		{
			player.sendPacket(new StaticObjectInfo(this, player.isGM()));
			player.sendPacket(new DoorStatusUpdate(this));
			if (this.getEmitter() > 0)
			{
				if (this._isInverted)
				{
					player.sendPacket(new OnEventTrigger(this.getEmitter(), !this._open));
				}
				else
				{
					player.sendPacket(new OnEventTrigger(this.getEmitter(), this._open));
				}
			}
		}
	}

	@Override
	public void setTargetable(boolean targetable)
	{
		super.setTargetable(targetable);
		this.broadcastStatusUpdate();
	}

	public boolean checkCollision()
	{
		return this.getTemplate().isCheckCollision();
	}

	private Door getSiblingDoor(int doorId)
	{
		Instance inst = this.getInstanceWorld();
		return inst != null ? inst.getDoor(doorId) : DoorData.getInstance().getDoor(doorId);
	}

	private void startAutoCloseTask()
	{
		if (this.getTemplate().getCloseTime() >= 0 && !this.isOpenableByTime())
		{
			Future<?> oldTask = this._autoCloseTask;
			if (oldTask != null)
			{
				this._autoCloseTask = null;
				oldTask.cancel(false);
			}

			this._autoCloseTask = ThreadPool.schedule(new Door.AutoClose(), this.getTemplate().getCloseTime() * 1000);
		}
	}

	@Override
	public boolean isDoor()
	{
		return true;
	}

	@Override
	public Door asDoor()
	{
		return this;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append("[");
		sb.append(this.getTemplate().getId());
		sb.append("](");
		sb.append(this.getObjectId());
		sb.append(")");
		return sb.toString();
	}

	class AutoClose implements Runnable
	{
		AutoClose()
		{
			Objects.requireNonNull(Door.this);
			super();
		}

		@Override
		public void run()
		{
			if (Door.this._open)
			{
				Door.this.closeMe();
			}
		}
	}

	class TimerOpen implements Runnable
	{
		TimerOpen()
		{
			Objects.requireNonNull(Door.this);
			super();
		}

		@Override
		public void run()
		{
			if (Door.this._open)
			{
				Door.this.closeMe();
			}
			else
			{
				Door.this.openMe();
			}

			int delay = Door.this._open ? Door.this.getTemplate().getCloseTime() : Door.this.getTemplate().getOpenTime();
			if (Door.this.getTemplate().getRandomTime() > 0)
			{
				delay += Rnd.get(Door.this.getTemplate().getRandomTime());
			}

			ThreadPool.schedule(this, delay * 1000);
		}
	}
}
