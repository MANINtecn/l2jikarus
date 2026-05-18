package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.VipSystemConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExWorldChatCnt extends ServerPacket
{
	private final int _points;

	public ExWorldChatCnt(Player player)
	{
		this._points = player.getLevel() >= GeneralConfig.WORLD_CHAT_MIN_LEVEL && (!VipSystemConfig.VIP_SYSTEM_ENABLED || player.getVipTier() > 0) ? Math.max(player.getWorldChatPoints() - player.getWorldChatUsed(), 0) : 0;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_WORLDCHAT_CNT.writeId(this, buffer);
		buffer.writeInt(this._points);
	}
}
