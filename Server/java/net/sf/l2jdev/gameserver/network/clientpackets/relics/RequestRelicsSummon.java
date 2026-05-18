package net.sf.l2jdev.gameserver.network.clientpackets.relics;

import java.util.List;

import net.sf.l2jdev.gameserver.config.RelicSystemConfig;
import net.sf.l2jdev.gameserver.data.holders.RelicCouponHolder;
import net.sf.l2jdev.gameserver.data.xml.RelicCouponData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.RelicSummonRequest;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.relics.ExRelicsList;
import net.sf.l2jdev.gameserver.network.serverpackets.relics.ExRelicsSummonResult;

public class RequestRelicsSummon extends ClientPacket
{
	private int _couponId;

	@Override
	protected void readImpl()
	{
		this._couponId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			RelicCouponHolder relicCoupon = RelicCouponData.getInstance().getCouponFromCouponItemId(this._couponId);
			if (relicCoupon == null)
			{
				player.sendPacket(SystemMessageId.AN_ERROR_HAS_OCCURRED_PLEASE_TRY_AGAIN_LATER);
				PacketLogger.finer("Relic Coupon: " + this._couponId + " wasn't found in RelicCouponData.xml");
			}
			else if (player.getAccountVariables().getInt("UNCONFIRMED_RELICS_COUNT", 0) == RelicSystemConfig.RELIC_UNCONFIRMED_LIST_LIMIT)
			{
				player.sendPacket(SystemMessageId.SUMMON_COMPOUND_IS_UNAVAILABLE_AS_YOU_HAVE_MORE_THAN_100_UNCONFIRMED_DOLLS);
			}
			else
			{
				Item couponItem = player.getInventory().getItemByItemId(this._couponId);
				if (couponItem != null && player.destroyItem(ItemProcessType.FEE, couponItem, 1L, player, true))
				{
					List<Integer> obtainedRelics = RelicCouponData.getInstance().generateSummonRelics(relicCoupon);
					if (obtainedRelics.isEmpty())
					{
						player.sendPacket(SystemMessageId.AN_ERROR_HAS_OCCURRED_PLEASE_TRY_AGAIN_LATER);
						PacketLogger.finer("Relic Coupon: " + this._couponId + " generated 0 relics!");
					}
					else
					{
						for (int relicId : obtainedRelics)
						{
							player.handleRelicAcquisition(relicId);
							if (RelicSystemConfig.RELIC_SYSTEM_DEBUG_ENABLED)
							{
								player.sendMessage("Summoned relic ID: " + relicId);
							}
						}

						player.sendPacket(new ExRelicsList(player));
						player.sendCombatPower();
						player.addRequest(new RelicSummonRequest(player));
						player.sendPacket(new ExRelicsSummonResult(relicCoupon, obtainedRelics));
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.FAILURE_ALL_ITEMS_HAVE_DISAPPEARED);
				}
			}
		}
	}
}
