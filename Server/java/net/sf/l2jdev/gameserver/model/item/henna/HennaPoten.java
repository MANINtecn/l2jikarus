package net.sf.l2jdev.gameserver.model.item.henna;

import net.sf.l2jdev.gameserver.data.xml.HennaPatternPotentialData;

public class HennaPoten
{
	private Henna _henna;
	private int _potenId;
	private int _enchantLevel = 1;
	private int _enchantExp;
	private int _slotPosition;
	private int _unlockSlot = 1;

	public void setHenna(Henna henna)
	{
		this._henna = henna;
	}

	public Henna getHenna()
	{
		return this._henna;
	}

	public void setPotenId(int val)
	{
		this._potenId = val;
	}

	public int getSlotPosition()
	{
		return this._slotPosition;
	}

	public void setSlotPosition(int val)
	{
		this._slotPosition = val;
	}

	public int getPotenId()
	{
		return this._potenId;
	}

	public void setEnchantLevel(int val)
	{
		this._enchantLevel = val;
	}

	public int getEnchantLevel()
	{
		return this._enchantLevel;
	}

	public void setEnchantExp(int val)
	{
		this._enchantExp = val;
	}

	public int getEnchantExp()
	{
		if (this._enchantExp > HennaPatternPotentialData.getInstance().getMaxPotenExp())
		{
			this._enchantExp = HennaPatternPotentialData.getInstance().getMaxPotenExp();
			return this._enchantExp;
		}
		return this._enchantExp;
	}

	public int getActiveStep()
	{
		return this._enchantExp == HennaPatternPotentialData.getInstance().getMaxPotenExp() ? Math.min(this._enchantLevel, this._unlockSlot) : Math.min(this._enchantLevel - 1, this._unlockSlot);
	}

	public int getUnlockSlot()
	{
		return this._unlockSlot;
	}

	public void setUnlockSlot(int slot)
	{
		this._unlockSlot = slot;
	}
}
