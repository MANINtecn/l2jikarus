package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;

public class GrandBoss extends Monster
{
	private boolean _useRaidCurse = true;

	public GrandBoss(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.GrandBoss);
		this.setIsRaid(true);
		this.setLethalable(false);
	}

	@Override
	public void onSpawn()
	{
		this.setRandomWalking(false);
		super.onSpawn();
	}

	@Override
	public int getVitalityPoints(int level, double exp, boolean isBoss)
	{
		return -super.getVitalityPoints(level, exp, isBoss);
	}

	@Override
	public boolean useVitalityRate()
	{
		return false;
	}

	public void setUseRaidCurse(boolean value)
	{
		this._useRaidCurse = value;
	}

	@Override
	public boolean giveRaidCurse()
	{
		return this._useRaidCurse;
	}
}
