package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Arrays;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.NpcStringId;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExSendUIEvent extends ServerPacket
{
	public static final int TYPE_COUNT_DOWN = 0;
	public static final int TYPE_REMOVE = 1;
	public static final int TYPE_ISTINA = 2;
	public static final int TYPE_COUNTER = 3;
	public static final int TYPE_GP_TIMER = 4;
	public static final int TYPE_NORNIL = 5;
	public static final int TYPE_DRACO_INCUBATION_1 = 6;
	public static final int TYPE_DRACO_INCUBATION_2 = 7;
	public static final int TYPE_CLAN_PROGRESS_BAR = 8;
	private final int _objectId;
	private final int _type;
	private final int _countUp;
	private final int _startTime;
	private final int _startTime2;
	private final int _endTime;
	private final int _endTime2;
	private final int _npcstringId;
	private List<String> _params = null;

	public ExSendUIEvent(Player player)
	{
		this(player, 1, 0, 0, 0, 0, 0, -1);
	}

	public ExSendUIEvent(Player player, int uiType, int currentPoints, int maxPoints, NpcStringId npcString, String... params)
	{
		this(player, uiType, -1, currentPoints, maxPoints, -1, -1, npcString.getId(), params);
	}

	public ExSendUIEvent(Player player, boolean hide, boolean countUp, int startTime, int endTime, String text)
	{
		this(player, hide ? 1 : 0, countUp ? 1 : 0, startTime / 60, startTime % 60, endTime / 60, endTime % 60, -1, text);
	}

	public ExSendUIEvent(Player player, boolean hide, boolean countUp, int startTime, int endTime, NpcStringId npcString, String... params)
	{
		this(player, hide ? 1 : 0, countUp ? 1 : 0, startTime / 60, startTime % 60, endTime / 60, endTime % 60, npcString.getId(), params);
	}

	public ExSendUIEvent(Player player, int type, int countUp, int startTime, int startTime2, int endTime, int endTime2, int npcstringId, String... params)
	{
		this._objectId = player.getObjectId();
		this._type = type;
		this._countUp = countUp;
		this._startTime = startTime;
		this._startTime2 = startTime2;
		this._endTime = endTime;
		this._endTime2 = endTime2;
		this._npcstringId = npcstringId;
		this._params = Arrays.asList(params);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SEND_UI_EVENT.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._type);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeString(String.valueOf(this._countUp));
		buffer.writeString(String.valueOf(this._startTime));
		buffer.writeString(String.valueOf(this._startTime2));
		buffer.writeString(String.valueOf(this._endTime));
		buffer.writeString(String.valueOf(this._endTime2));
		buffer.writeInt(this._npcstringId);
		if (this._params != null)
		{
			for (String param : this._params)
			{
				buffer.writeString(param);
			}
		}
	}
}
