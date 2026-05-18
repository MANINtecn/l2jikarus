package net.sf.l2jdev.gameserver.model.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.enums.ShotType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class SkillChannelizer implements Runnable
{
	private static final Logger LOGGER = Logger.getLogger(SkillChannelizer.class.getName());
	private final Creature _channelizer;
	private List<Creature> _channelized;
	private Skill _skill;
	private ScheduledFuture<?> _task = null;

	public SkillChannelizer(Creature channelizer)
	{
		this._channelizer = channelizer;
	}

	public Creature getChannelizer()
	{
		return this._channelizer;
	}

	public List<Creature> getChannelized()
	{
		return this._channelized;
	}

	public boolean hasChannelized()
	{
		return this._channelized != null;
	}

	public void startChanneling(Skill skill)
	{
		if (this.isChanneling())
		{
			LOGGER.warning("Character: " + this.toString() + " is attempting to channel skill but he already does!");
		}
		else
		{
			this._skill = skill;
			this._task = ThreadPool.scheduleAtFixedRate(this, skill.getChannelingTickInitialDelay(), skill.getChannelingTickInterval());
		}
	}

	public void stopChanneling()
	{
		if (!this.isChanneling())
		{
			LOGGER.warning("Character: " + this.toString() + " is attempting to stop channel skill but he does not!");
		}
		else
		{
			this._task.cancel(false);
			this._task = null;
			if (this._channelized != null)
			{
				for (Creature creature : this._channelized)
				{
					creature.getSkillChannelized().removeChannelizer(this._skill.getChannelingSkillId(), this._channelizer);
				}

				this._channelized = null;
			}

			this._skill = null;
		}
	}

	public Skill getSkill()
	{
		return this._skill;
	}

	public boolean isChanneling()
	{
		return this._task != null;
	}

	@Override
	public void run()
	{
		if (this.isChanneling())
		{
			Skill skill = this._skill;
			List<Creature> channelized = this._channelized;

			try
			{
				if (skill.getMpPerChanneling() > 0)
				{
					if (this._channelizer.getCurrentMp() < skill.getMpPerChanneling())
					{
						if (this._channelizer.isPlayer())
						{
							this._channelizer.sendPacket(SystemMessageId.YOUR_SKILL_WAS_DEACTIVATED_DUE_TO_LACK_OF_MP);
						}

						this._channelizer.abortCast();
						return;
					}

					this._channelizer.reduceCurrentMp(skill.getMpPerChanneling());
				}

				List<Creature> targetList = new ArrayList<>();
				WorldObject target = skill.getTarget(this._channelizer, false, false, false);
				if (target != null)
				{
					skill.forEachTargetAffected(this._channelizer, target, o -> {
						if (o.isCreature())
						{
							targetList.add(o.asCreature());
							o.asCreature().getSkillChannelized().addChannelizer(skill.getChannelingSkillId(), this._channelizer);
						}
					});
				}

				if (targetList.isEmpty())
				{
					return;
				}

				channelized = targetList;

				for (Creature creature : targetList)
				{
					if (LocationUtil.checkIfInRange(skill.getEffectRange(), this._channelizer, creature, true) && GeoEngine.getInstance().canSeeTarget(this._channelizer, creature))
					{
						if (skill.getChannelingSkillId() > 0)
						{
							int maxSkillLevel = SkillData.getInstance().getMaxLevel(skill.getChannelingSkillId());
							int skillLevel = Math.min(creature.getSkillChannelized().getChannerlizersSize(skill.getChannelingSkillId()), maxSkillLevel);
							if (skillLevel == 0)
							{
								continue;
							}

							BuffInfo info = creature.getEffectList().getBuffInfoBySkillId(skill.getChannelingSkillId());
							if (info == null || info.getSkill().getLevel() < skillLevel)
							{
								Skill channeledSkill = SkillData.getInstance().getSkill(skill.getChannelingSkillId(), skillLevel);
								if (channeledSkill == null)
								{
									LOGGER.warning(this.getClass().getSimpleName() + ": Non existent channeling skill requested: " + skill);
									this._channelizer.abortCast();
									return;
								}

								if (creature.isPlayable() && this._channelizer.isPlayer())
								{
									this._channelizer.asPlayer().updatePvPStatus(creature);
								}

								channeledSkill.applyEffects(this._channelizer, creature);
							}

							if (!skill.isToggle())
							{
								this._channelizer.broadcastSkillPacket(new MagicSkillLaunched(this._channelizer, skill.getId(), skill.getLevel(), SkillCastingType.NORMAL, creature), creature);
							}
						}
						else
						{
							skill.applyChannelingEffects(this._channelizer, creature);
						}

						if (skill.useSpiritShot())
						{
							this._channelizer.unchargeShot(this._channelizer.isChargedShot(ShotType.BLESSED_SPIRITSHOTS) ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS);
						}
						else
						{
							this._channelizer.unchargeShot(this._channelizer.isChargedShot(ShotType.BLESSED_SOULSHOTS) ? ShotType.BLESSED_SOULSHOTS : ShotType.SOULSHOTS);
						}

						this._channelizer.rechargeShots(skill.useSoulShot(), skill.useSpiritShot(), false);
					}
				}
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.WARNING, "Error while channelizing skill: " + skill + " channelizer: " + this._channelizer + " channelized: " + channelized + "; ", var11);
			}
		}
	}
}
