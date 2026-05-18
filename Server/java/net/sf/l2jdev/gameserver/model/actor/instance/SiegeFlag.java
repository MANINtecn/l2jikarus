package net.sf.l2jdev.gameserver.model.actor.instance;

import java.util.Objects;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.managers.FortSiegeManager;
import net.sf.l2jdev.gameserver.managers.SiegeManager;
import net.sf.l2jdev.gameserver.model.SiegeClan;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.status.SiegeFlagStatus;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.siege.Siegable;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class SiegeFlag extends Npc
{
	private final Clan _clan;
	private Siegable _siege;
	private final boolean _isAdvanced;
	private boolean _canTalk;

	public SiegeFlag(Player player, NpcTemplate template, boolean advanced)
	{
		super(template);
		this.setInstanceType(InstanceType.SiegeFlag);
		this._clan = player.getClan();
		this._canTalk = true;
		this._siege = SiegeManager.getInstance().getSiege(player.getX(), player.getY(), player.getZ());
		if (this._siege == null)
		{
			this._siege = FortSiegeManager.getInstance().getSiege(player.getX(), player.getY(), player.getZ());
		}

		if (this._clan != null && this._siege != null)
		{
			SiegeClan sc = this._siege.getAttackerClan(this._clan);
			if (sc == null)
			{
				throw new NullPointerException(this.getClass().getSimpleName() + ": Cannot find siege clan.");
			}
			sc.addFlag(this);
			this._isAdvanced = advanced;
			this.getStatus();
			this.setInvul(false);
		}
		else
		{
			throw new NullPointerException(this.getClass().getSimpleName() + ": Initialization failed.");
		}
	}

	@Override
	public boolean canBeAttacked()
	{
		return !this.isInvul();
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return !this.isInvul();
	}

	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		if (this._siege != null && this._clan != null)
		{
			SiegeClan sc = this._siege.getAttackerClan(this._clan);
			if (sc != null)
			{
				sc.removeFlag(this);
			}
		}

		return true;
	}

	@Override
	public void onForcedAttack(Player player)
	{
		this.onAction(player);
	}

	@Override
	public void onAction(Player player, boolean interact)
	{
		if (player != null && this.canTarget(player))
		{
			if (this != player.getTarget())
			{
				player.setTarget(this);
			}
			else if (interact)
			{
				if (this.isAutoAttackable(player) && Math.abs(player.getZ() - this.getZ()) < 100)
				{
					player.getAI().setIntention(Intention.ATTACK, this);
				}
				else
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
		}
	}

	public boolean isAdvancedHeadquarter()
	{
		return this._isAdvanced;
	}

	@Override
	public SiegeFlagStatus getStatus()
	{
		return (SiegeFlagStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		this.setStatus(new SiegeFlagStatus(this));
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, skill);
		if (this.canTalk() && (this.getCastle() != null && this.getCastle().getSiege().isInProgress() || this.getFort() != null && this.getFort().getSiege().isInProgress()) && this._clan != null)
		{
			this._clan.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.SIEGE_CAMP_IS_UNDER_ATTACK));
			this.setCanTalk(false);
			ThreadPool.schedule(new SiegeFlag.ScheduleTalkTask(), 20000L);
		}
	}

	void setCanTalk(boolean value)
	{
		this._canTalk = value;
	}

	private boolean canTalk()
	{
		return this._canTalk;
	}

	private class ScheduleTalkTask implements Runnable
	{
		public ScheduleTalkTask()
		{
			Objects.requireNonNull(SiegeFlag.this);
			super();
		}

		@Override
		public void run()
		{
			SiegeFlag.this.setCanTalk(true);
		}
	}
}
