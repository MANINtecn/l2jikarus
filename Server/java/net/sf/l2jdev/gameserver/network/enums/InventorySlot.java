package net.sf.l2jdev.gameserver.network.enums;

import net.sf.l2jdev.gameserver.model.interfaces.IUpdateTypeComponent;

public enum InventorySlot implements IUpdateTypeComponent
{
	UNDER(0),
	REAR(8),
	LEAR(9),
	NECK(4),
	RFINGER(13),
	LFINGER(14),
	HEAD(1),
	RHAND(5),
	LHAND(7),
	GLOVES(10),
	CHEST(6),
	LEGS(11),
	FEET(12),
	CLOAK(28),
	LRHAND(5),
	HAIR(2),
	HAIR2(3),
	RBRACELET(16),
	LBRACELET(15),
	AGATHION1(17),
	AGATHION2(18),
	AGATHION3(19),
	AGATHION4(20),
	AGATHION5(21),
	DECO1(22),
	DECO2(23),
	DECO3(24),
	DECO4(25),
	DECO5(26),
	DECO6(27),
	BELT(29),
	BROOCH(30),
	BROOCH_JEWEL(31),
	BROOCH_JEWEL2(32),
	BROOCH_JEWEL3(33),
	BROOCH_JEWEL4(34),
	BROOCH_JEWEL5(35),
	BROOCH_JEWEL6(36),
	ARTIFACT_BOOK(37),
	ARTIFACT1(38),
	ARTIFACT2(39),
	ARTIFACT3(40),
	ARTIFACT4(41),
	ARTIFACT5(42),
	ARTIFACT6(43),
	ARTIFACT7(44),
	ARTIFACT8(45),
	ARTIFACT9(46),
	ARTIFACT10(47),
	ARTIFACT11(48),
	ARTIFACT12(49),
	ARTIFACT13(50),
	ARTIFACT14(51),
	ARTIFACT15(52),
	ARTIFACT16(53),
	ARTIFACT17(54),
	ARTIFACT18(55),
	ARTIFACT19(56),
	ARTIFACT20(57),
	ARTIFACT21(58);

	private final int _paperdollSlot;

	private InventorySlot(int paperdollSlot)
	{
		this._paperdollSlot = paperdollSlot;
	}

	public int getSlot()
	{
		return this._paperdollSlot;
	}

	@Override
	public int getMask()
	{
		return this.ordinal();
	}
}
