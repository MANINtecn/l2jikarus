package net.sf.l2jdev.gameserver.model.zone.form;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.zone.ZoneForm;

public class ZoneCylinder extends ZoneForm
{
	private final int _x;
	private final int _y;
	private final int _z1;
	private final int _z2;
	private final int _rad;
	private final int _radS;
	private final Location _centerPoint;

	public ZoneCylinder(int x, int y, int z1, int z2, int rad)
	{
		this._x = x;
		this._y = y;
		this._z1 = z1;
		this._z2 = z2;
		this._rad = rad;
		this._radS = rad * rad;
		this._centerPoint = new Location(this._x, this._y, (this._z1 + this._z2) / 2);
	}

	@Override
	public boolean isInsideZone(int x, int y, int z)
	{
		return Math.pow(this._x - x, 2.0) + Math.pow(this._y - y, 2.0) <= this._radS && z >= this._z1 && z <= this._z2;
	}

	@Override
	public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
	{
		if (this._x > ax1 && this._x < ax2 && this._y > ay1 && this._y < ay2)
		{
			return true;
		}
		else if (Math.pow(ax1 - this._x, 2.0) + Math.pow(ay1 - this._y, 2.0) < this._radS)
		{
			return true;
		}
		else if (Math.pow(ax1 - this._x, 2.0) + Math.pow(ay2 - this._y, 2.0) < this._radS)
		{
			return true;
		}
		else if (Math.pow(ax2 - this._x, 2.0) + Math.pow(ay1 - this._y, 2.0) < this._radS)
		{
			return true;
		}
		else if (Math.pow(ax2 - this._x, 2.0) + Math.pow(ay2 - this._y, 2.0) < this._radS)
		{
			return true;
		}
		else
		{
			if (this._x > ax1 && this._x < ax2)
			{
				if ((Math.abs(this._y - ay2) < this._rad) || (Math.abs(this._y - ay1) < this._rad))
				{
					return true;
				}
			}

			if (this._y > ay1 && this._y < ay2)
			{
				if ((Math.abs(this._x - ax2) < this._rad) || (Math.abs(this._x - ax1) < this._rad))
				{
					return true;
				}
			}

			return false;
		}
	}

	@Override
	public double getDistanceToZone(int x, int y)
	{
		return Math.hypot(this._x - x, this._y - y) - this._rad;
	}

	@Override
	public int getLowZ()
	{
		return this._z1;
	}

	@Override
	public int getHighZ()
	{
		return this._z2;
	}

	@Override
	public void visualizeZone(int z)
	{
		int count = (int) ((Math.PI * 2) * this._rad / 10.0);
		double angle = (Math.PI * 2) / count;

		for (int i = 0; i < count; i++)
		{
			this.dropDebugItem(57, 1, this._x + (int) (Math.cos(angle * i) * this._rad), this._y + (int) (Math.sin(angle * i) * this._rad), z);
		}
	}

	@Override
	public Location getRandomPoint()
	{
		int x = 0;
		int y = 0;
		int x2 = this._x - this._rad;
		int y2 = this._y - this._rad;
		int x3 = this._x + this._rad;

		for (int y3 = this._y + this._rad; Math.pow(this._x - x, 2.0) + Math.pow(this._y - y, 2.0) > this._radS; y = Rnd.get(y2, y3))
		{
			x = Rnd.get(x2, x3);
		}

		return new Location(x, y, GeoEngine.getInstance().getHeight(x, y, (this._z1 + this._z2) / 2));
	}

	@Override
	public Location getCenterPoint()
	{
		return this._centerPoint;
	}
}
