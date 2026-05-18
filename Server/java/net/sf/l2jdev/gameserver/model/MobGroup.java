package net.sf.l2jdev.gameserver.model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.ai.ControllableMobAI;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.data.SpawnTable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.ControllableMob;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;

public class MobGroup
{
	private final NpcTemplate _npcTemplate;
	private final int _groupId;
	private final int _maxMobCount;
	private Set<ControllableMob> _mobs;

	public MobGroup(int groupId, NpcTemplate npcTemplate, int maxMobCount)
	{
		this._groupId = groupId;
		this._npcTemplate = npcTemplate;
		this._maxMobCount = maxMobCount;
	}

	public int getActiveMobCount()
	{
		return this.getMobs().size();
	}

	public int getGroupId()
	{
		return this._groupId;
	}

	public int getMaxMobCount()
	{
		return this._maxMobCount;
	}

	public Set<ControllableMob> getMobs()
	{
		if (this._mobs == null)
		{
			this._mobs = ConcurrentHashMap.newKeySet();
		}

		return this._mobs;
	}

	public String getStatus()
	{
		try
		{
			ControllableMobAI mobGroupAI = (ControllableMobAI) this.getMobs().stream().findFirst().get().getAI();
			switch (mobGroupAI.getAlternateAI())
			{
				case 2:
					return "Idle";
				case 3:
					return "Force Attacking";
				case 4:
					return "Following";
				case 5:
					return "Casting";
				case 6:
					return "Attacking Group";
				default:
					return "Idle";
			}
		}
		catch (Exception var2)
		{
			return "Unspawned";
		}
	}

	public NpcTemplate getTemplate()
	{
		return this._npcTemplate;
	}

	public boolean isGroupMember(ControllableMob mobInst)
	{
		for (ControllableMob groupMember : this.getMobs())
		{
			if (groupMember != null && groupMember.getObjectId() == mobInst.getObjectId())
			{
				return true;
			}
		}

		return false;
	}

	public void spawnGroup(int x, int y, int z)
	{
		if (this.getMobs().isEmpty())
		{
			try
			{
				for (int i = 0; i < this._maxMobCount; i++)
				{
					GroupSpawn spawn = new GroupSpawn(this._npcTemplate);
					int signX = Rnd.nextBoolean() ? -1 : 1;
					int signY = Rnd.nextBoolean() ? -1 : 1;
					int randX = Rnd.get(300);
					int randY = Rnd.get(300);
					spawn.setXYZ(x + signX * randX, y + signY * randY, z);
					spawn.stopRespawn();
					SpawnTable.getInstance().addSpawn(spawn);
					this.getMobs().add((ControllableMob) spawn.doGroupSpawn());
				}
			}
			catch (NoSuchMethodException | ClassNotFoundException var10)
			{
			}
		}
	}

	public void spawnGroup(Player player)
	{
		this.spawnGroup(player.getX(), player.getY(), player.getZ());
	}

	public void teleportGroup(Player player)
	{
		this.removeDead();

		for (ControllableMob mobInst : this.getMobs())
		{
			if (mobInst != null && !mobInst.isDead())
			{
				int x = player.getX() + Rnd.get(50);
				int y = player.getY() + Rnd.get(50);
				mobInst.teleToLocation(new Location(x, y, player.getZ()), true);
				((ControllableMobAI) mobInst.getAI()).follow(player);
			}
		}
	}

	public ControllableMob getRandomMob()
	{
		this.removeDead();
		if (this.getMobs().isEmpty())
		{
			return null;
		}
		int choice = Rnd.get(this.getMobs().size());

		for (ControllableMob mob : this.getMobs())
		{
			if (--choice == 0)
			{
				return mob;
			}
		}

		return null;
	}

	public void unspawnGroup()
	{
		this.removeDead();
		if (!this.getMobs().isEmpty())
		{
			for (ControllableMob mobInst : this.getMobs())
			{
				if (mobInst != null)
				{
					if (!mobInst.isDead())
					{
						mobInst.deleteMe();
					}

					SpawnTable.getInstance().removeSpawn(mobInst.getSpawn());
				}
			}

			this.getMobs().clear();
		}
	}

	public void killGroup(Player player)
	{
		this.removeDead();

		for (ControllableMob mobInst : this.getMobs())
		{
			if (mobInst != null)
			{
				if (!mobInst.isDead())
				{
					mobInst.reduceCurrentHp(mobInst.getMaxHp() + 1L, player, null);
				}

				SpawnTable.getInstance().removeSpawn(mobInst.getSpawn());
			}
		}

		this.getMobs().clear();
	}

	public void setAttackRandom()
	{
		this.removeDead();

		for (ControllableMob mobInst : this.getMobs())
		{
			if (mobInst != null)
			{
				ControllableMobAI ai = (ControllableMobAI) mobInst.getAI();
				ai.setAlternateAI(2);
				ai.setIntention(Intention.ACTIVE);
			}
		}
	}

	public void setAttackTarget(Creature target)
	{
		this.removeDead();

		for (ControllableMob mobInst : this.getMobs())
		{
			if (mobInst != null)
			{
				((ControllableMobAI) mobInst.getAI()).forceAttack(target);
			}
		}
	}

	public void setIdleMode()
	{
		this.removeDead();

		for (ControllableMob mobInst : this.getMobs())
		{
			if (mobInst != null)
			{
				((ControllableMobAI) mobInst.getAI()).stop();
			}
		}
	}

	public void returnGroup(Creature creature)
	{
		this.setIdleMode();

		for (ControllableMob mobInst : this.getMobs())
		{
			if (mobInst != null)
			{
				int signX = Rnd.nextBoolean() ? -1 : 1;
				int signY = Rnd.nextBoolean() ? -1 : 1;
				int randX = Rnd.get(300);
				int randY = Rnd.get(300);
				ControllableMobAI ai = (ControllableMobAI) mobInst.getAI();
				ai.move(creature.getX() + signX * randX, creature.getY() + signY * randY, creature.getZ());
			}
		}
	}

	public void setFollowMode(Creature creature)
	{
		this.removeDead();

		for (ControllableMob mobInst : this.getMobs())
		{
			if (mobInst != null)
			{
				((ControllableMobAI) mobInst.getAI()).follow(creature);
			}
		}
	}

	public void setCastMode()
	{
		this.removeDead();

		for (ControllableMob mobInst : this.getMobs())
		{
			if (mobInst != null)
			{
				((ControllableMobAI) mobInst.getAI()).setAlternateAI(5);
			}
		}
	}

	public void setNoMoveMode(boolean enabled)
	{
		this.removeDead();

		for (ControllableMob mobInst : this.getMobs())
		{
			if (mobInst != null)
			{
				((ControllableMobAI) mobInst.getAI()).setNotMoving(enabled);
			}
		}
	}

	protected void removeDead()
	{
		this.getMobs().removeIf(Creature::isDead);
	}

	public void setInvul(boolean invulState)
	{
		this.removeDead();

		for (ControllableMob mobInst : this.getMobs())
		{
			if (mobInst != null)
			{
				mobInst.setInvul(invulState);
			}
		}
	}

	public void setAttackGroup(MobGroup otherGrp)
	{
		this.removeDead();

		for (ControllableMob mobInst : this.getMobs())
		{
			if (mobInst != null)
			{
				ControllableMobAI ai = (ControllableMobAI) mobInst.getAI();
				ai.forceAttackGroup(otherGrp);
				ai.setIntention(Intention.ACTIVE);
			}
		}
	}
}
