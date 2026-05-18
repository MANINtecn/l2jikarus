package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ShotType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.ActionType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestAutoSoulShot extends ClientPacket
{
	private int _itemId;
	private boolean _enable;
	private int _type;

	@Override
	protected void readImpl()
	{
		this._itemId = this.readInt();
		this._enable = this.readInt() == 1;
		this._type = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (!player.isInStoreMode() && player.getActiveRequester() == null && !player.isDead())
			{
				Item item = player.getInventory().getItemByItemId(this._itemId);
				if (item == null)
				{
					return;
				}

				if (this._enable)
				{
					if (!player.getInventory().canManipulateWithItemId(item.getId()))
					{
						player.sendMessage("Cannot use this item.");
						return;
					}

					if (isSummonShot(item.getTemplate()))
					{
						if (player.hasSummon())
						{
							boolean isSoulshot = item.getEtcItem().getDefaultAction() == ActionType.SUMMON_SOULSHOT;
							boolean isSpiritshot = item.getEtcItem().getDefaultAction() == ActionType.SUMMON_SPIRITSHOT;
							if (isSoulshot)
							{
								int soulshotCount = 0;
								Summon pet = player.getPet();
								if (pet != null)
								{
									soulshotCount += pet.getSoulShotsPerHit();
								}

								for (Summon servitor : player.getServitors().values())
								{
									soulshotCount += servitor.getSoulShotsPerHit();
								}

								if (soulshotCount > item.getCount())
								{
									player.sendPacket(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_SERVITOR);
									return;
								}
							}
							else if (isSpiritshot)
							{
								int spiritshotCount = 0;
								Summon petx = player.getPet();
								if (petx != null)
								{
									spiritshotCount += petx.getSpiritShotsPerHit();
								}

								for (Summon servitor : player.getServitors().values())
								{
									spiritshotCount += servitor.getSpiritShotsPerHit();
								}

								if (spiritshotCount > item.getCount())
								{
									player.sendPacket(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_SERVITOR);
									return;
								}
							}

							player.addAutoSoulShot(this._itemId);
							player.sendPacket(new ExAutoSoulShot(this._itemId, this._enable, this._type));
							Summon petxx = player.getPet();
							if (petxx != null)
							{
								if (!petxx.isChargedShot(item.getTemplate().getDefaultAction() == ActionType.SUMMON_SOULSHOT ? ShotType.SOULSHOTS : (item.getId() != 6647 && item.getId() != 20334 ? ShotType.SPIRITSHOTS : ShotType.BLESSED_SPIRITSHOTS)))
								{
									SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED);
									sm.addItemName(item);
									player.sendPacket(sm);
								}

								petxx.rechargeShots(isSoulshot, isSpiritshot, false);
							}

							for (Summon summon : player.getServitors().values())
							{
								if (!summon.isChargedShot(item.getTemplate().getDefaultAction() == ActionType.SUMMON_SOULSHOT ? ShotType.SOULSHOTS : (item.getId() != 6647 && item.getId() != 20334 ? ShotType.SPIRITSHOTS : ShotType.BLESSED_SPIRITSHOTS)))
								{
									SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED);
									sm.addItemName(item);
									player.sendPacket(sm);
								}

								summon.rechargeShots(isSoulshot, isSpiritshot, false);
							}
						}
						else
						{
							player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_SERVITOR_AND_THEREFORE_CANNOT_USE_THE_AUTOMATIC_USE_FUNCTION);
						}
					}
					else if (isPlayerShot(item.getTemplate()))
					{
						boolean isSoulshotx = item.getEtcItem().getDefaultAction() == ActionType.SOULSHOT;
						boolean isSpiritshotx = item.getEtcItem().getDefaultAction() == ActionType.SPIRITSHOT;
						boolean isFishingshot = item.getEtcItem().getDefaultAction() == ActionType.FISHINGSHOT;
						if (player.getActiveWeaponItem() == player.getFistsWeaponItem())
						{
							player.sendPacket(isSoulshotx ? SystemMessageId.THE_SOULSHOT_YOU_ARE_ATTEMPTING_TO_USE_DOES_NOT_MATCH_THE_GRADE_OF_YOUR_EQUIPPED_WEAPON : SystemMessageId.YOUR_SPIRITSHOT_DOES_NOT_MATCH_THE_WEAPON_S_GRADE);
							return;
						}

						player.addAutoSoulShot(this._itemId);
						player.sendPacket(new ExAutoSoulShot(this._itemId, this._enable, this._type));
						SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED);
						sm.addItemName(item);
						player.sendPacket(sm);
						player.rechargeShots(isSoulshotx, isSpiritshotx, isFishingshot);
					}
				}
				else
				{
					player.removeAutoSoulShot(this._itemId);
					player.sendPacket(new ExAutoSoulShot(this._itemId, this._enable, this._type));
					SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED);
					sm.addItemName(item);
					player.sendPacket(sm);
				}
			}
		}
	}

	public static boolean isPlayerShot(ItemTemplate item)
	{
		switch (item.getDefaultAction())
		{
			case SPIRITSHOT:
			case SOULSHOT:
			case FISHINGSHOT:
				return true;
			default:
				return false;
		}
	}

	public static boolean isSummonShot(ItemTemplate item)
	{
		switch (item.getDefaultAction())
		{
			case SUMMON_SPIRITSHOT:
			case SUMMON_SOULSHOT:
				return true;
			default:
				return false;
		}
	}
}
