package net.sf.l2jdev.gameserver.network.serverpackets.randomcraft;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.holders.RandomCraftRewardItemHolder;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCraftRandomInfo extends ServerPacket
{
	private final List<RandomCraftRewardItemHolder> _rewards;

	public ExCraftRandomInfo(Player player)
	{
		this._rewards = player.getRandomCraft().getRewards();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CRAFT_RANDOM_INFO.writeId(this, buffer);
		int size = 5;
		buffer.writeInt(size);

		for (RandomCraftRewardItemHolder holder : this._rewards)
		{
			if (holder != null && holder.getItemId() != 0)
			{
				buffer.writeByte(holder.isLocked());
				buffer.writeInt(holder.getLockLeft());
				buffer.writeInt(holder.getItemId());
				buffer.writeLong(holder.getItemCount());
			}
			else
			{
				buffer.writeByte(0);
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeLong(0L);
			}

			size--;
		}

		for (int i = size; i > 0; i--)
		{
			buffer.writeByte(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeLong(0L);
		}
	}
}
