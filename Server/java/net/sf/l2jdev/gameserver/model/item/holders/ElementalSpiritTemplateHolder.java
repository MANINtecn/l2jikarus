package net.sf.l2jdev.gameserver.model.item.holders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElementalSpiritTemplateHolder
{
	private final byte _type;
	private final byte _stage;
	private final int _npcId;
	private final int _maxCharacteristics;
	private final int _extractItem;
	private final Map<Integer, ElementalSpiritTemplateHolder.SpiritLevel> _levels;
	private List<ItemHolder> _itemsToEvolve;
	private List<ElementalSpiritAbsorbItemHolder> _absorbItems;

	public ElementalSpiritTemplateHolder(byte type, byte stage, int npcId, int extractItem, int maxCharacteristics)
	{
		this._type = type;
		this._stage = stage;
		this._npcId = npcId;
		this._extractItem = extractItem;
		this._maxCharacteristics = maxCharacteristics;
		this._levels = new HashMap<>(10);
	}

	public void addLevelInfo(int level, int attack, int defense, int criticalRate, int criticalDamage, long maxExperience)
	{
		ElementalSpiritTemplateHolder.SpiritLevel spiritLevel = new ElementalSpiritTemplateHolder.SpiritLevel();
		spiritLevel.attack = attack;
		spiritLevel.defense = defense;
		spiritLevel.criticalRate = criticalRate;
		spiritLevel.criticalDamage = criticalDamage;
		spiritLevel.maxExperience = maxExperience;
		this._levels.put(level, spiritLevel);
	}

	public void addItemToEvolve(Integer itemId, Integer count)
	{
		if (this._itemsToEvolve == null)
		{
			this._itemsToEvolve = new ArrayList<>(2);
		}

		this._itemsToEvolve.add(new ItemHolder(itemId, count.intValue()));
	}

	public byte getType()
	{
		return this._type;
	}

	public byte getStage()
	{
		return this._stage;
	}

	public int getNpcId()
	{
		return this._npcId;
	}

	public long getMaxExperienceAtLevel(int level)
	{
		ElementalSpiritTemplateHolder.SpiritLevel spiritLevel = this._levels.get(level);
		return spiritLevel == null ? 0L : spiritLevel.maxExperience;
	}

	public int getMaxLevel()
	{
		return this._levels.size();
	}

	public int getAttackAtLevel(int level)
	{
		return this._levels.get(level).attack;
	}

	public int getDefenseAtLevel(int level)
	{
		return this._levels.get(level).defense;
	}

	public int getCriticalRateAtLevel(int level)
	{
		return this._levels.get(level).criticalRate;
	}

	public int getCriticalDamageAtLevel(int level)
	{
		return this._levels.get(level).criticalDamage;
	}

	public int getMaxCharacteristics()
	{
		return this._maxCharacteristics;
	}

	public List<ItemHolder> getItemsToEvolve()
	{
		return this._itemsToEvolve == null ? Collections.emptyList() : this._itemsToEvolve;
	}

	public void addAbsorbItem(Integer itemId, Integer experience)
	{
		if (this._absorbItems == null)
		{
			this._absorbItems = new ArrayList<>();
		}

		this._absorbItems.add(new ElementalSpiritAbsorbItemHolder(itemId, experience));
	}

	public List<ElementalSpiritAbsorbItemHolder> getAbsorbItems()
	{
		return this._absorbItems == null ? Collections.emptyList() : this._absorbItems;
	}

	public int getExtractItem()
	{
		return this._extractItem;
	}

	private static class SpiritLevel
	{
		long maxExperience;
		int criticalDamage;
		int criticalRate;
		int defense;
		int attack;

		public SpiritLevel()
		{
		}
	}
}
