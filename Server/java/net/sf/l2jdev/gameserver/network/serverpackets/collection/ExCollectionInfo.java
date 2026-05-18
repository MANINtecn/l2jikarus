package net.sf.l2jdev.gameserver.network.serverpackets.collection;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.CollectionData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.PlayerCollectionData;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCollectionInfo extends ServerPacket
{
	final Player _player;
	final int _category;
	final Set<Integer> _collectionIds = new HashSet<>();
	final List<Integer> _favoriteIds;
	final List<ExCollectionInfo.CollectionHolder> _collectionHolders = new LinkedList<>();

	public ExCollectionInfo(Player player, int category)
	{
		this._player = player;
		this._category = category;

		for (PlayerCollectionData collection : player.getCollections())
		{
			if (CollectionData.getInstance().getCollection(collection.getCollectionId()).getCategory() == category)
			{
				this._collectionIds.add(collection.getCollectionId());
			}
		}

		this._favoriteIds = player.getCollectionFavorites();

		for (int id : this._collectionIds)
		{
			ExCollectionInfo.CollectionHolder holder = new ExCollectionInfo.CollectionHolder(id);

			for (PlayerCollectionData collectionx : player.getCollections())
			{
				if (collectionx.getCollectionId() == id)
				{
					holder.addCollectionData(collectionx, CollectionData.getInstance().getCollection(id).getItems().get(collectionx.getIndex()).getEnchantLevel());
				}
			}

			this._collectionHolders.add(holder);
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_COLLECTION_INFO.writeId(this, buffer);
		buffer.writeInt(this._collectionHolders.size());

		for (ExCollectionInfo.CollectionHolder holder : this._collectionHolders)
		{
			List<ExCollectionInfo.CollectionDataHolder> collectionDataList = holder.getCollectionData();
			buffer.writeInt(collectionDataList.size());

			for (ExCollectionInfo.CollectionDataHolder dataHolder : collectionDataList)
			{
				PlayerCollectionData data = dataHolder.getCollectionData();
				buffer.writeByte(data.getIndex());
				buffer.writeInt(data.getItemId());
				buffer.writeByte(dataHolder.getEnchantLevel());
				buffer.writeByte(0);
				buffer.writeByte(0);
				buffer.writeInt(1);
			}

			buffer.writeShort(holder.getCollectionId());
		}

		buffer.writeInt(this._favoriteIds.size());

		for (int id : this._favoriteIds)
		{
			buffer.writeShort(id);
		}

		buffer.writeInt(0);
		buffer.writeByte(this._category);
		buffer.writeShort(0);
	}

	private class CollectionDataHolder
	{
		private final PlayerCollectionData _collectionData;
		private final int _enchantLevel;

		public CollectionDataHolder(PlayerCollectionData collectionData, int enchantLevel)
		{
			Objects.requireNonNull(ExCollectionInfo.this);
			super();
			this._collectionData = collectionData;
			this._enchantLevel = enchantLevel;
		}

		public PlayerCollectionData getCollectionData()
		{
			return this._collectionData;
		}

		public int getEnchantLevel()
		{
			return this._enchantLevel;
		}
	}

	private class CollectionHolder
	{
		private final int _collectionId;
		private final List<ExCollectionInfo.CollectionDataHolder> _collectionData;

		public CollectionHolder(int collectionId)
		{
			Objects.requireNonNull(ExCollectionInfo.this);
			super();
			this._collectionId = collectionId;
			this._collectionData = new LinkedList<>();
		}

		public int getCollectionId()
		{
			return this._collectionId;
		}

		public List<ExCollectionInfo.CollectionDataHolder> getCollectionData()
		{
			return this._collectionData;
		}

		public void addCollectionData(PlayerCollectionData data, int enchantLevel)
		{
			this._collectionData.add(ExCollectionInfo.this.new CollectionDataHolder(data, enchantLevel));
		}
	}
}
