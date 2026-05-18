package net.sf.l2jdev.log.formatter;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class FileLogFormatter extends Formatter
{
	@Override
	public String format(LogRecord record)
	{
		return record.getLevel().getName() + ": " + record.getMessage() + System.lineSeparator();
	}
}