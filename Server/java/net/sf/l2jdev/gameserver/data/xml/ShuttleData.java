package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.VehiclePathPoint;
import net.sf.l2jdev.gameserver.model.actor.instance.Shuttle;
import net.sf.l2jdev.gameserver.model.actor.templates.CreatureTemplate;
import net.sf.l2jdev.gameserver.model.shuttle.ShuttleDataHolder;
import net.sf.l2jdev.gameserver.model.shuttle.ShuttleEngine;
import net.sf.l2jdev.gameserver.model.shuttle.ShuttleStop;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ShuttleData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ShuttleData.class.getName());
	private final Map<Integer, ShuttleDataHolder> _shuttles = new HashMap<>();
	private final Map<Integer, Shuttle> _shuttleInstances = new HashMap<>();

	protected ShuttleData()
	{
		this.load();
	}

	@Override
	public synchronized void load()
	{
		if (!this._shuttleInstances.isEmpty())
		{
			for (Shuttle shuttle : this._shuttleInstances.values())
			{
				shuttle.deleteMe();
			}

			this._shuttleInstances.clear();
		}

		this.parseDatapackFile("data/ShuttleData.xml");
		this.init();
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._shuttles.size() + " shuttles.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("shuttle".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						StatSet set = new StatSet();

						for (int i = 0; i < attrs.getLength(); i++)
						{
							Node att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}

						ShuttleDataHolder data = new ShuttleDataHolder(set);

						for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
						{
							if ("doors".equalsIgnoreCase(b.getNodeName()))
							{
								for (Node a = b.getFirstChild(); a != null; a = a.getNextSibling())
								{
									if ("door".equalsIgnoreCase(a.getNodeName()))
									{
										attrs = a.getAttributes();
										data.addDoor(this.parseInteger(attrs, "id"));
									}
								}
							}
							else if ("stops".equalsIgnoreCase(b.getNodeName()))
							{
								for (Node ax = b.getFirstChild(); ax != null; ax = ax.getNextSibling())
								{
									if ("stop".equalsIgnoreCase(ax.getNodeName()))
									{
										attrs = ax.getAttributes();
										ShuttleStop stop = new ShuttleStop(this.parseInteger(attrs, "id"));

										for (Node z = ax.getFirstChild(); z != null; z = z.getNextSibling())
										{
											if ("dimension".equalsIgnoreCase(z.getNodeName()))
											{
												attrs = z.getAttributes();
												stop.addDimension(new Location(this.parseInteger(attrs, "x"), this.parseInteger(attrs, "y"), this.parseInteger(attrs, "z")));
											}
										}

										data.addStop(stop);
									}
								}
							}
							else if ("routes".equalsIgnoreCase(b.getNodeName()))
							{
								for (Node axx = b.getFirstChild(); axx != null; axx = axx.getNextSibling())
								{
									if ("route".equalsIgnoreCase(axx.getNodeName()))
									{
										attrs = axx.getAttributes();
										List<Location> locs = new ArrayList<>();

										for (Node zx = axx.getFirstChild(); zx != null; zx = zx.getNextSibling())
										{
											if ("loc".equalsIgnoreCase(zx.getNodeName()))
											{
												attrs = zx.getAttributes();
												locs.add(new Location(this.parseInteger(attrs, "x"), this.parseInteger(attrs, "y"), this.parseInteger(attrs, "z")));
											}
										}

										VehiclePathPoint[] route = new VehiclePathPoint[locs.size()];
										int i = 0;

										for (Location loc : locs)
										{
											route[i++] = new VehiclePathPoint(loc);
										}

										data.addRoute(route);
									}
								}
							}
						}

						this._shuttles.put(data.getId(), data);
					}
				}
			}
		}
	}

	private void init()
	{
		for (ShuttleDataHolder data : this._shuttles.values())
		{
			Shuttle shuttle = new Shuttle(new CreatureTemplate(new StatSet()));
			shuttle.setData(data);
			shuttle.setHeading(data.getLocation().getHeading());
			shuttle.setLocationInvisible(data.getLocation());
			shuttle.spawnMe();
			shuttle.getStat().setMoveSpeed(300.0F);
			shuttle.getStat().setRotationSpeed(0);
			shuttle.registerEngine(new ShuttleEngine(data, shuttle));
			shuttle.runEngine(1000);
			this._shuttleInstances.put(shuttle.getObjectId(), shuttle);
		}
	}

	public Shuttle getShuttle(int id)
	{
		for (Shuttle shuttle : this._shuttleInstances.values())
		{
			if (shuttle.getObjectId() == id || shuttle.getId() == id)
			{
				return shuttle;
			}
		}

		return null;
	}

	public static ShuttleData getInstance()
	{
		return ShuttleData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ShuttleData INSTANCE = new ShuttleData();
	}
}
