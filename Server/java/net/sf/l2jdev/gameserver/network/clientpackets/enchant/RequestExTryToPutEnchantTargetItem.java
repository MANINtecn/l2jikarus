package net.sf.l2jdev.gameserver.network.clientpackets.enchant;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.xml.EnchantChallengePointData;
import net.sf.l2jdev.gameserver.data.xml.EnchantItemData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.model.item.enchant.EnchantScroll;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.EnchantResult;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.ExPutEnchantScrollItemResult;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.ExPutEnchantTargetItemResult;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.single.ChangedEnchantTargetItemProbabilityList;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.single.ExChangedEnchantTargetItemProbList;

public class RequestExTryToPutEnchantTargetItem extends ClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
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
				Item scroll = request.getEnchantingScroll();
				if (scroll != null)
				{
					Item item = player.getInventory().getItemByObjectId(this._objectId);
					if (item == null)
					{
						PunishmentManager.handleIllegalPlayerAction(player, "RequestExTryToPutEnchantTargetItem: " + player + " tried to cheat using a packet manipulation tool! Ban this player!", GeneralConfig.DEFAULT_PUNISH);
					}
					else
					{
						EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);
						if (item.isEnchantable() && scrollTemplate != null && scrollTemplate.isValid(item, null) && item.getEnchantLevel() < scrollTemplate.getMaxEnchantLevel())
						{
							request.setEnchantingItem(this._objectId);
							request.setEnchantLevel(item.getEnchantLevel());
							request.setTimestamp(System.currentTimeMillis());
							player.sendPacket(new ExPutEnchantTargetItemResult(this._objectId));
							player.sendPacket(new ChangedEnchantTargetItemProbabilityList(player, false));
							double chance = scrollTemplate.getChance(player, item);
							if (chance > 0.0)
							{
								double challengePointsChance = 0.0;
								EnchantChallengePointData.EnchantChallengePointsItemInfo info = EnchantChallengePointData.getInstance().getInfoByItemId(item.getId());
								if (info != null)
								{
									int groupId = info.groupId();
									int pendingGroupId = player.getChallengeInfo().getChallengePointsPendingRecharge()[0];
									int pendingOptionIndex = player.getChallengeInfo().getChallengePointsPendingRecharge()[1];
									if (pendingGroupId == groupId && (pendingOptionIndex == 0 || pendingOptionIndex == 1))
									{
										EnchantChallengePointData.EnchantChallengePointsOptionInfo optionInfo = EnchantChallengePointData.getInstance().getOptionInfo(pendingGroupId, pendingOptionIndex);
										if (optionInfo != null && item.getEnchantLevel() >= optionInfo.minEnchant() && item.getEnchantLevel() <= optionInfo.maxEnchant())
										{
											challengePointsChance = optionInfo.chance();
											player.getChallengeInfo().setChallengePointsPendingRecharge(-1, -1);
										}
									}
								}

								int crystalLevel = item.getTemplate().getCrystalType().getLevel();
								double enchantRateStat = crystalLevel > CrystalType.NONE.getLevel() && crystalLevel < CrystalType.EVENT.getLevel() ? player.getStat().getValue(Stat.ENCHANT_RATE) : 0.0;
								player.sendPacket(new ExChangedEnchantTargetItemProbList(new ExChangedEnchantTargetItemProbList.EnchantProbInfo(item.getObjectId(), (int) ((chance + challengePointsChance + enchantRateStat) * 100.0), (int) (chance * 100.0), (int) (challengePointsChance * 100.0), (int) (enchantRateStat * 100.0))));
							}
						}
						else
						{
							player.sendPacket(SystemMessageId.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
							request.setEnchantingItem(0);
							player.sendPacket(new ExPutEnchantTargetItemResult(0));
							player.sendPacket(new EnchantResult(2, null, null, 0));
							player.sendPacket(new ExPutEnchantScrollItemResult(1));
							if (scrollTemplate == null)
							{
								PacketLogger.warning("RequestExTryToPutEnchantTargetItem: " + player + " has used undefined scroll with id " + scroll.getId());
							}
						}
					}
				}
			}
		}
	}
}
