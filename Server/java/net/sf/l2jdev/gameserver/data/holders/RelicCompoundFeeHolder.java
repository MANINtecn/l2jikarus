package net.sf.l2jdev.gameserver.data.holders;

import net.sf.l2jdev.gameserver.model.actor.enums.player.RelicGrade;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class RelicCompoundFeeHolder extends ItemHolder
{
	private final RelicGrade _grade;

	public RelicCompoundFeeHolder(RelicGrade grade, int id, long count)
	{
		super(id, count);
		this._grade = grade;
	}

	public RelicGrade getFeeGrade()
	{
		return this._grade;
	}
}
