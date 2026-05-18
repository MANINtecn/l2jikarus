package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.sql.CrestTable;
import net.sf.l2jdev.gameserver.model.Crest;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.model.clan.enums.CrestType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class RequestExSetPledgeCrestLarge extends ClientPacket
{
	private int _length;
	private byte[] _data = null;

	@Override
	protected void readImpl()
	{
		this._length = this.readInt();
		if (this._length <= 2176)
		{
			this._data = this.readBytes(this._length);
		}
	}

	@Override
	protected void runImpl()
	{
		if (this._data != null)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				Clan clan = player.getClan();
				if (clan != null)
				{
					if (this._length >= 0 && this._length <= 2176)
					{
						if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
						{
							player.sendPacket(SystemMessageId.AS_YOU_ARE_CURRENTLY_SCHEDULE_FOR_CLAN_DISSOLUTION_YOU_CANNOT_REGISTER_OR_DELETE_A_CLAN_CREST);
						}
						else if (!player.hasAccess(ClanAccess.CHANGE_CREST))
						{
							player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
						}
						else
						{
							if (this._length == 0)
							{
								if (clan.getCrestLargeId() != 0)
								{
									clan.changeLargeCrest(0);
									player.sendPacket(SystemMessageId.THE_CLAN_MARK_HAS_BEEN_DELETED);
								}
							}
							else
							{
								if (clan.getLevel() < 3)
								{
									player.sendPacket(SystemMessageId.A_CLAN_CREST_CAN_ONLY_BE_REGISTERED_WHEN_THE_CLAN_S_SKILL_LEVEL_IS_3_OR_ABOVE);
									return;
								}

								Crest crest = CrestTable.getInstance().createCrest(this._data, CrestType.PLEDGE_LARGE);
								if (crest != null)
								{
									clan.changeLargeCrest(crest.getId());
									player.sendPacket(SystemMessageId.THE_CLAN_MARK_WAS_SUCCESSFULLY_REGISTERED_THE_SYMBOL_WILL_APPEAR_ON_THE_CLAN_FLAG_AND_THE_INSIGNIA_IS_ONLY_DISPLAYED_ON_ITEMS_PERTAINING_TO_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_CASTLE);
								}
							}
						}
					}
					else
					{
						player.sendPacket(SystemMessageId.THE_SIZE_OF_THE_UPLOADED_SYMBOL_DOES_NOT_MEET_THE_STANDARD_REQUIREMENTS);
					}
				}
			}
		}
	}
}
