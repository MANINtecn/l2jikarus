package net.sf.l2jdev.gameserver.model.actor.instance;

import java.util.Objects;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.managers.FortSiegeManager;
import net.sf.l2jdev.gameserver.model.FortSiegeSpawn;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.NpcStringId;
import net.sf.l2jdev.gameserver.network.enums.ChatType;

public class FortCommander extends Defender
{
	private boolean _canTalk;

	public FortCommander(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.FortCommander);
		this._canTalk = true;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return attacker != null && attacker.isPlayer() ? this.getFort() != null && this.getFort().getResidenceId() > 0 && this.getFort().getSiege().isInProgress() && !this.getFort().getSiege().checkIsDefender(attacker.getClan()) : false;
	}

	@Override
	public void addDamageHate(Creature attacker, long damage, long aggro)
	{
		if (attacker != null)
		{
			if (!(attacker instanceof FortCommander))
			{
				super.addDamageHate(attacker, damage, aggro);
			}
		}
	}

	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		if (this.getFort().getSiege().isInProgress())
		{
			this.getFort().getSiege().killedCommander(this);
		}

		return true;
	}

	@Override
	public void returnHome()
	{
		if (!this.isInsideRadius2D(this.getSpawn(), 200))
		{
			this.clearAggroList();
			if (this.hasAI())
			{
				this.getAI().setIntention(Intention.MOVE_TO, this.getSpawn().getLocation());
			}
		}
	}

	@Override
	public void addDamage(Creature creature, int damage, Skill skill)
	{
		Creature attacker = creature;
		Spawn spawn = this.getSpawn();
		if (spawn != null && this.canTalk())
		{
			for (FortSiegeSpawn spawn2 : FortSiegeManager.getInstance().getCommanderSpawnList(this.getFort().getResidenceId()))
			{
				if (spawn2.getId() == spawn.getId())
				{
					NpcStringId npcString = null;
					switch (spawn2.getMessageId())
					{
						case 1:
							npcString = NpcStringId.ATTACKING_THE_ENEMY_S_REINFORCEMENTS_IS_NECESSARY_TIME_TO_DIE;
							break;
						case 2:
							if (attacker.isSummon())
							{
								attacker = attacker.asSummon().getOwner();
							}

							npcString = NpcStringId.EVERYONE_CONCENTRATE_YOUR_ATTACKS_ON_S1_SHOW_THE_ENEMY_YOUR_RESOLVE;
							break;
						case 3:
							npcString = NpcStringId.FIRE_SPIRIT_UNLEASH_YOUR_POWER_BURN_THE_ENEMY;
					}

					if (npcString != null)
					{
						this.broadcastSay(ChatType.NPC_SHOUT, npcString, npcString.getParamCount() == 1 ? attacker.getName() : null);
						this.setCanTalk(false);
						ThreadPool.schedule(new FortCommander.ScheduleTalkTask(), 10000L);
					}
				}
			}
		}

		super.addDamage(attacker, damage, skill);
	}

	void setCanTalk(boolean value)
	{
		this._canTalk = value;
	}

	private boolean canTalk()
	{
		return this._canTalk;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	private class ScheduleTalkTask implements Runnable
	{
		public ScheduleTalkTask()
		{
			Objects.requireNonNull(FortCommander.this);
			super();
		}

		@Override
		public void run()
		{
			FortCommander.this.setCanTalk(true);
		}
	}
}
