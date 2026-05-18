package net.sf.l2jdev.gameserver.network.clientpackets.worldexchange;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.config.WorldExchangeConfig;
import net.sf.l2jdev.gameserver.config.custom.MultilingualSupportConfig;
import net.sf.l2jdev.gameserver.managers.WorldExchangeManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.WorldExchangeItemSubType;
import net.sf.l2jdev.gameserver.model.item.enums.WorldExchangeSortType;
import net.sf.l2jdev.gameserver.model.item.holders.WorldExchangeHolder;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.worldexchange.WorldExchangeItemList;

public class ExWorldExchangeItemList extends ClientPacket
{
	private int _category;
	private int _sortType;
	private int _page;
	private final List<Integer> _itemIdList = new ArrayList<>();

	@Override
	protected void readImpl()
	{
		this._category = this.readShort();
		this._sortType = this.readByte();
		this._page = this.readInt();
		int size = this.readInt();

		for (int i = 0; i < size; i++)
		{
			this._itemIdList.add(this.readInt());
		}
	}

	@Override
	protected void runImpl()
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				String lang = MultilingualSupportConfig.MULTILANG_ENABLE ? (player.getLang() != null ? player.getLang() : WorldExchangeConfig.WORLD_EXCHANGE_DEFAULT_LANG) : WorldExchangeConfig.WORLD_EXCHANGE_DEFAULT_LANG;
				if (this._itemIdList.isEmpty())
				{
					List<WorldExchangeHolder> holders = WorldExchangeManager.getInstance().getItemBids(player.getObjectId(), WorldExchangeItemSubType.getWorldExchangeItemSubType(this._category), WorldExchangeSortType.getWorldExchangeSortType(this._sortType), lang);
					player.sendPacket(new WorldExchangeItemList(holders, WorldExchangeItemSubType.getWorldExchangeItemSubType(this._category), this._page));
				}
				else
				{
					WorldExchangeManager.getInstance().addCategoryType(this._itemIdList, this._category);
					List<WorldExchangeHolder> holders = WorldExchangeManager.getInstance().getItemBids(this._itemIdList, WorldExchangeSortType.getWorldExchangeSortType(this._sortType), lang);
					player.sendPacket(new WorldExchangeItemList(holders, WorldExchangeItemSubType.getWorldExchangeItemSubType(this._category), this._page));
				}
			}
		}
	}
}
