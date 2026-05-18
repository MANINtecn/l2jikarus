package net.sf.l2jdev.gameserver.network.serverpackets.equipmentupgradenormal;

import java.util.Collections;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.item.holders.UniqueItemEnchantHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractItemPacket;

public class ExUpgradeSystemNormalResult extends AbstractItemPacket
{
	public static final ExUpgradeSystemNormalResult FAIL = new ExUpgradeSystemNormalResult(0, 0, false, Collections.emptyList(), Collections.emptyList());
	private final int _result;
	private final int _upgradeId;
	private final boolean _success;
	private final List<UniqueItemEnchantHolder> _resultItems;
	private final List<UniqueItemEnchantHolder> _bonusItems;

	public ExUpgradeSystemNormalResult(int result, int upgradeId, boolean success, List<UniqueItemEnchantHolder> resultItems, List<UniqueItemEnchantHolder> bonusItems)
	{
		this._result = result;
		this._upgradeId = upgradeId;
		this._success = success;
		this._resultItems = resultItems;
		this._bonusItems = bonusItems;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_UPGRADE_SYSTEM_NORMAL_RESULT.writeId(this, buffer);
		buffer.writeShort(this._result);
		buffer.writeInt(this._upgradeId);
		buffer.writeByte(this._success);
		buffer.writeInt(this._resultItems.size());

		for (UniqueItemEnchantHolder item : this._resultItems)
		{
			buffer.writeInt(item.getObjectId());
			buffer.writeInt(item.getId());
			buffer.writeInt(item.getEnchantLevel());
			buffer.writeInt(Math.toIntExact(item.getCount()));
		}

		buffer.writeByte(0);
		buffer.writeInt(this._bonusItems.size());

		for (UniqueItemEnchantHolder bonus : this._bonusItems)
		{
			buffer.writeInt(bonus.getObjectId());
			buffer.writeInt(bonus.getId());
			buffer.writeInt(bonus.getEnchantLevel());
			buffer.writeInt(Math.toIntExact(bonus.getCount()));
		}
	}
}
