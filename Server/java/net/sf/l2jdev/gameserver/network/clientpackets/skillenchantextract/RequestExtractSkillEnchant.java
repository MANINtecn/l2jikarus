package net.sf.l2jdev.gameserver.network.clientpackets.skillenchantextract;

import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.skillenchantextract.ExExtractSkillEnchant;

public class RequestExtractSkillEnchant extends ClientPacket
{
	private byte _result = 1;
	private int _skillId;
	private int _skillLevel;
	private int _skillSubLevel;
	private int _itemId;
	private long _LCoinFee = 308600L;
	private int _rewardId = 57;

	@Override
	protected void readImpl()
	{
		this._skillId = this.readInt();
		this._skillLevel = this.readInt();
		this._skillSubLevel = this.readInt();
		this._itemId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			switch (this._skillSubLevel)
			{
				case 1001:
					this._LCoinFee = 35280L;
					this._rewardId = 100678;
					break;
				case 1002:
					this._LCoinFee = 97020L;
					this._rewardId = 100679;
					break;
				case 1003:
					this._LCoinFee = 308600L;
					this._rewardId = 100680;
			}

			long feeAdena = 3000000L;
			Skill enchantedSkill = SkillData.getInstance().getSkill(this._skillId, this._skillLevel, this._skillSubLevel);
			Skill playerSkill = player.getKnownSkill(this._skillId);
			Skill normalSkill = SkillData.getInstance().getSkill(this._skillId, this._skillLevel, 0);
			Item lCoin = player.getInventory().getItemByItemId(this._itemId);
			ItemTemplate reward = ItemData.getInstance().getTemplate(this._rewardId);
			if (reward == null)
			{
				player.sendPacket(SystemMessageId.THE_ENCHANTMENT_CANNOT_BE_EXTRACTED);
				PacketLogger.warning(this.getClass().getSimpleName() + ": " + player + " trying get a reward from extract enchanted skill that does not exist.");
			}
			else if (enchantedSkill == null)
			{
				player.sendPacket(SystemMessageId.THE_ENCHANTMENT_CANNOT_BE_EXTRACTED);
				PacketLogger.warning(this.getClass().getSimpleName() + ": " + player + " trying extract enchanted skill that does not exist. Skill:" + this._skillId);
			}
			else if (playerSkill == null)
			{
				player.sendPacket(SystemMessageId.THE_ENCHANTMENT_CANNOT_BE_EXTRACTED);
				PacketLogger.warning(this.getClass().getSimpleName() + ": " + player + " trying extract enchanted skill that does not have. Skill:" + this._skillId);
			}
			else if (playerSkill.getSubLevel() != this._skillSubLevel)
			{
				player.sendPacket(SystemMessageId.THE_ENCHANTMENT_CANNOT_BE_EXTRACTED);
				PacketLogger.warning(this.getClass().getSimpleName() + ": " + player + " trying extract enchanted skill that is different that the one in the list. Skill:" + enchantedSkill.getId());
			}
			else if (lCoin == null)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_L2_COINS);
				PacketLogger.warning(this.getClass().getSimpleName() + ": " + player + " trying extract enchanted skill without L-Coins (Client should have disabled the button).");
			}
			else if (lCoin.getCount() < this._LCoinFee)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_L2_COINS);
				PacketLogger.warning(this.getClass().getSimpleName() + ": " + player + " trying extract enchanted skill without the proper amount of L-Coins (Client should have disabled the button).");
			}
			else if (player.getAdena() < feeAdena)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
			}
			else if (!player.getInventory().validateCapacity(1L))
			{
				SystemMessage sme = new SystemMessage(SystemMessageId.UNABLE_TO_EXTRACT_BECAUSE_INVENTORY_IS_FULL);
				player.sendPacket(sme);
			}
			else if (player.isInCombat())
			{
				SystemMessage sma = new SystemMessage(SystemMessageId.UNABLE_TO_EXTRACT_WHILE_IN_COMBAT_MODE);
				player.sendPacket(sma);
			}
			else
			{
				player.reduceAdena(ItemProcessType.FEE, feeAdena, null, true);
				if (player.destroyItem(ItemProcessType.FEE, lCoin, this._LCoinFee, null, true))
				{
					player.removeSkill(enchantedSkill);
					player.addSkill(normalSkill, true);
					player.sendSkillList();
					player.updateShortcuts(this._skillId, this._skillLevel, 0);
					player.storeMe();
					player.addItem(ItemProcessType.REWARD, this._rewardId, 1L, 0, player, true);
					SystemMessage smi = new SystemMessage(SystemMessageId.EXTRACTED_S1_S2_SUCCESSFULLY);
					smi.addItemName(reward.getId());
					smi.addString("x1");
					player.sendPacket(smi);
					this._result = 0;
					this._skillSubLevel = playerSkill.getSubLevel();
				}

				player.sendPacket(new ExExtractSkillEnchant(this._result, this._skillId, this._skillLevel, this._skillSubLevel));
			}
		}
	}
}
