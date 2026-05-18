package net.sf.l2jdev.gameserver.model.actor.enums.player;

public enum PlayerAction
{
	ADMIN_COMMAND,
	ADMIN_SAVE_ZONE,
	ADMIN_SHOW_TERRITORY,
	MERCENARY_CONFIRM,
	USER_ENGAGE,
	OFFLINE_PLAY;

	private final int _mask = 1 << this.ordinal();

	public int getMask()
	{
		return this._mask;
	}
}
