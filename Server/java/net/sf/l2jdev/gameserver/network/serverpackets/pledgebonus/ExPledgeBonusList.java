package net.sf.l2jdev.gameserver.network.serverpackets.pledgebonus;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.ClanRewardData;
import net.sf.l2jdev.gameserver.model.clan.ClanRewardBonus;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanRewardType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPledgeBonusList extends ServerPacket
{
	private final List<Integer> _memberBonuses = new LinkedList<>();
	private final List<Integer> _huntingBonuses = new LinkedList<>();

	public ExPledgeBonusList()
	{
		ClanRewardData.getInstance().getClanRewardBonuses(ClanRewardType.MEMBERS_ONLINE).stream().sorted(Comparator.comparingInt(ClanRewardBonus::getLevel)).forEach(bonus -> this._memberBonuses.add(bonus.getSkillReward().getSkillId()));
		ClanRewardData.getInstance().getClanRewardBonuses(ClanRewardType.HUNTING_MONSTERS).stream().sorted(Comparator.comparingInt(ClanRewardBonus::getLevel)).forEach(bonus -> this._huntingBonuses.add(bonus.getSkillReward().getSkillId()));
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_BONUS_LIST.writeId(this, buffer);
		buffer.writeByte(0);

		for (int skillId : this._memberBonuses)
		{
			buffer.writeInt(skillId);
		}

		buffer.writeByte(0);

		for (int skillId : this._huntingBonuses)
		{
			buffer.writeInt(skillId);
		}
	}
}
