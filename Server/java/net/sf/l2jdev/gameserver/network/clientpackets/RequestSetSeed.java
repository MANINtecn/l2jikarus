package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.managers.CastleManorManager;
import net.sf.l2jdev.gameserver.model.Seed;
import net.sf.l2jdev.gameserver.model.SeedProduction;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class RequestSetSeed extends ClientPacket
{
 
	private int _manorId;
	private List<SeedProduction> _items;

	@Override
	protected void readImpl()
	{
		this._manorId = this.readInt();
		int count = this.readInt();
		if (count > 0 && count <= PlayerConfig.MAX_ITEM_IN_PACKET && count * 20 == this.remaining())
		{
			this._items = new ArrayList<>(count);

			for (int i = 0; i < count; i++)
			{
				int itemId = this.readInt();
				long sales = this.readLong();
				long price = this.readLong();
				if (itemId < 1 || sales < 0L || price < 0L)
				{
					this._items.clear();
					return;
				}

				if (sales > 0L)
				{
					this._items.add(new SeedProduction(itemId, sales, price, sales));
				}
			}
		}
	}

	@Override
	protected void runImpl()
	{
		if (!this._items.isEmpty())
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				CastleManorManager manor = CastleManorManager.getInstance();
				if (!manor.isModifiablePeriod())
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else if (player.getClan() != null && player.getClan().getCastleId() == this._manorId && player.hasAccess(ClanAccess.CASTLE_MANOR) && player.getLastFolkNPC().canInteract(player))
				{
					List<SeedProduction> list = new ArrayList<>(this._items.size());

					for (SeedProduction sp : this._items)
					{
						Seed s = manor.getSeed(sp.getId());
						if (s != null && sp.getStartAmount() <= s.getSeedLimit() && sp.getPrice() >= s.getSeedMinPrice() && sp.getPrice() <= s.getSeedMaxPrice())
						{
							list.add(sp);
						}
					}

					manor.setNextSeedProduction(list, this._manorId);
				}
				else
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
		}
	}
}
