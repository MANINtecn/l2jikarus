package net.sf.l2jdev.gameserver.model.actor.stat;

import net.sf.l2jdev.gameserver.data.xml.ExperienceData;
import net.sf.l2jdev.gameserver.data.xml.PetDataTable;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SocialAction;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class PetStat extends SummonStat
{
	public PetStat(Pet activeChar)
	{
		super(activeChar);
	}

	public boolean addExp(int value)
	{
		Pet pet = this.getActiveChar();
		if (!pet.isUncontrollable() && super.addExp(Math.round(value * (1.0 + this.getValue(Stat.BONUS_EXP_PET, 0.0) / 100.0))))
		{
			pet.updateAndBroadcastStatus(1);
			return true;
		}
		return false;
	}

	public boolean addExpAndSp(double addToExp)
	{
		long finalExp = Math.round(addToExp * (1.0 + this.getValue(Stat.BONUS_EXP_PET, 0.0) / 100.0));
		Pet pet = this.getActiveChar();
		if (!pet.isUncontrollable() && this.addExp(finalExp))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_GUARDIAN_HAS_ACQUIRED_S1_XP);
			sm.addLong(finalExp);
			pet.updateAndBroadcastStatus(1);
			pet.sendPacket(sm);
			return true;
		}
		return false;
	}

	@Override
	public boolean addLevel(int value)
	{
		if (this.getLevel() + value > this.getMaxLevel() - 1)
		{
			return false;
		}
		boolean levelIncreased = super.addLevel(value);
		Pet pet = this.getActiveChar();
		pet.broadcastStatusUpdate();
		if (levelIncreased)
		{
			pet.broadcastPacket(new SocialAction(pet.getObjectId(), 2122));
		}

		pet.updateAndBroadcastStatus(1);
		if (pet.getControlItem() != null)
		{
			pet.getControlItem().setEnchantLevel(this.getLevel());
		}

		return levelIncreased;
	}

	@Override
	public long getExpForLevel(int level)
	{
		Pet pet = this.getActiveChar();

		try
		{
			return PetDataTable.getInstance().getPetLevelData(pet.getId(), level).getPetMaxExp();
		}
		catch (NullPointerException var4)
		{
			if (pet != null)
			{
				LOGGER.warning("Pet objectId:" + pet.getObjectId() + ", NpcId:" + pet.getId() + ", level:" + level + " is missing data from pets_stats table!");
			}

			throw var4;
		}
	}

	@Override
	public Pet getActiveChar()
	{
		return super.getActiveChar().asPet();
	}

	public int getFeedBattle()
	{
		return this.getActiveChar().getPetLevelData().getPetFeedBattle();
	}

	public int getFeedNormal()
	{
		return this.getActiveChar().getPetLevelData().getPetFeedNormal();
	}

	@Override
	public void setLevel(int value)
	{
		Pet pet = this.getActiveChar();
		pet.setPetData(PetDataTable.getInstance().getPetLevelData(pet.getTemplate().getId(), value));
		if (pet.getPetLevelData() == null)
		{
			throw new IllegalArgumentException("No pet data for npc: " + pet.getTemplate().getId() + " level: " + value);
		}
		pet.stopFeed();
		super.setLevel(value);
		pet.startFeed();
		Item item = pet.getControlItem();
		if (item != null)
		{
			item.setEnchantLevel(this.getLevel());
			pet.getOwner().sendItemList();
		}
	}

	public int getMaxFeed()
	{
		return this.getActiveChar().getPetLevelData().getPetMaxFeed();
	}

	@Override
	public int getPAtkSpd()
	{
		int val = super.getPAtkSpd();
		if (this.getActiveChar().isHungry())
		{
			val /= 2;
		}

		return val;
	}

	@Override
	public int getMAtkSpd()
	{
		int val = super.getMAtkSpd();
		if (this.getActiveChar().isHungry())
		{
			val /= 2;
		}

		return val;
	}

	@Override
	public int getMaxLevel()
	{
		return ExperienceData.getInstance().getMaxPetLevel();
	}
}
