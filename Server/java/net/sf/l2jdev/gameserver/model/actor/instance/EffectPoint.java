package net.sf.l2jdev.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Team;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class EffectPoint extends Npc
{
	private final Player _owner;
	private ScheduledFuture<?> _skillTask;

	public EffectPoint(NpcTemplate template, Creature owner)
	{
		super(template);
		this.setInstanceType(InstanceType.EffectPoint);
		this.setInvul(false);
		this._owner = owner == null ? null : owner.asPlayer();
		if (owner != null)
		{
			this.setInstance(owner.getInstanceWorld());
		}

		SkillHolder skill = template.getParameters().getSkillHolder("union_skill");
		if (skill != null)
		{
			long castTime = (long) (template.getParameters().getFloat("cast_time", 0.1F) * 1000.0F);
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
		}
	}

	@Override
	public boolean deleteMe()
	{
		if (this._skillTask != null)
		{
			this._skillTask.cancel(false);
			this._skillTask = null;
		}

		return super.deleteMe();
	}

	@Override
	public Player asPlayer()
	{
		return this._owner;
	}

	@Override
	public void onAction(Player player, boolean interact)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onActionShift(Player player)
	{
		if (player != null)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	@Override
	public Party getParty()
	{
		return this._owner == null ? null : this._owner.getParty();
	}

	@Override
	public boolean isInParty()
	{
		return this._owner != null && this._owner.isInParty();
	}

	@Override
	public int getClanId()
	{
		return this._owner != null ? this._owner.getClanId() : 0;
	}

	@Override
	public int getAllyId()
	{
		return this._owner != null ? this._owner.getAllyId() : 0;
	}

	@Override
	public byte getPvpFlag()
	{
		return this._owner != null ? this._owner.getPvpFlag() : 0;
	}

	@Override
	public Team getTeam()
	{
		return this._owner != null ? this._owner.getTeam() : Team.NONE;
	}
}
