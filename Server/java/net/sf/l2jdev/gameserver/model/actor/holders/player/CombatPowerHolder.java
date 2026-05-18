package net.sf.l2jdev.gameserver.model.actor.holders.player;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.l2jdev.gameserver.data.holders.RelicCollectionDataHolder;
import net.sf.l2jdev.gameserver.data.holders.RelicDataHolder;
import net.sf.l2jdev.gameserver.data.holders.RelicEnchantHolder;
import net.sf.l2jdev.gameserver.data.xml.RelicCollectionData;
import net.sf.l2jdev.gameserver.data.xml.RelicData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class CombatPowerHolder
{
	private final Player _owner;
	private final AtomicInteger _itemCP = new AtomicInteger();
	private final AtomicInteger _blessCP = new AtomicInteger();
	private final AtomicInteger _ensoulCP = new AtomicInteger();
	private final AtomicInteger _relicEffectCP = new AtomicInteger();
	private final AtomicInteger _relicCollectionCP = new AtomicInteger();
	private final AtomicInteger _adenLabCollectionCP = new AtomicInteger();
	private final AtomicInteger _skillCP = new AtomicInteger();

	public CombatPowerHolder(Player player)
	{
		this._owner = player;
	}

	public int getItemCombatPower()
	{
		return this._itemCP.get();
	}

	public int getRelicEffectCombatPower()
	{
		return this._relicEffectCP.get();
	}

	public int getRelicCollectionCombatPower()
	{
		return this._relicCollectionCP.get();
	}

	public int getAdenLabCollectionCP()
	{
		return this._adenLabCollectionCP.get();
	}

	public int getBlessCP()
	{
		return this._blessCP.get();
	}

	public int getEnsoulCP()
	{
		return this._ensoulCP.get();
	}

	public int getSkillCombatPower()
	{
		return this._skillCP.get();
	}

	public int getTotalCombatPower()
	{
		return this._itemCP.get() + this._blessCP.get() + this._ensoulCP.get() + this._relicEffectCP.get() + this._relicCollectionCP.get() + this._adenLabCollectionCP.get() + this._skillCP.get();
	}

	public void addItemCombatPower(Item item)
	{
		if (item.isBlessed())
		{
			this._blessCP.addAndGet(item.getTemplate().getGearScore() + (int) Math.pow(1.2, item.getEnchantLevel()));
		}
		else if (item.getSpecialAbilities().isEmpty() && item.getAdditionalSpecialAbilities().isEmpty())
		{
			this._itemCP.addAndGet(item.getTemplate().getGearScore() + (int) Math.pow(1.2, item.getEnchantLevel()));
		}
		else
		{
			this._ensoulCP.addAndGet(item.getTemplate().getGearScore() + (int) Math.pow(1.2, item.getEnchantLevel()));
		}

		this._owner.sendCombatPower();
	}

	public void removeItemCombatPower(Item item)
	{
		if (item.isBlessed())
		{
			this._blessCP.addAndGet(-(item.getTemplate().getGearScore() + (int) Math.pow(1.2, item.getEnchantLevel())));
		}
		else if (item.getSpecialAbilities().isEmpty() && item.getAdditionalSpecialAbilities().isEmpty())
		{
			this._itemCP.addAndGet(-(item.getTemplate().getGearScore() + (int) Math.pow(1.2, item.getEnchantLevel())));
		}
		else
		{
			this._ensoulCP.addAndGet(-(item.getTemplate().getGearScore() + (int) Math.pow(1.2, item.getEnchantLevel())));
		}

		this._owner.sendCombatPower();
	}

	public void updateRelicCombatPower(Player player)
	{
		this._relicEffectCP.set(0);

		for (PlayerRelicData data : player.getRelics())
		{
			RelicDataHolder rdh = RelicData.getInstance().getRelic(data.getRelicId());

			for (RelicEnchantHolder reh : rdh.getEnchantHoldders())
			{
				if (reh.getEnchantLevel() == data.getRelicLevel())
				{
					this._relicEffectCP.addAndGet(reh.getCombatPower());
				}
			}
		}
	}

	public void updateRelicCollectionCombatPower()
	{
		this._relicCollectionCP.set(0);
		Map<Integer, Integer> relicCollectionCounts = new HashMap<>();

		for (PlayerRelicCollectionData relicCollection : this._owner.getRelicCollections())
		{
			relicCollectionCounts.merge(relicCollection.getRelicCollectionId(), 1, Integer::sum);
		}

		for (Entry<Integer, Integer> entry : relicCollectionCounts.entrySet())
		{
			int relicCollectionId = entry.getKey();
			int count = entry.getValue();
			RelicCollectionDataHolder relicCollection = RelicCollectionData.getInstance().getRelicCollection(relicCollectionId);
			if (relicCollection != null && count >= relicCollection.getCompleteCount())
			{
				this._relicCollectionCP.addAndGet(relicCollection.getCombatPower());
			}
		}
	}

	public void setAdenLabCombatPower(int totalAmount)
	{
		this._adenLabCollectionCP.set(totalAmount);
		this._owner.sendCombatPower();
	}

	public void addAdenLabCombatPower(int additionalPower)
	{
		this._adenLabCollectionCP.addAndGet(additionalPower);
		this._owner.sendCombatPower();
	}

	public void setSkillCombatPower(int value)
	{
		this._skillCP.set(value);
		this._owner.sendCombatPower();
	}

	public void addSkillCombatPower(int value)
	{
		this._skillCP.addAndGet(value);
		this._owner.sendCombatPower();
	}
}
