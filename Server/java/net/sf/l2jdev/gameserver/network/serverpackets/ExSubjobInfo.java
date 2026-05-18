package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.SubclassInfoType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.SubclassType;
import net.sf.l2jdev.gameserver.model.actor.holders.player.SubClassHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExSubjobInfo extends ServerPacket
{
	private final int _currClassId;
	private final int _currRace;
	private final int _type;
	private final List<ExSubjobInfo.SubInfo> _subs;

	public ExSubjobInfo(Player player, SubclassInfoType type)
	{
		this._currClassId = player.getPlayerClass().getId();
		this._currRace = player.getRace().ordinal();
		this._type = type.ordinal();
		this._subs = new ArrayList<>();
		this._subs.add(0, new ExSubjobInfo.SubInfo(player));

		for (SubClassHolder sub : player.getSubClasses().values())
		{
			this._subs.add(new ExSubjobInfo.SubInfo(sub));
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SUBJOB_INFO.writeId(this, buffer);
		buffer.writeByte(this._type);
		buffer.writeInt(this._currClassId);
		buffer.writeInt(this._currRace);
		buffer.writeInt(this._subs.size());

		for (ExSubjobInfo.SubInfo sub : this._subs)
		{
			buffer.writeInt(sub.getIndex());
			buffer.writeInt(sub.getClassId());
			buffer.writeInt(sub.getLevel());
			buffer.writeByte(sub.getType());
		}
	}

	private class SubInfo
	{
		private final int _index;
		private final int _classId;
		private final int _level;
		private final int _type;

		public SubInfo(SubClassHolder sub)
		{
			Objects.requireNonNull(ExSubjobInfo.this);
			super();
			this._index = sub.getClassIndex();
			this._classId = sub.getId();
			this._level = sub.getLevel();
			this._type = sub.isDualClass() ? SubclassType.DUALCLASS.ordinal() : SubclassType.SUBCLASS.ordinal();
		}

		public SubInfo(Player player)
		{
			Objects.requireNonNull(ExSubjobInfo.this);
			super();
			this._index = 0;
			this._classId = player.getBaseClass();
			this._level = player.getStat().getBaseLevel();
			this._type = SubclassType.BASECLASS.ordinal();
		}

		public int getIndex()
		{
			return this._index;
		}

		public int getClassId()
		{
			return this._classId;
		}

		public int getLevel()
		{
			return this._level;
		}

		public int getType()
		{
			return this._type;
		}
	}
}
