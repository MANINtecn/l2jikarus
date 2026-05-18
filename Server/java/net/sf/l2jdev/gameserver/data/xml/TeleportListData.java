package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.holders.TeleportListHolder;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.StatSet;
import org.w3c.dom.Document;

public class TeleportListData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(TeleportListData.class.getName());
	private final Map<Integer, TeleportListHolder> _teleports = new HashMap<>();
	private int _teleportCount = 0;

	protected TeleportListData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._teleports.clear();
		this.parseDatapackFile("data/TeleportListData.xml");
		this._teleportCount = this._teleports.size();
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._teleportCount + " teleports.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "teleport", teleportNode -> {
			StatSet set = new StatSet(this.parseAttributes(teleportNode));
			int tpId = set.getInt("id");
			int tpPrice = set.getInt("price");
			boolean special = set.getBoolean("special", false);
			List<Location> locations = new ArrayList<>();
			this.forEach(teleportNode, "location", locationsNode -> {
				StatSet locationSet = new StatSet(this.parseAttributes(locationsNode));
				locations.add(new Location(locationSet.getInt("x"), locationSet.getInt("y"), locationSet.getInt("z")));
			});
			if (locations.isEmpty())
			{
				locations.add(new Location(set.getInt("x"), set.getInt("y"), set.getInt("z")));
			}

			this._teleports.put(tpId, new TeleportListHolder(tpId, locations, tpPrice, special));
		}));
	}

	public TeleportListHolder getTeleport(int teleportId)
	{
		return this._teleports.get(teleportId);
	}

	public int getTeleportCount()
	{
		return this._teleportCount;
	}

	public static TeleportListData getInstance()
	{
		return TeleportListData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final TeleportListData INSTANCE = new TeleportListData();
	}
}
