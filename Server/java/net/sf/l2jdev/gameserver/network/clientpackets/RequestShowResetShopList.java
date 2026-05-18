package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.xml.BeautyShopData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.beautyshop.BeautyData;
import net.sf.l2jdev.gameserver.model.beautyshop.BeautyItem;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.serverpackets.ExResponseBeautyRegistReset;

public class RequestShowResetShopList extends ClientPacket
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
			if (this._hairId > 0)
			{
				BeautyItem hair = beautyData.getHairList().get(this._hairId);
				if (hair == null)
				{
					player.sendPacket(new ExResponseBeautyRegistReset(player, 1, 0));
					return;
				}

				requiredAdena += hair.getResetAdena();
				if (this._colorId > 0)
				{
					BeautyItem color = hair.getColors().get(this._colorId);
					if (color == null)
					{
						player.sendPacket(new ExResponseBeautyRegistReset(player, 1, 0));
						return;
					}

					requiredAdena += color.getResetAdena();
				}
			}

			if (this._faceId > 0)
			{
				BeautyItem face = beautyData.getFaceList().get(this._faceId);
				if (face == null)
				{
					player.sendPacket(new ExResponseBeautyRegistReset(player, 1, 0));
					return;
				}

				requiredAdena += face.getResetAdena();
			}

			if (player.getAdena() < requiredAdena)
			{
				player.sendPacket(new ExResponseBeautyRegistReset(player, 1, 0));
			}
			else if (requiredAdena > 0 && !player.reduceAdena(ItemProcessType.FEE, requiredAdena, null, true))
			{
				player.sendPacket(new ExResponseBeautyRegistReset(player, 1, 0));
			}
			else
			{
				player.getVariables().remove("visualHairId");
				player.getVariables().remove("visualHairColorId");
				player.getVariables().remove("visualFaceId");
				player.sendPacket(new ExResponseBeautyRegistReset(player, 1, 1));
			}
		}
	}
}
