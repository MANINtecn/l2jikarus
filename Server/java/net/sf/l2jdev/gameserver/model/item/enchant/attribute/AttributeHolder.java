package net.sf.l2jdev.gameserver.model.item.enchant.attribute;

import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;

public class AttributeHolder
{
	private final AttributeType _type;
	private int _value;

	public AttributeHolder(AttributeType type, int value)
	{
		this._type = type;
		this._value = value;
	}

	public AttributeType getType()
	{
		return this._type;
	}

	public int getValue()
	{
		return this._value;
	}

	public void setValue(int value)
	{
		this._value = value;
	}

	public void incValue(int with)
	{
		this._value += with;
	}

	@Override
	public String toString()
	{
		return this._type.name() + " +" + this._value;
	}
}
