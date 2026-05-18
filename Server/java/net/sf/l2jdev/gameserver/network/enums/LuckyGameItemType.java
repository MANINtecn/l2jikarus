package net.sf.l2jdev.gameserver.network.enums;

public enum LuckyGameItemType
{
	COMMON(1),
	UNIQUE(2),
	RARE(3);

	private final int _clientId;

	private LuckyGameItemType(int clientId)
	{
		this._clientId = clientId;
	}

	public int getClientId()
	{
		return this._clientId;
	}
}
