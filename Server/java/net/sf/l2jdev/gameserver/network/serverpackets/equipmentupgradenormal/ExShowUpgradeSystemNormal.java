package net.sf.l2jdev.gameserver.network.serverpackets.equipmentupgradenormal;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.EquipmentUpgradeNormalData;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractItemPacket;

public class ExShowUpgradeSystemNormal extends AbstractItemPacket
{
	private final int _mode;
	private final int _type;
	private final int _commission;
	private final List<Integer> _materials = new ArrayList<>();
	private final List<Integer> _discountRatio = new ArrayList<>();

	public ExShowUpgradeSystemNormal(int mode, int type)
	{
		this._mode = mode;
		this._type = type;
		EquipmentUpgradeNormalData data = EquipmentUpgradeNormalData.getInstance();
		this._commission = data.getCommission();

		for (ItemHolder item : data.getDiscount())
		{
			this._materials.add(item.getId());
			this._discountRatio.add((int) item.getCount());
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_UPGRADE_SYSTEM_NORMAL.writeId(this, buffer);
		buffer.writeShort(this._mode);
		buffer.writeShort(this._type);
		buffer.writeShort(this._commission);
		buffer.writeInt(this._materials.size());

		for (int id : this._materials)
		{
			buffer.writeInt(id);
		}

		buffer.writeInt(this._discountRatio.size());

		for (int discount : this._discountRatio)
		{
			buffer.writeInt(discount);
		}
	}
}
