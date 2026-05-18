package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.holders.TeleportListHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import org.w3c.dom.Document;

public class RaidTeleportListData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(RaidTeleportListData.class.getName());
	private final Map<Integer, TeleportListHolder> _teleports = new HashMap<>();

	protected RaidTeleportListData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._teleports.clear();
		this.parseDatapackFile("data/RaidTeleportListData.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._teleports.size() + " teleports.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "teleport", teleportNode -> {
			StatSet set = new StatSet(this.parseAttributes(teleportNode));
			int tpId = set.getInt("id");
			int x = set.getInt("x");
			int y = set.getInt("y");
			int z = set.getInt("z");
			int tpPrice = set.getInt("price");
			this._teleports.put(tpId, new TeleportListHolder(tpId, x, y, z, tpPrice, false));
		}));
	}

	public TeleportListHolder getTeleport(int teleportId)
	{
		return this._teleports.get(teleportId);
	}

	public static RaidTeleportListData getInstance()
	{
		return RaidTeleportListData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final RaidTeleportListData INSTANCE = new RaidTeleportListData();
	}
}
