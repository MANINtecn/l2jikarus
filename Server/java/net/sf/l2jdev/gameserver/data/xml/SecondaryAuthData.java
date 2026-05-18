package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class SecondaryAuthData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(SecondaryAuthData.class.getName());
	private final Set<String> _forbiddenPasswords = new HashSet<>();
	private boolean _enabled = false;
	private int _maxAttempts = 5;
	private int _banTime = 480;
	private String _recoveryLink = "";

	protected SecondaryAuthData()
	{
		this.load();
	}

	@Override
	public synchronized void load()
	{
		this._forbiddenPasswords.clear();
		this.parseFile(new File("config/SecondaryAuth.xml"));
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._forbiddenPasswords.size() + " forbidden passwords.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		try
		{
			for (Node node = document.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if ("list".equalsIgnoreCase(node.getNodeName()))
				{
					for (Node list_node = node.getFirstChild(); list_node != null; list_node = list_node.getNextSibling())
					{
						if ("enabled".equalsIgnoreCase(list_node.getNodeName()))
						{
							this._enabled = Boolean.parseBoolean(list_node.getTextContent());
						}
						else if ("maxAttempts".equalsIgnoreCase(list_node.getNodeName()))
						{
							this._maxAttempts = Integer.parseInt(list_node.getTextContent());
						}
						else if ("banTime".equalsIgnoreCase(list_node.getNodeName()))
						{
							this._banTime = Integer.parseInt(list_node.getTextContent());
						}
						else if ("recoveryLink".equalsIgnoreCase(list_node.getNodeName()))
						{
							this._recoveryLink = list_node.getTextContent();
						}
						else if ("forbiddenPasswords".equalsIgnoreCase(list_node.getNodeName()))
						{
							for (Node forbiddenPasswords_node = list_node.getFirstChild(); forbiddenPasswords_node != null; forbiddenPasswords_node = forbiddenPasswords_node.getNextSibling())
							{
								if ("password".equalsIgnoreCase(forbiddenPasswords_node.getNodeName()))
								{
									this._forbiddenPasswords.add(forbiddenPasswords_node.getTextContent());
								}
							}
						}
					}
				}
			}
		}
		catch (Exception var6)
		{
			LOGGER.log(Level.WARNING, "Failed to load secondary auth data from xml.", var6);
		}
	}

	public boolean isEnabled()
	{
		return this._enabled;
	}

	public int getMaxAttempts()
	{
		return this._maxAttempts;
	}

	public int getBanTime()
	{
		return this._banTime;
	}

	public String getRecoveryLink()
	{
		return this._recoveryLink;
	}

	public Set<String> getForbiddenPasswords()
	{
		return this._forbiddenPasswords;
	}

	public boolean isForbiddenPassword(String password)
	{
		return this._forbiddenPasswords.contains(password);
	}

	public static SecondaryAuthData getInstance()
	{
		return SecondaryAuthData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final SecondaryAuthData INSTANCE = new SecondaryAuthData();
	}
}
