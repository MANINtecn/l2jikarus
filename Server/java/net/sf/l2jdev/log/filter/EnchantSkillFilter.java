package net.sf.l2jdev.log.filter;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class EnchantSkillFilter implements Filter
{
	@Override
	public boolean isLoggable(LogRecord record)
	{
		return "enchant.skills".equals(record.getLoggerName());
	}
}
