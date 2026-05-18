package net.sf.l2jdev.gameserver.network.clientpackets.characterstyle;

import net.sf.l2jdev.gameserver.data.enums.CharacterStyleCategoryType;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.characterstyle.ExCharacterStyleUpdateFavorite;

public class ExRequestCharacterStyleUpdateFavorite extends ClientPacket
{
	private int _styleType;
	private int _styleId;
	private boolean _isFavorite;

	@Override
	protected void readImpl()
	{
		this._styleType = this.readInt();
		this._styleId = this.readInt();
		this._isFavorite = this.readBoolean();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			CharacterStyleCategoryType category = CharacterStyleCategoryType.getByClientId(this._styleType);
			player.modifyCharacterStyle(category, this._styleId, true, this._isFavorite);
			player.sendPacket(ExCharacterStyleUpdateFavorite.STATIC_PACKET_UPDATE);
		}
	}
}
