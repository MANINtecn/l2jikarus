package net.sf.l2jdev.gameserver.model.variables;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;

public class NpcVariables extends StatSet
{
	@Override
	public int getInt(String key)
	{
		return super.getInt(key, 0);
	}

	public boolean hasVariable(String name)
	{
		return this.getSet().containsKey(name);
	}

	public Player getPlayer(String name)
	{
		return this.getObject(name, Player.class);
	}

	public Summon getSummon(String name)
	{
		return this.getObject(name, Summon.class);
	}
}
