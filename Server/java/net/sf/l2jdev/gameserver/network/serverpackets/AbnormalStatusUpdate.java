package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class AbnormalStatusUpdate extends ServerPacket
{
	private final List<BuffInfo> _effects = new ArrayList<>();

	public void addSkill(BuffInfo info)
	{
		if (!info.getSkill().isHealingPotionSkill())
		{
			this._effects.add(info);
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.ABNORMAL_STATUS_UPDATE.writeId(this, buffer);
		buffer.writeShort(this._effects.size());

		for (BuffInfo info : this._effects)
		{
			if (info != null && info.isInUse())
			{
				Skill skill = info.getSkill();
				buffer.writeInt(skill.getDisplayId());
				buffer.writeShort(skill.getDisplayLevel());
				buffer.writeShort(skill.getSubLevel());
				buffer.writeInt(skill.getAbnormalType().getClientId());
				this.writeOptionalInt(!skill.isAura() && !skill.isToggle() ? info.getTime() : -1, buffer);
			}
		}
	}
}
