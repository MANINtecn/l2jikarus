package net.sf.l2jdev.gameserver.network.clientpackets.skillenchantguarantee;

import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.skillenchantguarantee.ExRequestSkillEnchantConfirm;

public class RequestSkillEnchantConfirm extends ClientPacket
{
	private int _skillId;
	private int _itemId;
	private int _commisionId;
	private byte _result = 1;
	private int _echantSkillSubLevel = 0;
	private long _LCoinFee = 132300L;
	private long feeAdena = 3000000L;

	@Override
	protected void readImpl()
	{
		this._skillId = this.readInt();
		this._itemId = this.readInt();
		this._commisionId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			switch (this._itemId)
			{
				case 100678:
					this._LCoinFee = 15120L;
					this._echantSkillSubLevel = 1001;
					this.feeAdena = 1000000L;
					break;
				case 100679:
					this._LCoinFee = 41580L;
					this._echantSkillSubLevel = 1002;
					this.feeAdena = 2000000L;
					break;
				case 100680:
					this._LCoinFee = 132300L;
					this._echantSkillSubLevel = 1003;
					this.feeAdena = 3000000L;
			}

			Skill playerSkill = player.getKnownSkill(this._skillId);
			Item lCoin = player.getInventory().getItemByItemId(this._commisionId);
			Item guaranteeEnchantCoupon = player.getInventory().getItemByItemId(this._itemId);
			if (guaranteeEnchantCoupon == null)
			{
				PacketLogger.warning(this.getClass().getSimpleName() + ": " + player + " try guarantee enchanted skill without guarantee coupon.");
			}
			else if (playerSkill == null)
			{
				player.sendPacket(SystemMessageId.THE_ENCHANTMENT_CANNOT_BE_EXTRACTED);
				PacketLogger.warning(this.getClass().getSimpleName() + ": " + player + " try guarantee enchant skill that does not have. Skill:" + this._skillId);
			}
			else if (lCoin == null)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_L2_COINS);
				PacketLogger.warning(this.getClass().getSimpleName() + ": " + player + " try guarantee enchanted skill without L-Coins.");
			}
			else if (lCoin.getCount() < this._LCoinFee)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_L2_COINS);
				PacketLogger.warning(this.getClass().getSimpleName() + ": " + player + " try guarantee enchanted skill without the proper amount of L-Coins.");
			}
			else if (player.getAdena() < this.feeAdena)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
			}
			else
			{
				Skill enchantSkill = SkillData.getInstance().getSkill(this._skillId, playerSkill.getLevel(), this._echantSkillSubLevel);
				player.reduceAdena(ItemProcessType.FEE, this.feeAdena, null, true);
				if (player.destroyItem(ItemProcessType.FEE, lCoin, this._LCoinFee, null, true) && player.destroyItem(ItemProcessType.FEE, guaranteeEnchantCoupon, 1L, null, true))
				{
					player.removeSkill(playerSkill.getId());
					player.addSkill(enchantSkill, true);
					player.sendSkillList();
					player.updateShortcuts(this._skillId, playerSkill.getLevel(), 0);
					player.storeMe();
					this._result = 0;
				}

				player.sendPacket(new ExRequestSkillEnchantConfirm(this._result, this._skillId, playerSkill.getLevel(), this._echantSkillSubLevel));
			}
		}
	}
}
