package net.sf.l2jdev.gameserver.network.serverpackets.worldexchange;

import java.util.List;
import java.util.Map;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.WorldExchangeManager;
import net.sf.l2jdev.gameserver.model.VariationInstance;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulOption;
import net.sf.l2jdev.gameserver.model.item.enums.WorldExchangeItemStatusType;
import net.sf.l2jdev.gameserver.model.item.holders.WorldExchangeHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class WorldExchangeSettleList extends ServerPacket
{
	private final Player _player;

	public WorldExchangeSettleList(Player player)
	{
		this._player = player;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_WORLD_EXCHANGE_SETTLE_LIST.writeId(this, buffer);
		Map<WorldExchangeItemStatusType, List<WorldExchangeHolder>> holders = WorldExchangeManager.getInstance().getPlayerBids(this._player.getObjectId());
		if (holders.isEmpty())
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
		else
		{
			buffer.writeInt(holders.get(WorldExchangeItemStatusType.WORLD_EXCHANGE_REGISTERED).size());

			for (WorldExchangeHolder holder : holders.get(WorldExchangeItemStatusType.WORLD_EXCHANGE_REGISTERED))
			{
				this.getItemInfo(buffer, holder);
			}

			buffer.writeInt(holders.get(WorldExchangeItemStatusType.WORLD_EXCHANGE_SOLD).size());

			for (WorldExchangeHolder holder : holders.get(WorldExchangeItemStatusType.WORLD_EXCHANGE_SOLD))
			{
				this.getItemInfo(buffer, holder);
			}

			buffer.writeInt(holders.get(WorldExchangeItemStatusType.WORLD_EXCHANGE_OUT_TIME).size());

			for (WorldExchangeHolder holder : holders.get(WorldExchangeItemStatusType.WORLD_EXCHANGE_OUT_TIME))
			{
				this.getItemInfo(buffer, holder);
			}

			buffer.writeInt(0);
		}
	}

	protected void getItemInfo(WritableBuffer buffer, WorldExchangeHolder holder)
	{
		buffer.writeLong(holder.getWorldExchangeId());
		buffer.writeLong(holder.getPrice());
		buffer.writeInt((int) (holder.getEndTime() / 1000L));
		Item item = holder.getItemInstance();
		buffer.writeInt(item.getId());
		buffer.writeLong(item.getCount());
		buffer.writeInt(item.getEnchantLevel() < 1 ? 0 : item.getEnchantLevel());
		VariationInstance augment = item.getAugmentation();
		if (augment != null)
		{
			buffer.writeInt(augment.getOption1Id());
			buffer.writeInt(augment.getOption2Id());
			buffer.writeInt(augment.getOption3Id());
		}
		else
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}

		buffer.writeInt(-1);
		buffer.writeShort(item.getAttackAttribute() != null ? item.getAttackAttribute().getType().getClientId() : 0);
		buffer.writeShort(item.getAttackAttribute() != null ? item.getAttackAttribute().getValue() : 0);
		buffer.writeShort(item.getDefenceAttribute(AttributeType.FIRE));
		buffer.writeShort(item.getDefenceAttribute(AttributeType.WATER));
		buffer.writeShort(item.getDefenceAttribute(AttributeType.WIND));
		buffer.writeShort(item.getDefenceAttribute(AttributeType.EARTH));
		buffer.writeShort(item.getDefenceAttribute(AttributeType.HOLY));
		buffer.writeShort(item.getDefenceAttribute(AttributeType.DARK));
		buffer.writeInt(item.getVisualId());
		List<EnsoulOption> soul = (List<EnsoulOption>) holder.getItemInfo().getSoulCrystalOptions();

		try
		{
			buffer.writeInt(soul.get(0).getId());
		}
		catch (IndexOutOfBoundsException var10)
		{
			buffer.writeInt(0);
		}

		try
		{
			buffer.writeInt(soul.get(1).getId());
		}
		catch (IndexOutOfBoundsException var9)
		{
			buffer.writeInt(0);
		}

		List<EnsoulOption> specialSoul = (List<EnsoulOption>) holder.getItemInfo().getSoulCrystalSpecialOptions();

		try
		{
			buffer.writeInt(specialSoul.get(0).getId());
		}
		catch (IndexOutOfBoundsException var8)
		{
			buffer.writeInt(0);
		}

		buffer.writeShort(item.isBlessed());
	}
}
