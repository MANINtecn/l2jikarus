package net.sf.l2jdev.gameserver.network.serverpackets.olympiad;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExOlympiadSpelledInfo extends ServerPacket
{
	private final int _playerId;
	private final List<BuffInfo> _effects = new ArrayList<>();
	private final List<Skill> _effects2 = new ArrayList<>();

	public ExOlympiadSpelledInfo(Player player)
	{
		this._playerId = player.getObjectId();
	}

	public void addSkill(BuffInfo info)
	{
		this._effects.add(info);
	}

	public void addSkill(Skill skill)
	{
		this._effects2.add(skill);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_OLYMPIAD_SPELLED_INFO.writeId(this, buffer);
		buffer.writeInt(this._playerId);
		buffer.writeInt(this._effects.size() + this._effects2.size());

		for (BuffInfo info : this._effects)
		{
			if (info != null && info.isInUse())
			{
				buffer.writeInt(info.getSkill().getDisplayId());
				buffer.writeShort(info.getSkill().getDisplayLevel());
				buffer.writeShort(0);
				buffer.writeInt(info.getSkill().getAbnormalType().getClientId());
				this.writeOptionalInt(info.getSkill().isAura() ? -1 : info.getTime(), buffer);
			}
		}

		for (Skill skill : this._effects2)
		{
			if (skill != null)
			{
				buffer.writeInt(skill.getDisplayId());
				buffer.writeShort(skill.getDisplayLevel());
				buffer.writeShort(0);
				buffer.writeInt(skill.getAbnormalType().getClientId());
				buffer.writeShort(-1);
			}
		}
	}
}
