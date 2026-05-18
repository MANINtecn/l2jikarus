package net.sf.l2jdev.gameserver.model.html.pagehandlers;

import net.sf.l2jdev.gameserver.model.html.IBypassFormatter;
import net.sf.l2jdev.gameserver.model.html.IHtmlStyle;
import net.sf.l2jdev.gameserver.model.html.IPageHandler;

public class NextPrevPageHandler implements IPageHandler
{
	public static final NextPrevPageHandler INSTANCE = new NextPrevPageHandler();

	@Override
	public void apply(String bypass, int currentPage, int pages, StringBuilder sb, IBypassFormatter bypassFormatter, IHtmlStyle style)
	{
		sb.append(style.applyBypass(bypassFormatter.formatBypass(bypass, 0), "<<", currentPage - 1 < 0));
		sb.append(style.applySeparator());
		sb.append(style.applyBypass(bypassFormatter.formatBypass(bypass, currentPage - 1), "<", currentPage <= 0));
		sb.append(style.applySeparator());
		sb.append(String.format("<td align=\"center\">Page: %d/%d</td>", currentPage + 1, pages + 1));
		sb.append(style.applySeparator());
		sb.append(style.applyBypass(bypassFormatter.formatBypass(bypass, currentPage + 1), ">", currentPage >= pages));
		sb.append(style.applySeparator());
		sb.append(style.applyBypass(bypassFormatter.formatBypass(bypass, pages), ">>", currentPage + 1 > pages));
	}
}
