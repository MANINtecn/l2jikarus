package net.sf.l2jdev.gameserver.model.actor.enums.player;

public enum DamageTakenType
{
	NORMAL_DAMAGE(1),
	FALL_DAMAGE(2),
	DROWN(3),
	OTHER_DAMAGE(4),
	DAMAGE_ZONE(6),
	POISON_FIELD(8),
	TRANSFERRED_DAMAGE(9),
	REFLECTED_DAMAGE(14);

	private final int _clientId;

	private DamageTakenType(int clientId)
	{
		this._clientId = clientId;
	}

	public int getClientId()
	{
		return this._clientId;
	}
}
