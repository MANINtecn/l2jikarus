package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Map;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.BeautyShopData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.appearance.PlayerAppearance;
import net.sf.l2jdev.gameserver.model.beautyshop.BeautyItem;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExResponseBeautyList extends ServerPacket
{
	public static final int SHOW_FACESHAPE = 1;
	public static final int SHOW_HAIRSTYLE = 0;
	private final Player _player;
	private final int _type;
	private final Map<Integer, BeautyItem> _beautyItem;

	public ExResponseBeautyList(Player player, int type)
	{
		this._player = player;
		this._type = type;
		PlayerAppearance appearance = player.getAppearance();
		if (type == 0)
		{
			this._beautyItem = BeautyShopData.getInstance().getBeautyData(player.getRace(), appearance.getSexType()).getHairList();
		}
		else
		{
			this._beautyItem = BeautyShopData.getInstance().getBeautyData(player.getRace(), appearance.getSexType()).getFaceList();
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RESPONSE_BEAUTY_LIST.writeId(this, buffer);
		buffer.writeLong(this._player.getAdena());
		buffer.writeLong(this._player.getBeautyTickets());
		buffer.writeInt(this._type);
		buffer.writeInt(this._beautyItem.size());

		for (BeautyItem item : this._beautyItem.values())
		{
			buffer.writeInt(item.getId());
			buffer.writeInt(1);
		}

		buffer.writeInt(0);
	}
}
