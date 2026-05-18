package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.custom.MultilingualSupportConfig;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.NpcStringId;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.ChatType;

public class NpcSay extends ServerPacket
{
	private final int _objectId;
	private final ChatType _textType;
	private final int _npcId;
	private String _text;
	private final int _npcString;
	private List<String> _parameters;

	public NpcSay(int objectId, ChatType messageType, int npcId, String text)
	{
		this._objectId = objectId;
		this._textType = messageType;
		this._npcId = 1000000 + npcId;
		this._npcString = -1;
		this._text = text;
	}

	public NpcSay(Npc npc, ChatType messageType, String text)
	{
		this._objectId = npc.getObjectId();
		this._textType = messageType;
		this._npcId = 1000000 + npc.getTemplate().getDisplayId();
		this._npcString = -1;
		this._text = text;
	}

	public NpcSay(int objectId, ChatType messageType, int npcId, NpcStringId npcString)
	{
		this._objectId = objectId;
		this._textType = messageType;
		this._npcId = 1000000 + npcId;
		this._npcString = npcString.getId();
	}

	public NpcSay(Npc npc, ChatType messageType, NpcStringId npcString)
	{
		this._objectId = npc.getObjectId();
		this._textType = messageType;
		this._npcId = 1000000 + npc.getTemplate().getDisplayId();
		this._npcString = npcString.getId();
	}

	public NpcSay addStringParameter(String text)
	{
		if (this._parameters == null)
		{
			this._parameters = new ArrayList<>();
		}

		this._parameters.add(text);
		return this;
	}

	public NpcSay addStringParameters(String... params)
	{
		if (params != null && params.length > 0)
		{
			if (this._parameters == null)
			{
				this._parameters = new ArrayList<>();
			}

			for (String item : params)
			{
				if (item != null && item.length() > 0)
				{
					this._parameters.add(item);
				}
			}
		}

		return this;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.NPC_SAY.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeInt(this._textType.getClientId());
		buffer.writeInt(this._npcId);
		if (MultilingualSupportConfig.MULTILANG_ENABLE)
		{
			Player player = client.getPlayer();
			if (player != null)
			{
				String lang = player.getLang();
				if (lang != null && !lang.equals("en"))
				{
					NpcStringId ns = NpcStringId.getNpcStringId(this._npcString);
					if (ns != null)
					{
						NpcStringId.NSLocalisation nsl = ns.getLocalisation(lang);
						if (nsl != null)
						{
							buffer.writeInt(-1);
							buffer.writeString(nsl.getLocalisation(this._parameters != null ? this._parameters : Collections.emptyList()));
							return;
						}
					}
				}
			}
		}

		buffer.writeInt(this._npcString);
		if (this._npcString == -1)
		{
			buffer.writeString(this._text);
		}
		else if (this._parameters != null)
		{
			for (String s : this._parameters)
			{
				buffer.writeString(s);
			}
		}
	}
}
