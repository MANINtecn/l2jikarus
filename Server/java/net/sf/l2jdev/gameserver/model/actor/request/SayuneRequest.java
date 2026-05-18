package net.sf.l2jdev.gameserver.model.actor.request;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import net.sf.l2jdev.gameserver.data.xml.SayuneData;
import net.sf.l2jdev.gameserver.model.SayuneEntry;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.enums.SayuneType;
import net.sf.l2jdev.gameserver.network.serverpackets.sayune.ExFlyMove;
import net.sf.l2jdev.gameserver.network.serverpackets.sayune.ExFlyMoveBroadcast;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class SayuneRequest extends AbstractRequest
{
	private final int _mapId;
	private boolean _isSelecting;
	private final Deque<SayuneEntry> _possibleEntries = new LinkedList<>();

	public SayuneRequest(Player player, int mapId)
	{
		super(player);
		this._mapId = mapId;
		SayuneEntry map = SayuneData.getInstance().getMap(this._mapId);
		Objects.requireNonNull(map);
		this._possibleEntries.addAll(map.getInnerEntries());
	}

	@Override
	public boolean isUsing(int objectId)
	{
		return false;
	}

	private SayuneEntry findEntry(int pos)
	{
		if (this._possibleEntries.isEmpty())
		{
			return null;
		}
		else if (this._isSelecting)
		{
			for (SayuneEntry entry : this._possibleEntries)
			{
				if (entry.getId() == pos)
				{
					return entry;
				}
			}

			return null;
		}
		else
		{
			return this._possibleEntries.removeFirst();
		}
	}

	public synchronized void move(Player player, int pos)
	{
		SayuneEntry map = SayuneData.getInstance().getMap(this._mapId);
		if (map != null && !map.getInnerEntries().isEmpty())
		{
			SayuneEntry nextEntry = this.findEntry(pos);
			if (nextEntry == null)
			{
				player.removeRequest(this.getClass());
			}
			else
			{
				if (this._isSelecting)
				{
					this._isSelecting = false;
					if (!nextEntry.isSelector())
					{
						this._possibleEntries.clear();
						this._possibleEntries.addAll(nextEntry.getInnerEntries());
					}
				}

				SayuneType type = pos == 0 && nextEntry.isSelector() ? SayuneType.START_LOC : (nextEntry.isSelector() ? SayuneType.MULTI_WAY_LOC : SayuneType.ONE_WAY_LOC);
				List<SayuneEntry> locations = nextEntry.isSelector() ? nextEntry.getInnerEntries() : Arrays.asList(nextEntry);
				if (nextEntry.isSelector())
				{
					this._possibleEntries.clear();
					this._possibleEntries.addAll(locations);
					this._isSelecting = true;
				}

				player.sendPacket(new ExFlyMove(player, type, this._mapId, locations));
				SayuneEntry activeEntry = locations.get(0);
				Broadcast.toKnownPlayersInRadius(player, new ExFlyMoveBroadcast(player, type, map.getId(), activeEntry), 1000);
				player.setXYZ(activeEntry);
			}
		}
		else
		{
			player.sendMessage("MapId: " + this._mapId + " was not found in the map!");
		}
	}

	public void onLogout()
	{
		SayuneEntry map = SayuneData.getInstance().getMap(this._mapId);
		if (map != null && !map.getInnerEntries().isEmpty())
		{
			SayuneEntry nextEntry = this.findEntry(0);
			if (!this._isSelecting && (nextEntry == null || !nextEntry.isSelector()))
			{
				SayuneEntry lastEntry = map.getInnerEntries().get(map.getInnerEntries().size() - 1);
				if (lastEntry != null)
				{
					this.getPlayer().setXYZ(lastEntry);
				}
				else
				{
					this.getPlayer().setXYZ(map);
				}
			}
			else
			{
				this.getPlayer().setXYZ(map);
			}
		}
	}
}
