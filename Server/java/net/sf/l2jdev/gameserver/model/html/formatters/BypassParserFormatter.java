package net.sf.l2jdev.gameserver.model.html.formatters;

import net.sf.l2jdev.gameserver.model.html.IBypassFormatter;

public class BypassParserFormatter implements IBypassFormatter
{
	public static final BypassParserFormatter INSTANCE = new BypassParserFormatter();

	@Override
	public String formatBypass(String bypass, int page)
	{
		return bypass + " page=" + page;
	}
}
