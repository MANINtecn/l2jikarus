package net.sf.l2jdev.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.item.EtcItem;

public class ItemHandler implements IHandler<IItemHandler, EtcItem>
{
	private final Map<String, IItemHandler> _datatable = new HashMap<>();

	protected ItemHandler()
	{
	}

	@Override
	public void registerHandler(IItemHandler handler)
	{
		this._datatable.put(handler.getClass().getSimpleName(), handler);
	}

	@Override
	public synchronized void removeHandler(IItemHandler handler)
	{
		this._datatable.remove(handler.getClass().getSimpleName());
	}

	@Override
	public IItemHandler getHandler(EtcItem item)
	{
		return item != null && item.getHandlerName() != null ? this._datatable.get(item.getHandlerName()) : null;
	}

	@Override
	public int size()
	{
		return this._datatable.size();
	}

	public static ItemHandler getInstance()
	{
		return ItemHandler.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ItemHandler INSTANCE = new ItemHandler();
	}
}
