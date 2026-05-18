package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;

public class FeedableBeast extends Monster
{
	public FeedableBeast(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.FeedableBeast);
	}
}
