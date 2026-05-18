package net.sf.l2jdev.gameserver.model.actor.holders.player;

import net.sf.l2jdev.gameserver.model.actor.enums.player.AchievementBoxStateType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.AchievementBoxType;

public class AchievementBoxInfoHolder
{
	private final int _slotId;
	private AchievementBoxStateType _boxState;
	private AchievementBoxType _boxType;

	public AchievementBoxInfoHolder(int slotId, int boxState, int boxType)
	{
		this._slotId = slotId;
		this._boxState = AchievementBoxStateType.values()[boxState];
		this._boxType = AchievementBoxType.values()[boxType];
	}

	public void setState(AchievementBoxStateType value)
	{
		this._boxState = value;
	}

	public AchievementBoxStateType getState()
	{
		return this._boxState;
	}

	public AchievementBoxType getType()
	{
		return this._boxType;
	}

	public void setType(AchievementBoxType value)
	{
		this._boxType = value;
	}

	public int getSlotId()
	{
		return this._slotId;
	}
}
