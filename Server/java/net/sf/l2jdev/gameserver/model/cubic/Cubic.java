package net.sf.l2jdev.gameserver.model.cubic;

import java.util.Comparator;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Stream;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.CubicTemplate;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExDamagePopUp;
import net.sf.l2jdev.gameserver.network.serverpackets.ExMagicAttackInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ExUserInfoCubic;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class Cubic extends Creature
{
	private final Player _owner;
	private final Player _caster;
	private final CubicTemplate _template;
	private ScheduledFuture<?> _skillUseTask;
	private ScheduledFuture<?> _expireTask;

	public Cubic(Player owner, Player caster, CubicTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Cubic);
		this._owner = owner;
		this._caster = caster == null ? owner : caster;
		this._template = template;
		this.activate();
	}

	private void activate()
	{
		this._skillUseTask = ThreadPool.scheduleAtFixedRate(this::readyToUseSkill, 0L, this._template.getDelay() * 1000);
		this._expireTask = ThreadPool.schedule(this::deactivate, this._template.getDuration() * 1000);
	}

	public void deactivate()
	{
		if (this._skillUseTask != null && !this._skillUseTask.isDone())
		{
			this._skillUseTask.cancel(true);
		}

		this._skillUseTask = null;
		if (this._expireTask != null && !this._expireTask.isDone())
		{
			this._expireTask.cancel(true);
		}

		this._expireTask = null;
		this._owner.getCubics().remove(this._template.getId());
		this._owner.sendPacket(new ExUserInfoCubic(this._owner));
		this._owner.broadcastCharInfo();
	}

	private void readyToUseSkill()
	{
		switch (this._template.getTargetType())
		{
			case TARGET:
				this.actionToCurrentTarget();
				break;
			case BY_SKILL:
				this.actionToTargetBySkill();
				break;
			case HEAL:
				this.actionHeal();
				break;
			case MASTER:
				this.actionToMaster();
		}
	}

	private CubicSkill chooseSkill()
	{
		double random = Rnd.nextDouble() * 100.0;
		double commulativeChance = 0.0;

		for (CubicSkill cubicSkill : this._template.getCubicSkills())
		{
			if ((commulativeChance += cubicSkill.getTriggerRate()) > random)
			{
				return cubicSkill;
			}
		}

		return null;
	}

	private void actionToCurrentTarget()
	{
		CubicSkill skill = this.chooseSkill();
		WorldObject target = this._owner.getTarget();
		if (skill != null && target != null)
		{
			this.tryToUseSkill(target, skill);
		}
	}

	private void actionToTargetBySkill()
	{
		CubicSkill skill = this.chooseSkill();
		if (skill != null)
		{
			switch (skill.getTargetType())
			{
				case TARGET:
					WorldObject target = this._owner.getTarget();
					if (target != null)
					{
						this.tryToUseSkill(target, skill);
					}
				case BY_SKILL:
				default:
					break;
				case HEAL:
					this.actionHeal();
					break;
				case MASTER:
					this.tryToUseSkill(this._owner, skill);
			}
		}
	}

	private void actionHeal()
	{
		double random = Rnd.nextDouble() * 100.0;
		double commulativeChance = 0.0;

		for (CubicSkill cubicSkill : this._template.getCubicSkills())
		{
			if ((commulativeChance += cubicSkill.getTriggerRate()) > random)
			{
				Skill skill = cubicSkill.getSkill();
				if (skill != null && Rnd.get(100) < cubicSkill.getSuccessRate())
				{
					Party party = this._owner.getParty();
					Stream<Creature> stream;
					if (party != null)
					{
						stream = World.getInstance().getVisibleObjectsInRange(this._owner, Creature.class, PlayerConfig.ALT_PARTY_RANGE, c -> c.getParty() == party && this._template.validateConditions(this, this._owner, c) && cubicSkill.validateConditions(this, this._owner, c)).stream();
					}
					else
					{
						stream = this._owner.getServitorsAndPets().stream().filter(summon -> this._template.validateConditions(this, this._owner, summon) && cubicSkill.validateConditions(this, this._owner, summon)).map(Creature.class::cast);
					}

					if (this._template.validateConditions(this, this._owner, this._owner) && cubicSkill.validateConditions(this, this._owner, this._owner))
					{
						stream = Stream.concat(stream, Stream.of(this._owner));
					}

					Creature target = stream.sorted(Comparator.comparingInt(Creature::getCurrentHpPercent)).findFirst().orElse(null);
					if (target != null && !target.isDead())
					{
						if (Rnd.nextDouble() > target.getCurrentHp() / target.getMaxHp())
						{
							this.activateCubicSkill(skill, target);
						}
						break;
					}
				}
			}
		}
	}

	private void actionToMaster()
	{
		CubicSkill skill = this.chooseSkill();
		if (skill != null)
		{
			this.tryToUseSkill(this._owner, skill);
		}
	}

	private void tryToUseSkill(WorldObject worldObject, CubicSkill cubicSkill)
	{
		WorldObject target = worldObject;
		Skill skill = cubicSkill.getSkill();
		if (this._template.getTargetType() != CubicTargetType.MASTER && (this._template.getTargetType() != CubicTargetType.BY_SKILL || cubicSkill.getTargetType() != CubicTargetType.MASTER))
		{
			target = skill.getTarget(this._owner, worldObject, false, false, false);
		}

		if (target != null)
		{
			if (target.isDoor() && !cubicSkill.canUseOnStaticObjects())
			{
				return;
			}

			if (this._template.validateConditions(this, this._owner, target) && cubicSkill.validateConditions(this, this._owner, target) && Rnd.get(100) < cubicSkill.getSuccessRate())
			{
				this.activateCubicSkill(skill, target);
			}
		}
	}

	private void activateCubicSkill(Skill skill, WorldObject target)
	{
		if (!this._owner.hasSkillReuse(skill.getReuseHashCode()))
		{
			this._caster.broadcastSkillPacket(new MagicSkillUse(this._owner, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), skill.getReuseDelay()), target);
			skill.activateSkill(this, target);
			this._owner.addTimeStamp(skill, skill.getReuseDelay());
		}
	}

	@Override
	public void sendDamageMessage(Creature target, Skill skill, int damage, double elementalDamage, boolean crit, boolean miss, boolean elementalCrit)
	{
		if (!miss && this._owner != null)
		{
			if (this._owner.isInOlympiadMode() && target.isPlayer() && target.asPlayer().isInOlympiadMode() && target.asPlayer().getOlympiadGameId() == this._owner.getOlympiadGameId())
			{
				OlympiadGameManager.getInstance().notifyCompetitorDamage(this._owner, damage);
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
				sm.addPopup(target.getObjectId(), this._owner.getObjectId(), damage * -1);
				this._owner.sendPacket(sm);
			}
		}
		else
		{
			this.sendPacket(new ExDamagePopUp(this.getObjectId(), target.getObjectId(), 0, (byte) 11));
		}
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
	public Player asPlayer()
	{
		return this._owner;
	}

	public Creature getCaster()
	{
		return this._caster;
	}

	public boolean isGivenByOther()
	{
		return this._caster != this._owner;
	}

	@Override
	public String getName()
	{
		return this._owner.getName();
	}

	@Override
	public int getLevel()
	{
		return this._owner.getLevel();
	}

	@Override
	public int getX()
	{
		return this._owner.getX();
	}

	@Override
	public int getY()
	{
		return this._owner.getY();
	}

	@Override
	public int getZ()
	{
		return this._owner.getZ();
	}

	@Override
	public int getHeading()
	{
		return this._owner.getHeading();
	}

	@Override
	public int getInstanceId()
	{
		return this._owner.getInstanceId();
	}

	@Override
	public boolean isInInstance()
	{
		return this._owner.isInInstance();
	}

	@Override
	public Instance getInstanceWorld()
	{
		return this._owner.getInstanceWorld();
	}

	@Override
	public Location getLocation()
	{
		return this._owner.getLocation();
	}

	@Override
	public double getRandomDamageMultiplier()
	{
		int random = (int) this._owner.getStat().getValue(Stat.RANDOM_DAMAGE);
		return 1.0 + Rnd.get(-random, random) / 100.0;
	}

	@Override
	public int getMagicAccuracy()
	{
		return this._owner.getMagicAccuracy();
	}

	@Override
	public CubicTemplate getTemplate()
	{
		return this._template;
	}

	@Override
	public int getId()
	{
		return this._template.getId();
	}

	@Override
	public int getPAtk()
	{
		return this._template.getBasePAtk();
	}

	@Override
	public int getMAtk()
	{
		return this._template.getBaseMAtk();
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
	public ItemTemplate getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}

	@Override
	public boolean spawnMe()
	{
		return true;
	}

	@Override
	public void onSpawn()
	{
	}

	@Override
	public boolean deleteMe()
	{
		return true;
	}

	@Override
	public boolean decayMe()
	{
		return true;
	}

	@Override
	public void onDecay()
	{
	}

	@Override
	public synchronized void onTeleported()
	{
	}

	@Override
	public void sendInfo(Player player)
	{
	}

	@Override
	public boolean isInvul()
	{
		return this._owner.isInvul();
	}

	@Override
	public boolean isTargetable()
	{
		return false;
	}

	@Override
	public boolean isUndying()
	{
		return true;
	}

	@Override
	public boolean isPlayer()
	{
		return true;
	}

	@Override
	public boolean isCubic()
	{
		return true;
	}
}
