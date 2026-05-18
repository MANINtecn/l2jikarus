package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.config.VipSystemConfig;
import net.sf.l2jdev.gameserver.model.vip.VipInfo;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class VipData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(VipData.class.getName());
	private final Map<Byte, VipInfo> _vipTiers = new HashMap<>();

	protected VipData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		if (VipSystemConfig.VIP_SYSTEM_ENABLED)
		{
			this._vipTiers.clear();
			this.parseDatapackFile("data/Vip.xml");
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._vipTiers.size() + " vips.");
		}
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				label55:
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("vip".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						byte tier = -1;
						int required = -1;
						int lose = -1;
						Node att = attrs.getNamedItem("tier");
						if (att == null)
						{
							LOGGER.severe(this.getClass().getSimpleName() + ": Missing tier for vip, skipping");
						}
						else
						{
							tier = Byte.parseByte(att.getNodeValue());
							att = attrs.getNamedItem("points-required");
							if (att == null)
							{
								LOGGER.severe(this.getClass().getSimpleName() + ": Missing points-required for vip: " + tier + ", skipping");
							}
							else
							{
								required = Integer.parseInt(att.getNodeValue());
								att = attrs.getNamedItem("points-lose");
								if (att == null)
								{
									LOGGER.severe(this.getClass().getSimpleName() + ": Missing points-lose for vip: " + tier + ", skipping");
								}
								else
								{
									lose = Integer.parseInt(att.getNodeValue());
									VipInfo vipInfo = new VipInfo(tier, required, lose);

									for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
									{
										if ("bonus".equalsIgnoreCase(c.getNodeName()))
										{
											int skill = Integer.parseInt(c.getAttributes().getNamedItem("skill").getNodeValue());

											try
											{
												vipInfo.setSkill(skill);
											}
											catch (Exception var14)
											{
												LOGGER.severe(this.getClass().getSimpleName() + ": Error in bonus parameter for vip: " + tier + ", skipping");
												continue label55;
											}
										}
									}

									this._vipTiers.put(tier, vipInfo);
								}
							}
						}
					}
				}
			}
		}
	}

	public static VipData getInstance()
	{
		return VipData.SingletonHolder.INSTANCE;
	}

	public int getSkillId(byte tier)
	{
		return this._vipTiers.get(tier).getSkill();
	}

	public Map<Byte, VipInfo> getVipTiers()
	{
		return this._vipTiers;
	}

	private static class SingletonHolder
	{
		protected static final VipData INSTANCE = new VipData();
	}
}
