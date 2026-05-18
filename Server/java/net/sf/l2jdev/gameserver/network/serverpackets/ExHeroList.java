package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Map;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.olympiad.Hero;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExHeroList extends ServerPacket
{
	private final Map<Integer, StatSet> _heroList = Hero.getInstance().getHeroes();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_HERO_LIST.writeId(this, buffer);
		buffer.writeInt(this._heroList.size());

		for (StatSet hero : this._heroList.values())
		{
			buffer.writeString(hero.getString("char_name"));
			buffer.writeInt(hero.getInt("class_id"));
			buffer.writeString(hero.getString("clan_name", ""));
			buffer.writeInt(0);
			buffer.writeString(hero.getString("ally_name", ""));
			buffer.writeInt(0);
			buffer.writeInt(hero.getInt("count"));
			buffer.writeInt(ServerConfig.SERVER_ID);
			buffer.writeByte(0);
		}
	}
}
