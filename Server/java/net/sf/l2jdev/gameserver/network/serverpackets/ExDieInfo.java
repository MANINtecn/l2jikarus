package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.holders.player.DamageTakenHolder;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExDieInfo extends ServerPacket
{
	private final Collection<Item> _droppedItems;
	private final Collection<DamageTakenHolder> _lastDamageTaken;

	public ExDieInfo(Collection<Item> droppedItems, Collection<DamageTakenHolder> lastDamageTaken)
	{
		this._droppedItems = droppedItems;
		this._lastDamageTaken = lastDamageTaken;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_DIE_INFO.writeId(this, buffer);
		buffer.writeShort(this._droppedItems.size());

		for (Item item : this._droppedItems)
		{
			buffer.writeInt(item.getId());
			buffer.writeInt(item.getEnchantLevel());
			buffer.writeInt((int) item.getCount());
		}

		buffer.writeShort(this._lastDamageTaken.size());

		for (DamageTakenHolder damageHolder : this._lastDamageTaken)
		{
			if (damageHolder.getCreature().isNpc())
			{
				buffer.writeShort(1);
				buffer.writeInt(damageHolder.getCreature().getId());
				buffer.writeShort(0);
			}
			else
			{
				Clan clan = damageHolder.getCreature().getClan();
				buffer.writeShort(2);
				buffer.writeString(damageHolder.getCreature().getName());
				buffer.writeString(clan == null ? "" : clan.getName());
			}

			buffer.writeInt(damageHolder.getSkillId());
			buffer.writeInt(0);
			buffer.writeDouble(damageHolder.getDamage());
			buffer.writeShort(damageHolder.getClientId());
		}
	}
}
