package net.sf.l2jdev.gameserver.model.actor.instance;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.CharInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.taskmanagers.DecayTaskManager;

public class Decoy extends Creature
{
	private final Player _owner;
	private Future<?> _decoyLifeTask;
	private Future<?> _hateSpam;
	private ScheduledFuture<?> _skillTask;

	public Decoy(NpcTemplate template, Player owner, int totalLifeTime)
	{
		this(template, owner, totalLifeTime, true);
	}

	public Decoy(NpcTemplate template, Player owner, int totalLifeTime, boolean aggressive)
	{
		super(template);
		this.setInstanceType(InstanceType.Decoy);
		this._owner = owner;
		this.setXYZInvisible(owner.getX(), owner.getY(), owner.getZ());
		this.setInvul(false);
		this._decoyLifeTask = ThreadPool.schedule(this::unSummon, totalLifeTime);
		if (aggressive)
		{
			int skilllevel = Math.min(this.getTemplate().getDisplayId() - 13070, SkillData.getInstance().getMaxLevel(5272));
			this._hateSpam = ThreadPool.scheduleAtFixedRate(new Decoy.HateSpam(this, SkillData.getInstance().getSkill(5272, skilllevel)), 2000L, 5000L);
		}

		SkillHolder skill = template.getParameters().getSkillHolder("decoy_skill");
		if (skill != null)
		{
			ThreadPool.schedule(() -> {
				this.doCast(skill.getSkill());
				long castTime = (long) (template.getParameters().getFloat("cast_time", 5.0F) * 1000.0F) - 100L;
				long skillDelay = (long) (template.getParameters().getFloat("skill_delay", 2.0F) * 1000.0F);
				this._skillTask = ThreadPool.scheduleAtFixedRate(() -> {
					if ((this.isDead() || !this.isSpawned()) && this._skillTask != null)
					{
						this._skillTask.cancel(false);
						this._skillTask = null;
					}
					else
					{
						this.doCast(skill.getSkill());
					}
				}, castTime, skillDelay);
			}, 100L);
		}
	}

	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		if (this._hateSpam != null)
		{
			this._hateSpam.cancel(true);
			this._hateSpam = null;
		}

		this.unSummon();
		DecayTaskManager.getInstance().add(this);
		return true;
	}

	public synchronized void unSummon()
	{
		if (this._skillTask != null)
		{
			this._skillTask.cancel(false);
			this._skillTask = null;
		}

		if (this._hateSpam != null)
		{
			this._hateSpam.cancel(true);
			this._hateSpam = null;
		}

		if (this.isSpawned() && !this.isDead())
		{
			ZoneManager.getInstance().getRegion(this).removeFromZones(this);
			this.decayMe();
		}

		if (this._decoyLifeTask != null)
		{
			this._decoyLifeTask.cancel(false);
			this._decoyLifeTask = null;
		}
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		this.sendPacket(new CharInfo(this, false));
	}

	@Override
	public void updateAbnormalVisualEffects()
	{
		World.getInstance().forEachVisibleObject(this, Player.class, player -> {
			if (this.isVisibleFor(player))
			{
				player.sendPacket(new CharInfo(this, this.isInvisible() && player.isGM()));
			}
		});
	}

	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancel(this);
	}

	@Override
	public void onDecay()
	{
		this.deleteMe(this._owner);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return this._owner.isAutoAttackable(attacker);
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
	public int getId()
	{
		return this.getTemplate().getId();
	}

	@Override
	public int getLevel()
	{
		return this.getTemplate().getLevel();
	}

	public void deleteMe(Player owner)
	{
		this.decayMe();
	}

	public Player getOwner()
	{
		return this._owner;
	}

	@Override
	public Player asPlayer()
	{
		return this._owner;
	}

	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) super.getTemplate();
	}

	@Override
	public void sendInfo(Player player)
	{
		player.sendPacket(new CharInfo(this, this.isInvisible() && player.isGM()));
	}

	@Override
	public void sendPacket(ServerPacket packet)
	{
		if (this._owner != null)
		{
			this._owner.sendPacket(packet);
		}
	}

	@Override
	public void sendPacket(SystemMessageId id)
	{
		if (this._owner != null)
		{
			this._owner.sendPacket(id);
		}
	}

	private static class HateSpam implements Runnable
	{
		private final Decoy _player;
		private final Skill _skill;

		HateSpam(Decoy player, Skill hate)
		{
			this._player = player;
			this._skill = hate;
		}

		@Override
		public void run()
		{
			try
			{
				this._player.setTarget(this._player);
				this._player.doCast(this._skill);
			}
			catch (Throwable var2)
			{
				Creature.LOGGER.log(Level.SEVERE, "Decoy Error: ", var2);
			}
		}
	}
}
