package net.sf.l2jdev.gameserver.geoengine;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.GeoEngineConfig;
import net.sf.l2jdev.gameserver.data.xml.DoorData;
import net.sf.l2jdev.gameserver.data.xml.FenceData;
import net.sf.l2jdev.gameserver.geoengine.geodata.IRegion;
import net.sf.l2jdev.gameserver.geoengine.geodata.regions.NullRegion;
import net.sf.l2jdev.gameserver.geoengine.geodata.regions.Region;
import net.sf.l2jdev.gameserver.geoengine.util.GridLineIterator2D;
import net.sf.l2jdev.gameserver.geoengine.util.GridLineIterator3D;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.util.GeoUtils;

public class GeoEngine
{
	private static final Logger LOGGER = Logger.getLogger(GeoEngine.class.getName());
	public static final String FILE_NAME_FORMAT = "%d_%d.l2j";
	public static final int ELEVATED_SEE_OVER_DISTANCE = 2;
	public static final int MAX_SEE_OVER_HEIGHT = 48;
	public static final int SPAWN_Z_DELTA_LIMIT = 100;
	public static final int WORLD_MIN_X = -655360;
	public static final int WORLD_MIN_Y = -589824;
	public static final int GEO_REGIONS_X = 32;
	public static final int GEO_REGIONS_Y = 32;
	public static final int GEO_REGIONS = 1024;
	public static final int COORDINATE_SCALE = 16;
	public static final int COORDINATE_OFFSET = 8;
	public static final int HEIGHT_INCREASE_LIMIT = 40;
	public static final int SPAWN_HEIGHT_OFFSET = 20;
	public static final AtomicReferenceArray<IRegion> REGIONS = new AtomicReferenceArray<>(1024);

	protected GeoEngine()
	{
		for (int i = 0; i < 1024; i++)
		{
			REGIONS.set(i, NullRegion.INSTANCE);
		}

		int loadedRegions = 0;

		try
		{
			for (int regionX = 11; regionX <= 28; regionX++)
			{
				for (int regionY = 10; regionY <= 26; regionY++)
				{
					Path geoFilePath = GeoEngineConfig.GEODATA_PATH.resolve(String.format("%d_%d.l2j", regionX, regionY));
					if (Files.exists(geoFilePath))
					{
						try
						{
							this.loadRegion(geoFilePath, regionX, regionY);
							loadedRegions++;
						}
						catch (Exception var6)
						{
							LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed to load " + geoFilePath.getFileName() + "!", var6);
						}
					}
				}
			}
		}
		catch (Exception var7)
		{
			LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Critical error during geodata initialization!", var7);
			System.exit(1);
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + loadedRegions + " regions.");
		if (loadedRegions == 0 && GeoEngineConfig.PATHFINDING > 0)
		{
			GeoEngineConfig.PATHFINDING = 0;
			LOGGER.info(this.getClass().getSimpleName() + ": Pathfinding is disabled.");
		}
	}

	public void loadRegion(Path filePath, int regionX, int regionY) throws IOException
	{
		int regionOffset = regionX * 32 + regionY;

		try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath.toFile(), "r"))
		{
			REGIONS.set(regionOffset, new Region(randomAccessFile.getChannel().map(MapMode.READ_ONLY, 0L, randomAccessFile.length()).order(ByteOrder.LITTLE_ENDIAN)));
		}
	}

	public boolean reloadRegion(int regionX, int regionY)
	{
		int regionOffset = regionX * 32 + regionY;
		Path geoFilePath = GeoEngineConfig.GEODATA_PATH.resolve(String.format("%d_%d.l2j", regionX, regionY));
		if (!Files.exists(geoFilePath))
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Cannot reload, file not found for region " + regionX + "_" + regionY);
			return false;
		}
		try
		{
			IRegion region = REGIONS.get(regionOffset);
			if (region instanceof Region)
			{
				ByteBuffer buffer = ByteBuffer.wrap(Files.readAllBytes(geoFilePath)).order(ByteOrder.LITTLE_ENDIAN);
				((Region) region).load(buffer);
				LOGGER.info(this.getClass().getSimpleName() + ": Reloaded region " + regionX + "_" + regionY + " from bytes.");
				return true;
			}
			this.loadRegion(geoFilePath, regionX, regionY);
			LOGGER.info(this.getClass().getSimpleName() + ": Replaced NullRegion with new region " + regionX + "_" + regionY);
			return true;
		}
		catch (Exception var7)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed to reload region " + regionX + "_" + regionY + "!", var7);
			return false;
		}
	}

	public void setRegion(int regionX, int regionY, Region region)
	{
		int regionOffset = regionX * 32 + regionY;
		REGIONS.set(regionOffset, region);
	}

	public IRegion getRegion(int geoX, int geoY)
	{
		return REGIONS.get(geoX / 2048 * 32 + geoY / 2048);
	}

	public boolean hasGeoPos(int geoX, int geoY)
	{
		return this.getRegion(geoX, geoY).hasGeo();
	}

	public boolean checkNearestNswe(int geoX, int geoY, int worldZ, int nswe)
	{
		return this.getRegion(geoX, geoY).checkNearestNswe(geoX, geoY, worldZ, nswe);
	}

	public boolean checkNearestNsweAntiCornerCut(int geoX, int geoY, int worldZ, int nswe)
	{
		boolean canMove = true;
		IRegion region = this.getRegion(geoX, geoY);
		if ((nswe & 9) == 9)
		{
			canMove = region.checkNearestNswe(geoX, geoY - 1, worldZ, 1) && region.checkNearestNswe(geoX + 1, geoY, worldZ, 8);
		}

		if (canMove && (nswe & 10) == 10)
		{
			canMove = region.checkNearestNswe(geoX, geoY - 1, worldZ, 2) && region.checkNearestNswe(geoX - 1, geoY, worldZ, 8);
		}

		if (canMove && (nswe & 5) == 5)
		{
			canMove = region.checkNearestNswe(geoX, geoY + 1, worldZ, 1) && region.checkNearestNswe(geoX + 1, geoY, worldZ, 4);
		}

		if (canMove && (nswe & 6) == 6)
		{
			canMove = region.checkNearestNswe(geoX, geoY + 1, worldZ, 2) && region.checkNearestNswe(geoX - 1, geoY, worldZ, 4);
		}

		return canMove && region.checkNearestNswe(geoX, geoY, worldZ, nswe);
	}

	public void setNearestNswe(int geoX, int geoY, int worldZ, byte nswe)
	{
		this.getRegion(geoX, geoY).setNearestNswe(geoX, geoY, worldZ, nswe);
	}

	public void unsetNearestNswe(int geoX, int geoY, int worldZ, byte nswe)
	{
		this.getRegion(geoX, geoY).unsetNearestNswe(geoX, geoY, worldZ, nswe);
	}

	public int getNearestZ(int geoX, int geoY, int worldZ)
	{
		return this.getRegion(geoX, geoY).getNearestZ(geoX, geoY, worldZ);
	}

	public int getNextLowerZ(int geoX, int geoY, int worldZ)
	{
		return this.getRegion(geoX, geoY).getNextLowerZ(geoX, geoY, worldZ);
	}

	public int getNextHigherZ(int geoX, int geoY, int worldZ)
	{
		return this.getRegion(geoX, geoY).getNextHigherZ(geoX, geoY, worldZ);
	}

	public static int getGeoX(int worldX)
	{
		return (worldX - -655360) / 16;
	}

	public static int getGeoY(int worldY)
	{
		return (worldY - -589824) / 16;
	}

	public static int getWorldX(int geoX)
	{
		return geoX * 16 + -655360 + 8;
	}

	public static int getWorldY(int geoY)
	{
		return geoY * 16 + -589824 + 8;
	}

	public int getHeight(int x, int y, int z)
	{
		return this.getNearestZ(getGeoX(x), getGeoY(y), z);
	}

	public int getSpawnHeight(int x, int y, int z)
	{
		int geoX = getGeoX(x);
		int geoY = getGeoY(y);
		if (!this.hasGeoPos(geoX, geoY))
		{
			return z;
		}
		int nextLowerZ = this.getNextLowerZ(geoX, geoY, z + 20);
		return Math.abs(nextLowerZ - z) <= 100 ? nextLowerZ : z;
	}

	public int getSpawnHeight(Location location)
	{
		return this.getSpawnHeight(location.getX(), location.getY(), location.getZ());
	}

	public boolean canSeeTarget(WorldObject creature, WorldObject target)
	{
		return target != null && (target.isDoor() || target.isArtefact() || this.canSeeTarget(creature.getX(), creature.getY(), creature.getZ(), creature.getInstanceWorld(), target.getX(), target.getY(), target.getZ(), target.getInstanceWorld()));
	}

	public boolean canSeeTarget(WorldObject creature, ILocational worldPosition)
	{
		return this.canSeeTarget(creature.getX(), creature.getY(), creature.getZ(), worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), creature.getInstanceWorld());
	}

	public boolean canSeeTarget(int x, int y, int z, Instance instance, int targetX, int targetY, int targetZ, Instance targetInstance)
	{
		return instance == targetInstance && this.canSeeTarget(x, y, z, targetX, targetY, targetZ, instance);
	}

	public boolean canSeeTarget(int x, int y, int z, int targetX, int targetY, int targetZ, Instance instance)
	{
		if (DoorData.getInstance().checkIfDoorsBetween(x, y, z, targetX, targetY, targetZ, instance, true))
		{
			return false;
		}
		return FenceData.getInstance().checkIfFenceBetween(x, y, z, targetX, targetY, targetZ, instance) ? false : this.canSeeTarget(x, y, z, targetX, targetY, targetZ);
	}

	private int getLosGeoZ(int previousX, int previousY, int previousGeoZ, int currentX, int currentY, int nswe)
	{
		if (((nswe & 8) == 0 || (nswe & 4) == 0) && ((nswe & 2) == 0 || (nswe & 1) == 0))
		{
			return this.checkNearestNsweAntiCornerCut(previousX, previousY, previousGeoZ, nswe) ? this.getNearestZ(currentX, currentY, previousGeoZ) : this.getNextHigherZ(currentX, currentY, previousGeoZ);
		}
		throw new RuntimeException("Multiple directions specified in NSWE: " + nswe);
	}

	public boolean canSeeTarget(int x, int y, int z, int targetX, int targetY, int targetZ)
	{
		int geoX = getGeoX(x);
		int geoY = getGeoY(y);
		int targetGeoX = getGeoX(targetX);
		int targetGeoY = getGeoY(targetY);
		int nearestFromZ = this.getNearestZ(geoX, geoY, z);
		int nearestToZ = this.getNearestZ(targetGeoX, targetGeoY, targetZ);
		if (geoX == targetGeoX && geoY == targetGeoY)
		{
			return !this.hasGeoPos(targetGeoX, targetGeoY) || nearestFromZ == nearestToZ;
		}
		if (nearestToZ > nearestFromZ)
		{
			int pointIterator = nearestToZ;
			nearestToZ = nearestFromZ;
			nearestFromZ = pointIterator;
			pointIterator = targetGeoX;
			targetGeoX = geoX;
			geoX = pointIterator;
			pointIterator = targetGeoY;
			targetGeoY = geoY;
			geoY = pointIterator;
		}

		GridLineIterator3D pointIterator = new GridLineIterator3D(geoX, geoY, nearestFromZ, targetGeoX, targetGeoY, nearestToZ);
		pointIterator.next();
		int previousX = pointIterator.x();
		int previousY = pointIterator.y();
		int previousGeoZ = pointIterator.z();
		int pointIndex = 0;

		while (pointIterator.next())
		{
			int currentX = pointIterator.x();
			int currentY = pointIterator.y();
			if (currentX != previousX || currentY != previousY)
			{
				int beeCurrentZ = pointIterator.z();
				int currentGeoZ = previousGeoZ;
				if (this.hasGeoPos(currentX, currentY))
				{
					int nswe = GeoUtils.computeNswe(previousX, previousY, currentX, currentY);
					currentGeoZ = this.getLosGeoZ(previousX, previousY, previousGeoZ, currentX, currentY, nswe);
					int maxHeight = pointIndex < 2 ? nearestFromZ + 48 : beeCurrentZ + 48;
					boolean canSeeThrough = false;
					if (currentGeoZ <= maxHeight)
					{
						if ((nswe & 9) == 9)
						{
							int northGeoZ = this.getLosGeoZ(previousX, previousY, previousGeoZ, previousX, previousY - 1, 1);
							int eastGeoZ = this.getLosGeoZ(previousX, previousY, previousGeoZ, previousX + 1, previousY, 8);
							canSeeThrough = northGeoZ <= maxHeight && eastGeoZ <= maxHeight && northGeoZ <= this.getNearestZ(previousX, previousY - 1, beeCurrentZ) && eastGeoZ <= this.getNearestZ(previousX + 1, previousY, beeCurrentZ);
						}
						else if ((nswe & 10) == 10)
						{
							int northGeoZ = this.getLosGeoZ(previousX, previousY, previousGeoZ, previousX, previousY - 1, 2);
							int westGeoZ = this.getLosGeoZ(previousX, previousY, previousGeoZ, previousX - 1, previousY, 8);
							canSeeThrough = northGeoZ <= maxHeight && westGeoZ <= maxHeight && northGeoZ <= this.getNearestZ(previousX, previousY - 1, beeCurrentZ) && westGeoZ <= this.getNearestZ(previousX - 1, previousY, beeCurrentZ);
						}
						else if ((nswe & 5) == 5)
						{
							int southGeoZ = this.getLosGeoZ(previousX, previousY, previousGeoZ, previousX, previousY + 1, 1);
							int eastGeoZ = this.getLosGeoZ(previousX, previousY, previousGeoZ, previousX + 1, previousY, 4);
							canSeeThrough = southGeoZ <= maxHeight && eastGeoZ <= maxHeight && southGeoZ <= this.getNearestZ(previousX, previousY + 1, beeCurrentZ) && eastGeoZ <= this.getNearestZ(previousX + 1, previousY, beeCurrentZ);
						}
						else if ((nswe & 6) != 6)
						{
							canSeeThrough = true;
						}
						else
						{
							int southGeoZ = this.getLosGeoZ(previousX, previousY, previousGeoZ, previousX, previousY + 1, 2);
							int westGeoZ = this.getLosGeoZ(previousX, previousY, previousGeoZ, previousX - 1, previousY, 4);
							canSeeThrough = southGeoZ <= maxHeight && westGeoZ <= maxHeight && southGeoZ <= this.getNearestZ(previousX, previousY + 1, beeCurrentZ) && westGeoZ <= this.getNearestZ(previousX - 1, previousY, beeCurrentZ);
						}
					}

					if (!canSeeThrough)
					{
						return false;
					}
				}

				previousX = currentX;
				previousY = currentY;
				previousGeoZ = currentGeoZ;
				pointIndex++;
			}
		}

		return true;
	}

	public Location getValidLocation(ILocational origin, ILocational destination)
	{
		return this.getValidLocation(origin.getX(), origin.getY(), origin.getZ(), destination.getX(), destination.getY(), destination.getZ(), null);
	}

	public Location getValidLocation(int x, int y, int z, int targetX, int targetY, int targetZ, Instance instance)
	{
		int geoX = getGeoX(x);
		int geoY = getGeoY(y);
		int nearestFromZ = this.getNearestZ(geoX, geoY, z);
		int targetGeoX = getGeoX(targetX);
		int targetGeoY = getGeoY(targetY);
		int nearestToZ = this.getNearestZ(targetGeoX, targetGeoY, targetZ);
		if (DoorData.getInstance().checkIfDoorsBetween(x, y, nearestFromZ, targetX, targetY, nearestToZ, instance, false))
		{
			return new Location(x, y, this.getHeight(x, y, nearestFromZ));
		}
		else if (FenceData.getInstance().checkIfFenceBetween(x, y, nearestFromZ, targetX, targetY, nearestToZ, instance))
		{
			return new Location(x, y, this.getHeight(x, y, nearestFromZ));
		}
		else
		{
			GridLineIterator2D pointIterator = new GridLineIterator2D(geoX, geoY, targetGeoX, targetGeoY);
			pointIterator.next();
			int previousX = pointIterator.x();
			int previousY = pointIterator.y();
			int previousZ = nearestFromZ;

			while (pointIterator.next())
			{
				int currentX = pointIterator.x();
				int currentY = pointIterator.y();
				int currentZ = this.getNearestZ(currentX, currentY, previousZ);
				if (currentZ - previousZ > 40)
				{
					return new Location(getWorldX(previousX), getWorldY(previousY), previousZ);
				}

				if (this.hasGeoPos(previousX, previousY))
				{
					if (this.isCompletelyBlocked(currentX, currentY, currentZ) || !this.checkNearestNsweAntiCornerCut(previousX, previousY, previousZ, GeoUtils.computeNswe(previousX, previousY, currentX, currentY)))
					{
						return new Location(getWorldX(previousX), getWorldY(previousY), previousZ);
					}
				}

				previousX = currentX;
				previousY = currentY;
				previousZ = currentZ;
			}

			return this.hasGeoPos(previousX, previousY) && previousZ != nearestToZ ? new Location(x, y, nearestFromZ) : new Location(targetX, targetY, nearestToZ);
		}
	}

	public boolean canMoveToTarget(int fromX, int fromY, int fromZ, int toX, int toY, int toZ, Instance instance)
	{
		int geoX = getGeoX(fromX);
		int geoY = getGeoY(fromY);
		int nearestFromZ = this.getNearestZ(geoX, geoY, fromZ);
		int targetGeoX = getGeoX(toX);
		int targetGeoY = getGeoY(toY);
		int nearestToZ = this.getNearestZ(targetGeoX, targetGeoY, toZ);
		if (DoorData.getInstance().checkIfDoorsBetween(fromX, fromY, nearestFromZ, toX, toY, nearestToZ, instance, false))
		{
			return false;
		}
		else if (FenceData.getInstance().checkIfFenceBetween(fromX, fromY, nearestFromZ, toX, toY, nearestToZ, instance))
		{
			return false;
		}
		else
		{
			GridLineIterator2D pointIterator = new GridLineIterator2D(geoX, geoY, targetGeoX, targetGeoY);
			pointIterator.next();
			int previousX = pointIterator.x();
			int previousY = pointIterator.y();
			int previousZ = nearestFromZ;

			while (pointIterator.next())
			{
				int currentX = pointIterator.x();
				int currentY = pointIterator.y();
				int currentZ = this.getNearestZ(currentX, currentY, previousZ);
				if (currentZ - previousZ > 40)
				{
					return false;
				}

				if (this.hasGeoPos(previousX, previousY))
				{
					if ((GeoEngineConfig.AVOID_OBSTRUCTED_PATH_NODES && !this.checkNearestNswe(currentX, currentY, currentZ, 15)) || !this.checkNearestNsweAntiCornerCut(previousX, previousY, previousZ, GeoUtils.computeNswe(previousX, previousY, currentX, currentY)))
					{
						return false;
					}
				}

				previousX = currentX;
				previousY = currentY;
				previousZ = currentZ;
			}

			return !this.hasGeoPos(previousX, previousY) || previousZ == nearestToZ;
		}
	}

	public boolean canMoveToTarget(ILocational from, int toX, int toY, int toZ)
	{
		return this.canMoveToTarget(from.getX(), from.getY(), from.getZ(), toX, toY, toZ, null);
	}

	public boolean canMoveToTarget(ILocational from, ILocational to)
	{
		return this.canMoveToTarget(from, to.getX(), to.getY(), to.getZ());
	}

	public boolean hasGeo(int x, int y)
	{
		return this.hasGeoPos(getGeoX(x), getGeoY(y));
	}

	public boolean isCompletelyBlocked(int geoX, int geoY, int geoZ)
	{
		if (GeoEngineConfig.PATHFINDING < 1)
		{
			return false;
		}
		IRegion region = this.getRegion(geoX, geoY);
		return region == null ? true : region.hasGeo() && !region.checkNearestNswe(geoX, geoY, geoZ, 8) && !region.checkNearestNswe(geoX, geoY, geoZ, 4) && !region.checkNearestNswe(geoX, geoY, geoZ, 1) && !region.checkNearestNswe(geoX, geoY, geoZ, 2);
	}

	public static GeoEngine getInstance()
	{
		return GeoEngine.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final GeoEngine INSTANCE = new GeoEngine();
	}
}
