package net.sf.l2jdev.gameserver.network.clientpackets.stats;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.variables.PlayerVariables;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExResetStatusBonus extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			PlayerVariables vars = player.getVariables();
			int points = vars.getInt("STAT_STR".toString(), 0) + vars.getInt("STAT_DEX".toString(), 0) + vars.getInt("STAT_CON".toString(), 0) + vars.getInt("STAT_INT".toString(), 0) + vars.getInt("STAT_WIT".toString(), 0) + vars.getInt("STAT_MEN".toString(), 0);
			int adenaCost;
			int lcoinCost;
			if (points < 6)
			{
				lcoinCost = 200;
				adenaCost = 200000;
			}
			else if (points < 11)
			{
				lcoinCost = 300;
				adenaCost = 500000;
			}
			else if (points < 16)
			{
				lcoinCost = 400;
				adenaCost = 1000000;
			}
			else if (points < 21)
			{
				lcoinCost = 500;
				adenaCost = 2000000;
			}
			else if (points < 26)
			{
				lcoinCost = 600;
				adenaCost = 5000000;
			}
			else
			{
				lcoinCost = 700;
				adenaCost = 10000000;
			}

			long adena = player.getAdena();
			long lcoin = player.getInventory().getItemByItemId(91663) == null ? 0L : player.getInventory().getItemByItemId(91663).getCount();
			if (adena >= adenaCost && lcoin >= lcoinCost)
			{
				if (player.reduceAdena(ItemProcessType.FEE, adenaCost, player, true) && player.destroyItemByItemId(ItemProcessType.FEE, 91663, lcoinCost, player, true))
				{
					player.getVariables().remove("STAT_POINTS");
					player.getVariables().remove("STAT_STR");
					player.getVariables().remove("STAT_DEX");
					player.getVariables().remove("STAT_CON");
					player.getVariables().remove("STAT_INT");
					player.getVariables().remove("STAT_WIT");
					player.getVariables().remove("STAT_MEN");
					player.getVariables().set("ELIXIRS_AVAILABLE", player.getVariables().getInt("ELIXIRS_AVAILABLE", 0));
					player.getStat().recalculateStats(true);
					player.calculateStatIncreaseSkills();
					player.updateUserInfo();
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_MONEY_TO_USE_THE_FUNCTION);
			}
		}
	}
}
