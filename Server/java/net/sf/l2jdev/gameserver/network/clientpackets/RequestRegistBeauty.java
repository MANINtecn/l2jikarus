package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.xml.BeautyShopData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.beautyshop.BeautyData;
import net.sf.l2jdev.gameserver.model.beautyshop.BeautyItem;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.serverpackets.ExResponseBeautyList;
import net.sf.l2jdev.gameserver.network.serverpackets.ExResponseBeautyRegistReset;

public class RequestRegistBeauty extends ClientPacket
{
	private int _hairId;
	private int _faceId;
	private int _colorId;

	@Override
	protected void readImpl()
	{
		this._hairId = this.readInt();
		this._faceId = this.readInt();
		this._colorId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			BeautyData beautyData = BeautyShopData.getInstance().getBeautyData(player.getRace(), player.getAppearance().getSexType());
			int requiredAdena = 0;
			int requiredBeautyShopTicket = 0;
			if (this._hairId > 0)
			{
				BeautyItem hair = beautyData.getHairList().get(this._hairId);
				if (hair == null)
				{
					player.sendPacket(new ExResponseBeautyRegistReset(player, 0, 0));
					player.sendPacket(new ExResponseBeautyList(player, 1));
					return;
				}

				if (hair.getId() != player.getVisualHair())
				{
					requiredAdena += hair.getAdena();
					requiredBeautyShopTicket += hair.getBeautyShopTicket();
				}

				if (this._colorId > 0)
				{
					BeautyItem color = hair.getColors().get(this._colorId);
					if (color == null)
					{
						player.sendPacket(new ExResponseBeautyRegistReset(player, 0, 0));
						player.sendPacket(new ExResponseBeautyList(player, 1));
						return;
					}

					requiredAdena += color.getAdena();
					requiredBeautyShopTicket += color.getBeautyShopTicket();
				}
			}

			if (this._faceId > 0 && this._faceId != player.getVisualFace())
			{
				BeautyItem face = beautyData.getFaceList().get(this._faceId);
				if (face == null)
				{
					player.sendPacket(new ExResponseBeautyRegistReset(player, 0, 0));
					player.sendPacket(new ExResponseBeautyList(player, 1));
					return;
				}

				requiredAdena += face.getAdena();
				requiredBeautyShopTicket += face.getBeautyShopTicket();
			}

			if (player.getAdena() < requiredAdena || player.getBeautyTickets() < requiredBeautyShopTicket)
			{
				player.sendPacket(new ExResponseBeautyRegistReset(player, 0, 0));
				player.sendPacket(new ExResponseBeautyList(player, 1));
			}
			else if (requiredAdena > 0 && !player.reduceAdena(ItemProcessType.FEE, requiredAdena, null, true))
			{
				player.sendPacket(new ExResponseBeautyRegistReset(player, 0, 0));
				player.sendPacket(new ExResponseBeautyList(player, 1));
			}
			else if (requiredBeautyShopTicket > 0 && !player.reduceBeautyTickets(ItemProcessType.FEE, requiredBeautyShopTicket, null, true))
			{
				player.sendPacket(new ExResponseBeautyRegistReset(player, 0, 0));
				player.sendPacket(new ExResponseBeautyList(player, 1));
			}
			else
			{
				if (this._hairId > 0)
				{
					player.setVisualHair(this._hairId);
				}

				if (this._colorId > 0)
				{
					player.setVisualHairColor(this._colorId);
				}

				if (this._faceId > 0)
				{
					player.setVisualFace(this._faceId);
				}

				player.sendPacket(new ExResponseBeautyRegistReset(player, 0, 1));
			}
		}
	}
}
