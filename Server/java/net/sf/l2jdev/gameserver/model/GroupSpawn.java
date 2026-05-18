package net.sf.l2jdev.gameserver.model;

import java.util.logging.Level;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.instance.ControllableMob;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;

public class GroupSpawn extends Spawn
{
	private final NpcTemplate _template;

	public GroupSpawn(NpcTemplate mobTemplate) throws ClassNotFoundException, NoSuchMethodException
	{
		super(mobTemplate);
		this._template = mobTemplate;
		this.setAmount(1);
	}

	public Npc doGroupSpawn()
	{
		try
		{
			if (!this._template.isType("Pet") && !this._template.isType("Minion"))
			{
				int newlocx = 0;
				int newlocy = 0;
				int newlocz = 0;
				if (this.getX() != 0 || this.getY() != 0)
				{
					newlocx = this.getX();
					newlocy = this.getY();
					newlocz = this.getZ();
					Npc mob = new ControllableMob(this._template);
					mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
					mob.setHeading(this.getHeading() == -1 ? Rnd.get(61794) : this.getHeading());
					mob.setSpawn(this);
					mob.spawnMe(newlocx, newlocy, newlocz);
					return mob;
				}
				return this.getLocationId() == 0 ? null : null;
			}
			return null;
		}
		catch (Exception var5)
		{
			LOGGER.log(Level.WARNING, "NPC class not found: " + var5.getMessage(), var5);
			return null;
		}
	}
}
