package net.sf.l2jdev.gameserver.geoengine.pathfinding;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.l2jdev.gameserver.config.GeoEngineConfig;

public class NodeBuffer
{
	public static final int MAX_ITERATIONS = 7000;
	private final ReentrantLock _lock = new ReentrantLock();
	private final int _mapSize;
	private final GeoNode[][] _buffer;
	private final PriorityQueue<GeoNode> _openList;
	private final Set<GeoNode> _closedList;
	private int _baseX = 0;
	private int _baseY = 0;
	private int _targetX = 0;
	private int _targetY = 0;
	private int _targetZ = 0;
	private GeoNode _current = null;

	public NodeBuffer(int size)
	{
		this._mapSize = size;
		this._buffer = new GeoNode[this._mapSize][this._mapSize];
		this._openList = new PriorityQueue<>((a, b) -> a.getFCost() != b.getFCost() ? Double.compare(a.getFCost(), b.getFCost()) : Double.compare(a.getHCost(), b.getHCost()));
		this._closedList = new HashSet<>();
	}

	public final boolean lock()
	{
		return this._lock.tryLock();
	}

	public GeoNode findPath(int x, int y, int z, int tx, int ty, int tz)
	{
		this._baseX = x + (tx - x - this._mapSize) / 2;
		this._baseY = y + (ty - y - this._mapSize) / 2;
		this._targetX = tx;
		this._targetY = ty;
		this._targetZ = tz;
		this._current = this.getNode(x, y, z);
		if (this._current == null)
		{
			return null;
		}
		this._current.setGCost(0.0);
		this._current.setHCost(this.getCost(x, y, z));
		this._current.calculateFCost();
		this._openList.add(this._current);

		for (int count = 0; count < 7000; count++)
		{
			if (this._openList.isEmpty())
			{
				return null;
			}

			this._current = this._openList.poll();
			if (this._current.getLocation().getNodeX() == this._targetX && this._current.getLocation().getNodeY() == this._targetY && Math.abs(this._current.getLocation().getZ() - this._targetZ) < 64)
			{
				return this._current;
			}

			this._closedList.add(this._current);
			this.getNeighbors();
		}

		return null;
	}

	public void free()
	{
		this._current = null;
		this._openList.clear();
		this._closedList.clear();

		for (int i = 0; i < this._mapSize; i++)
		{
			for (int j = 0; j < this._mapSize; j++)
			{
				GeoNode node = this._buffer[i][j];
				if (node != null)
				{
					node.free();
				}
			}
		}

		this._lock.unlock();
	}

	private void getNeighbors()
	{
		if (!this._current.getLocation().canGoNone())
		{
			int x = this._current.getLocation().getNodeX();
			int y = this._current.getLocation().getNodeY();
			int z = this._current.getLocation().getZ();
			GeoNode nodeE = null;
			GeoNode nodeS = null;
			GeoNode nodeW = null;
			GeoNode nodeN = null;
			if (this._current.getLocation().canGoEast())
			{
				nodeE = this.addNode(x + 1, y, z, false);
			}

			if (this._current.getLocation().canGoSouth())
			{
				nodeS = this.addNode(x, y + 1, z, false);
			}

			if (this._current.getLocation().canGoWest())
			{
				nodeW = this.addNode(x - 1, y, z, false);
			}

			if (this._current.getLocation().canGoNorth())
			{
				nodeN = this.addNode(x, y - 1, z, false);
			}

			if (GeoEngineConfig.ADVANCED_DIAGONAL_STRATEGY)
			{
				if (nodeE != null && nodeS != null && nodeE.getLocation().canGoSouth() && nodeS.getLocation().canGoEast())
				{
					this.addNode(x + 1, y + 1, z, true);
				}

				if (nodeS != null && nodeW != null && nodeW.getLocation().canGoSouth() && nodeS.getLocation().canGoWest())
				{
					this.addNode(x - 1, y + 1, z, true);
				}

				if (nodeN != null && nodeE != null && nodeE.getLocation().canGoNorth() && nodeN.getLocation().canGoEast())
				{
					this.addNode(x + 1, y - 1, z, true);
				}

				if (nodeN != null && nodeW != null && nodeW.getLocation().canGoNorth() && nodeN.getLocation().canGoWest())
				{
					this.addNode(x - 1, y - 1, z, true);
				}
			}
		}
	}

	private GeoNode getNode(int x, int y, int z)
	{
		int aX = x - this._baseX;
		if (aX >= 0 && aX < this._mapSize)
		{
			int aY = y - this._baseY;
			if (aY >= 0 && aY < this._mapSize)
			{
				GeoNode result = this._buffer[aX][aY];
				if (result == null)
				{
					result = new GeoNode(new GeoLocation(x, y, z));
					this._buffer[aX][aY] = result;
				}
				else if (!result.isInUse())
				{
					result.setInUse();
					if (result.getLocation() != null)
					{
						result.getLocation().set(x, y, z);
					}
					else
					{
						result.setLoc(new GeoLocation(x, y, z));
					}

					result.resetCosts();
					result.setParent(null);
				}

				return result;
			}
			return null;
		}
		return null;
	}

	private GeoNode addNode(int x, int y, int z, boolean diagonal)
	{
		GeoNode newNode = this.getNode(x, y, z);
		if (newNode == null)
		{
			return null;
		}
		else if (this._closedList.contains(newNode))
		{
			return newNode;
		}
		else
		{
			int geoZ = newNode.getLocation().getZ();
			int stepZ = Math.abs(geoZ - this._current.getLocation().getZ());
			float weight = diagonal ? GeoEngineConfig.DIAGONAL_WEIGHT : GeoEngineConfig.LOW_WEIGHT;
			if (newNode.getLocation().canGoAll() && stepZ <= 16)
			{
				if (this.isHighWeight(x + 1, y, geoZ) || this.isHighWeight(x - 1, y, geoZ) || this.isHighWeight(x, y + 1, geoZ) || this.isHighWeight(x, y - 1, geoZ))
				{
					weight = GeoEngineConfig.MEDIUM_WEIGHT;
				}
			}
			else
			{
				weight = GeoEngineConfig.HIGH_WEIGHT;
			}

			double newGCost = this._current.getGCost() + weight;
			boolean inOpenList = this._openList.contains(newNode);
			if (!inOpenList || newGCost < newNode.getGCost())
			{
				newNode.setParent(this._current);
				newNode.setGCost(newGCost);
				newNode.setHCost(this.getCost(x, y, geoZ));
				newNode.calculateFCost();
				if (!inOpenList)
				{
					this._openList.add(newNode);
				}
				else
				{
					this._openList.remove(newNode);
					this._openList.add(newNode);
				}
			}

			return newNode;
		}
	}

	private boolean isHighWeight(int x, int y, int z)
	{
		GeoNode result = this.getNode(x, y, z);
		return result == null || !result.getLocation().canGoAll() || Math.abs(result.getLocation().getZ() - z) > 16;
	}

	private double getCost(int x, int y, int z)
	{
		int dX = x - this._targetX;
		int dY = y - this._targetY;
		int dZ = z - this._targetZ;
		return Math.sqrt(dX * dX + dY * dY + dZ * dZ / 256.0);
	}
}
