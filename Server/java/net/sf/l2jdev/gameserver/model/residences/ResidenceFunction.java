package net.sf.l2jdev.gameserver.model.residences;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.xml.ResidenceFunctionsData;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.ItemContainer;
import net.sf.l2jdev.gameserver.network.serverpackets.AgitDecoInfo;

public class ResidenceFunction
{
	private final int _id;
	private final int _level;
	private long _expiration;
	private final AbstractResidence _residense;
	private ScheduledFuture<?> _task;

	public ResidenceFunction(int id, int level, long expiration, AbstractResidence residense)
	{
		this._id = id;
		this._level = level;
		this._expiration = expiration;
		this._residense = residense;
		this.init();
	}

	public ResidenceFunction(int id, int level, AbstractResidence residense)
	{
		this._id = id;
		this._level = level;
		ResidenceFunctionTemplate template = this.getTemplate();
		this._expiration = Instant.now().toEpochMilli() + template.getDuration().toMillis();
		this._residense = residense;
		this.init();
	}

	private void init()
	{
		ResidenceFunctionTemplate template = this.getTemplate();
		if (template != null && this._expiration > System.currentTimeMillis())
		{
			this._task = ThreadPool.schedule(this::onFunctionExpiration, this._expiration - System.currentTimeMillis());
		}
	}

	public int getId()
	{
		return this._id;
	}

	public int getLevel()
	{
		return this._level;
	}

	public long getExpiration()
	{
		return this._expiration;
	}

	public int getOwnerId()
	{
		return this._residense.getOwnerId();
	}

	public double getValue()
	{
		ResidenceFunctionTemplate template = this.getTemplate();
		return template == null ? 0.0 : template.getValue();
	}

	public ResidenceFunctionType getType()
	{
		ResidenceFunctionTemplate template = this.getTemplate();
		return template == null ? ResidenceFunctionType.NONE : template.getType();
	}

	public ResidenceFunctionTemplate getTemplate()
	{
		return ResidenceFunctionsData.getInstance().getFunction(this._id, this._level);
	}

	private void onFunctionExpiration()
	{
		if (!this.reactivate())
		{
			this._residense.removeFunction(this);
			Clan clan = ClanTable.getInstance().getClan(this._residense.getOwnerId());
			if (clan != null)
			{
				clan.broadcastToOnlineMembers(new AgitDecoInfo(this._residense));
			}
		}
	}

	public boolean reactivate()
	{
		ResidenceFunctionTemplate template = this.getTemplate();
		if (template == null)
		{
			return false;
		}
		Clan clan = ClanTable.getInstance().getClan(this._residense.getOwnerId());
		if (clan == null)
		{
			return false;
		}
		ItemContainer wh = clan.getWarehouse();
		Item item = wh.getItemByItemId(template.getCost().getId());
		if (item != null && item.getCount() >= template.getCost().getCount())
		{
			if (wh.destroyItem(ItemProcessType.FEE, item, template.getCost().getCount(), null, this) != null)
			{
				this._expiration = System.currentTimeMillis() + template.getDuration().getSeconds() * 1000L;
				this.init();
			}

			return true;
		}
		return false;
	}

	public void cancelExpiration()
	{
		if (this._task != null && !this._task.isDone())
		{
			this._task.cancel(true);
		}

		this._task = null;
	}
}
