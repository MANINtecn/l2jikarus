package net.sf.l2jdev.gameserver.network.serverpackets.newhenna;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class NewHennaPotenEnchant extends ServerPacket
{
	private final int _slotId;
	private final int _enchantStep;
	private final int _enchantExp;
	private final int _dailyStep;
	private final int _dailyCount;
	private final int _activeStep;
	private final boolean _success;

	public NewHennaPotenEnchant(int slotId, int enchantStep, int enchantExp, int dailyStep, int dailyCount, int activeStep, boolean success)
	{
		this._slotId = slotId;
		this._enchantStep = enchantStep;
		this._enchantExp = enchantExp;
		this._dailyStep = dailyStep;
		this._dailyCount = dailyCount;
		this._activeStep = activeStep;
		this._success = success;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_NEW_HENNA_POTEN_ENCHANT.writeId(this, buffer);
		buffer.writeByte(this._slotId);
		buffer.writeShort(this._enchantStep);
		buffer.writeInt(this._enchantExp);
		buffer.writeShort(this._dailyStep);
		buffer.writeShort(this._dailyCount);
		buffer.writeShort(this._activeStep);
		buffer.writeByte(this._success);
		buffer.writeShort(this._dailyStep);
		buffer.writeShort(this._dailyCount);
	}
}
