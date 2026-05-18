package net.sf.l2jdev.gameserver.model.item.holders;

import java.util.function.Function;

import net.sf.l2jdev.gameserver.model.ArmorSet;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;

public class ArmorsetSkillHolder extends SkillHolder
{
	private final int _minimumPieces;
	private final int _minEnchant;
	private final int _artifactSlotMask;
	private final int _artifactBookSlot;
	private final boolean _isOptional;

	public ArmorsetSkillHolder(int skillId, int skillLevel, int minimumPieces, int minEnchant, boolean isOptional, int artifactSlotMask, int artifactBookSlot)
	{
		super(skillId, skillLevel);
		this._minimumPieces = minimumPieces;
		this._minEnchant = minEnchant;
		this._isOptional = isOptional;
		this._artifactSlotMask = artifactSlotMask;
		this._artifactBookSlot = artifactBookSlot;
	}

	public int getMinimumPieces()
	{
		return this._minimumPieces;
	}

	public int getMinEnchant()
	{
		return this._minEnchant;
	}

	public boolean isOptional()
	{
		return this._isOptional;
	}

	public boolean validateConditions(Playable playable, ArmorSet armorSet, Function<Item, Integer> idProvider)
	{
		if (this._artifactSlotMask > armorSet.getArtifactSlotMask(playable, this._artifactBookSlot))
		{
			return false;
		}
		else if (this._minimumPieces > armorSet.getPieceCount(playable, idProvider))
		{
			return false;
		}
		else if (this._minEnchant > armorSet.getSetEnchant(playable))
		{
			return false;
		}
		else
		{
			return this._isOptional && !armorSet.hasOptionalEquipped(playable, idProvider) ? false : playable.getSkillLevel(this.getSkillId()) != this.getSkillLevel();
		}
	}
}
