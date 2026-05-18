package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class RequestChangeNicknameEmote extends ClientPacket
{
	 
	private static final int[] COLORS = new int[]
	{
		9671679,
		8145404,
		9959676,
		16423662,
		16735635,
		64672,
		10528257,
		7903407,
		4743829,
		10066329,
		15981577,
		381942,
		3977716,
		15959027,
		592371,
		15981577,
		0
	};
	private int _colorNum;
	private int _itemId;
	private String _title;

	@Override
	protected void readImpl()
	{
		this._itemId = this.readInt();
		this._colorNum = this.readInt();
		this._title = this.readSizedString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Item item = player.getInventory().getItemByItemId(this._itemId);
			if (item != null && item.getEtcItem() != null && item.getEtcItem().getHandlerName() != null && item.getEtcItem().getHandlerName().equalsIgnoreCase("NicknameColor"))
			{
				if (this._colorNum >= 0 && this._colorNum < COLORS.length)
				{
					if ((this._itemId == 95892 || this._itemId == 94764 || this._itemId == 49662) && player.destroyItem(ItemProcessType.NONE, item, 1L, null, true))
					{
						player.setTitle(this._title);
						player.getAppearance().setTitleColor(COLORS[this._colorNum - 1]);
						player.broadcastUserInfo();
						player.sendPacket(SystemMessageId.YOUR_TITLE_HAS_BEEN_CHANGED);
					}
					else
					{
						if (player.destroyItem(ItemProcessType.NONE, item, 1L, null, true))
						{
							int skyblue = this._colorNum - 2;
							if (skyblue > 11 && player.getLevel() >= 90)
							{
								skyblue = 15;
							}

							player.setTitle(this._title);
							player.sendPacket(SystemMessageId.YOUR_TITLE_HAS_BEEN_CHANGED);
							player.getAppearance().setTitleColor(COLORS[skyblue]);
							player.broadcastUserInfo();
						}
					}
				}
			}
		}
	}
}
