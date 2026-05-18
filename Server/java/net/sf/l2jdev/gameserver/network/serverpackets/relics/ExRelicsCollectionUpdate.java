package net.sf.l2jdev.gameserver.network.serverpackets.relics;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.RelicSystemConfig;
import net.sf.l2jdev.gameserver.data.holders.RelicCollectionDataHolder;
import net.sf.l2jdev.gameserver.data.holders.RelicCollectionInfoHolder;
import net.sf.l2jdev.gameserver.data.xml.OptionData;
import net.sf.l2jdev.gameserver.data.xml.RelicCollectionData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.PlayerRelicCollectionData;
import net.sf.l2jdev.gameserver.model.actor.holders.player.PlayerRelicData;
import net.sf.l2jdev.gameserver.model.options.Options;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRelicsCollectionUpdate extends ServerPacket
{
	private final Player _player;
	private final int _relicId;
	private final int _relicLevel;

	public ExRelicsCollectionUpdate(Player player, int relicId, int relicLevel)
	{
		this._player = player;
		this._relicId = relicId;
		this._relicLevel = relicLevel;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RELICS_COLLECTION_UPDATE.writeId(this, buffer);
		Collection<PlayerRelicData> storedRelics = this._player.getRelics();
		PlayerRelicData existingRelic = null;

		for (PlayerRelicData relic : storedRelics)
		{
			if (relic.getRelicId() == this._relicId && relic.getRelicIndex() == 0)
			{
				existingRelic = relic;
				break;
			}
		}

		if (existingRelic != null)
		{
			int relicId = existingRelic.getRelicId();
			int neededRelicCollectionId = 0;
 
			int neededRelicLevel = 0;

			for (RelicCollectionDataHolder cRelicCollectionHolder : RelicCollectionData.getInstance().getRelicCollections())
			{
				for (RelicCollectionInfoHolder relicData : cRelicCollectionHolder.getRelics())
				{
					if (relicData.getRelicId() == relicId && !this._player.isRelicRegisteredInCollection(relicId, cRelicCollectionHolder.getCollectionId()))
					{
						for (int i = 0; i < cRelicCollectionHolder.getRelics().size(); i++)
						{
							RelicCollectionInfoHolder relicx = cRelicCollectionHolder.getRelic(i);
							if (relicx.getRelicId() == relicId)
							{
								neededRelicCollectionId = cRelicCollectionHolder.getCollectionId();
								neededRelicLevel = cRelicCollectionHolder.getRelic(i).getEnchantLevel();
								if (existingRelic.getRelicLevel() >= neededRelicLevel)
								{
									if (RelicSystemConfig.RELIC_SYSTEM_DEBUG_ENABLED)
									{
										this._player.sendMessage("2.Relic id: " + existingRelic.getRelicId() + " with level: " + existingRelic.getRelicLevel() + " needed in collection: " + neededRelicCollectionId);
									}

									this._player.getRelicCollections().add(new PlayerRelicCollectionData(neededRelicCollectionId, existingRelic.getRelicId(), neededRelicLevel, i));
									this._player.storeRelicCollections();
									this._player.sendPacket(new ExRelicsCollectionInfo(this._player));
									if (RelicSystemConfig.RELIC_SYSTEM_DEBUG_ENABLED)
									{
										this._player.sendMessage("Added Relic Id: " + existingRelic.getRelicId() + " into Collection Id: " + neededRelicCollectionId);
									}
								}
							}
						}
					}
				}
			}

			if (neededRelicCollectionId != 0 && !this._player.isRelicRegistered(existingRelic.getRelicId(), existingRelic.getRelicLevel()) && this._player.isCompleteCollection(neededRelicCollectionId))
			{
				this._player.sendPacket(new ExRelicsCollectionCompleteAnnounce(neededRelicCollectionId));
				Options options = OptionData.getInstance().getOptions(RelicCollectionData.getInstance().getRelicCollection(neededRelicCollectionId).getOptionId());
				if (options != null)
				{
					options.apply(this._player);
					if (RelicSystemConfig.RELIC_SYSTEM_DEBUG_ENABLED)
					{
						this._player.sendMessage("Added Skill for complete collection: " + options.getPassiveSkills());
					}

					this._player.getCombatPower().updateRelicCollectionCombatPower();
					this._player.sendCombatPower();
				}

				buffer.writeInt(1);
				buffer.writeInt(neededRelicCollectionId);
				buffer.writeByte(this._player.isCompleteCollection(neededRelicCollectionId));
				buffer.writeInt(1);
				buffer.writeInt(1);
				buffer.writeInt(this._relicId);
				buffer.writeInt(this._relicLevel);
			}
		}
	}
}
