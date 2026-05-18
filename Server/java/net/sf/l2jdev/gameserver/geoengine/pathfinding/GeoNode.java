package net.sf.l2jdev.gameserver.geoengine.pathfinding;

public class GeoNode
{
	private GeoLocation _location;
	private GeoNode _parent;
	private GeoNode _next = null;
	private boolean _isInUse = true;
	private float _cost = -1000.0F;
	private double _gCost = -1.0;
	private double _hCost = 0.0;
	private double _fCost = 0.0;

	public GeoNode(GeoLocation location)
	{
		this._location = location;
	}

	public void setParent(GeoNode parent)
	{
		this._parent = parent;
	}

	public GeoNode getParent()
	{
		return this._parent;
	}

	public GeoLocation getLocation()
	{
		return this._location;
	}

	public void setLoc(GeoLocation location)
	{
		this._location = location;
	}

	public boolean isInUse()
	{
		return this._isInUse;
	}

	public void setInUse()
	{
		this._isInUse = true;
	}

	public GeoNode getNext()
	{
		return this._next;
	}

	public void setNext(GeoNode next)
	{
		this._next = next;
	}

	public float getCost()
	{
		return this._cost;
	}

	public void setCost(double cost)
	{
		this._cost = (float) cost;
	}

	public double getGCost()
	{
		return this._gCost;
	}

	public void setGCost(double gCost)
	{
		this._gCost = gCost;
	}

	public double getHCost()
	{
		return this._hCost;
	}

	public void setHCost(double hCost)
	{
		this._hCost = hCost;
	}

	public double getFCost()
	{
		return this._fCost;
	}

	public void calculateFCost()
	{
		this._fCost = this._gCost + this._hCost;
	}

	public void resetCosts()
	{
		this._gCost = -1.0;
		this._hCost = 0.0;
		this._fCost = 0.0;
	}

	public void free()
	{
		this.setParent(null);
		this._cost = -1000.0F;
		this._isInUse = false;
		this._next = null;
		this.resetCosts();
	}

	@Override
	public int hashCode()
	{
		return 31 + (this._location == null ? 0 : this._location.hashCode());
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		else if (obj == null)
		{
			return false;
		}
		else if (!(obj instanceof GeoNode other))
		{
			return false;
		}
		else
		{
			if (this._location == null)
			{
				if (other._location != null)
				{
					return false;
				}
			}
			else if (!this._location.equals(other._location))
			{
				return false;
			}

			return true;
		}
	}
}
