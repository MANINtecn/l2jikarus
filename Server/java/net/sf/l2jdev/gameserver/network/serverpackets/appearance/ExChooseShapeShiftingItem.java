package net.sf.l2jdev.gameserver.network.serverpackets.appearance;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.item.appearance.AppearanceStone;
import net.sf.l2jdev.gameserver.model.item.appearance.AppearanceTargetType;
import net.sf.l2jdev.gameserver.model.item.appearance.AppearanceType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExChooseShapeShiftingItem extends ServerPacket
{
	private final AppearanceType _type;
	private final AppearanceTargetType _targetType;
	private final int _itemId;

	public ExChooseShapeShiftingItem(AppearanceStone stone)
	{
		this._type = stone.getType();
		this._targetType = stone.getTargetTypes().size() > 1 ? AppearanceTargetType.ALL : stone.getTargetTypes().stream().findFirst().get();
		this._itemId = stone.getId();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CHOOSE_SHAPE_SHIFTING_ITEM.writeId(this, buffer);
		buffer.writeInt(this._targetType != null ? this._targetType.ordinal() : 0);
		buffer.writeInt(this._type != null ? this._type.ordinal() : 0);
		buffer.writeInt(this._itemId);
	}
}
