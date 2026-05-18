package net.sf.l2jdev.gameserver.model.interfaces;

import net.sf.l2jdev.gameserver.model.Location;

public interface IPositionable extends ILocational
{
	void setXYZ(int var1, int var2, int var3);

	void setXYZ(ILocational var1);

	void setHeading(int var1);

	void setLocation(Location var1);
}
