package net.sf.l2jdev.gameserver.model.events.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.sf.l2jdev.gameserver.model.events.EventType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RegisterEvent
{
	EventType value();
}
