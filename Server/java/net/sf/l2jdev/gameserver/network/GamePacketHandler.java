package net.sf.l2jdev.gameserver.network;

import java.util.logging.Logger;

import net.sf.l2jdev.commons.network.PacketHandler;
import net.sf.l2jdev.commons.network.ReadableBuffer;
import net.sf.l2jdev.commons.network.ReadablePacket;
import net.sf.l2jdev.commons.util.TraceUtil;
import net.sf.l2jdev.gameserver.network.clientpackets.RequestBookMarkSlotInfo;
import net.sf.l2jdev.gameserver.network.clientpackets.RequestChangeBookMarkSlot;
import net.sf.l2jdev.gameserver.network.clientpackets.RequestDeleteBookMarkSlot;
import net.sf.l2jdev.gameserver.network.clientpackets.RequestModifyBookMarkSlot;
import net.sf.l2jdev.gameserver.network.clientpackets.RequestSaveBookMarkSlot;
import net.sf.l2jdev.gameserver.network.clientpackets.RequestTeleportBookMark;

public class GamePacketHandler implements PacketHandler<GameClient>
{
	private static final Logger LOGGER = Logger.getLogger(GamePacketHandler.class.getName());

	@Override
	public ReadablePacket<GameClient> handlePacket(ReadableBuffer buffer, GameClient client)
	{
		int packetId;
		try
		{
			packetId = Byte.toUnsignedInt(buffer.readByte());
		}
		catch (Exception var7)
		{
			LOGGER.warning("PacketHandler: Problem receiving packet id from " + client);
			LOGGER.warning(TraceUtil.getStackTrace(var7));
			client.closeNow();
			return null;
		}

		if (packetId == 208)
		{
			int exPacketId = Short.toUnsignedInt(buffer.readShort());
			if (exPacketId >= 0 && exPacketId < ExClientPackets.PACKET_ARRAY.length)
			{
				ExClientPackets packetEnum = ExClientPackets.PACKET_ARRAY[exPacketId];
				if (packetEnum == null)
				{
					return null;
				}
				else if (!packetEnum.getConnectionStates().contains(client.getConnectionState()))
				{
					return null;
				}
				else if (exPacketId == 78)
				{
					int subId = buffer.readInt();
					switch (subId)
					{
						case 0:
							return new RequestBookMarkSlotInfo();
						case 1:
							return new RequestSaveBookMarkSlot();
						case 2:
							return new RequestModifyBookMarkSlot();
						case 3:
							return new RequestDeleteBookMarkSlot();
						case 4:
							return new RequestTeleportBookMark();
						case 5:
							return new RequestChangeBookMarkSlot();
						default:
							return null;
					}
				}
				else
				{
					return packetEnum.newPacket();
				}
			}
			return null;
		}
		else if (packetId >= 0 && packetId < ClientPackets.PACKET_ARRAY.length)
		{
			ClientPackets packetEnum = ClientPackets.PACKET_ARRAY[packetId];
			if (packetEnum == null)
			{
				return null;
			}
			return !packetEnum.getConnectionStates().contains(client.getConnectionState()) ? null : packetEnum.newPacket();
		}
		else
		{
			return null;
		}
	}
}
