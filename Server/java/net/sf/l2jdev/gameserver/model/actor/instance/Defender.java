package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class Defender extends Attackable
{
	private Castle _castle = null;
	private Fort _fort = null;

	public Defender(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Defender);
	}

	@Override
	public void addDamage(Creature attacker, int damage, Skill skill)
	{
		super.addDamage(attacker, damage, skill);
		World.getInstance().forEachVisibleObjectInRange(this, Defender.class, 500, defender -> defender.addDamageHate(attacker, 0L, 10L));
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		if (!attacker.isPlayable())
		{
			return false;
		}
		Player player = attacker.asPlayer();
		if (this._fort != null && this._fort.getZone().isActive() || this._castle != null && this._castle.getZone().isActive())
		{
			int activeSiegeId = this._fort != null ? this._fort.getResidenceId() : this._castle.getResidenceId();
			if (player != null && (player.getSiegeState() == 2 && !player.isRegisteredOnThisSiegeField(activeSiegeId) || player.getSiegeState() == 1 || player.getSiegeState() == 0))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public void returnHome()
	{
		if (!(this.getWalkSpeed() <= 0.0))
		{
			if (this.getSpawn() != null)
			{
				if (!this.isInsideRadius2D(this.getSpawn(), 40))
				{
					this.clearAggroList();
					if (this.hasAI())
					{
						this.getAI().setIntention(Intention.MOVE_TO, this.getSpawn().getLocation());
					}
				}
			}
		}
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		this._fort = net.sf.l2jdev.gameserver.managers.FortManager.getInstance().getFort(this.getX(), this.getY(), this.getZ());
		this._castle = CastleManager.getInstance().getCastle(this.getX(), this.getY(), this.getZ());
		if (this._fort == null && this._castle == null)
		{
			LOGGER.warning("Defender spawned outside of Fortress or Castle zone!" + this);
		}
	}

	@Override
	public void onAction(Player player, boolean interact)
	{
		if (!this.canTarget(player))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			if (this != player.getTarget())
			{
				player.setTarget(this);
			}
			else if (interact)
			{
				if (this.isAutoAttackable(player) && !this.isAlikeDead() && Math.abs(player.getZ() - this.getZ()) < 600)
				{
					player.getAI().setIntention(Intention.ATTACK, this);
				}

				if (!this.isAutoAttackable(player) && !this.canInteract(player))
				{
					player.getAI().setIntention(Intention.INTERACT, this);
				}
			}

			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	@Override
	public void useMagic(Skill skill)
	{
		if (!skill.hasNegativeEffect())
		{
			Creature target = this;
			double lowestHpValue = Double.MAX_VALUE;

			for (Creature nearby : World.getInstance().getVisibleObjectsInRange(this, Creature.class, skill.getCastRange()))
			{
				if (nearby != null && !nearby.isDead() && GeoEngine.getInstance().canSeeTarget(this, nearby))
				{
					if (nearby instanceof Defender)
					{
						double targetHp = nearby.getCurrentHp();
						if (lowestHpValue > targetHp)
						{
							target = nearby;
							lowestHpValue = targetHp;
						}
					}
					else if (nearby.isPlayer())
					{
						Player player = nearby.asPlayer();
						if (player.getSiegeState() == 2 && !player.isRegisteredOnThisSiegeField(this.getScriptValue()))
						{
							double targetHp = nearby.getCurrentHp();
							if (lowestHpValue > targetHp)
							{
								target = nearby;
								lowestHpValue = targetHp;
							}
						}
					}
				}
			}

			this.setTarget(target);
		}

		super.useMagic(skill);
	}

	@Override
	public void addDamageHate(Creature attacker, long damage, long aggro)
	{
		if (attacker != null)
		{
			if (!(attacker instanceof Defender))
			{
				if (damage == 0L && aggro <= 1L && attacker.isPlayable())
				{
					Player player = attacker.asPlayer();
					if (this._fort != null && this._fort.getZone().isActive() || this._castle != null && this._castle.getZone().isActive())
					{
						int activeSiegeId = this._fort != null ? this._fort.getResidenceId() : this._castle.getResidenceId();
						if (player.getSiegeState() == 2 && player.isRegisteredOnThisSiegeField(activeSiegeId))
						{
							return;
						}
					}
				}

				super.addDamageHate(attacker, damage, aggro);
			}
		}
	}
}
