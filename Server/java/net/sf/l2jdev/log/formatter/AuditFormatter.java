package net.sf.l2jdev.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.util.StringUtil;

public class AuditFormatter extends Formatter
{
	private final SimpleDateFormat _dateFormat = new SimpleDateFormat("dd MMM H:mm:ss");

	@Override
	public String format(LogRecord record)
	{
		Object[] params = record.getParameters();
		StringBuilder output = new StringBuilder(30 + record.getMessage().length() + (params == null ? 0 : params.length * 10));
		StringUtil.append(output, "[", this._dateFormat.format(new Date(record.getMillis())), "] ", record.getMessage());
		if (params != null)
		{
			output.append(Arrays.stream(params).filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(", ")));
		}

		output.append(System.lineSeparator());
		return output.toString();
	}
}
