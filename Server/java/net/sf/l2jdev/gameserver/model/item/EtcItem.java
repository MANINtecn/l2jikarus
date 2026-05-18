package net.sf.l2jdev.gameserver.model.item;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.model.ExtractableProduct;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.type.EtcItemType;

public class EtcItem extends ItemTemplate
{
	private String _handler;
	private EtcItemType _type;
	private List<ExtractableProduct> _extractableItems;
	private int _extractableCountMin;
	private int _extractableCountMax;
	private boolean _isInfinite;
	private boolean _isMineral = false;
	private boolean _isEnsoulStone = false;

	public EtcItem(StatSet set)
	{
		super(set);
	}

	@Override
	public void set(StatSet set)
	{
		super.set(set);
		this._type = set.getEnum("etcitem_type", EtcItemType.class, EtcItemType.NONE);
		this._type1 = 4;
		this._type2 = 5;
		if (this.isQuestItem())
		{
			this._type2 = 3;
		}
		else if (this.getId() == 57 || this.getId() == 5575)
		{
			this._type2 = 4;
		}

		this._handler = set.getString("handler", null);
		this._extractableCountMin = set.getInt("extractableCountMin", 0);
		this._extractableCountMax = set.getInt("extractableCountMax", 0);
		if (this._extractableCountMin > this._extractableCountMax)
		{
			LOGGER.warning("Item " + this + " extractableCountMin is bigger than extractableCountMax!");
		}

		this._isInfinite = set.getBoolean("is_infinite", false);
	}

	@Override
	public EtcItemType getItemType()
	{
		return this._type;
	}

	@Override
	public int getItemMask()
	{
		return this._type.mask();
	}

	@Override
	public boolean isEtcItem()
	{
		return true;
	}

	public String getHandlerName()
	{
		return this._handler;
	}

	public List<ExtractableProduct> getExtractableItems()
	{
		return this._extractableItems;
	}

	public int getExtractableCountMin()
	{
		return this._extractableCountMin;
	}

	public int getExtractableCountMax()
	{
		return this._extractableCountMax;
	}

	public boolean isInfinite()
	{
		return this._isInfinite;
	}

	@Override
	public void addCapsuledItem(ExtractableProduct extractableProduct)
	{
		if (this._extractableItems == null)
		{
			this._extractableItems = new ArrayList<>();
		}

		this._extractableItems.add(extractableProduct);
	}

	public boolean isMineral()
	{
		return this._isMineral;
	}

	public void setMineral()
	{
		this._isMineral = true;
	}

	public boolean isEnsoulStone()
	{
		return this._isEnsoulStone;
	}

	public void setEnsoulStone()
	{
		this._isEnsoulStone = true;
	}
}
