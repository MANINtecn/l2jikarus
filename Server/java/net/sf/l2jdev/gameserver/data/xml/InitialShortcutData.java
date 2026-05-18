package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.MacroType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ShortcutType;
import net.sf.l2jdev.gameserver.model.actor.holders.player.Macro;
import net.sf.l2jdev.gameserver.model.actor.holders.player.MacroCmd;
import net.sf.l2jdev.gameserver.model.actor.holders.player.Shortcut;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.serverpackets.ShortcutRegister;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class InitialShortcutData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(InitialShortcutData.class.getName());
	private final Map<PlayerClass, List<Shortcut>> _initialShortcutData = new EnumMap<>(PlayerClass.class);
	private final List<Shortcut> _initialGlobalShortcutList = new ArrayList<>();
	private final Map<Integer, Macro> _macroPresets = new HashMap<>();

	protected InitialShortcutData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._initialShortcutData.clear();
		this._initialGlobalShortcutList.clear();
		this.parseDatapackFile("data/stats/players/initialShortcuts.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._initialGlobalShortcutList.size() + " initial global shortcut data.");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._initialShortcutData.size() + " initial shortcut data.");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._macroPresets.size() + " macro presets.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node node = document.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if ("list".equals(node.getNodeName()))
			{
				for (Node dataNode = node.getFirstChild(); dataNode != null; dataNode = dataNode.getNextSibling())
				{
					String var5 = dataNode.getNodeName();
					switch (var5)
					{
						case "shortcuts":
							NamedNodeMap attributes = dataNode.getAttributes();
							Node classIdNode = attributes.getNamedItem("classId");
							List<Shortcut> shortcutList = new ArrayList<>();
							Node childNode = dataNode.getFirstChild();

							for (; childNode != null; childNode = childNode.getNextSibling())
							{
								if ("page".equals(childNode.getNodeName()))
								{
									attributes = childNode.getAttributes();
									int pageId = this.parseInteger(attributes, "pageId");

									for (Node slotNode = childNode.getFirstChild(); slotNode != null; slotNode = slotNode.getNextSibling())
									{
										if ("slot".equals(slotNode.getNodeName()))
										{
											NamedNodeMap slotAttributes = slotNode.getAttributes();
											int slotId = this.parseInteger(slotAttributes, "slotId");
											ShortcutType shortcutType = this.parseEnum(slotAttributes, ShortcutType.class, "shortcutType");
											int shortcutId = this.parseInteger(slotAttributes, "shortcutId");
											int shortcutLevel = this.parseInteger(slotAttributes, "shortcutLevel", 0);
											int characterType = this.parseInteger(slotAttributes, "characterType", 0);
											shortcutList.add(new Shortcut(slotId, pageId, shortcutType, shortcutId, shortcutLevel, 0, characterType));
										}
									}
								}
							}

							if (classIdNode != null)
							{
								this._initialShortcutData.put(PlayerClass.getPlayerClass(Integer.parseInt(classIdNode.getNodeValue())), shortcutList);
							}
							else
							{
								this._initialGlobalShortcutList.addAll(shortcutList);
							}
							break;
						case "macros":
							for (Node macroNode = dataNode.getFirstChild(); macroNode != null; macroNode = macroNode.getNextSibling())
							{
								if ("macro".equals(macroNode.getNodeName()))
								{
									NamedNodeMap macroAttrs = macroNode.getAttributes();
									if (this.parseBoolean(macroAttrs, "enabled", true))
									{
										int macroId = this.parseInteger(macroAttrs, "macroId");
										int icon = this.parseInteger(macroAttrs, "icon");
										String name = this.parseString(macroAttrs, "name");
										String description = this.parseString(macroAttrs, "description");
										String acronym = this.parseString(macroAttrs, "acronym");
										List<MacroCmd> commands = new ArrayList<>(1);
										int entryIndex = 0;

										for (Node commandNode = macroNode.getFirstChild(); commandNode != null; commandNode = commandNode.getNextSibling())
										{
											if ("command".equals(commandNode.getNodeName()))
											{
												NamedNodeMap cmdAttrs = commandNode.getAttributes();
												MacroType type = this.parseEnum(cmdAttrs, MacroType.class, "type");
												int parameterOne = 0;
												int parameterTwo = 0;
												String commandText = commandNode.getTextContent();
												switch (type)
												{
													case SKILL:
														parameterOne = this.parseInteger(cmdAttrs, "skillId");
														parameterTwo = this.parseInteger(cmdAttrs, "skillLevel", 0);
														break;
													case ACTION:
														parameterOne = this.parseInteger(cmdAttrs, "actionId");
														break;
													case TEXT:
													default:
														break;
													case SHORTCUT:
														parameterOne = this.parseInteger(cmdAttrs, "page");
														parameterTwo = this.parseInteger(cmdAttrs, "slot", 0);
														break;
													case ITEM:
														parameterOne = this.parseInteger(cmdAttrs, "itemId");
														break;
													case DELAY:
														parameterOne = this.parseInteger(cmdAttrs, "delay");
												}

												commands.add(new MacroCmd(entryIndex++, type, parameterOne, parameterTwo, commandText));
											}
										}

										this._macroPresets.put(macroId, new Macro(macroId, icon, name, description, acronym, commands));
									}
								}
							}
					}
				}
			}
		}
	}

	public void registerAllShortcuts(Player player)
	{
		if (player != null)
		{
			for (Shortcut shortcut : this._initialGlobalShortcutList)
			{
				int shortcutId = shortcut.getId();
				switch (shortcut.getType())
				{
					case ITEM:
						Item item = player.getInventory().getItemByItemId(shortcutId);
						if (item == null)
						{
							continue;
						}

						shortcutId = item.getObjectId();
						break;
					case SKILL:
						if (!player.getSkills().containsKey(shortcutId))
						{
							continue;
						}
						break;
					case MACRO:
						Macro macro = this._macroPresets.get(shortcutId);
						if (macro == null)
						{
							continue;
						}

						player.registerMacro(macro);
				}

				Shortcut newShortcut = new Shortcut(shortcut.getSlot(), shortcut.getPage(), shortcut.getType(), shortcutId, shortcut.getLevel(), shortcut.getSubLevel(), shortcut.getCharacterType());
				player.sendPacket(new ShortcutRegister(newShortcut, player));
				player.registerShortcut(newShortcut);
			}

			if (this._initialShortcutData.containsKey(player.getPlayerClass()))
			{
				for (Shortcut shortcut : this._initialShortcutData.get(player.getPlayerClass()))
				{
					int shortcutId = shortcut.getId();
					switch (shortcut.getType())
					{
						case ITEM:
							Item item = player.getInventory().getItemByItemId(shortcutId);
							if (item == null)
							{
								continue;
							}

							shortcutId = item.getObjectId();
							break;
						case SKILL:
							if (!player.getSkills().containsKey(shortcut.getId()))
							{
								continue;
							}
							break;
						case MACRO:
							Macro macro = this._macroPresets.get(shortcutId);
							if (macro == null)
							{
								continue;
							}

							player.registerMacro(macro);
					}

					Shortcut newShortcut = new Shortcut(shortcut.getSlot(), shortcut.getPage(), shortcut.getType(), shortcutId, shortcut.getLevel(), shortcut.getSubLevel(), shortcut.getCharacterType());
					player.sendPacket(new ShortcutRegister(newShortcut, player));
					player.registerShortcut(newShortcut);
				}
			}
		}
	}

	public static InitialShortcutData getInstance()
	{
		return InitialShortcutData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final InitialShortcutData INSTANCE = new InitialShortcutData();
	}
}
