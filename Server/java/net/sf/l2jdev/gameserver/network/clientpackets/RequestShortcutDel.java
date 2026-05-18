package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.Shortcut;
import net.sf.l2jdev.gameserver.taskmanagers.AutoUseTaskManager;

public class RequestShortcutDel extends ClientPacket
{
	private int _slot;
	private int _page;

	@Override
	protected void readImpl()
	{
		int position = this.readInt();
		this._slot = position % 12;
		this._page = position / 12;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._page <= 24 && this._page >= 0)
			{
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
								AutoUseTaskManager.getInstance().removeAutoSupplyItem(player, oldShortcut.getId());
								break;
							case ACTION:
								AutoUseTaskManager.getInstance().removeAutoAction(player, oldShortcut.getId());
						}
					}
				}

				player.restoreAutoShortcutVisual();
			}
		}
	}
}
