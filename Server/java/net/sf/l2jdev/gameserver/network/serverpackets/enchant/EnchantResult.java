package net.sf.l2jdev.gameserver.network.serverpackets.enchant;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class EnchantResult extends ServerPacket
{
	public static final int SUCCESS = 0;
	public static final int FAIL = 1;
	public static final int ERROR = 2;
	public static final int BLESSED_FAIL = 3;
	public static final int NO_CRYSTAL = 4;
	public static final int SAFE_FAIL = 5;
	public static final int CHALLENGE_ENCHANT_SAFE = 6;
	public static final int DECREASE = 7;
	public static final int REMAIN = 8;
	public static final int FAILED_WITH_OPTIONS_NO_AND_NO_POINTS = 9;
	public static final int SAFE_FAIL_02 = 10;
	private final int _result;
	private final ItemHolder _crystal;
	private final ItemHolder _additional;
	private final int _enchantLevel;

	public EnchantResult(int result, ItemHolder crystal, ItemHolder additionalItem, int enchantLevel)
	{
		this._result = result;
		this._crystal = crystal == null ? new ItemHolder(0, 0L) : crystal;
		this._additional = additionalItem == null ? new ItemHolder(0, 0L) : additionalItem;
		this._enchantLevel = enchantLevel;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.ENCHANT_RESULT.writeId(this, buffer);
		buffer.writeInt(this._result);
		buffer.writeInt(this._crystal.getId());
		buffer.writeLong(this._crystal.getCount());
		buffer.writeInt(this._additional.getId());
		buffer.writeLong(this._additional.getCount());
		buffer.writeInt(this._enchantLevel);
	}
}
