package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExAbnormalStatusUpdateFromTarget extends ServerPacket
{
	private final Creature _creature;
	private final List<BuffInfo> _effects = new ArrayList<>();

	public ExAbnormalStatusUpdateFromTarget(Creature creature)
	{
		this._creature = creature;

		for (BuffInfo info : creature.getEffectList().getEffects())
		{
			if (info != null && info.isInUse() && !info.getSkill().isToggle())
			{
				this._effects.add(info);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ABNORMAL_STATUS_UPDATE_FROM_TARGET.writeId(this, buffer);
		buffer.writeInt(this._creature.getObjectId());
		buffer.writeShort(this._effects.size());

		for (BuffInfo info : this._effects)
		{
			Skill skill = info.getSkill();
			buffer.writeInt(skill.getDisplayId());
			buffer.writeShort(skill.getDisplayLevel());
			buffer.writeShort(skill.getSubLevel());
			buffer.writeShort(skill.getAbnormalType().getClientId());
			this.writeOptionalInt(skill.isAura() ? -1 : info.getTime(), buffer);
			buffer.writeInt(info.getEffectorObjectId());
		}
	}
}
