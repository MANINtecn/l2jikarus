package net.sf.l2jdev.gameserver.network.serverpackets.relics;

import java.util.Comparator;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.RelicSystemConfig;
import net.sf.l2jdev.gameserver.data.holders.RelicSummonCategoryHolder;
import net.sf.l2jdev.gameserver.data.xml.RelicData;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRelicsSummonList extends ServerPacket
{
	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (RelicSystemConfig.RELIC_SYSTEM_ENABLED)
		{
			ServerPackets.EX_RELICS_SUMMON_LIST.writeId(this, buffer);
			buffer.writeInt(RelicData.getInstance().getRelicActiveCategories().size());

			for (RelicSummonCategoryHolder category : RelicData.getInstance().getRelicActiveCategories().stream().sorted(Comparator.comparingInt(RelicSummonCategoryHolder::getCategoryId)).toList())
			{
				buffer.writeInt(category.getCategoryId());
				buffer.writeInt(0);
			}
		}
	}
}
