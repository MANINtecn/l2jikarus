package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.commons.network.WritablePacket;
import net.sf.l2jdev.commons.util.TraceUtil;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.ConnectionState;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;

public abstract class ServerPacket extends WritablePacket<GameClient>
{
	private static final int[] PAPERDOLL_ORDER = new int[]
	{
		0,
		8,
		9,
		4,
		13,
		14,
		1,
		5,
		7,
		10,
		6,
		11,
		12,
		28,
		5,
		2,
		3,
		16,
		15,
		17,
		18,
		19,
		20,
		21,
		22,
		23,
		24,
		25,
		26,
		27,
		29,
		30,
		31,
		32,
		33,
		34,
		35,
		36,
		37,
		38,
		39,
		40,
		41,
		42,
		43,
		44,
		45,
		46,
		47,
		48,
		49,
		50,
		51,
		52,
		53,
		54,
		55,
		56,
		57,
		58
	};
	private static final int[] PAPERDOLL_ORDER_AUGMENT = new int[]
	{
		5,
		7,
		5
	};
	private static final int[] PAPERDOLL_ORDER_VISUAL_ID = new int[]
	{
		5,
		7,
		5,
		10,
		6,
		11,
		12,
		2,
		3
	};

	protected int[] getPaperdollOrder()
	{
		return PAPERDOLL_ORDER;
	}

	protected int[] getPaperdollOrderAugument()
	{
		return PAPERDOLL_ORDER_AUGMENT;
	}

	protected int[] getPaperdollOrderVisualId()
	{
		return PAPERDOLL_ORDER_VISUAL_ID;
	}

	protected void writeOptionalInt(int value, WritableBuffer buffer)
	{
		if (value >= 32767)
		{
			buffer.writeShort((short) 32767);
			buffer.writeInt(value);
		}
		else
		{
			buffer.writeShort(value);
		}
	}

	@Override
	protected boolean write(GameClient client, WritableBuffer buffer)
	{
		GameClient c = client;
		if (client != null && !client.isDetached() && client.getConnectionState() != ConnectionState.DISCONNECTED)
		{
			try
			{
				this.writeImpl(c, buffer);
				return true;
			}
			catch (Exception var5)
			{
				PacketLogger.warning("Error writing packet " + this + " to client (" + var5.getMessage() + ") " + client + "]]");
				PacketLogger.warning(TraceUtil.getStackTrace(var5));
				return false;
			}
		}
		return true;
	}

	public void runImpl(Player player)
	{
	}

	protected abstract void writeImpl(GameClient var1, WritableBuffer var2) throws Exception;
}
