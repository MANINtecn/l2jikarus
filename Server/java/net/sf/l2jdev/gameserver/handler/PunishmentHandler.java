package net.sf.l2jdev.gameserver.handler;

import java.util.EnumMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.punishment.PunishmentType;

public class PunishmentHandler implements IHandler<IPunishmentHandler, PunishmentType>
{
	private final Map<PunishmentType, IPunishmentHandler> _handlers = new EnumMap<>(PunishmentType.class);

	protected PunishmentHandler()
	{
	}

	@Override
	public void registerHandler(IPunishmentHandler handler)
	{
		this._handlers.put(handler.getType(), handler);
	}

	@Override
	public synchronized void removeHandler(IPunishmentHandler handler)
	{
		this._handlers.remove(handler.getType());
	}

	@Override
	public IPunishmentHandler getHandler(PunishmentType val)
	{
		return this._handlers.get(val);
	}

	@Override
	public int size()
	{
		return this._handlers.size();
	}

	public static PunishmentHandler getInstance()
	{
		return PunishmentHandler.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final PunishmentHandler INSTANCE = new PunishmentHandler();
	}
}
