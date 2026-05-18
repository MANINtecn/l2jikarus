package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.data.xml.BuyListData;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.buylist.ProductList;
import net.sf.l2jdev.gameserver.model.siege.TaxType;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ExBuySellList;

public class Merchant extends Folk
{
	public Merchant(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Merchant);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return attacker.isMonster() ? true : super.isAutoAttackable(attacker);
	}

	@Override
	public String getHtmlPath(int npcId, int value, Player player)
	{
		String pom;
		if (value == 0)
		{
			pom = Integer.toString(npcId);
		}
		else
		{
			pom = npcId + "-" + value;
		}

		return "data/html/merchant/" + pom + ".htm";
	}

	public void showBuyWindow(Player player, int value)
	{
		this.showBuyWindow(player, value, true);
	}

	public void showBuyWindow(Player player, int value, boolean applyCastleTax)
	{
		ProductList buyList = BuyListData.getInstance().getBuyList(value);
		if (buyList == null)
		{
			LOGGER.warning("BuyList not found! BuyListId:" + value);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if (!buyList.isNpcAllowed(this.getId()))
		{
			LOGGER.warning("Npc not allowed in BuyList! BuyListId:" + value + " NpcId:" + this.getId());
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.setInventoryBlockingStatus(true);
			player.sendPacket(new ExBuySellList(buyList, player, applyCastleTax ? this.getCastleTaxRate(TaxType.BUY) : 0.0));
			player.sendPacket(new ExBuySellList(player, false));
			player.sendPacket(new ExBuySellList());
		}
	}
}
