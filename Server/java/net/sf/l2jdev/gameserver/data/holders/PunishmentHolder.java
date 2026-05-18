package net.sf.l2jdev.gameserver.data.holders;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.model.punishment.PunishmentTask;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentType;

public class PunishmentHolder
{
	private final Map<String, Map<PunishmentType, PunishmentTask>> _holder = new ConcurrentHashMap<>();

	public void addPunishment(PunishmentTask task)
	{
		if (!task.isExpired())
		{
			this._holder.computeIfAbsent(String.valueOf(task.getKey()), _ -> new ConcurrentHashMap<>()).put(task.getType(), task);
		}
	}

	public void stopPunishment(PunishmentTask task)
	{
		String key = String.valueOf(task.getKey());
		if (this._holder.containsKey(key))
		{
			task.stopPunishment();
			Map<PunishmentType, PunishmentTask> punishments = this._holder.get(key);
			punishments.remove(task.getType());
			if (punishments.isEmpty())
			{
				this._holder.remove(key);
			}
		}
	}

	public void stopPunishment(PunishmentType type)
	{
		for (Map<PunishmentType, PunishmentTask> punishments : this._holder.values())
		{
			for (PunishmentTask task : punishments.values())
			{
				if (task.getType() == type)
				{
					task.stopPunishment();
					String key = String.valueOf(task.getKey());
					punishments.remove(task.getType());
					if (punishments.isEmpty())
					{
						this._holder.remove(key);
					}
				}
			}
		}
	}

	public boolean hasPunishment(String key, PunishmentType type)
	{
		return this.getPunishment(key, type) != null;
	}

	public PunishmentTask getPunishment(String key, PunishmentType type)
	{
		return this._holder.containsKey(key) ? this._holder.get(key).get(type) : null;
	}
}
