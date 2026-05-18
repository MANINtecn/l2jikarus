package net.sf.l2jdev.gameserver.network.clientpackets.pledgedonation;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.managers.MailManager;
import net.sf.l2jdev.gameserver.model.Message;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.itemcontainer.Mail;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.enums.MailType;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgedonation.ExPledgeDonationInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgedonation.ExPledgeDonationRequest;

public class RequestExPledgeDonationRequest extends ClientPacket
{
	private int _type;

	@Override
	protected void readImpl()
	{
		this._type = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Clan clan = player.getClan();
			if (clan != null)
			{
				switch (this._type)
				{
					case 0:
						if (player.reduceAdena(ItemProcessType.FEE, 100000L, null, true))
						{
							clan.addExp(player.getObjectId(), 50);
						}
						else
						{
							player.sendPacket(new ExPledgeDonationRequest(false, this._type, 2));
						}
						break;
					case 1:
						if (player.getInventory().getInventoryItemCount(91663, -1) >= 100L)
						{
							player.destroyItemByItemId(ItemProcessType.FEE, 91663, 100L, player, true);
							clan.addExp(player.getObjectId(), 100);
							player.setHonorCoins(player.getHonorCoins() + 100L);
						}
						else
						{
							player.sendPacket(new ExPledgeDonationRequest(false, this._type, 2));
						}
						break;
					case 2:
						if (player.getInventory().getInventoryItemCount(91663, -1) >= 500L)
						{
							player.destroyItemByItemId(ItemProcessType.FEE, 91663, 500L, player, true);
							clan.addExp(player.getObjectId(), 500);
							player.setHonorCoins(player.getHonorCoins() + 500L);
						}
						else
						{
							player.sendPacket(new ExPledgeDonationRequest(false, this._type, 2));
						}
				}

				player.getVariables().set("CLAN_DONATION_POINTS", Math.max(player.getClanDonationPoints() - 1, 0));
				this.criticalSuccess(player, clan, this._type);
				player.sendItemList();
				player.sendPacket(new ExPledgeDonationRequest(true, this._type, player.getClanDonationPoints()));
				player.sendPacket(new ExPledgeDonationInfo(player.getClanDonationPoints(), true));
			}
		}
	}

	private void criticalSuccess(Player player, Clan clan, int type)
	{
		if (type == 1)
		{
			if (Rnd.get(100) < 5)
			{
				player.setHonorCoins(player.getHonorCoins() + 200L);
				clan.getMembers().forEach(clanMember -> this.sendMail(clanMember.getObjectId(), 1, player.getName()));
			}
		}
		else if (type == 2 && Rnd.get(100) < 5)
		{
			player.setHonorCoins(player.getHonorCoins() + 1000L);
			clan.getMembers().forEach(clanMember -> this.sendMail(clanMember.getObjectId(), 5, player.getName()));
		}
	}

	protected void sendMail(int charId, int amount, String donator)
	{
		Message msg = new Message(charId, "Clan Rewards for " + donator + " Donation", "The entire clan receives rewards for " + donator + " donation.", MailType.PLEDGE_DONATION_CRITICAL_SUCCESS);
		Mail attachment = msg.createAttachments();
		attachment.addItem(ItemProcessType.REWARD, 95672, amount, null, donator);
		MailManager.getInstance().sendMessage(msg);
	}
}
