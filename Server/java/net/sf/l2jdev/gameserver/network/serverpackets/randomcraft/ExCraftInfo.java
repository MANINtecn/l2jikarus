package net.sf.l2jdev.gameserver.network.serverpackets.randomcraft;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerRandomCraft;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCraftInfo extends ServerPacket
{
	private final PlayerRandomCraft _randomCraft;

	public ExCraftInfo(Player player)
	{
		this._randomCraft = player.getRandomCraft();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CRAFT_INFO.writeId(this, buffer);
		buffer.writeInt(this._randomCraft.getFullCraftPoints());
		buffer.writeInt(this._randomCraft.getCraftPoints());
		buffer.writeByte(this._randomCraft.isSayhaRoll());
	}
}
