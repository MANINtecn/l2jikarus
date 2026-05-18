package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExAttributeEnchantResult extends ServerPacket
{
	private final int _result;
	private final boolean _isWeapon;
	private final int _type;
	private final int _before;
	private final int _after;
	private final int _successCount;
	private final int _failedCount;

	public ExAttributeEnchantResult(int result, boolean isWeapon, AttributeType type, int before, int after, int successCount, int failedCount)
	{
		this._result = result;
		this._isWeapon = isWeapon;
		this._type = type.getClientId();
		this._before = before;
		this._after = after;
		this._successCount = successCount;
		this._failedCount = failedCount;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ATTRIBUTE_ENCHANT_RESULT.writeId(this, buffer);
		buffer.writeInt(this._result);
		buffer.writeByte(this._isWeapon);
		buffer.writeShort(this._type);
		buffer.writeShort(this._before);
		buffer.writeShort(this._after);
		buffer.writeShort(this._successCount);
		buffer.writeShort(this._failedCount);
	}
}
