package net.sf.l2jdev.gameserver.managers.events;

public class EventShrineManager
{
	private static boolean ENABLE_SHRINES = false;

	public boolean areShrinesEnabled()
	{
		return ENABLE_SHRINES;
	}

	public void setEnabled(boolean enabled)
	{
		ENABLE_SHRINES = enabled;
	}

	public static EventShrineManager getInstance()
	{
		return EventShrineManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final EventShrineManager INSTANCE = new EventShrineManager();
	}
}
