package net.sf.l2jdev.gameserver.network.clientpackets.enchant.challengepoint;

import net.sf.l2jdev.gameserver.data.xml.EnchantItemData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.model.item.enchant.EnchantScroll;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.challengepoint.ExResetEnchantChallengePoint;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.single.ExChangedEnchantTargetItemProbList;

public class ExRequestResetEnchantChallengePoint extends ClientPacket
{
	@Override
	protected void readImpl()
	{
		this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.getChallengeInfo().setChallengePointsPendingRecharge(-1, -1);
			player.sendPacket(new ExResetEnchantChallengePoint(true));
			EnchantItemRequest request = player.getRequest(EnchantItemRequest.class);
			if (request != null && !request.isProcessing())
			{
				EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(request.getEnchantingScroll());
				double chance = scrollTemplate.getChance(player, request.getEnchantingItem());
				int crystalLevel = request.getEnchantingItem().getTemplate().getCrystalType().getLevel();
				double enchantRateStat = crystalLevel > CrystalType.NONE.getLevel() && crystalLevel < CrystalType.EVENT.getLevel() ? player.getStat().getValue(Stat.ENCHANT_RATE) : 0.0;
				player.sendPacket(new ExChangedEnchantTargetItemProbList(new ExChangedEnchantTargetItemProbList.EnchantProbInfo(request.getEnchantingItem().getObjectId(), (int) ((chance + enchantRateStat) * 100.0), (int) (chance * 100.0), 0, (int) (enchantRateStat * 100.0))));
			}
		}
	}
}
