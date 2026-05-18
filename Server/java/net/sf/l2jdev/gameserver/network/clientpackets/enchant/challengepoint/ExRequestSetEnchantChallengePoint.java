package net.sf.l2jdev.gameserver.network.clientpackets.enchant.challengepoint;

import net.sf.l2jdev.gameserver.data.xml.EnchantChallengePointData;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.model.item.enchant.EnchantScroll;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.challengepoint.ExEnchantChallengePointInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.challengepoint.ExSetEnchantChallengePoint;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.single.ExChangedEnchantTargetItemProbList;

public class ExRequestSetEnchantChallengePoint extends ClientPacket
{
	private int _useType;
	private boolean _useTicket;

	@Override
	protected void readImpl()
	{
		this._useType = this.readInt();
		this._useTicket = this.readBoolean();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			EnchantItemRequest request = player.getRequest(EnchantItemRequest.class);
			if (request != null && !request.isProcessing())
			{
				Item item = request.getEnchantingItem();
				if (item == null)
				{
					player.sendPacket(new ExSetEnchantChallengePoint(false));
				}
				else
				{
					EnchantChallengePointData.EnchantChallengePointsItemInfo info = EnchantChallengePointData.getInstance().getInfoByItemId(item.getId());
					if (info == null)
					{
						player.sendPacket(new ExSetEnchantChallengePoint(false));
					}
					else
					{
						int groupId = info.groupId();
						if (this._useTicket)
						{
							int remainingRecharges = player.getChallengeInfo().getChallengePointsRecharges(groupId, this._useType);
							if (remainingRecharges <= 0)
							{
								player.sendPacket(new ExSetEnchantChallengePoint(false));
								return;
							}

							player.getChallengeInfo().setChallengePointsPendingRecharge(groupId, this._useType);
							player.sendPacket(new ExSetEnchantChallengePoint(true));
							player.sendPacket(new ExEnchantChallengePointInfo(player));
						}
						else
						{
							int remainingRecharges = player.getChallengeInfo().getChallengePointsRecharges(groupId, this._useType);
							if (remainingRecharges < EnchantChallengePointData.getInstance().getMaxTicketCharge())
							{
								int remainingPoints = player.getChallengeInfo().getChallengePoints().getOrDefault(groupId, 0);
								int fee = EnchantChallengePointData.getInstance().getFeeForOptionIndex(this._useType);
								if (remainingPoints < fee)
								{
									player.sendPacket(new ExSetEnchantChallengePoint(false));
									return;
								}

								remainingPoints -= fee;
								player.getChallengeInfo().getChallengePoints().put(groupId, remainingPoints);
								player.getChallengeInfo().addChallengePointsRecharge(groupId, this._useType, 1);
								player.getChallengeInfo().setChallengePointsPendingRecharge(groupId, this._useType);
								player.sendPacket(new ExSetEnchantChallengePoint(true));
								player.sendPacket(new ExEnchantChallengePointInfo(player));
							}
						}

						EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(request.getEnchantingScroll());
						double chance = scrollTemplate.getChance(player, item);
						double challengePointsChance = 0.0;
						int pendingGroupId = player.getChallengeInfo().getChallengePointsPendingRecharge()[0];
						int pendingOptionIndex = player.getChallengeInfo().getChallengePointsPendingRecharge()[1];
						if (pendingGroupId == groupId && (pendingOptionIndex == 0 || pendingOptionIndex == 1))
						{
							EnchantChallengePointData.EnchantChallengePointsOptionInfo optionInfo = EnchantChallengePointData.getInstance().getOptionInfo(pendingGroupId, pendingOptionIndex);
							if (optionInfo != null && item.getEnchantLevel() >= optionInfo.minEnchant() && item.getEnchantLevel() <= optionInfo.maxEnchant())
							{
								challengePointsChance = optionInfo.chance();
							}
						}

						int crystalLevel = item.getTemplate().getCrystalType().getLevel();
						double enchantRateStat = crystalLevel > CrystalType.NONE.getLevel() && crystalLevel < CrystalType.EVENT.getLevel() ? player.getStat().getValue(Stat.ENCHANT_RATE) : 0.0;
						player.sendPacket(new ExChangedEnchantTargetItemProbList(new ExChangedEnchantTargetItemProbList.EnchantProbInfo(item.getObjectId(), (int) ((chance + challengePointsChance + enchantRateStat) * 100.0), (int) (chance * 100.0), (int) (challengePointsChance * 100.0), (int) (enchantRateStat * 100.0))));
					}
				}
			}
			else
			{
				player.sendPacket(new ExSetEnchantChallengePoint(false));
			}
		}
	}
}
