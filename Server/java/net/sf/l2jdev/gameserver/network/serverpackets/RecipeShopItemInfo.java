package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.stat.PlayerStat;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class RecipeShopItemInfo extends ServerPacket
{
	private final Player _player;
	private final int _recipeId;
	private final double _craftRate;
	private final double _craftCritical;

	public RecipeShopItemInfo(Player player, int recipeId)
	{
		this._player = player;
		this._recipeId = recipeId;
		PlayerStat stat = this._player.getStat();
		this._craftRate = stat.getValue(Stat.CRAFT_RATE, 0.0);
		this._craftCritical = stat.getValue(Stat.CRAFTING_CRITICAL, 0.0);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.RECIPE_SHOP_ITEM_INFO.writeId(this, buffer);
		buffer.writeInt(this._player.getObjectId());
		buffer.writeInt(this._recipeId);
		buffer.writeInt((int) this._player.getCurrentMp());
		buffer.writeInt(this._player.getMaxMp());
		buffer.writeInt(-1);
		buffer.writeLong(0L);
		buffer.writeByte(0);
		buffer.writeLong(0L);
		buffer.writeDouble(Math.min(this._craftRate, 100.0));
		buffer.writeByte(this._craftCritical > 0.0);
		buffer.writeDouble(Math.min(this._craftCritical, 100.0));
		buffer.writeByte(0);
	}
}
