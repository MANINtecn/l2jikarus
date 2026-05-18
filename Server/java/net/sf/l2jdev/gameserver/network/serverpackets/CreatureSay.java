package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.enums.CharacterStyleCategoryType;
import net.sf.l2jdev.gameserver.managers.MentorManager;
import net.sf.l2jdev.gameserver.managers.RankManager;
import net.sf.l2jdev.gameserver.managers.SharedTeleportManager;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.NpcStringId;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.ChatType;

public class CreatureSay extends ServerPacket
{
	private final Creature _sender;
	private final ChatType _chatType;
	private String _senderName = null;
	private String _text = null;
	private int _charId = 0;
	private int _messageId = -1;
	private int _mask;
	private List<String> _parameters;
	private boolean _shareLocation;

	public CreatureSay(Player sender, Player receiver, String name, ChatType chatType, String text)
	{
		this(sender, receiver, name, chatType, text, false);
	}

	public CreatureSay(Player sender, Player receiver, String name, ChatType chatType, String text, boolean shareLocation)
	{
		this._sender = sender;
		this._senderName = name;
		this._chatType = chatType;
		this._text = text;
		this._shareLocation = shareLocation;
		if (receiver != null)
		{
			if (receiver.getFriendList().contains(sender.getObjectId()))
			{
				this._mask |= 1;
			}

			if (receiver.getClanId() > 0 && receiver.getClanId() == sender.getClanId())
			{
				this._mask |= 2;
			}

			if (MentorManager.getInstance().getMentee(receiver.getObjectId(), sender.getObjectId()) != null || MentorManager.getInstance().getMentee(sender.getObjectId(), receiver.getObjectId()) != null)
			{
				this._mask |= 4;
			}

			if (receiver.getAllyId() > 0 && receiver.getAllyId() == sender.getAllyId())
			{
				this._mask |= 8;
			}
		}

		if (sender.isGM())
		{
			this._mask |= 16;
		}
	}

	public CreatureSay(Creature sender, ChatType chatType, String senderName, String text)
	{
		this(sender, chatType, senderName, text, false);
	}

	public CreatureSay(Creature sender, ChatType chatType, String senderName, String text, boolean shareLocation)
	{
		this._sender = sender;
		this._chatType = chatType;
		this._senderName = senderName;
		this._text = text;
		this._shareLocation = shareLocation;
	}

	public CreatureSay(Creature sender, ChatType chatType, NpcStringId npcStringId)
	{
		this._sender = sender;
		this._chatType = chatType;
		this._messageId = npcStringId.getId();
		if (sender != null)
		{
			this._senderName = sender.getName();
		}
	}

	public CreatureSay(ChatType chatType, int charId, SystemMessageId systemMessageId)
	{
		this._sender = null;
		this._chatType = chatType;
		this._charId = charId;
		this._messageId = systemMessageId.getId();
	}

	public void addStringParameter(String text)
	{
		if (this._parameters == null)
		{
			this._parameters = new ArrayList<>();
		}

		this._parameters.add(text);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SAY2.writeId(this, buffer);
		buffer.writeInt(this._sender == null ? 0 : this._sender.getObjectId());
		buffer.writeInt(this._chatType.getClientId());
		if (this._senderName != null)
		{
			buffer.writeString(this._senderName);
		}
		else
		{
			buffer.writeInt(this._charId);
		}

		buffer.writeInt(this._messageId);
		if (this._text != null)
		{
			buffer.writeString(this._text);
			if (this._sender != null && (this._sender.isPlayer() || this._sender.isFakePlayer()) && this._chatType == ChatType.WHISPER)
			{
				buffer.writeByte(this._mask);
				if ((this._mask & 16) == 0)
				{
					buffer.writeByte(this._sender.getLevel());
				}
			}
		}
		else if (this._parameters != null)
		{
			for (String s : this._parameters)
			{
				buffer.writeString(s);
			}
		}

		if (this._sender != null && this._sender.isPlayer())
		{
			Clan clan = this._sender.getClan();
			if (clan != null && (this._chatType == ChatType.CLAN || this._chatType == ChatType.ALLIANCE))
			{
				buffer.writeByte(0);
			}

			Player player = this._sender.asPlayer();
			int rank = RankManager.getInstance().getPlayerGlobalRank(player);
			if (rank == 0 || rank > 100)
			{
				buffer.writeByte(0);
			}
			else if (rank <= 10)
			{
				buffer.writeByte(1);
			}
			else if (rank <= 50)
			{
				buffer.writeByte(2);
			}
			else if (rank <= 100)
			{
				buffer.writeByte(3);
			}

			if (clan != null)
			{
				buffer.writeByte(clan.getCastleId());
			}
			else
			{
				buffer.writeByte(0);
			}

			if (this._shareLocation)
			{
				buffer.writeByte(1);
				buffer.writeShort(SharedTeleportManager.getInstance().nextId(this._sender));
			}
			else
			{
				buffer.writeInt(0);
			}

			buffer.writeByte(player.isMentee());
			buffer.writeInt(player.getVariables().getInt("ACTIVE_CHARACTER_STYLE_" + CharacterStyleCategoryType.CHAT_BACKGROUND, 0));
		}
		else
		{
			buffer.writeByte(0);
		}
	}

	@Override
	public void runImpl(Player player)
	{
		if (player != null)
		{
			player.broadcastSnoop(this._chatType, this._senderName, this._text, this);
		}
	}
}
