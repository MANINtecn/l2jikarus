package net.sf.l2jdev.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Team;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class Guardian extends Attackable
{
	private ScheduledFuture<?> _attackTask = null;
	private Creature _attackTarget = null;

	public Guardian(NpcTemplate template, Player owner, boolean isClone)
	{
		super(template);
		this.setSummoner(owner);
		if (isClone)
		{
			this.setCloneObjId(owner.getObjectId());
		}

		this.setClanId(owner.getClanId());
		this.setInstance(owner.getInstanceWorld());
		this.setXYZInvisible(owner.getX() + Rnd.get(-100, 100), owner.getY() + Rnd.get(-100, 100), owner.getZ());
		this.followSummoner(true);
		this.startAttackTask();
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		Creature summoner = this.getSummoner();
		if (summoner != null)
		{
			this.followSummoner(true);

			for (BuffInfo info : summoner.getEffectList().getBuffs())
			{
				Skill skill = info.getSkill();
				if (skill != null && !this.isAffectedBySkill(skill.getId()) && !skill.hasNegativeEffect() && skill.isContinuous())
				{
					skill.applyEffects(this, this, false, info.getAbnormalTime());
				}
			}

			for (BuffInfo infox : summoner.getEffectList().getPassives())
			{
				Skill skill = infox.getSkill();
				if (skill != null && !this.isAffectedBySkill(skill.getId()) && skill.isPassive())
				{
					this.addSkill(skill);
				}
			}
		}
		else
		{
			this.deleteMe();
		}
	}

	public void followSummoner(boolean followSummoner)
	{
		if (!this.isMoving())
		{
			Player summoner = this.getSummoner().asPlayer();
			this.setTarget(summoner);
			if (!summoner.isOnline() && !summoner.isOfflinePlay())
			{
				this.deleteMe();
			}

			if (followSummoner)
			{
				if (this.getAI().getIntention() != Intention.IDLE && this.getAI().getIntention() != Intention.ACTIVE)
				{
					this.getAI().setIntention(Intention.FOLLOW);
				}
				else
				{
					this.setRunning();
					this.getAI().setIntention(Intention.FOLLOW, this.getSummoner());
				}
			}
			else if (this.getAI().getIntention() == Intention.FOLLOW)
			{
				this.getAI().setIntention(Intention.IDLE);
			}

			this.broadcastMoveToLocation(true);
		}
	}

	public void stopAttackTask()
	{
		if (this._attackTask != null && !this._attackTask.isCancelled() && !this._attackTask.isDone())
		{
			this._attackTask.cancel(false);
			this._attackTask = null;
			this._attackTarget = null;
		}
	}

	public void startAttackTask()
	{
		this.stopAttackTask();
		this._attackTask = ThreadPool.scheduleAtFixedRate(this::thinkCombat, 1000L, 1000L);
	}

	private void thinkCombat()
	{
		if (!this.getSummoner().asPlayer().isOnline() && !this.getSummoner().asPlayer().isOfflinePlay())
		{
			this.deleteMe();
		}

		if (!this.isCastingNow())
		{
			if (!this.isControlBlocked())
			{
				Creature summoner = this.getSummoner();
				if (this.calculateDistance3D(summoner) > 400.0)
				{
					this.setTarget(summoner);
					this.getAI().setTarget(summoner);
					this.abortAttack();
					this.followSummoner(true);
					this.broadcastInfo();
				}

				if (this._attackTarget == null)
				{
					if (summoner != null && !summoner.isDead())
					{
						WorldObject target = summoner.getTarget();
						if (target == null || target.asCreature().isDead())
						{
							this._attackTarget = null;
						}
						else if (target.isCreature() && target.isAutoAttackable(summoner))
						{
							this._attackTarget = target.asCreature();
						}
					}

					if (this._attackTarget == null)
					{
						this.followSummoner(true);
						return;
					}
				}
			}
		}
	}

	@Override
	public byte getPvpFlag()
	{
		return this.getSummoner() != null ? this.getSummoner().getPvpFlag() : 0;
	}

	@Override
	public Team getTeam()
	{
		return this.getSummoner() != null ? this.getSummoner().getTeam() : Team.NONE;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return this.getSummoner() != null ? this.getSummoner().isAutoAttackable(attacker) : super.isAutoAttackable(attacker);
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, skill);
		if (this.getSummoner() != null && this.getSummoner().isPlayer() && attacker != null && !this.isDead() && !this.isHpBlocked())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_RECEIVED_S3_DAMAGE_FROM_C2);
			sm.addNpcName(this);
			sm.addString(attacker.getName());
			sm.addInt((int) damage);
			sm.addPopup(this.getObjectId(), attacker.getObjectId(), (int) (-damage));
			this.sendPacket(sm);
		}
	}

	@Override
	public Player asPlayer()
	{
		return this.getSummoner() != null ? this.getSummoner().asPlayer() : super.asPlayer();
	}

	@Override
	public boolean deleteMe()
	{
		this.getSummoner().asPlayer().removeServitor(this.getObjectId());
		this.stopAttackTask();
		return super.deleteMe();
	}

	@Override
	public void sendPacket(ServerPacket packet)
	{
		if (this.getSummoner() != null)
		{
			this.getSummoner().sendPacket(packet);
		}
	}

	@Override
	public void sendPacket(SystemMessageId id)
	{
		if (this.getSummoner() != null)
		{
			this.getSummoner().sendPacket(id);
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("(");
		sb.append(this.getId());
		sb.append(") Summoner: ");
		sb.append(this.getSummoner());
		return sb.toString();
	}
}
