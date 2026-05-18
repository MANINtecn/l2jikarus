package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.List;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ShortcutType;
import net.sf.l2jdev.gameserver.model.actor.holders.player.Shortcut;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.serverpackets.ShortcutRegister;
import net.sf.l2jdev.gameserver.network.serverpackets.autoplay.ExActivateAutoShortcut;
import net.sf.l2jdev.gameserver.taskmanagers.AutoUseTaskManager;

public class RequestShortcutReg extends ClientPacket
{
	private ShortcutType _type;
	private int _id;
	private int _slot;
	private int _page;
	private int _level;
	private int _subLevel;
	private int _characterType;
	private boolean _active;

	@Override
	protected void readImpl()
	{
		int typeId = this.readInt();
		this._type = ShortcutType.values()[typeId >= 1 && typeId <= 6 ? typeId : 0];
		int position = this.readInt();
		this._slot = position % 12;
		this._page = position / 12;
		this._active = this.readByte() == 1;
		this._id = this.readInt();
		this._level = this.readShort();
		this._subLevel = this.readShort();
		this._characterType = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._page <= 25 && this._page >= 0)
			{
				if (this._page != 22 && this._page != 24)
				{
					if (this._page == 23)
					{
						if (this._slot != 0 && this._slot != 3)
						{
							Item item = player.getInventory().getItemByObjectId(this._id);
							if (item == null || !item.isPotion())
							{
								return;
							}
						}
						else if (this._type != ShortcutType.MACRO)
						{
							return;
						}
					}
				}
				else
				{
					if (this._type != ShortcutType.ITEM)
					{
						return;
					}

					Item item = player.getInventory().getItemByObjectId(this._id);
					if (item == null || item.isPotion())
					{
						return;
					}
				}

				Shortcut oldShortcut = player.getShortcut(this._slot, this._page);
				player.deleteShortcut(this._slot, this._page);
				if (oldShortcut != null)
				{
					boolean removed = true;
					if (oldShortcut.isAutoUse())
					{
						player.removeAutoShortcut(this._slot, this._page);

						for (Shortcut shortcut : player.getAllShortcuts())
						{
							if (oldShortcut.getId() == shortcut.getId() && oldShortcut.getType() == shortcut.getType())
							{
								player.addAutoShortcut(shortcut.getSlot(), shortcut.getPage());
								removed = false;
							}
						}
					}

					if (removed)
					{
						switch (oldShortcut.getType())
						{
							case SKILL:
								AutoUseTaskManager.getInstance().removeAutoBuff(player, oldShortcut.getId());
								AutoUseTaskManager.getInstance().removeAutoSkill(player, oldShortcut.getId());
								break;
							case ITEM:
								if (player.getInventory().getItemByObjectId(oldShortcut.getId()).isPotion())
								{
									AutoUseTaskManager.getInstance().removeAutoPotionItem(player);
								}
								else
								{
									AutoUseTaskManager.getInstance().removeAutoSupplyItem(player, oldShortcut.getId());
								}
								break;
							case ACTION:
								AutoUseTaskManager.getInstance().removeAutoAction(player, oldShortcut.getId());
						}
					}
				}

				player.restoreAutoShortcutVisual();
				Shortcut sc = new Shortcut(this._slot, this._page, this._type, this._id, this._level, this._subLevel, this._characterType);
				sc.setAutoUse(this._active);
				player.registerShortcut(sc);
				player.sendPacket(new ShortcutRegister(sc, player));
				player.sendPacket(new ExActivateAutoShortcut(sc, this._active));
				player.sendSkillList();
				if (!player.getAutoUseSettings().isAutoSkill(this._id) && !player.getAutoUseSettings().getAutoSupplyItems().contains(this._id))
				{
					List<Integer> positions = player.getVariables().getIntegerList("AUTO_USE_SHORTCUTS");
					Integer position = this._slot + this._page * 12;
					if (positions.contains(position))
					{
						positions.remove(position);
						player.getVariables().setIntegerList("AUTO_USE_SHORTCUTS", positions);
					}
				}
				else
				{
					for (Shortcut shortcutx : player.getAllShortcuts())
					{
						if (shortcutx.isAutoUse() && shortcutx.getId() == this._id && shortcutx.getType() == this._type)
						{
							player.addAutoShortcut(this._slot, this._page);
							break;
						}
					}
				}
			}
		}
	}
}
