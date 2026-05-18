package net.sf.l2jdev.gameserver.model.ensoul;

import java.util.ArrayList;
import java.util.List;

public class EnsoulStone
{
	private final int _id;
	private final int _slotType;
	private final List<Integer> _options = new ArrayList<>();

	public EnsoulStone(int id, int slotType)
	{
		this._id = id;
		this._slotType = slotType;
	}

	public int getId()
	{
		return this._id;
	}

	public int getSlotType()
	{
		return this._slotType;
	}

	public List<Integer> getOptions()
	{
		return this._options;
	}

	public void addOption(int option)
	{
		this._options.add(option);
	}
}
