package net.sf.l2jdev.gameserver.data.enums;

import java.util.Locale;

public enum CharacterStyleCategoryType
{
	APPEARANCE_WEAPON(0),
	KILL_EFFECT(1),
	CHAT_BACKGROUND(2);

	public final int _categoryId;

	private CharacterStyleCategoryType(int categoryId)
	{
		this._categoryId = categoryId;
	}

	public int getClientId()
	{
		return this._categoryId;
	}

	public static CharacterStyleCategoryType from(String s)
	{
		return valueOf(s.trim().toUpperCase(Locale.ROOT));
	}

	public static CharacterStyleCategoryType getByClientId(int clientId)
	{
		for (CharacterStyleCategoryType style : values())
		{
			if (style.getClientId() == clientId)
			{
				return style;
			}
		}

		return null;
	}
}
