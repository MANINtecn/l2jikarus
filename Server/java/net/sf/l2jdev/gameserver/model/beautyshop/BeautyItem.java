package net.sf.l2jdev.gameserver.model.beautyshop;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.StatSet;

public class BeautyItem
{
	private final int _id;
	private final int _adena;
	private final int _resetAdena;
	private final int _beautyShopTicket;
	private final Map<Integer, BeautyItem> _colors = new HashMap<>();

	public BeautyItem(StatSet set)
	{
		this._id = set.getInt("id");
		this._adena = set.getInt("adena", 0);
		this._resetAdena = set.getInt("reset_adena", 0);
		this._beautyShopTicket = set.getInt("beauty_shop_ticket", 0);
	}

	public int getId()
	{
		return this._id;
	}

	public int getAdena()
	{
		return this._adena;
	}

	public int getResetAdena()
	{
		return this._resetAdena;
	}

	public int getBeautyShopTicket()
	{
		return this._beautyShopTicket;
	}

	public void addColor(StatSet set)
	{
		BeautyItem color = new BeautyItem(set);
		this._colors.put(set.getInt("id"), color);
	}

	public Map<Integer, BeautyItem> getColors()
	{
		return this._colors;
	}
}
