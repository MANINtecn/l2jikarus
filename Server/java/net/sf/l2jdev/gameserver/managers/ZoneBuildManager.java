package net.sf.l2jdev.gameserver.managers;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.handler.AdminCommandHandler;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExServerPrimitive;

public class ZoneBuildManager
{
	public static final String HTML_DELETE_BUTTON = "<button value=\"Delete\" action=\"bypass -h admin_zone_build_delete %d\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
	private static final Map<Player, List<Location>> PLAYER_LOCATIONS = new ConcurrentHashMap<>();

	public void addPoint(Player player, Location location)
	{
		List<Location> locations = PLAYER_LOCATIONS.get(player);
		if (locations == null)
		{
			locations = new LinkedList<>();
			PLAYER_LOCATIONS.put(player, locations);
		}

		locations.add(location);
		player.sendMessage("Point saved " + location);
		this.displayZones(player);
		AdminCommandHandler.getInstance().onCommand(player, "admin_zone_build", false);
	}

	public void displayZones(Player player)
	{
		List<Location> locations = PLAYER_LOCATIONS.get(player);
		if (locations != null && !locations.isEmpty())
		{
			int packetCount = 1;
			ExServerPrimitive packet = new ExServerPrimitive("ZoneBuilder" + packetCount, locations.get(0).getX(), locations.get(0).getY(), 65535 + locations.get(0).getZ());
			packet.addPoint("0", Color.RED, true, locations.get(0).getX(), locations.get(0).getY(), locations.get(0).getZ());

			for (int i = 1; i < locations.size(); i++)
			{
				if (i % 10 == 0)
				{
					packetCount++;
					player.sendPacket(packet);
					packet = new ExServerPrimitive("ZoneBuilder" + packetCount, locations.get(i - 1).getX(), locations.get(i - 1).getY(), 65535 + locations.get(i - 1).getZ());
				}

				packet.addPoint(i + "", Color.RED, true, locations.get(i).getX(), locations.get(i).getY(), locations.get(i).getZ());
				packet.addLine(Color.GREEN, locations.get(i - 1).getX(), locations.get(i - 1).getY(), locations.get(i - 1).getZ(), locations.get(i).getX(), locations.get(i).getY(), locations.get(i).getZ());
			}

			player.sendPacket(packet);
		}
	}

	public List<Location> getLocations(Player player)
	{
		return PLAYER_LOCATIONS.get(player);
	}

	public String getPathsForHtml(Player player)
	{
		List<Location> locations = PLAYER_LOCATIONS.get(player);
		if (locations != null && !locations.isEmpty())
		{
			StringBuilder sb = new StringBuilder(locations.size() * 50);
			sb.append("<table width=300>");

			for (int i = 0; i < locations.size(); i++)
			{
				Location location = locations.get(i);
				sb.append("<tr>");
				sb.append("<td width=20>" + i + "</td>");
				sb.append("<td width=120>X: " + location.getX() + " Y: " + location.getY() + " Z: " + location.getZ() + "</td>");
				sb.append("<td width=80>" + String.format("<button value=\"Delete\" action=\"bypass -h admin_zone_build_delete %d\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">", i) + "</td>");
				sb.append("</tr>");
			}

			sb.append("</table>");
			return sb.toString();
		}
		return "";
	}

	public void clearZone(Player player)
	{
		List<Location> locations = PLAYER_LOCATIONS.remove(player);
		if (locations != null && !locations.isEmpty())
		{
			int packetCount = 1;
			ExServerPrimitive packet = new ExServerPrimitive("ZoneBuilder" + packetCount, player.getX(), player.getY(), -16000);
			packet.addPoint(Color.GREEN, 0, 0, -32768);

			for (int i = 1; i < locations.size(); i++)
			{
				if (i % 10 == 0)
				{
					packetCount++;
					player.sendPacket(packet);
					packet = new ExServerPrimitive("ZoneBuilder" + packetCount, locations.get(i - 1).getX(), locations.get(i - 1).getY(), -16000);
					packet.addPoint(Color.GREEN, 0, 0, -32768);
				}
			}

			player.sendPacket(packet);
		}
	}

	public void buildZone(Player player)
	{
		List<Location> locations = PLAYER_LOCATIONS.get(player);
		if (locations != null && !locations.isEmpty())
		{
			try
			{
				long currentTime = System.currentTimeMillis();
				String fileName = "data/zones/" + player.getName() + "-" + currentTime + ".xml";
				File spawnFile = new File(fileName);
				BufferedWriter writer = new BufferedWriter(new FileWriter(spawnFile));
				StringBuilder sb = new StringBuilder();
				sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				sb.append("<list enabled=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"../xsd/zones.xsd\">\n");
				sb.append("\t<zone name=\"").append(player.getName()).append("_Zone_").append(currentTime).append("\" type=\"ScriptZone\" shape=\"NPoly\" minZ=\"").append(locations.get(0).getZ() - 1000).append("\" maxZ=\"").append(locations.get(0).getZ() + 1000).append("\">\n");

				for (Location location : locations)
				{
					sb.append("\t\t<node X=\"").append(location.getX()).append("\" Y=\"").append(location.getY()).append("\" />\n");
				}

				sb.append("\t</zone>\n");
				sb.append("</list>");
				writer.write(sb.toString());
				writer.close();
				player.sendMessage("Zone saved at " + fileName);
				AdminCommandHandler.getInstance().onCommand(player, "admin_zone_build_clear", false);
			}
			catch (Exception var11)
			{
				var11.printStackTrace();
			}
		}
		else
		{
			player.sendMessage("No path entries to save.");
		}
	}

	public void deleteEntry(Player player, int entry)
	{
		List<Location> locations = PLAYER_LOCATIONS.get(player);
		if (locations != null && entry >= 0 && entry < locations.size())
		{
			List<Location> modifiedLocations = new LinkedList<>();

			for (int i = 0; i < locations.size(); i++)
			{
				if (i != entry)
				{
					modifiedLocations.add(locations.get(i));
				}
			}

			if (modifiedLocations.size() <= 1)
			{
				this.clearZone(player);
			}
			else
			{
				PLAYER_LOCATIONS.put(player, modifiedLocations);
				this.displayZones(player);
			}
		}
	}

	public static ZoneBuildManager getInstance()
	{
		return ZoneBuildManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ZoneBuildManager INSTANCE = new ZoneBuildManager();
	}
}
