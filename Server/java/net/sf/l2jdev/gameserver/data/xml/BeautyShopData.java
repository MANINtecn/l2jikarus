package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.enums.player.Sex;
import net.sf.l2jdev.gameserver.model.beautyshop.BeautyData;
import net.sf.l2jdev.gameserver.model.beautyshop.BeautyItem;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class BeautyShopData implements IXmlReader
{
	private final Map<Race, Map<Sex, BeautyData>> _beautyList = new EnumMap<>(Race.class);
	private final Map<Sex, BeautyData> _beautyData = new EnumMap<>(Sex.class);

	protected BeautyShopData()
	{
		this.load();
	}

	@Override
	public synchronized void load()
	{
		this._beautyList.clear();
		this._beautyData.clear();
		this.parseDatapackFile("data/BeautyShop.xml");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		Race race = null;
		Sex sex = null;

		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("race".equalsIgnoreCase(d.getNodeName()))
					{
						Node att = d.getAttributes().getNamedItem("type");
						if (att != null)
						{
							race = this.parseEnum(att, Race.class);
						}

						for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
						{
							if ("sex".equalsIgnoreCase(b.getNodeName()))
							{
								att = b.getAttributes().getNamedItem("type");
								if (att != null)
								{
									sex = this.parseEnum(att, Sex.class);
								}

								BeautyData beautyData = new BeautyData();

								for (Node a = b.getFirstChild(); a != null; a = a.getNextSibling())
								{
									if ("hair".equalsIgnoreCase(a.getNodeName()))
									{
										NamedNodeMap attrs = a.getAttributes();
										StatSet set = new StatSet();

										for (int i = 0; i < attrs.getLength(); i++)
										{
											att = attrs.item(i);
											set.set(att.getNodeName(), att.getNodeValue());
										}

										BeautyItem hair = new BeautyItem(set);

										for (Node g = a.getFirstChild(); g != null; g = g.getNextSibling())
										{
											if ("color".equalsIgnoreCase(g.getNodeName()))
											{
												attrs = g.getAttributes();
												set = new StatSet();

												for (int i = 0; i < attrs.getLength(); i++)
												{
													att = attrs.item(i);
													set.set(att.getNodeName(), att.getNodeValue());
												}

												hair.addColor(set);
											}
										}

										beautyData.addHair(hair);
									}
									else if ("face".equalsIgnoreCase(a.getNodeName()))
									{
										NamedNodeMap attrs = a.getAttributes();
										StatSet set = new StatSet();

										for (int i = 0; i < attrs.getLength(); i++)
										{
											att = attrs.item(i);
											set.set(att.getNodeName(), att.getNodeValue());
										}

										BeautyItem face = new BeautyItem(set);
										beautyData.addFace(face);
									}
								}

								this._beautyData.put(sex, beautyData);
							}
						}

						this._beautyList.put(race, this._beautyData);
					}
				}
			}
		}
	}

	public boolean hasBeautyData(Race race, Sex sex)
	{
		return this._beautyList.containsKey(race) && this._beautyList.get(race).containsKey(sex);
	}

	public BeautyData getBeautyData(Race race, Sex sex)
	{
		return this._beautyList.containsKey(race) ? this._beautyList.get(race).get(sex) : null;
	}

	public static BeautyShopData getInstance()
	{
		return BeautyShopData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final BeautyShopData INSTANCE = new BeautyShopData();
	}
}
