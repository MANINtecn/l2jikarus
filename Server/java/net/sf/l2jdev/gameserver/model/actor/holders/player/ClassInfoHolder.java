package net.sf.l2jdev.gameserver.model.actor.holders.player;

import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;

public class ClassInfoHolder
{
	private final PlayerClass _playerClass;
	private final PlayerClass _parentClass;
	private final String _className;

	public ClassInfoHolder(PlayerClass playerClass, PlayerClass parentClass, String className)
	{
		this._playerClass = playerClass;
		this._parentClass = parentClass;
		this._className = className;
	}

	public PlayerClass getPlayerClass()
	{
		return this._playerClass;
	}

	public PlayerClass getParentClass()
	{
		return this._parentClass;
	}

	public String getClassName()
	{
		return this._className;
	}
}
