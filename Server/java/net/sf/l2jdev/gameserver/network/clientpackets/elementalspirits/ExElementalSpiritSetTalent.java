package net.sf.l2jdev.gameserver.network.clientpackets.elementalspirits;

import net.sf.l2jdev.gameserver.model.ElementalSpirit;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.UserInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits.ElementalSpiritSetTalent;

public class ExElementalSpiritSetTalent extends ClientPacket
{
	private byte _type;
	private byte _attackPoints;
	private byte _defensePoints;
	private byte _critRate;
	private byte _critDamage;

	@Override
	protected void readImpl()
	{
		this._type = this.readByte();
		this.readByte();
		this.readByte();
		this._attackPoints = this.readByte();
		this.readByte();
		this._defensePoints = this.readByte();
		this.readByte();
		this._critRate = this.readByte();
		this.readByte();
		this._critDamage = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			ElementalSpirit spirit = player.getElementalSpirit(ElementalSpiritType.of(this._type));
			boolean result = false;
			if (spirit != null)
			{
				if (this._attackPoints > 0 && spirit.getAvailableCharacteristicsPoints() >= this._attackPoints)
				{
					spirit.addAttackPoints(this._attackPoints);
					result = true;
				}

				if (this._defensePoints > 0 && spirit.getAvailableCharacteristicsPoints() >= this._defensePoints)
				{
					spirit.addDefensePoints(this._defensePoints);
					result = true;
				}

				if (this._critRate > 0 && spirit.getAvailableCharacteristicsPoints() >= this._critRate)
				{
					spirit.addCritRatePoints(this._critRate);
					result = true;
				}

				if (this._critDamage > 0 && spirit.getAvailableCharacteristicsPoints() >= this._critDamage)
				{
					spirit.addCritDamage(this._critDamage);
					result = true;
				}
			}

			if (result)
			{
				UserInfo userInfo = new UserInfo(player);
				userInfo.addComponentType(UserInfoType.ATT_SPIRITS);
				player.sendPacket(userInfo);
				player.sendPacket(new SystemMessage(SystemMessageId.CHARACTERISTICS_WERE_APPLIED_SUCCESSFULLY));
			}

			player.sendPacket(new ElementalSpiritSetTalent(player, this._type, result));
		}
	}
}
