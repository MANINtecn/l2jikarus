package net.sf.l2jdev.gameserver.network.serverpackets.newskillenchant;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExSkillEnchantCharge extends ServerPacket
{
	private final int _skillId;
	private final int _skillresult;

	public ExSkillEnchantCharge(int skillId, int skillresult)
	{
		this._skillId = skillId;
		this._skillresult = skillresult;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SKILL_ENCHANT_CHARGE.writeId(this, buffer);
		buffer.writeInt(this._skillId);
		buffer.writeInt(this._skillresult);
	}
}
