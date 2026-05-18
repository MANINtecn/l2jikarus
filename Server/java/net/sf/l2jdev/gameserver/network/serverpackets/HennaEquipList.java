package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.HennaData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.henna.Henna;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class HennaEquipList extends ServerPacket
{
	private final Player _player;
	private final List<Henna> _hennaEquipList;

	public HennaEquipList(Player player)
	{
		this._player = player;
		this._hennaEquipList = HennaData.getInstance().getHennaList(player);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.HENNA_EQUIP_LIST.writeId(this, buffer);
		buffer.writeLong(this._player.getAdena());
		buffer.writeInt(3);
		buffer.writeInt(this._hennaEquipList.size());

		for (Henna henna : this._hennaEquipList)
		{
			if (this._player.getInventory().getItemByItemId(henna.getDyeItemId()) != null)
			{
				buffer.writeInt(henna.getDyeId());
				buffer.writeInt(henna.getDyeItemId());
				buffer.writeInt(henna.getWearCount());
				buffer.writeLong(henna.getWearFee());
			}
		}
	}
}
