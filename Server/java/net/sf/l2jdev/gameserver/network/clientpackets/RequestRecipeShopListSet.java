package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.RecipeData;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.ManufactureItem;
import net.sf.l2jdev.gameserver.model.RecipeList;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PrivateStoreType;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.RecipeShopMsg;
import net.sf.l2jdev.gameserver.taskmanagers.AttackStanceTaskManager;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class RequestRecipeShopListSet extends ClientPacket
{
	 
	private ManufactureItem[] _items = null;

	@Override
	protected void readImpl()
	{
		int count = this.readInt();
		if (count > 0 && count <= PlayerConfig.MAX_ITEM_IN_PACKET && count * 12 == this.remaining())
		{
			this._items = new ManufactureItem[count];

			for (int i = 0; i < count; i++)
			{
				int id = this.readInt();
				long cost = this.readLong();
				if (cost < 0L)
				{
					this._items = null;
					return;
				}

				this._items[i] = new ManufactureItem(id, cost);
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._items == null)
			{
				player.setPrivateStoreType(PrivateStoreType.NONE);
				player.broadcastUserInfo();
			}
			else if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) && !player.isInDuel())
			{
				if (player.isInsideZone(ZoneId.NO_STORE))
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_OPEN_A_PRIVATE_WORKSHOP_HERE);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					player.getManufactureItems().clear();

					for (ManufactureItem i : this._items)
					{
						RecipeList list = RecipeData.getInstance().getRecipeList(i.getRecipeId());
						if (!player.getDwarvenRecipeBook().contains(list) && !player.getCommonRecipeBook().contains(list))
						{
							PunishmentManager.handleIllegalPlayerAction(player, "Warning!! " + player + " of account " + player.getAccountName() + " tried to set recipe which he does not have.", GeneralConfig.DEFAULT_PUNISH);
							return;
						}

						if (i.getCost() > Inventory.MAX_ADENA)
						{
							PunishmentManager.handleIllegalPlayerAction(player, "Warning!! " + player + " of account " + player.getAccountName() + " tried to set price more than " + Inventory.MAX_ADENA + " adena in Private Manufacture.", GeneralConfig.DEFAULT_PUNISH);
							return;
						}

						player.getManufactureItems().put(i.getRecipeId(), i);
					}

					player.setStoreName(!player.hasManufactureShop() ? "" : player.getStoreName());
					player.setPrivateStoreType(PrivateStoreType.MANUFACTURE);
					player.sitDown();
					player.broadcastUserInfo();
					Broadcast.toSelfAndKnownPlayers(player, new RecipeShopMsg(player));
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
}
