package net.sf.l2jdev.gameserver.model.script;

import net.sf.l2jdev.gameserver.model.actor.Player;

public abstract class Event extends Script
{
	public abstract boolean eventStart(Player var1);

	public abstract boolean eventStop();

	public abstract boolean eventBypass(Player var1, String var2);
}
