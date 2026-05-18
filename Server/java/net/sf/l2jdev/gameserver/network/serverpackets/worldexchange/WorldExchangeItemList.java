package net.sf.l2jdev.gameserver.network.serverpackets.worldexchange;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.VariationInstance;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulOption;
import net.sf.l2jdev.gameserver.model.item.enums.WorldExchangeItemSubType;
import net.sf.l2jdev.gameserver.model.item.holders.WorldExchangeHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class WorldExchangeItemList extends ServerPacket
{
 
	private final List<WorldExchangeHolder> _holders;
	private final WorldExchangeItemSubType _type;
	private final int _page;

	public WorldExchangeItemList(List<WorldExchangeHolder> holders, WorldExchangeItemSubType type, int page)
	{
		this._holders = holders;
		this._type = type;
		this._page = page;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_WORLD_EXCHANGE_ITEM_LIST.writeId(this, buffer);
		int totalPages = (int) Math.ceil(this._holders.size() / 100.0);
		int startIndex = this._page == 0 ? 0 : (this._page - 1) * 100;
		int endIndex = Math.min(startIndex + 100, this._holders.size());
		if (!this._holders.isEmpty() && this._page <= totalPages)
		{
			buffer.writeShort(this._type.getId());
			buffer.writeByte(0);
			buffer.writeInt(this._page);
			buffer.writeInt(endIndex - startIndex);

			for (int i = startIndex; i < endIndex; i++)
			{
				this.getItemInfo(buffer, this._holders.get(i));
			}
		}
		else
		{
			buffer.writeShort(0);
			buffer.writeByte(0);
			buffer.writeInt(this._page);
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
			buffer.writeInt(soul != null ? soul.get(0).getId() : 0);
		}
		catch (IndexOutOfBoundsException var10)
		{
			buffer.writeInt(0);
		}

		try
		{
			buffer.writeInt(soul != null ? soul.get(1).getId() : 0);
		}
		catch (IndexOutOfBoundsException var9)
		{
			buffer.writeInt(0);
		}

		List<EnsoulOption> specialSoul = (List<EnsoulOption>) holder.getItemInfo().getSoulCrystalSpecialOptions();

		try
		{
			buffer.writeInt(specialSoul != null ? specialSoul.get(0).getId() : 0);
		}
		catch (IndexOutOfBoundsException var8)
		{
			buffer.writeInt(0);
		}

		buffer.writeShort(item.isBlessed());
	}
}
