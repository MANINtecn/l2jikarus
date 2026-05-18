package net.sf.l2jdev.gameserver.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.item.holders.ArmorsetSkillHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.util.ArrayUtil;

public class ArmorSet
{
	private final int _id;
	private final int _minimumPieces;
	private final boolean _isVisual;
	private final int[] _requiredItems;
	private final int[] _optionalItems;
	private final List<ArmorsetSkillHolder> _skills;
	private final Map<BaseStat, Double> _stats;
	private static final int[] ARMORSET_SLOTS = new int[]
	{
		6,
		11,
		1,
		10,
		12
	};
	private static final int[] ARTIFACT_1_SLOTS = new int[]
	{
		38,
		39,
		40,
		41,
		50,
		53,
		56
	};
	private static final int[] ARTIFACT_2_SLOTS = new int[]
	{
		42,
		43,
		44,
		45,
		51,
		54,
		57
	};
	private static final int[] ARTIFACT_3_SLOTS = new int[]
	{
		46,
		47,
		48,
		49,
		52,
		55,
		58
	};

	public ArmorSet(int id, int minimumPieces, boolean isVisual, Set<Integer> requiredItems, Set<Integer> optionalItems, List<ArmorsetSkillHolder> skills, Map<BaseStat, Double> stats)
	{
		this._id = id;
		this._minimumPieces = minimumPieces;
		this._isVisual = isVisual;
		this._requiredItems = requiredItems.stream().mapToInt(x -> x).toArray();
		this._optionalItems = optionalItems.stream().mapToInt(x -> x).toArray();
		this._skills = skills;
		this._stats = stats;
	}

	public int getId()
	{
		return this._id;
	}

	public int getMinimumPieces()
	{
		return this._minimumPieces;
	}

	public boolean isVisual()
	{
		return this._isVisual;
	}

	public int[] getRequiredItems()
	{
		return this._requiredItems;
	}

	public int[] getOptionalItems()
	{
		return this._optionalItems;
	}

	public List<ArmorsetSkillHolder> getSkills()
	{
		return this._skills;
	}

	public double getStatsBonus(BaseStat stat)
	{
		return this._stats.getOrDefault(stat, 0.0);
	}

	public boolean containOptionalItem(int shieldId)
	{
		return ArrayUtil.contains(this._optionalItems, shieldId);
	}

	public int getSetEnchant(Playable playable)
	{
		if (this.getPieceCount(playable) < this._minimumPieces)
		{
			return 0;
		}
		Inventory inv = playable.getInventory();
		int enchantLevel = 127;

		for (int armorSlot : ARMORSET_SLOTS)
		{
			Item itemPart = inv.getPaperdollItem(armorSlot);
			if (itemPart != null && ArrayUtil.contains(this._requiredItems, itemPart.getId()) && enchantLevel > itemPart.getEnchantLevel())
			{
				enchantLevel = itemPart.getEnchantLevel();
			}
		}

		if (enchantLevel == 127)
		{
			enchantLevel = 0;
		}

		return enchantLevel;
	}

	public int getArtifactSlotMask(Playable playable, int bookSlot)
	{
		Inventory inv = playable.getInventory();
		int slotMask = 0;
		switch (bookSlot)
		{
			case 1:
				for (int artifactSlotxx : ARTIFACT_1_SLOTS)
				{
					Item itemPart = inv.getPaperdollItem(artifactSlotxx);
					if (itemPart != null && ArrayUtil.contains(this._requiredItems, itemPart.getId()))
					{
						slotMask += artifactSlotxx;
					}
				}
				break;
			case 2:
				for (int artifactSlotx : ARTIFACT_2_SLOTS)
				{
					Item itemPart = inv.getPaperdollItem(artifactSlotx);
					if (itemPart != null && ArrayUtil.contains(this._requiredItems, itemPart.getId()))
					{
						slotMask += artifactSlotx;
					}
				}
				break;
			case 3:
				for (int artifactSlot : ARTIFACT_3_SLOTS)
				{
					Item itemPart = inv.getPaperdollItem(artifactSlot);
					if (itemPart != null && ArrayUtil.contains(this._requiredItems, itemPart.getId()))
					{
						slotMask += artifactSlot;
					}
				}
		}

		return slotMask;
	}

	public boolean hasOptionalEquipped(Playable playable, Function<Item, Integer> idProvider)
	{
		for (Item item : playable.getInventory().getPaperdollItems())
		{
			if (ArrayUtil.contains(this._optionalItems, idProvider.apply(item).intValue()))
			{
				return true;
			}
		}

		return false;
	}

	public long getPieceCount(Playable playable, Function<Item, Integer> idProvider)
	{
		return playable.getInventory().getPaperdollItemCount(item -> ArrayUtil.contains(this._requiredItems, idProvider.apply(item).intValue()));
	}

	public long getPieceCount(Playable playable)
	{
		return playable.getInventory().getPaperdollItemCount(item -> ArrayUtil.contains(this._requiredItems, item.getId()));
	}
}
