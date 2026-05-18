package net.sf.l2jdev.gameserver.model;

public class KeyValuePair<K, V>
{
	private final K _key;
	private final V _value;

	public KeyValuePair(K key, V value)
	{
		this._key = key;
		this._value = value;
	}

	public K getKey()
	{
		return this._key;
	}

	public V getValue()
	{
		return this._value;
	}
}
