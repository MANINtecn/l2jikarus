package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldRegion;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.FenceState;
import net.sf.l2jdev.gameserver.model.actor.instance.Fence;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class FenceData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(FenceData.class.getSimpleName());
	public static final int MAX_Z_DIFF = 100;
	private final Map<Integer, Fence> _fences = new ConcurrentHashMap<>();

	protected FenceData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		if (!this._fences.isEmpty())
		{
			this._fences.values().forEach(this::removeFence);
		}

		this.parseDatapackFile("data/FenceData.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._fences.size() + " fences.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "fence", this::spawnFence));
	}

	private void spawnFence(Node fenceNode)
	{
		StatSet set = new StatSet(this.parseAttributes(fenceNode));
		this.spawnFence(set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getString("name"), set.getInt("width"), set.getInt("length"), set.getInt("height"), 0, set.getEnum("state", FenceState.class, FenceState.CLOSED));
	}

	public Fence spawnFence(int x, int y, int z, int width, int length, int height, int instanceId, FenceState state)
	{
		return this.spawnFence(x, y, z, null, width, length, height, instanceId, state);
	}

	public Fence spawnFence(int x, int y, int z, String name, int width, int length, int height, int instanceId, FenceState state)
	{
		Fence fence = new Fence(x, y, name, width, length, height, state);
		if (instanceId > 0)
		{
			fence.setInstanceById(instanceId);
		}

		fence.spawnMe(x, y, z);
		this.addFence(fence);
		return fence;
	}

	private void addFence(Fence fence)
	{
		this._fences.put(fence.getObjectId(), fence);
	}

	public void removeFence(Fence fence)
	{
		this._fences.remove(fence.getObjectId());
	}

	public Map<Integer, Fence> getFences()
	{
		return this._fences;
	}

	public Fence getFence(int objectId)
	{
		return this._fences.get(objectId);
	}

	public int getLoadedElementsCount()
	{
		return this._fences.size();
	}

	public boolean checkIfFenceBetween(int x, int y, int z, int tx, int ty, int tz, Instance instance)
	{
		WorldRegion region = World.getInstance().getRegion(x, y, z);
		Collection<Fence> fences = region != null ? region.getFences() : null;
		if (fences != null && !fences.isEmpty())
		{
			for (Fence fence : fences)
			{
				if (fence.getState().isGeodataEnabled())
				{
					int instanceId = instance == null ? 0 : instance.getId();
					if (fence.getInstanceId() == instanceId)
					{
						int xMin = fence.getXMin();
						int xMax = fence.getXMax();
						int yMin = fence.getYMin();
						int yMax = fence.getYMax();
						if ((x >= xMin || tx >= xMin) && (x <= xMax || tx <= xMax) && (y >= yMin || ty >= yMin) && (y <= yMax || ty <= yMax) && (x <= xMin || tx <= xMin || x >= xMax || tx >= xMax || y <= yMin || ty <= yMin || y >= yMax || ty >= yMax) && (crossLinePart(xMin, yMin, xMax, yMin, x, y, tx, ty, xMin, yMin, xMax, yMax) || crossLinePart(xMax, yMin, xMax, yMax, x, y, tx, ty, xMin, yMin, xMax, yMax) || crossLinePart(xMax, yMax, xMin, yMax, x, y, tx, ty, xMin, yMin, xMax, yMax) || crossLinePart(xMin, yMax, xMin, yMin, x, y, tx, ty, xMin, yMin, xMax, yMax)) && z > fence.getZ() - 100 && z < fence.getZ() + 100)
						{
							return true;
						}
					}
				}
			}

			return false;
		}
		return false;
	}

	private static boolean crossLinePart(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, double xMin, double yMin, double xMax, double yMax)
	{
		double[] result = intersection(x1, y1, x2, y2, x3, y3, x4, y4);
		if (result == null)
		{
			return false;
		}
		double xCross = result[0];
		double yCross = result[1];
		return xCross <= xMax && xCross >= xMin ? true : yCross <= yMax && yCross >= yMin;
	}

	private static double[] intersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	{
		double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		if (d == 0.0)
		{
			return null;
		}
		double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
		double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
		return new double[]
		{
			xi,
			yi
		};
	}

	public static FenceData getInstance()
	{
		return FenceData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final FenceData INSTANCE = new FenceData();
	}
}
