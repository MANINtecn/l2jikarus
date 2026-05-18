package net.sf.l2jdev.gameserver.model.html;

@FunctionalInterface
public interface IPageHandler
{
	void apply(String var1, int var2, int var3, StringBuilder var4, IBypassFormatter var5, IHtmlStyle var6);
}
