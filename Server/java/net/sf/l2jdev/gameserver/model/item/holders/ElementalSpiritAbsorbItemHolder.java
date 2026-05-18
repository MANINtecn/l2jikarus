package net.sf.l2jdev.gameserver.model.item.holders;

public class ElementalSpiritAbsorbItemHolder
{
	private final int _id;
	private final int _experience;

	public ElementalSpiritAbsorbItemHolder(int itemId, int experience)
	{
		this._id = itemId;
		this._experience = experience;
	}

	public int getId()
	{
		return this._id;
	}

	public int getExperience()
	{
		return this._experience;
	}
}
