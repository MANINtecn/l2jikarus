package net.sf.l2jdev.gameserver.network.serverpackets.pledgebonus;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.ClanRewardData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanRewardBonus;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanRewardType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPledgeBonusOpen extends ServerPacket
{
	private Clan _clan;
	private ClanRewardBonus _highestMembersOnlineBonus;
	private ClanRewardBonus _highestHuntingBonus;
	private ClanRewardBonus _membersOnlineBonus;
	private ClanRewardBonus _huntingBonus;
	private boolean _canClaimMemberReward;
	private boolean _canClaimHuntingReward;

	public ExPledgeBonusOpen(Player player)
	{
		this._clan = player.getClan();
		if (this._clan == null)
		{
			PacketLogger.warning("Player: " + player + " attempting to write to a null clan!");
		}
		else
		{
			ClanRewardData data = ClanRewardData.getInstance();
			this._highestMembersOnlineBonus = data.getHighestReward(ClanRewardType.MEMBERS_ONLINE);
			this._highestHuntingBonus = data.getHighestReward(ClanRewardType.HUNTING_MONSTERS);
			this._membersOnlineBonus = ClanRewardType.MEMBERS_ONLINE.getAvailableBonus(this._clan);
			this._huntingBonus = ClanRewardType.HUNTING_MONSTERS.getAvailableBonus(this._clan);
			if (this._highestMembersOnlineBonus == null)
			{
				PacketLogger.warning("Couldn't find highest available clan members online bonus!!");
				this._clan = null;
			}
			else if (this._highestHuntingBonus == null)
			{
				PacketLogger.warning("Couldn't find highest available clan hunting bonus!!");
				this._clan = null;
			}
			else if (this._highestMembersOnlineBonus.getSkillReward() == null)
			{
				PacketLogger.warning("Couldn't find skill reward for highest available members online bonus!!");
				this._clan = null;
			}
			else if (this._highestHuntingBonus.getSkillReward() == null)
			{
				PacketLogger.warning("Couldn't find skill reward for highest available hunting bonus!!");
				this._clan = null;
			}
			else
			{
				this._canClaimMemberReward = this._clan.canClaimBonusReward(player, ClanRewardType.MEMBERS_ONLINE);
				this._canClaimHuntingReward = this._clan.canClaimBonusReward(player, ClanRewardType.HUNTING_MONSTERS);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (this._clan != null)
		{
			ServerPackets.EX_PLEDGE_BONUS_UI_OPEN.writeId(this, buffer);
			buffer.writeInt(this._highestMembersOnlineBonus.getRequiredAmount());
			buffer.writeInt(this._clan.getMaxOnlineMembers());
			buffer.writeByte(2);
			buffer.writeInt(this._membersOnlineBonus != null ? this._highestMembersOnlineBonus.getSkillReward().getSkillId() : 0);
			buffer.writeByte(this._membersOnlineBonus != null ? this._membersOnlineBonus.getLevel() : 0);
			buffer.writeByte(this._canClaimMemberReward);
			buffer.writeInt(this._highestHuntingBonus.getRequiredAmount());
			buffer.writeInt(this._clan.getHuntingPoints());
			buffer.writeByte(2);
			buffer.writeInt(this._huntingBonus != null ? this._highestHuntingBonus.getSkillReward().getSkillId() : 0);
			buffer.writeByte(this._huntingBonus != null ? this._huntingBonus.getLevel() : 0);
			buffer.writeByte(this._canClaimHuntingReward);
		}
	}
}
