package net.sf.l2jdev.gameserver.network.serverpackets.variation;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ApplyVariationOption extends ServerPacket
{
	private final int _result;
	private final int _enchantedObjectId;
	private final int _option1;
	private final int _option2;
	private final int _option3;

	public ApplyVariationOption(int result, int enchantedObjectId, int option1, int option2, int option3)
	{
		this._result = result;
		this._enchantedObjectId = enchantedObjectId;
		this._option1 = option1;
		this._option2 = option2;
		this._option3 = option3;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_APPLY_VARIATION_OPTION.writeId(this, buffer);
		buffer.writeByte(this._result);
		buffer.writeInt(this._enchantedObjectId);
		buffer.writeInt(this._option1);
		buffer.writeInt(this._option2);
		buffer.writeInt(this._option3);
	}
}
