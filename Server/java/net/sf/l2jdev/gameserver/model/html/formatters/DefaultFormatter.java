package net.sf.l2jdev.gameserver.model.html.formatters;

import net.sf.l2jdev.gameserver.model.html.IBypassFormatter;

public class DefaultFormatter implements IBypassFormatter
{
	public static final DefaultFormatter INSTANCE = new DefaultFormatter();

	@Override
	public String formatBypass(String bypass, int page)
	{
		return bypass + " " + page;
	}
}
