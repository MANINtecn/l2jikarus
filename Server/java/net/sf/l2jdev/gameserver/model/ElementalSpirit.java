package net.sf.l2jdev.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.data.xml.ElementalSpiritData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnElementalSpiritUpgrade;
import net.sf.l2jdev.gameserver.model.item.holders.ElementalSpiritAbsorbItemHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ElementalSpiritDataHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ElementalSpiritTemplateHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.UserInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits.ElementalSpiritInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits.ExElementalSpiritAttackType;
import net.sf.l2jdev.gameserver.network.serverpackets.elementalspirits.ExElementalSpiritGetExp;

public class ElementalSpirit
{
	public static final String STORE_ELEMENTAL_SPIRIT_QUERY = "REPLACE INTO character_spirits (charId, type, level, stage, experience, attack_points, defense_points, crit_rate_points, crit_damage_points, in_use) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private final Player _owner;
	private ElementalSpiritTemplateHolder _template;
	private final ElementalSpiritDataHolder _data;

	public ElementalSpirit(ElementalSpiritType type, Player owner)
	{
		this._data = new ElementalSpiritDataHolder(type.getId(), owner.getObjectId());
		this._template = ElementalSpiritData.getInstance().getSpirit(type.getId(), this._data.getStage());
		this._owner = owner;
	}

	public ElementalSpirit(ElementalSpiritDataHolder data, Player owner)
	{
		this._owner = owner;
		this._data = data;
		this._template = ElementalSpiritData.getInstance().getSpirit(data.getType(), data.getStage());
	}

	public void addExperience(int experience)
	{
		if (this._data.getLevel() != this._template.getMaxLevel() || this._data.getExperience() < this._template.getMaxExperienceAtLevel(this._template.getMaxLevel()))
		{
			this._data.addExperience(experience);
			this._owner.sendPacket(new ExElementalSpiritGetExp(this._data.getType(), this._data.getExperience()));
			this._owner.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_S2_ATTRIBUTE_XP).addInt(experience).addElementalSpiritName(this._data.getType()));
			if (this._data.getExperience() > this.getExperienceToNextLevel())
			{
				this.levelUp();
				this._owner.sendPacket(new SystemMessage(SystemMessageId.S1_ATTRIBUTE_SPIRIT_HAS_REACHED_LV_S2).addElementalSpiritName(this._data.getType()).addByte(this._data.getLevel()));
				this._owner.sendPacket(new ElementalSpiritInfo(this._owner, (byte) 0));
				this._owner.sendPacket(new ExElementalSpiritAttackType(this._owner));
				UserInfo userInfo = new UserInfo(this._owner);
				userInfo.addComponentType(UserInfoType.ATT_SPIRITS);
				this._owner.sendPacket(userInfo);
			}
		}
	}

	private void levelUp()
	{
		do
		{
			if (this._data.getLevel() < this._template.getMaxLevel())
			{
				this._data.increaseLevel();
			}
			else
			{
				this._data.setExperience(this.getExperienceToNextLevel());
			}
		}
		while (this._data.getExperience() > this.getExperienceToNextLevel());
	}

	public void reduceLevel()
	{
		this._data.setLevel(Math.max(1, this._data.getLevel() - 1));
		this._data.setExperience(ElementalSpiritData.getInstance().getSpirit(this._data.getType(), this._data.getStage()).getMaxExperienceAtLevel(this._data.getLevel() - 1));
		this.resetCharacteristics();
	}

	public int getAvailableCharacteristicsPoints()
	{
		int stage = this._data.getStage();
		int level = this._data.getLevel();
		int points = (stage > 3 ? (stage - 2) * 20 : (stage - 1) * 10) + (stage > 2 ? level * 2 : level * 1);
		return Math.max(points - this._data.getAttackPoints() - this._data.getDefensePoints() - this._data.getCritDamagePoints() - this._data.getCritRatePoints(), 0);
	}

	public ElementalSpiritAbsorbItemHolder getAbsorbItem(int itemId)
	{
		for (ElementalSpiritAbsorbItemHolder absorbItem : this.getAbsorbItems())
		{
			if (absorbItem.getId() == itemId)
			{
				return absorbItem;
			}
		}

		return null;
	}

	public int getExtractAmount()
	{
		int amount = Math.round(this._data.getExperience() / 50000.0F);
		if (this.getLevel() > 1)
		{
			amount = (int) (amount + ElementalSpiritData.getInstance().getSpirit(this._data.getType(), this._data.getStage()).getMaxExperienceAtLevel(this.getLevel() - 1) / 50000.0F);
		}

		return amount;
	}

	public void resetStage()
	{
		this._data.setLevel(1);
		this._data.setExperience(0L);
		this.resetCharacteristics();
	}

	public boolean canEvolve()
	{
		return this._data.getStage() < 5 && this._data.getLevel() == 10 && this._data.getExperience() == this.getExperienceToNextLevel();
	}

	public void upgrade()
	{
		this._data.increaseStage();
		this._data.setLevel(1);
		this._data.setExperience(0L);
		this._template = ElementalSpiritData.getInstance().getSpirit(this._data.getType(), this._data.getStage());
		if (EventDispatcher.getInstance().hasListener(EventType.ON_ELEMENTAL_SPIRIT_UPGRADE, this._owner))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnElementalSpiritUpgrade(this._owner, this), this._owner);
		}
	}

	public void resetCharacteristics()
	{
		this._data.setAttackPoints((byte) 0);
		this._data.setDefensePoints((byte) 0);
		this._data.setCritRatePoints((byte) 0);
		this._data.setCritDamagePoints((byte) 0);
	}

	public byte getType()
	{
		return this._template.getType();
	}

	public byte getStage()
	{
		return this._template.getStage();
	}

	public int getNpcId()
	{
		return this._template.getNpcId();
	}

	public long getExperience()
	{
		return this._data.getExperience();
	}

	public long getExperienceToNextLevel()
	{
		return this._template.getMaxExperienceAtLevel(this._data.getLevel());
	}

	public int getLevel()
	{
		return this._data.getLevel();
	}

	public int getMaxLevel()
	{
		return this._template.getMaxLevel();
	}

	public int getAttack()
	{
		return this._template.getAttackAtLevel(this._data.getLevel()) + this._data.getAttackPoints() * 5;
	}

	public int getDefense()
	{
		return this._template.getDefenseAtLevel(this._data.getLevel()) + this._data.getDefensePoints() * 5;
	}

	public int getMaxCharacteristics()
	{
		return this._template.getMaxCharacteristics();
	}

	public int getAttackPoints()
	{
		return this._data.getAttackPoints();
	}

	public int getDefensePoints()
	{
		return this._data.getDefensePoints();
	}

	public int getCriticalRatePoints()
	{
		return this._data.getCritRatePoints();
	}

	public int getCriticalDamagePoints()
	{
		return this._data.getCritDamagePoints();
	}

	public List<ItemHolder> getItemsToEvolve()
	{
		return this._template.getItemsToEvolve();
	}

	public List<ElementalSpiritAbsorbItemHolder> getAbsorbItems()
	{
		return this._template.getAbsorbItems();
	}

	public int getExtractItem()
	{
		return this._template.getExtractItem();
	}

	public void save()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE INTO character_spirits (charId, type, level, stage, experience, attack_points, defense_points, crit_rate_points, crit_damage_points, in_use) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");)
		{
			statement.setInt(1, this._data.getCharId());
			statement.setInt(2, this._data.getType());
			statement.setInt(3, this._data.getLevel());
			statement.setInt(4, this._data.getStage());
			statement.setLong(5, this._data.getExperience());
			statement.setInt(6, this._data.getAttackPoints());
			statement.setInt(7, this._data.getDefensePoints());
			statement.setInt(8, this._data.getCritRatePoints());
			statement.setInt(9, this._data.getCritDamagePoints());
			statement.setInt(10, this._data.isInUse() ? 1 : 0);
			statement.execute();
		}
		catch (Exception var9)
		{
			var9.printStackTrace();
		}
	}

	public void addAttackPoints(byte attackPoints)
	{
		this._data.addAttackPoints(attackPoints);
	}

	public void addDefensePoints(byte defensePoints)
	{
		this._data.addDefensePoints(defensePoints);
	}

	public void addCritRatePoints(byte critRatePoints)
	{
		this._data.addCritRatePoints(critRatePoints);
	}

	public void addCritDamage(byte critDamagePoints)
	{
		this._data.addCritDamagePoints(critDamagePoints);
	}

	public int getCriticalRate()
	{
		return this._template.getCriticalRateAtLevel(this._data.getLevel()) + this.getCriticalRatePoints();
	}

	public int getCriticalDamage()
	{
		return this._template.getCriticalDamageAtLevel(this._data.getLevel()) + this.getCriticalDamagePoints();
	}

	public void setInUse(boolean value)
	{
		this._data.setInUse(value);
	}

	public boolean isInUse()
	{
		return this._data.isInUse();
	}
}
