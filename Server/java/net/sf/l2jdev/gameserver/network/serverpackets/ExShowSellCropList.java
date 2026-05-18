package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.CastleManorManager;
import net.sf.l2jdev.gameserver.model.CropProcure;
import net.sf.l2jdev.gameserver.model.Seed;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerInventory;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowSellCropList extends ServerPacket
{
	private final int _manorId;
	private final Map<Integer, Item> _cropsItems = new HashMap<>();
	private final Map<Integer, CropProcure> _castleCrops = new HashMap<>();

	public ExShowSellCropList(PlayerInventory inventory, int manorId)
	{
		this._manorId = manorId;

		for (int cropId : CastleManorManager.getInstance().getCropIds())
		{
			Item item = inventory.getItemByItemId(cropId);
			if (item != null)
			{
				this._cropsItems.put(cropId, item);
			}
		}

		for (CropProcure crop : CastleManorManager.getInstance().getCropProcure(this._manorId, false))
		{
			if (this._cropsItems.containsKey(crop.getId()) && crop.getAmount() > 0L)
			{
				this._castleCrops.put(crop.getId(), crop);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_SELL_CROP_LIST.writeId(this, buffer);
		buffer.writeInt(this._manorId);
		buffer.writeInt(this._cropsItems.size());

		for (Item item : this._cropsItems.values())
		{
			Seed seed = CastleManorManager.getInstance().getSeedByCrop(item.getId());
			buffer.writeInt(item.getObjectId());
			buffer.writeInt(item.getId());
			buffer.writeInt(seed.getLevel());
			buffer.writeByte(1);
			buffer.writeInt(seed.getReward(1));
			buffer.writeByte(1);
			buffer.writeInt(seed.getReward(2));
			if (this._castleCrops.containsKey(item.getId()))
			{
				CropProcure crop = this._castleCrops.get(item.getId());
				buffer.writeInt(this._manorId);
				buffer.writeLong(crop.getAmount());
				buffer.writeLong(crop.getPrice());
				buffer.writeByte(crop.getReward());
			}
			else
			{
				buffer.writeInt(-1);
				buffer.writeLong(0L);
				buffer.writeLong(0L);
				buffer.writeByte(0);
			}

			buffer.writeLong(item.getCount());
		}
	}
}
