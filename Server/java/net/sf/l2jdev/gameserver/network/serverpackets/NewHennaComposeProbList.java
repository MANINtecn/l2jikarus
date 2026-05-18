package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.List;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.HennaCombinationData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.henna.CombinationHenna;
import net.sf.l2jdev.gameserver.model.item.henna.Henna;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class NewHennaComposeProbList extends ServerPacket
{
	private final Player _player;

	public NewHennaComposeProbList(Player player)
	{
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		int allHennaSlots = this._player.getAvailableHennaSlots();
		int emptyHennaSlots = this._player.getHennaEmptySlots();
		ServerPackets.EX_NEW_HENNA_COMPOSE_PROB_LIST.writeId(this, buffer);
		buffer.writeInt(allHennaSlots - emptyHennaSlots);

		for (int slot = 1; slot <= allHennaSlots; slot++)
		{
			Henna henna = this._player.getHenna(slot);
			if (henna != null)
			{
				List<CombinationHenna> hennaList = HennaCombinationData.getInstance().getHenna().stream().filter(h -> h.getHenna() == henna.getDyeId()).collect(Collectors.toList());
				buffer.writeInt(henna.getDyeId());
				buffer.writeInt(hennaList.size());

				for (CombinationHenna item : hennaList)
				{
					buffer.writeInt(item.getItemTwo());
					buffer.writeInt((int) (item.getChance() * 100.0F));
				}
			}
		}
	}
}
