package net.sf.l2jdev.gameserver.model.script.timers;

@FunctionalInterface
public interface IEventTimerEvent<T>
{
	void onTimerEvent(TimerHolder<T> var1);
}
