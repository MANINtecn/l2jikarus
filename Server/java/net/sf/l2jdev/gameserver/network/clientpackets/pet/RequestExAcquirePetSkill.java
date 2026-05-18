package net.sf.l2jdev.gameserver.network.clientpackets.pet;

import java.util.List;
import java.util.Optional;

import net.sf.l2jdev.gameserver.data.xml.PetAcquireList;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.holders.PetSkillAcquireHolder;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.ExAcquirePetSkillResult;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.ExPetSkillList;

public class RequestExAcquirePetSkill extends ClientPacket
{
	private int skillId;
	private int skillLevel;

	@Override
	protected void readImpl()
	{
		this.skillId = this.readInt();
		this.skillLevel = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Pet pet = player.getPet();
			if (pet != null)
			{
				Skill skill = SkillData.getInstance().getSkill(this.skillId, this.skillLevel);
				if (skill != null)
				{
					Optional<PetSkillAcquireHolder> reqSkill = PetAcquireList.getInstance().getSkills(pet.getPetData().getType()).stream().filter(it -> it.getSkillId() == this.skillId && it.getSkillLevel() == this.skillLevel).findFirst();
					if (reqSkill.isPresent())
					{
						List<ItemHolder> requiredItems = reqSkill.get().getItems();
						if (requiredItems.isEmpty())
						{
							pet.addSkill(skill);
							pet.storePetSkills(this.skillId, this.skillLevel);
							player.sendPacket(new ExPetSkillList(false, pet));
							player.sendPacket(new ExAcquirePetSkillResult(this.skillId, this.skillLevel, true));
							return;
						}

						for (ItemHolder item : requiredItems)
						{
							if (player.destroyItemByItemId(ItemProcessType.FEE, item.getId(), item.getCount(), null, true))
							{
								pet.addSkill(skill);
								pet.storePetSkills(this.skillId, this.skillLevel);
								player.sendPacket(new ExPetSkillList(false, pet));
								player.sendPacket(new ExAcquirePetSkillResult(this.skillId, this.skillLevel, true));
								break;
							}
						}
					}
				}
			}
		}
	}
}
