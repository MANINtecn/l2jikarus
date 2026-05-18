package net.sf.l2jdev.gameserver.model.options;

public class Variation
{
	private final int _mineralId;
	private final OptionDataGroup[] _effects = new OptionDataGroup[3];
	private int _itemGroup = -1;

	public Variation(int mineralId, int itemGroup)
	{
		this._mineralId = mineralId;
		this._itemGroup = itemGroup;
	}

	public int getMineralId()
	{
		return this._mineralId;
	}

	public int getItemGroup()
	{
		return this._itemGroup;
	}

	public OptionDataGroup[] getOptionDataGroup()
	{
		return this._effects;
	}

	public void setEffectGroup(int order, OptionDataGroup group)
	{
		this._effects[order] = group;
	}

	public Options getRandomEffect(int order, int targetItemId)
	{
		return this._effects[order] == null ? null : this._effects[order].getRandomEffect(targetItemId);
	}
}
