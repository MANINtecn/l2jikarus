package net.sf.l2jdev.gameserver.network.clientpackets.autoplay;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ShortcutType;
import net.sf.l2jdev.gameserver.model.actor.holders.player.Shortcut;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.taskmanagers.AutoUseTaskManager;

public class ExRequestActivateAutoShortcut extends ClientPacket
{
	private int _slot;
	private int _page;
	private boolean _active;

	@Override
	protected void readImpl()
	{
		int position = this.readShort();
		this._slot = position % 12;
		this._page = position / 12;
		this._active = this.readByte() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._active)
			{
				player.addAutoShortcut(this._slot, this._page);
			}
			else
			{
				player.removeAutoShortcut(this._slot, this._page);
			}

			Item item = null;
			Skill skill = null;
			if (this._slot == -1 && this._page == 0 && this._active)
			{
				for (int i = 0; i < 12; i++)
				{
					Shortcut autoUseAllSupply1 = player.getShortcut(i, 22);
					Shortcut autoUseAllSupply2 = player.getShortcut(i, 24);
					if (autoUseAllSupply1 != null)
					{
						Item itemAll1 = player.getInventory().getItemByObjectId(autoUseAllSupply1.getId());
						if (itemAll1 != null)
						{
							player.addAutoShortcut(i, 22);
							AutoUseTaskManager.getInstance().addAutoSupplyItem(player, itemAll1.getId());
						}
					}

					if (autoUseAllSupply2 != null)
					{
						Item itemAll2 = player.getInventory().getItemByObjectId(autoUseAllSupply2.getId());
						if (itemAll2 != null)
						{
							player.addAutoShortcut(i, 24);
							AutoUseTaskManager.getInstance().addAutoSupplyItem(player, itemAll2.getId());
						}
					}
				}
			}
			else if (this._slot == -1 && this._page == 0 && !this._active)
			{
				for (int i = 0; i < 12; i++)
				{
					Shortcut autoUseAllSupply1x = player.getShortcut(i, 22);
					Shortcut autoUseAllSupply2x = player.getShortcut(i, 24);
					if (autoUseAllSupply1x != null)
					{
						Item itemAll1 = player.getInventory().getItemByObjectId(autoUseAllSupply1x.getId());
						if (itemAll1 != null)
						{
							player.removeAutoShortcut(i, 22);
							AutoUseTaskManager.getInstance().removeAutoSupplyItem(player, itemAll1.getId());
						}
					}

					if (autoUseAllSupply2x != null)
					{
						Item itemAll2 = player.getInventory().getItemByObjectId(autoUseAllSupply2x.getId());
						if (itemAll2 != null)
						{
							player.removeAutoShortcut(i, 24);
							AutoUseTaskManager.getInstance().removeAutoSupplyItem(player, itemAll2.getId());
						}
					}
				}
			}
			else
			{
				Shortcut shortcut = player.getShortcut(this._slot, this._page);
				if (shortcut != null)
				{
					if (shortcut.getType() == ShortcutType.SKILL)
					{
						int skillId = player.getReplacementSkill(shortcut.getId());
						skill = player.getKnownSkill(skillId);
						if (skill == null)
						{
							if (player.hasServitors())
							{
								for (Summon summon : player.getServitors().values())
								{
									skill = summon.getKnownSkill(skillId);
									if (skill != null)
									{
										break;
									}
								}
							}

							if (skill == null && player.hasPet())
							{
								skill = player.getPet().getKnownSkill(skillId);
							}
						}
					}
					else
					{
						item = player.getInventory().getItemByObjectId(shortcut.getId());
					}

					if (!this._active)
					{
						if (item != null)
						{
							if (!item.isPotion())
							{
								AutoUseTaskManager.getInstance().removeAutoSupplyItem(player, item.getId());
							}
							else
							{
								AutoUseTaskManager.getInstance().removeAutoPotionItem(player);
								AutoUseTaskManager.getInstance().removeAutoPetPotionItem(player);
							}
						}

						if (skill != null)
						{
							if (skill.hasNegativeEffect())
							{
								AutoUseTaskManager.getInstance().removeAutoSkill(player, skill.getId());
							}
							else
							{
								AutoUseTaskManager.getInstance().removeAutoBuff(player, skill.getId());
							}
						}
						else
						{
							AutoUseTaskManager.getInstance().removeAutoAction(player, shortcut.getId());
						}
					}
					else
					{
						if (item != null && !item.isPotion())
						{
							if (GeneralConfig.ENABLE_AUTO_ITEM)
							{
								AutoUseTaskManager.getInstance().addAutoSupplyItem(player, item.getId());
							}
						}
						else
						{
							if (this._page == 23)
							{
								if (this._slot == 1)
								{
									if (GeneralConfig.ENABLE_AUTO_POTION && item != null && item.isPotion())
									{
										AutoUseTaskManager.getInstance().setAutoPotionItem(player, item.getId());
										return;
									}
								}
								else if (this._slot == 2 && GeneralConfig.ENABLE_AUTO_PET_POTION && item != null && item.isPotion())
								{
									AutoUseTaskManager.getInstance().setAutoPetPotionItem(player, item.getId());
									return;
								}
							}

							if (GeneralConfig.ENABLE_AUTO_SKILL && skill != null)
							{
								if (skill.hasNegativeEffect())
								{
									AutoUseTaskManager.getInstance().addAutoSkill(player, skill.getId());
								}
								else
								{
									AutoUseTaskManager.getInstance().addAutoBuff(player, skill.getId());
								}

								return;
							}

							AutoUseTaskManager.getInstance().addAutoAction(player, shortcut.getId());
						}
					}
				}
			}
		}
	}
}
