package net.sf.l2jdev.gameserver.model.script;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.time.TimeUtil;
import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.sql.AnnouncementsTable;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.managers.events.EventDropManager;
import net.sf.l2jdev.gameserver.managers.events.EventShrineManager;
import net.sf.l2jdev.gameserver.managers.events.ItemDeletionInfoManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.npc.EventDropHolder;
import net.sf.l2jdev.gameserver.model.announce.EventAnnouncement;
import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.OnServerStart;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.util.Broadcast;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class LongTimeEvent extends Script
{
	protected String _eventName;
	protected Date _startDate = null;
	protected Date _endDate = null;
	protected Date _dropStartDate = null;
	protected Date _dropEndDate = null;
	protected boolean _initialized = false;
	protected boolean _active = false;
	protected boolean _enableShrines = false;
	protected String _onEnterMsg = "";
	protected String _endMsg = "";
	protected int _enterAnnounceId = -1;
	protected final List<LongTimeEvent.NpcSpawn> _spawnList = new ArrayList<>();
	protected final List<EventDropHolder> _dropList = new ArrayList<>();
	protected final List<Integer> _destroyItemsOnEnd = new ArrayList<>();
	private final Consumer<OnServerStart> _spawnNpcs = _ -> {
		this.spawnNpcs();
		Containers.Global().removeListenerIf(EventType.ON_SERVER_START, listener -> listener.getOwner() == this);
	};
	
	public LongTimeEvent()
	{
		this.loadConfig();
		if (this._startDate != null && this._endDate != null)
		{
			Date now = new Date();
			if (this.isWithinRange(now))
			{
				this.startEvent();
				LOGGER.info("Event " + this._eventName + " active till " + this._endDate);
			}
			else if (this._startDate.after(now))
			{
				long delay = this._startDate.getTime() - System.currentTimeMillis();
				ThreadPool.schedule(new LongTimeEvent.ScheduleStart(), delay);
				LOGGER.info("Event " + this._eventName + " will be started at " + this._startDate);
			}
			else
			{
				this.destroyItemsOnEnd();
				LOGGER.info("Event " + this._eventName + " has passed... Ignored ");
			}
		}
		
		this._initialized = true;
	}
	
	private boolean isWithinRange(Date date)
	{
		return (date.equals(this._startDate) || date.after(this._startDate)) && (date.equals(this._endDate) || date.before(this._endDate));
	}
	
	private boolean isWithinDropPeriod(Date date)
	{
		return (date.equals(this._dropStartDate) || date.after(this._dropStartDate)) && (date.equals(this._dropEndDate) || date.before(this._dropEndDate));
	}
	
	private boolean isValidPeriod()
	{
		return this._startDate != null && this._endDate != null && this._startDate.before(this._endDate);
	}
	
	public Date[] parseDateRange(String dateRange, SimpleDateFormat format)
	{
		String[] dates = dateRange.split("-");
		if (dates.length == 2)
		{
			try
			{
				Date start = format.parse(dates[0].trim());
				Date end = format.parse(dates[1].trim());
				return new Date[]
				{
					start,
					end
				};
			}
			catch (Exception var6)
			{
				LOGGER.warning("Invalid Date Format: " + var6.getMessage());
			}
		}
		
		return null;
	}
	
	private void loadConfig()
	{
		(new IXmlReader()
		{
			{
				Objects.requireNonNull(LongTimeEvent.this);
			}
			
			@Override
			public void load()
			{
				this.parseDatapackFile("data/scripts/events/" + LongTimeEvent.this.getName() + "/config.xml");
			}
			
			@Override
			public void parseDocument(Document document, File file)
			{
				if (!document.getDocumentElement().getNodeName().equalsIgnoreCase("event"))
				{
					throw new NullPointerException("WARNING!!! " + LongTimeEvent.this.getName() + " event: bad config file!");
				}
				LongTimeEvent.this._eventName = document.getDocumentElement().getAttributes().getNamedItem("name").getNodeValue();
				String currentYear = String.valueOf(Calendar.getInstance().get(1));
				String period = document.getDocumentElement().getAttributes().getNamedItem("active").getNodeValue();
				String dropPeriod = document.getDocumentElement().getAttributes().getNamedItem("dropPeriod") != null ? document.getDocumentElement().getAttributes().getNamedItem("dropPeriod").getNodeValue() : null;
				if (document.getDocumentElement().getAttributes().getNamedItem("enableShrines") != null && document.getDocumentElement().getAttributes().getNamedItem("enableShrines").getNodeValue().equalsIgnoreCase("true"))
				{
					LongTimeEvent.this._enableShrines = true;
				}
				
				if (dropPeriod == null || dropPeriod.isEmpty())
				{
					dropPeriod = period;
				}
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM yyyy", Locale.US);
				if (period.length() == 21)
				{
					Date[] range = LongTimeEvent.this.parseDateRange(period, dateFormat);
					if (range != null)
					{
						LongTimeEvent.this._startDate = range[0];
						LongTimeEvent.this._endDate = range[1];
					}
				}
				else if (period.length() == 11)
				{
					String[] parts = period.split("-");
					String start = parts[0] + " " + currentYear;
					String end = parts[1] + " " + currentYear;
					Date[] range = LongTimeEvent.this.parseDateRange(start + "-" + end, dateFormat);
					if (range != null)
					{
						LongTimeEvent.this._startDate = range[0];
						LongTimeEvent.this._endDate = range[1];
					}
				}
				
				if (dropPeriod.length() == 21)
				{
					Date[] dropRange = LongTimeEvent.this.parseDateRange(dropPeriod, dateFormat);
					if (dropRange != null)
					{
						LongTimeEvent.this._dropStartDate = dropRange[0];
						LongTimeEvent.this._dropEndDate = dropRange[1];
					}
				}
				else if (dropPeriod.length() == 11)
				{
					String[] parts = dropPeriod.split("-");
					String start = parts[0] + " " + currentYear;
					String end = parts[1] + " " + currentYear;
					Date[] dropRange = LongTimeEvent.this.parseDateRange(start + "-" + end, dateFormat);
					if (dropRange != null)
					{
						LongTimeEvent.this._dropStartDate = dropRange[0];
						LongTimeEvent.this._dropEndDate = dropRange[1];
					}
				}
				
				if ((LongTimeEvent.this._dropStartDate == null || !LongTimeEvent.this._dropStartDate.before(LongTimeEvent.this._startDate)) && (LongTimeEvent.this._dropEndDate == null || !LongTimeEvent.this._dropEndDate.after(LongTimeEvent.this._endDate)))
				{
					if (!LongTimeEvent.this.isValidPeriod())
					{
						throw new NullPointerException("WARNING!!! " + LongTimeEvent.this.getName() + " event: illegal event period");
					}
					Date today = new Date();
					if (LongTimeEvent.this._startDate.after(today) || LongTimeEvent.this.isWithinRange(today))
					{
						for (Node n = document.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling())
						{
							if (!n.getNodeName().equalsIgnoreCase("droplist"))
							{
								if (n.getNodeName().equalsIgnoreCase("spawnlist"))
								{
									for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
									{
										if (d.getNodeName().equalsIgnoreCase("add"))
										{
											try
											{
												int npcId = Integer.parseInt(d.getAttributes().getNamedItem("npc").getNodeValue());
												int xPos = Integer.parseInt(d.getAttributes().getNamedItem("x").getNodeValue());
												int yPos = Integer.parseInt(d.getAttributes().getNamedItem("y").getNodeValue());
												int zPos = Integer.parseInt(d.getAttributes().getNamedItem("z").getNodeValue());
												Node headingNode = d.getAttributes().getNamedItem("heading");
												String headingValue = headingNode == null ? null : headingNode.getNodeValue();
												int heading = headingValue != null ? Integer.parseInt(headingValue) : 0;
												Node respawnTimeNode = d.getAttributes().getNamedItem("respawnTime");
												String respawnTimeValue = respawnTimeNode == null ? null : respawnTimeNode.getNodeValue();
												Duration respawnTime = TimeUtil.parseDuration(respawnTimeValue != null ? respawnTimeValue : "0sec");
												if (NpcData.getInstance().getTemplate(npcId) == null)
												{
													LOGGER.warning(LongTimeEvent.this.getName() + " event: " + npcId + " is wrong NPC id, NPC was not added in spawnlist");
												}
												else
												{
													LongTimeEvent.this._spawnList.add(LongTimeEvent.this.new NpcSpawn(npcId, new Location(xPos, yPos, zPos, heading), respawnTime));
												}
											}
											catch (NumberFormatException var27)
											{
												LOGGER.warning("Wrong number format in config.xml spawnlist block for " + LongTimeEvent.this.getName() + " event");
											}
										}
									}
								}
								else if (n.getNodeName().equalsIgnoreCase("messages"))
								{
									for (Node dx = n.getFirstChild(); dx != null; dx = dx.getNextSibling())
									{
										if (dx.getNodeName().equalsIgnoreCase("add"))
										{
											String msgType = dx.getAttributes().getNamedItem("type").getNodeValue();
											String msgText = dx.getAttributes().getNamedItem("text").getNodeValue();
											if (msgType != null && msgText != null)
											{
												if (msgType.equalsIgnoreCase("onEnd"))
												{
													LongTimeEvent.this._endMsg = msgText;
												}
												else if (msgType.equalsIgnoreCase("onEnter"))
												{
													LongTimeEvent.this._onEnterMsg = msgText;
												}
											}
										}
									}
								}
							}
							else
							{
								for (Node dxx = n.getFirstChild(); dxx != null; dxx = dxx.getNextSibling())
								{
									if (dxx.getNodeName().equalsIgnoreCase("add"))
									{
										try
										{
											int itemId = Integer.parseInt(dxx.getAttributes().getNamedItem("item").getNodeValue());
											int minCount = Integer.parseInt(dxx.getAttributes().getNamedItem("min").getNodeValue());
											int maxCount = Integer.parseInt(dxx.getAttributes().getNamedItem("max").getNodeValue());
											String chance = dxx.getAttributes().getNamedItem("chance").getNodeValue();
											double finalChance = !chance.isEmpty() && chance.endsWith("%") ? Double.parseDouble(chance.substring(0, chance.length() - 1)) : 0.0;
											Node minLevelNode = dxx.getAttributes().getNamedItem("minLevel");
											int minLevel = minLevelNode == null ? 1 : Integer.parseInt(minLevelNode.getNodeValue());
											Node maxLevelNode = dxx.getAttributes().getNamedItem("maxLevel");
											int maxLevel = maxLevelNode == null ? Integer.MAX_VALUE : Integer.parseInt(maxLevelNode.getNodeValue());
											Node monsterIdsNode = dxx.getAttributes().getNamedItem("monsterIds");
											Set<Integer> monsterIds = new HashSet<>();
											if (monsterIdsNode != null)
											{
												for (String id : monsterIdsNode.getNodeValue().split(","))
												{
													monsterIds.add(Integer.parseInt(id));
												}
											}
											
											if (ItemData.getInstance().getTemplate(itemId) == null)
											{
												LOGGER.warning(LongTimeEvent.this.getName() + " event: " + itemId + " is wrong item id, item was not added in droplist");
											}
											else if (minCount > maxCount)
											{
												LOGGER.warning(LongTimeEvent.this.getName() + " event: item " + itemId + " - min greater than max, item was not added in droplist");
											}
											else if (!(finalChance < 0.0) && !(finalChance > 100.0))
											{
												LongTimeEvent.this._dropList.add(new EventDropHolder(itemId, minCount, maxCount, finalChance, minLevel, maxLevel, monsterIds));
											}
											else
											{
												LOGGER.warning(LongTimeEvent.this.getName() + " event: item " + itemId + " - incorrect drop chance, item was not added in droplist");
											}
										}
										catch (NumberFormatException var28)
										{
											LOGGER.warning("Wrong number format in config.xml droplist block for " + LongTimeEvent.this.getName() + " event");
										}
									}
								}
							}
						}
					}
					
					for (Node nx = document.getDocumentElement().getFirstChild(); nx != null; nx = nx.getNextSibling())
					{
						if (nx.getNodeName().equalsIgnoreCase("destroyItemsOnEnd"))
						{
							long endtime = LongTimeEvent.this._endDate.getTime();
							
							for (Node dxxx = nx.getFirstChild(); dxxx != null; dxxx = dxxx.getNextSibling())
							{
								if (dxxx.getNodeName().equalsIgnoreCase("item"))
								{
									try
									{
										int itemIdx = Integer.parseInt(dxxx.getAttributes().getNamedItem("id").getNodeValue());
										if (ItemData.getInstance().getTemplate(itemIdx) == null)
										{
											LOGGER.warning(LongTimeEvent.this.getName() + " event: Item " + itemIdx + " does not exist.");
										}
										else
										{
											LongTimeEvent.this._destroyItemsOnEnd.add(itemIdx);
											if (endtime > System.currentTimeMillis())
											{
												ItemDeletionInfoManager.getInstance().addItemDate(itemIdx, (int) (endtime / 1000L));
											}
										}
									}
									catch (NumberFormatException var26)
									{
										LOGGER.warning("Wrong number format in config.xml destroyItemsOnEnd block for " + LongTimeEvent.this.getName() + " event");
									}
								}
							}
						}
					}
				}
				else
				{
					throw new NullPointerException("WARNING!!! " + LongTimeEvent.this.getName() + " event: drop period must be within the active period");
				}
			}
		}).load();
	}
	
	protected void startEvent()
	{
		this._active = true;
		if (this._dropStartDate != null && this._dropEndDate != null && this.isWithinDropPeriod(new Date()))
		{
			EventDropManager.getInstance().addDrops(this, this._dropList);
		}
		
		if (!this._spawnList.isEmpty())
		{
			if (this._initialized)
			{
				this.spawnNpcs();
			}
			else
			{
				Containers.Global().addListener(new ConsumerEventListener(Containers.Global(), EventType.ON_SERVER_START, event -> this._spawnNpcs.accept((OnServerStart) event), this));
			}
		}
		
		if (this._enableShrines)
		{
			EventShrineManager.getInstance().setEnabled(true);
		}
		
		if (!this._onEnterMsg.isEmpty())
		{
			Broadcast.toAllOnlinePlayers(this._onEnterMsg);
			EventAnnouncement announce = new EventAnnouncement(this._startDate, this._endDate, this._onEnterMsg);
			AnnouncementsTable.getInstance().addAnnouncement(announce);
			this._enterAnnounceId = announce.getId();
		}
		
		Long millisToEventEnd = this._endDate.getTime() - System.currentTimeMillis();
		ThreadPool.schedule(new LongTimeEvent.ScheduleEnd(), millisToEventEnd);
	}
	
	protected void spawnNpcs()
	{
		Long millisToEventEnd = this._endDate.getTime() - System.currentTimeMillis();
		
		for (LongTimeEvent.NpcSpawn npcSpawn : this._spawnList)
		{
			Npc npc = addSpawn(npcSpawn.npcId, npcSpawn.loc.getX(), npcSpawn.loc.getY(), npcSpawn.loc.getZ(), npcSpawn.loc.getHeading(), false, millisToEventEnd, false);
			int respawnDelay = (int) npcSpawn.respawnTime.toMillis();
			if (respawnDelay > 0)
			{
				Spawn spawn = npc.getSpawn();
				spawn.setRespawnDelay(respawnDelay);
				spawn.startRespawn();
				ThreadPool.schedule(spawn::stopRespawn, millisToEventEnd - respawnDelay);
			}
		}
	}
	
	protected void stopEvent()
	{
		this._active = false;
		EventDropManager.getInstance().removeDrops(this);
		if (this._enableShrines)
		{
			EventShrineManager.getInstance().setEnabled(false);
		}
		
		this.destroyItemsOnEnd();
		if (!this._endMsg.isEmpty())
		{
			Broadcast.toAllOnlinePlayers(this._endMsg);
		}
		
		if (this._enterAnnounceId != -1)
		{
			AnnouncementsTable.getInstance().deleteAnnouncement(this._enterAnnounceId);
		}
	}
	
	protected void destroyItemsOnEnd()
	{
		if (!this._destroyItemsOnEnd.isEmpty())
		{
			for (int itemId : this._destroyItemsOnEnd)
			{
				for (Player player : World.getInstance().getPlayers())
				{
					if (player != null)
					{
						player.destroyItemByItemId(ItemProcessType.DESTROY, itemId, -1L, player, true);
					}
				}
				
				try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE item_id=?");)
				{
					statement.setInt(1, itemId);
					statement.execute();
				}
				catch (SQLException var11)
				{
					LOGGER.warning(var11.toString());
				}
			}
		}
	}
	
	public Date getStartDate()
	{
		return this._startDate;
	}
	
	public Date getEndDate()
	{
		return this._endDate;
	}
	
	public boolean isEventPeriod()
	{
		return this._active;
	}
	
	protected class NpcSpawn
	{
		protected final int npcId;
		protected final Location loc;
		protected final Duration respawnTime;
		
		protected NpcSpawn(int spawnNpcId, Location spawnLoc, Duration spawnRespawnTime)
		{
			Objects.requireNonNull(LongTimeEvent.this);
			super();
			this.npcId = spawnNpcId;
			this.loc = spawnLoc;
			this.respawnTime = spawnRespawnTime;
		}
	}
	
	protected class ScheduleEnd implements Runnable
	{
		protected ScheduleEnd()
		{
			Objects.requireNonNull(LongTimeEvent.this);
			super();
		}
		
		@Override
		public void run()
		{
			LongTimeEvent.this.stopEvent();
		}
	}
	
	protected class ScheduleStart implements Runnable
	{
		protected ScheduleStart()
		{
			Objects.requireNonNull(LongTimeEvent.this);
			super();
		}
		
		@Override
		public void run()
		{
			LongTimeEvent.this.startEvent();
		}
	}
}
