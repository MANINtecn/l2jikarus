package net.sf.l2jdev.gameserver.model.itemcontainer;

public enum InventoryBlockType
{
	NONE(-1),
	BLACKLIST(0),
	WHITELIST(1);

	private final int _clientId;

	private InventoryBlockType(int clientId)
	{
		this._clientId = clientId;
	}

	public int getClientId()
	{
		return this._clientId;
	}
}
