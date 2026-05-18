package net.sf.l2jdev.gameserver.model.item.enums;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.item.instance.Item;

public enum BodyPart
{
	NONE(0L, 59, "none"),
	UNDERWEAR(1L, 0, "underwear", "shirt"),
	R_EAR(2L, 8, "rear", "rbracelet"),
	L_EAR(4L, 9, "lear"),
	LR_EAR(6L, -1, "rear;lear"),
	NECK(8L, 4, "neck"),
	R_FINGER(16L, 13, "rfinger"),
	L_FINGER(32L, 14, "lfinger"),
	LR_FINGER(48L, -1, "rfinger;lfinger"),
	HEAD(64L, 1, "head"),
	R_HAND(128L, 5, "rhand"),
	L_HAND(256L, 7, "lhand"),
	GLOVES(512L, 10, "gloves"),
	CHEST(1024L, 6, "chest"),
	LEGS(2048L, 11, "legs"),
	FEET(4096L, 12, "feet"),
	BACK(8192L, 28, "back"),
	LR_HAND(16384L, -1, "lrhand"),
	FULL_ARMOR(32768L, -1, "fullarmor", "onepiece"),
	HAIR(65536L, 2, "hair"),
	ALLDRESS(131072L, -1, "alldress"),
	HAIR2(262144L, 3, "hair2"),
	HAIRALL(524288L, -1, "hairall", "dhair"),
	R_BRACELET(1048576L, 16, "rbracelet"),
	L_BRACELET(2097152L, 15, "lbracelet"),
	DECO(4194304L, 22, "deco1", "talisman"),
	BELT(268435456L, 29, "belt", "waist"),
	BROOCH(536870912L, 30, "brooch"),
	BROOCH_JEWEL(1073741824L, 31, "brooch_jewel"),
	AGATHION(206158430208L, 17, "agathion"),
	ARTIFACT_BOOK(2199023255552L, 37, "artifactbook"),
	ARTIFACT(4398046511104L, 38, "artifact"),
	WOLF(-100L, -1, "wolf"),
	HATCHLING(-101L, -1, "hatchling"),
	STRIDER(-102L, -1, "strider"),
	BABYPET(-103L, -1, "babypet"),
	GREATWOLF(-104L, -1, "greatwolf");

	private final long _mask;
	private final int _paperdollSlot;
	private final String[] _names;
	private static final Map<Integer, BodyPart> BODY_PARTS_BY_PAPERDOLL_SLOT = new HashMap<>();
	private static final Map<String, BodyPart> BODY_PARTS_BY_NAME = new HashMap<>();

	private BodyPart(long mask, int paperdollSlot, String... names)
	{
		this._mask = mask;
		this._paperdollSlot = paperdollSlot;
		this._names = names;
	}

	public long getMask()
	{
		return this._mask;
	}

	public int getPaperdollSlot()
	{
		return this._paperdollSlot;
	}

	public static int getPaperdollIndex(BodyPart bodyPart)
	{
		switch (bodyPart)
		{
			case LR_HAND:
				return 5;
			case FULL_ARMOR:
			case ALLDRESS:
				return 6;
			case HAIR:
			case HAIR2:
			default:
				return bodyPart.getPaperdollSlot();
			case HAIRALL:
				return 2;
		}
	}

	public static BodyPart fromPaperdollSlot(int slot)
	{
		return BODY_PARTS_BY_PAPERDOLL_SLOT.get(slot);
	}

	public static BodyPart fromItem(Item item)
	{
		if (item == null)
		{
			return NONE;
		}
		int slot = item.getLocationSlot();
		switch (slot)
		{
			case 6:
				return item.getTemplate().getBodyPart();
			default:
				return BODY_PARTS_BY_PAPERDOLL_SLOT.get(slot);
		}
	}

	public static BodyPart fromName(String name)
	{
		return BODY_PARTS_BY_NAME.get(name);
	}

	static
	{
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(17, AGATHION);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(18, AGATHION);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(19, AGATHION);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(20, AGATHION);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(21, AGATHION);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(22, DECO);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(23, DECO);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(24, DECO);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(25, DECO);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(26, DECO);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(27, DECO);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(31, BROOCH_JEWEL);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(32, BROOCH_JEWEL);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(33, BROOCH_JEWEL);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(34, BROOCH_JEWEL);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(35, BROOCH_JEWEL);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(36, BROOCH_JEWEL);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(38, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(39, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(40, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(41, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(42, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(43, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(44, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(45, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(46, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(47, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(48, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(49, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(50, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(51, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(52, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(53, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(54, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(55, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(56, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(57, ARTIFACT);
		BODY_PARTS_BY_PAPERDOLL_SLOT.put(58, ARTIFACT);

		for (BodyPart bodyPart : values())
		{
			if (bodyPart._paperdollSlot >= 0 && bodyPart._paperdollSlot < 59)
			{
				BODY_PARTS_BY_PAPERDOLL_SLOT.put(bodyPart._paperdollSlot, bodyPart);
			}

			for (String name : bodyPart._names)
			{
				BODY_PARTS_BY_NAME.put(name, bodyPart);
			}
		}
	}
}
