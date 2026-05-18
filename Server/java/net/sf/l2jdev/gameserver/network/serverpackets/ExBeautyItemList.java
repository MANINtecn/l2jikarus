package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.BeautyShopData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.beautyshop.BeautyData;
import net.sf.l2jdev.gameserver.model.beautyshop.BeautyItem;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExBeautyItemList extends ServerPacket
{
 
	private int _colorCount;
	private final BeautyData _beautyData;
	private final Map<Integer, List<BeautyItem>> _colorData = new HashMap<>();

	public ExBeautyItemList(Player player)
	{
		this._beautyData = BeautyShopData.getInstance().getBeautyData(player.getRace(), player.getAppearance().getSexType());

		for (BeautyItem hair : this._beautyData.getHairList().values())
		{
			List<BeautyItem> colors = new ArrayList<>();

			for (BeautyItem color : hair.getColors().values())
			{
				colors.add(color);
				this._colorCount++;
			}

			this._colorData.put(hair.getId(), colors);
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BEAUTY_ITEM_LIST.writeId(this, buffer);
		buffer.writeInt(0);
		buffer.writeInt(this._beautyData.getHairList().size());

		for (BeautyItem hair : this._beautyData.getHairList().values())
		{
			buffer.writeInt(0);
			buffer.writeInt(hair.getId());
			buffer.writeInt(hair.getAdena());
			buffer.writeInt(hair.getResetAdena());
			buffer.writeInt(hair.getBeautyShopTicket());
			buffer.writeInt(1);
		}

		buffer.writeInt(1);
		buffer.writeInt(this._beautyData.getFaceList().size());

		for (BeautyItem face : this._beautyData.getFaceList().values())
		{
			buffer.writeInt(0);
			buffer.writeInt(face.getId());
			buffer.writeInt(face.getAdena());
			buffer.writeInt(face.getResetAdena());
			buffer.writeInt(face.getBeautyShopTicket());
			buffer.writeInt(1);
		}

		buffer.writeInt(2);
		buffer.writeInt(this._colorCount);

		for (Entry<Integer, List<BeautyItem>> entry : this._colorData.entrySet())
		{
			for (BeautyItem color : entry.getValue())
			{
				buffer.writeInt(entry.getKey());
				buffer.writeInt(color.getId());
				buffer.writeInt(color.getAdena());
				buffer.writeInt(color.getResetAdena());
				buffer.writeInt(color.getBeautyShopTicket());
				buffer.writeInt(1);
			}
		}
	}
}
