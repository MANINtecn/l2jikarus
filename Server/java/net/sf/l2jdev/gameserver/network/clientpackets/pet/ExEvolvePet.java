package net.sf.l2jdev.gameserver.network.clientpackets.pet;

import java.util.List;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.enums.EvolveLevel;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.data.xml.PetDataTable;
import net.sf.l2jdev.gameserver.data.xml.PetTypeData;
import net.sf.l2jdev.gameserver.model.PetData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExEvolvePet extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = this.getPlayer();
		if (activeChar != null)
		{
			Pet pet = activeChar.getPet();
			if (pet != null)
			{
				if (!activeChar.isMounted() && !pet.isDead() && !activeChar.isDead() && !pet.isHungry() && !activeChar.isControlBlocked() && !activeChar.isInDuel() && !activeChar.isSitting() && !activeChar.isFishing() && !activeChar.isInCombat() && !pet.isInCombat())
				{
					boolean isAbleToEvolveLevel1 = pet.getLevel() >= 40 && pet.getEvolveLevel() == EvolveLevel.None.ordinal();
					boolean isAbleToEvolveLevel2 = pet.getLevel() >= 76 && pet.getEvolveLevel() == EvolveLevel.First.ordinal();
					if (!isAbleToEvolveLevel1 || !activeChar.destroyItemByItemId(ItemProcessType.FEE, 94096, 1L, null, true) && !activeChar.destroyItemByItemId(ItemProcessType.FEE, 103391, 1L, null, true))
					{
						if (isAbleToEvolveLevel2 && (activeChar.destroyItemByItemId(ItemProcessType.FEE, 94117, 1L, null, true) || activeChar.destroyItemByItemId(ItemProcessType.FEE, 103392, 1L, null, true)))
						{
							this.doEvolve(activeChar, pet, EvolveLevel.Second);
						}
					}
					else
					{
						this.doEvolve(activeChar, pet, EvolveLevel.First);
					}
				}
				else
				{
					activeChar.sendMessage("You can't evolve in this time.");
				}
			}
		}
	}

	protected void doEvolve(Player activeChar, Pet pet, EvolveLevel evolveLevel)
	{
		Item controlItem = pet.getControlItem();
		pet.unSummon(activeChar);
		List<PetData> pets = PetDataTable.getInstance().getPetDatasByEvolve(controlItem.getId(), evolveLevel);
		PetData targetPet = pets.get(Rnd.get(pets.size()));
		PetData petData = PetDataTable.getInstance().getPetData(targetPet.getNpcId());
		if (petData != null && petData.getNpcId() != -1)
		{
			NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(evolveLevel == EvolveLevel.Second ? pet.getId() + 2 : petData.getNpcId());
			Pet evolved = Pet.spawnPet(npcTemplate, activeChar, controlItem);
			if (evolved != null)
			{
				if (evolveLevel == EvolveLevel.First)
				{
					Entry<Integer, SkillHolder> skillType = PetTypeData.getInstance().getRandomSkill();
					String name = PetTypeData.getInstance().getNamePrefix(skillType.getKey()) + " " + PetTypeData.getInstance().getRandomName();
					evolved.addSkill(skillType.getValue().getSkill());
					evolved.setName(name);
					PetDataTable.getInstance().setPetName(controlItem.getObjectId(), name);
				}

				activeChar.setPet(evolved);
				evolved.setShowSummonAnimation(true);
				evolved.setEvolveLevel(evolveLevel);
				evolved.setRunning();
				evolved.storeEvolvedPets(evolveLevel.ordinal(), evolved.getPetData().getIndex(), controlItem.getObjectId());
				controlItem.setEnchantLevel(evolved.getLevel());
				evolved.spawnMe(pet.getX(), pet.getY(), pet.getZ());
				evolved.startFeed();
			}
		}
	}
}
