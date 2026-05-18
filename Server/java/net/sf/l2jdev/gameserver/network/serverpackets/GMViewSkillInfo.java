package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class GMViewSkillInfo extends ServerPacket
{
	private final Player _player;
	private final Collection<Skill> _skills;

	public GMViewSkillInfo(Player player)
	{
		this._player = player;
		this._skills = this._player.getSkillList();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.GM_VIEW_SKILL_INFO.writeId(this, buffer);
		buffer.writeString(this._player.getName());
		buffer.writeInt(this._skills.size());
		boolean isDisabled = this._player.getClan() != null && this._player.getClan().getReputationScore() < 0;

		for (Skill skill : this._skills)
		{
			buffer.writeInt(skill.isPassive());
			buffer.writeShort(skill.getDisplayLevel());
			buffer.writeShort(skill.getSubLevel());
			buffer.writeInt(skill.getDisplayId());
			buffer.writeInt(0);
			buffer.writeByte(isDisabled && skill.isClanSkill());
			buffer.writeByte(skill.isEnchantable());
		}
	}
}
