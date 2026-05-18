package net.sf.l2jdev.gameserver.model.actor.holders.player;

import net.sf.l2jdev.gameserver.model.actor.enums.player.ShortcutType;

public class Shortcut
{
	private final int _slot;
	private final int _page;
	private final ShortcutType _type;
	private final int _id;
	private final int _level;
	private final int _subLevel;
	private final int _characterType;
	private int _sharedReuseGroup = -1;
	private boolean _autoUse = false;

	public Shortcut(int slot, int page, ShortcutType type, int id, int level, int subLevel, int characterType)
	{
		this._slot = slot;
		this._page = page;
		this._type = type;
		this._id = id;
		this._level = level;
		this._subLevel = subLevel;
		this._characterType = characterType;
	}

	public int getId()
	{
		return this._id;
	}

	public int getLevel()
	{
		return this._level;
	}

	public int getSubLevel()
	{
		return this._subLevel;
	}

	public int getPage()
	{
		return this._page;
	}

	public int getSlot()
	{
		return this._slot;
	}

	public ShortcutType getType()
	{
		return this._type;
	}

	public int getCharacterType()
	{
		return this._characterType;
	}

	public int getSharedReuseGroup()
	{
		return this._sharedReuseGroup;
	}

	public void setSharedReuseGroup(int sharedReuseGroup)
	{
		this._sharedReuseGroup = sharedReuseGroup;
	}

	public boolean isAutoUse()
	{
		return this._autoUse;
	}

	public void setAutoUse(boolean value)
	{
		this._autoUse = value;
	}
}
