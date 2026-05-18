package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class RelationChanged extends ServerPacket
{
	public static final int RELATION_PARTY1 = 1;
	public static final int RELATION_PARTY2 = 2;
	public static final int RELATION_PARTY3 = 4;
	public static final int RELATION_PARTY4 = 8;
	public static final int RELATION_PARTYLEADER = 16;
	public static final int RELATION_HAS_PARTY = 32;
	public static final int RELATION_CLAN_MEMBER = 64;
	public static final int RELATION_LEADER = 128;
	public static final int RELATION_CLAN_MATE = 256;
	public static final int RELATION_INSIEGE = 512;
	public static final int RELATION_ATTACKER = 1024;
	public static final int RELATION_ALLY = 2048;
	public static final int RELATION_ENEMY = 4096;
	public static final int RELATION_DECLARED_WAR = 8192;
	public static final int RELATION_MUTUAL_WAR = 24576;
	public static final int RELATION_ALLY_MEMBER = 65536;
	public static final int RELATION_TERRITORY_WAR = 524288;
	public static final int RELATION_DEATH_KNIGHT_PK = 536870912;
	public static final long RELATION_SURVEILLANCE = 2147483648L;
	public static final byte SEND_DEFAULT = 1;
	public static final byte SEND_ONE = 2;
	public static final byte SEND_MULTI = 4;
	private RelationChanged.Relation _singled;
	private final List<RelationChanged.Relation> _multi;
	private byte _mask = 0;

	public RelationChanged(Playable activeChar, long relation, boolean autoattackable)
	{
		this._mask = (byte) (this._mask | 2);
		this._singled = new RelationChanged.Relation();
		this._singled._objId = activeChar.getObjectId();
		this._singled._relation = relation;
		this._singled._autoAttackable = autoattackable;
		this._singled._reputation = activeChar.getReputation();
		this._singled._pvpFlag = activeChar.getPvpFlag();
		this._multi = null;
	}

	public RelationChanged()
	{
		this._mask = (byte) (this._mask | 4);
		this._multi = new LinkedList<>();
	}

	public void addRelation(Playable activeChar, long relation, boolean autoattackable)
	{
		if (!activeChar.isInvisible())
		{
			RelationChanged.Relation r = new RelationChanged.Relation();
			r._objId = activeChar.getObjectId();
			r._relation = relation;
			r._autoAttackable = autoattackable;
			r._reputation = activeChar.getReputation();
			r._pvpFlag = activeChar.getPvpFlag();
			this._multi.add(r);
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.RELATION_CHANGED.writeId(this, buffer);
		buffer.writeByte(this._mask);
		if (this._multi == null)
		{
			this.writeRelation(this._singled, buffer);
		}
		else
		{
			buffer.writeShort(this._multi.size());

			for (RelationChanged.Relation r : this._multi)
			{
				this.writeRelation(r, buffer);
			}
		}
	}

	private void writeRelation(RelationChanged.Relation relation, WritableBuffer buffer)
	{
		buffer.writeInt(relation._objId);
		if ((this._mask & 1) != 1)
		{
			buffer.writeLong(relation._relation);
			buffer.writeByte(relation._autoAttackable);
			buffer.writeInt(relation._reputation);
			buffer.writeByte(relation._pvpFlag);
		}
	}

	protected static class Relation
	{
		int _objId;
		long _relation;
		boolean _autoAttackable;
		int _reputation;
		int _pvpFlag;
	}
}
