package net.sf.l2jdev.gameserver.model.script.timers;

@FunctionalInterface
public interface IEventTimerCancel<T>
{
	void onTimerCancel(TimerHolder<T> var1);
}
