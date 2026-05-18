package net.sf.l2jdev.gameserver.model.html.styles;

import net.sf.l2jdev.gameserver.model.html.IHtmlStyle;

public class DefaultStyle implements IHtmlStyle
{
	public static final String DEFAULT_PAGE_LINK_FORMAT = "<td><a action=\"%s\">%s</a></td>";
	public static final String DEFAULT_PAGE_TEXT_FORMAT = "<td>%s</td>";
	public static final String DEFAULT_PAGER_SEPARATOR = "<td align=center> | </td>";
	public static final DefaultStyle INSTANCE = new DefaultStyle();

	@Override
	public String applyBypass(String bypass, String name, boolean isEnabled)
	{
		return isEnabled ? String.format("<td>%s</td>", name) : String.format("<td><a action=\"%s\">%s</a></td>", bypass, name);
	}

	@Override
	public String applySeparator()
	{
		return "<td align=center> | </td>";
	}
}
