package net.sf.l2jdev.gameserver.geoengine.pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.GeoEngineConfig;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;

public class PathFinding
{
	private static final Logger LOGGER = Logger.getLogger(PathFinding.class.getName());
	private PathFinding.BufferInfo[] _allBuffers;

	protected PathFinding()
	{
		try
		{
			String[] array = GeoEngineConfig.PATHFIND_BUFFERS.split(";");
			this._allBuffers = new PathFinding.BufferInfo[array.length];

			for (int i = 0; i < array.length; i++)
			{
				String buffer = array[i];
				String[] args = buffer.split("x");
				if (args.length != 2)
				{
					throw new Exception("Invalid buffer definition: " + buffer);
				}

				this._allBuffers[i] = new PathFinding.BufferInfo(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			}
		}
		catch (Exception var5)
		{
			LOGGER.log(Level.WARNING, "CellPathFinding: Problem during buffer init: " + var5.getMessage(), var5);
			throw new Error("CellPathFinding: load aborted");
		}
	}

	public short getNodePos(int geoPos)
	{
		return (short) (geoPos >> 3);
	}

	public short getNodeBlock(int nodePos)
	{
		return (short) (nodePos % 256);
	}

	public byte getRegionX(int nodePos)
	{
		return (byte) ((nodePos >> 8) + 11);
	}

	public byte getRegionY(int nodePos)
	{
		return (byte) ((nodePos >> 8) + 10);
	}

	public short getRegionOffset(byte rx, byte ry)
	{
		return (short) ((rx << 5) + ry);
	}

	public int calculateWorldX(short nodeX)
	{
		return -294912 + nodeX * 128 + 48;
	}

	public int calculateWorldY(short nodeY)
	{
		return -262144 + nodeY * 128 + 48;
	}

	public List<GeoLocation> findPath(int x, int y, int z, int tx, int ty, int tz, Instance instance, boolean playable)
	{
		GeoEngine geoEngine = GeoEngine.getInstance();
		int gx = GeoEngine.getGeoX(x);
		int gy = GeoEngine.getGeoY(y);
		if (!geoEngine.hasGeo(x, y))
		{
			return null;
		}
		int gz = geoEngine.getHeight(x, y, z);
		int gtx = GeoEngine.getGeoX(tx);
		int gty = GeoEngine.getGeoY(ty);
		if (!geoEngine.hasGeo(tx, ty))
		{
			return null;
		}
		int gtz = geoEngine.getHeight(tx, ty, tz);
		NodeBuffer buffer = this.alloc(64 + 2 * Math.max(Math.abs(gx - gtx), Math.abs(gy - gty)));
		if (buffer == null)
		{
			return null;
		}
		List<GeoLocation> path = null;

		try
		{
			GeoNode result = buffer.findPath(gx, gy, gz, gtx, gty, gtz);
			if (result == null)
			{
				return null;
			}

			path = constructPath(result);
		}
		catch (Exception var23)
		{
			LOGGER.log(Level.WARNING, "CellPathFinding: Problem finding path: " + var23.getMessage(), var23);
			return null;
		}
		finally
		{
			buffer.free();
		}

		return path.size() >= 3 && GeoEngineConfig.MAX_POSTFILTER_PASSES > 0 ? applyPostFiltering(path, x, y, z, instance, playable) : path;
	}

	private static List<GeoLocation> applyPostFiltering(List<GeoLocation> initialPath, int startX, int startY, int startZ, Instance instance, boolean playable)
	{
		GeoEngine geoEngine = GeoEngine.getInstance();
		List<GeoLocation> path = initialPath;
		int pass = 0;

		boolean changed;
		List<GeoLocation> optimizedPath;
		do
		{
			pass++;
			changed = false;
			int currentX = startX;
			int currentY = startY;
			int currentZ = startZ;
			optimizedPath = new ArrayList<>();

			for (int i = 0; i < path.size() - 1; i++)
			{
				GeoLocation current = path.get(i);
				GeoLocation next = path.get(i + 1);
				if (geoEngine.canMoveToTarget(currentX, currentY, currentZ, next.getX(), next.getY(), next.getZ(), instance))
				{
					changed = true;
				}
				else
				{
					optimizedPath.add(current);
					currentX = current.getX();
					currentY = current.getY();
					currentZ = current.getZ();
				}
			}

			if (!path.isEmpty())
			{
				optimizedPath.add(path.get(path.size() - 1));
			}

			path = optimizedPath;
		}
		while (playable && changed && optimizedPath.size() > 2 && pass < GeoEngineConfig.MAX_POSTFILTER_PASSES);

		return optimizedPath;
	}

	private static List<GeoLocation> constructPath(GeoNode node)
	{
		List<GeoLocation> path = new ArrayList<>();
		int previousDirectionX = Integer.MIN_VALUE;
		int previousDirectionY = Integer.MIN_VALUE;

		for (GeoNode tempNode = node; tempNode.getParent() != null; tempNode = tempNode.getParent())
		{
			int directionX;
			int directionY;
			if (!GeoEngineConfig.ADVANCED_DIAGONAL_STRATEGY && tempNode.getParent().getParent() != null)
			{
				int tmpX = tempNode.getLocation().getNodeX() - tempNode.getParent().getParent().getLocation().getNodeX();
				int tmpY = tempNode.getLocation().getNodeY() - tempNode.getParent().getParent().getLocation().getNodeY();
				if (Math.abs(tmpX) == Math.abs(tmpY))
				{
					directionX = tmpX;
					directionY = tmpY;
				}
				else
				{
					directionX = tempNode.getLocation().getNodeX() - tempNode.getParent().getLocation().getNodeX();
					directionY = tempNode.getLocation().getNodeY() - tempNode.getParent().getLocation().getNodeY();
				}
			}
			else
			{
				directionX = tempNode.getLocation().getNodeX() - tempNode.getParent().getLocation().getNodeX();
				directionY = tempNode.getLocation().getNodeY() - tempNode.getParent().getLocation().getNodeY();
			}

			if (directionX != previousDirectionX || directionY != previousDirectionY)
			{
				previousDirectionX = directionX;
				previousDirectionY = directionY;
				path.addFirst(tempNode.getLocation());
				tempNode.setLoc(null);
			}
		}

		return path;
	}

	private NodeBuffer alloc(int size)
	{
		NodeBuffer current = null;

		for (PathFinding.BufferInfo info : this._allBuffers)
		{
			if (info.mapSize >= size)
			{
				for (NodeBuffer buffer : info.buffers)
				{
					if (buffer.lock())
					{
						current = buffer;
						break;
					}
				}

				if (current != null)
				{
					break;
				}

				current = new NodeBuffer(info.mapSize);
				current.lock();
				if (info.buffers.size() < info.count)
				{
					info.buffers.add(current);
					break;
				}
			}
		}

		return current;
	}

	public static PathFinding getInstance()
	{
		return PathFinding.SingletonHolder.INSTANCE;
	}

	private static class BufferInfo
	{
		final int mapSize;
		final int count;
		final List<NodeBuffer> buffers;

		public BufferInfo(int size, int cnt)
		{
			this.mapSize = size;
			this.count = cnt;
			this.buffers = Collections.synchronizedList(new ArrayList<>(this.count));
		}
	}

	private static class SingletonHolder
	{
		protected static final PathFinding INSTANCE = new PathFinding();
	}
}
