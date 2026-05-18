package net.sf.l2jdev.gameserver.model.item.appearance;

import java.time.Duration;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.ArmorType;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class AppearanceStone
{
	private final int _id;
	private final int _cost;
	private final int _visualId;
	private final long _lifeTime;
	private final AppearanceType _type;
	private final WeaponType _weaponType;
	private final ArmorType _armorType;
	private final AppearanceHandType _handType;
	private final AppearanceMagicType _magicType;
	private Set<CrystalType> _crystalTypes;
	private Set<AppearanceTargetType> _targetTypes;
	private Set<BodyPart> _bodyParts;
	private Set<Race> _races;
	private Set<Race> _racesNot;
	private Set<AppearanceHolder> _allVisualIds;

	public AppearanceStone(StatSet set)
	{
		this._id = set.getInt("id");
		this._visualId = set.getInt("visualId", 0);
		this._cost = set.getInt("cost", 0);
		this._lifeTime = set.getDuration("lifeTime", Duration.ofSeconds(0L)).toMillis();
		this._type = set.getEnum("type", AppearanceType.class, AppearanceType.NONE);
		this._weaponType = set.getEnum("weaponType", WeaponType.class, WeaponType.NONE);
		this._armorType = set.getEnum("armorType", ArmorType.class, ArmorType.NONE);
		this._handType = set.getEnum("handType", AppearanceHandType.class, AppearanceHandType.NONE);
		this._magicType = set.getEnum("magicType", AppearanceMagicType.class, AppearanceMagicType.NONE);
		AppearanceTargetType targetType = set.getEnum("targetType", AppearanceTargetType.class, AppearanceTargetType.NONE);
		if (targetType != AppearanceTargetType.NONE)
		{
			this.addTargetType(targetType);
		}

		CrystalType crystalType = set.getEnum("grade", CrystalType.class, null);
		if (crystalType == null)
		{
			switch (targetType)
			{
				case ACCESSORY:
				case ALL:
					this.addCrystalType(CrystalType.NONE);
				case WEAPON:
				case ARMOR:
					for (CrystalType cryType : CrystalType.values())
					{
						if (cryType != CrystalType.NONE && cryType != CrystalType.EVENT)
						{
							this.addCrystalType(cryType);
						}
					}
			}
		}
		else
		{
			this.addCrystalType(crystalType);
		}

		BodyPart bodyPart = BodyPart.fromName(set.getString("bodyPart", "none"));
		if (bodyPart != BodyPart.NONE)
		{
			this.addBodyPart(bodyPart);
		}

		Race race = set.getEnum("race", Race.class, Race.NONE);
		if (race != Race.NONE)
		{
			this.addRace(race);
		}

		Race raceNot = set.getEnum("raceNot", Race.class, Race.NONE);
		if (raceNot != Race.NONE)
		{
			this.addRaceNot(raceNot);
		}
	}

	public int getId()
	{
		return this._id;
	}

	public int getVisualId()
	{
		return this._visualId;
	}

	public int getCost()
	{
		return this._cost;
	}

	public long getLifeTime()
	{
		return this._lifeTime;
	}

	public AppearanceType getType()
	{
		return this._type;
	}

	public WeaponType getWeaponType()
	{
		return this._weaponType;
	}

	public ArmorType getArmorType()
	{
		return this._armorType;
	}

	public AppearanceHandType getHandType()
	{
		return this._handType;
	}

	public AppearanceMagicType getMagicType()
	{
		return this._magicType;
	}

	public void addCrystalType(CrystalType type)
	{
		if (this._crystalTypes == null)
		{
			this._crystalTypes = EnumSet.noneOf(CrystalType.class);
		}

		this._crystalTypes.add(type);
	}

	public Set<CrystalType> getCrystalTypes()
	{
		return this._crystalTypes != null ? this._crystalTypes : Collections.emptySet();
	}

	public void addTargetType(AppearanceTargetType type)
	{
		if (this._targetTypes == null)
		{
			this._targetTypes = EnumSet.noneOf(AppearanceTargetType.class);
		}

		this._targetTypes.add(type);
	}

	public Set<AppearanceTargetType> getTargetTypes()
	{
		return this._targetTypes != null ? this._targetTypes : Collections.emptySet();
	}

	public void addBodyPart(BodyPart bodyPart)
	{
		if (this._bodyParts == null)
		{
			this._bodyParts = new HashSet<>();
		}

		this._bodyParts.add(bodyPart);
	}

	public void addVisualId(AppearanceHolder appearanceHolder)
	{
		if (this._allVisualIds == null)
		{
			this._allVisualIds = new HashSet<>();
		}

		this._allVisualIds.add(appearanceHolder);
	}

	public Set<AppearanceHolder> getVisualIds()
	{
		return this._allVisualIds != null ? this._allVisualIds : Collections.emptySet();
	}

	public Set<BodyPart> getBodyParts()
	{
		return this._bodyParts != null ? this._bodyParts : Collections.emptySet();
	}

	public void addRace(Race race)
	{
		if (this._races == null)
		{
			this._races = EnumSet.noneOf(Race.class);
		}

		this._races.add(race);
	}

	public Set<Race> getRaces()
	{
		return this._races != null ? this._races : Collections.emptySet();
	}

	public void addRaceNot(Race race)
	{
		if (this._racesNot == null)
		{
			this._racesNot = EnumSet.noneOf(Race.class);
		}

		this._racesNot.add(race);
	}

	public Set<Race> getRacesNot()
	{
		return this._racesNot != null ? this._racesNot : Collections.emptySet();
	}

	public boolean checkConditions(Player player, Item targetItem)
	{
		if (targetItem == null)
		{
			return false;
		}
		else if (this.getTargetTypes().isEmpty())
		{
			return false;
		}
		else if (!targetItem.isEquipped() || !this.getRacesNot().contains(player.getRace()) && (this.getRaces().isEmpty() || this.getRaces().contains(player.getRace())))
		{
			switch (this._type)
			{
				case RESTORE:
					if (targetItem.getVisualId() == 0)
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_RESTORE_ITEMS_THAT_HAVE_NOT_BEEN_MODIFIED);
						return false;
					}

					if (targetItem.isWeapon() && !this.getTargetTypes().contains(AppearanceTargetType.WEAPON) || targetItem.isArmor() && !this.getTargetTypes().contains(AppearanceTargetType.ARMOR) && targetItem.getTemplate().getBodyPart() != BodyPart.HAIR && targetItem.getTemplate().getBodyPart() != BodyPart.HAIR2 && targetItem.getTemplate().getBodyPart() != BodyPart.HAIRALL || targetItem.isEtcItem() && !this.getTargetTypes().contains(AppearanceTargetType.ACCESSORY))
					{
						player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
						return false;
					}

					if ((targetItem.getTemplate().getBodyPart() == BodyPart.HAIR || targetItem.getTemplate().getBodyPart() == BodyPart.HAIR2 || targetItem.getTemplate().getBodyPart() == BodyPart.HAIRALL) && !this.getTargetTypes().contains(AppearanceTargetType.ACCESSORY))
					{
						player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
						return false;
					}
					break;
				default:
					AppearanceTargetType targetType = this.getTargetTypes().stream().findFirst().get();
					switch (targetType)
					{
						case ACCESSORY:
							if (targetItem.getTemplate().getBodyPart() != BodyPart.HAIR && targetItem.getTemplate().getBodyPart() != BodyPart.HAIR2 && targetItem.getTemplate().getBodyPart() != BodyPart.HAIRALL)
							{
								player.sendPacket(SystemMessageId.HEAD_ACCESSORIES_ONLY);
								return false;
							}
							break;
						case ALL:
							if ((!this.getCrystalTypes().isEmpty() && !this.getCrystalTypes().contains(targetItem.getTemplate().getCrystalType())) || (this.findVisualChange(targetItem) == null))
							{
								player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
								return false;
							}

							return true;
						case WEAPON:
							if (!targetItem.isWeapon())
							{
								player.sendPacket(SystemMessageId.WEAPONS_ONLY);
								return false;
							}

							if (targetItem.getTemplate().getCrystalType() == CrystalType.NONE)
							{
								player.sendPacket(SystemMessageId.YOU_CANNOT_MODIFY_OR_RESTORE_NO_GRADE_ITEMS);
								return false;
							}
							break;
						case ARMOR:
							if (!targetItem.isArmor())
							{
								player.sendPacket(SystemMessageId.ARMOR_ONLY);
								return false;
							}

							if (targetItem.getTemplate().getCrystalType() == CrystalType.NONE)
							{
								player.sendPacket(SystemMessageId.YOU_CANNOT_MODIFY_OR_RESTORE_NO_GRADE_ITEMS);
								return false;
							}
							break;
						case NONE:
							return false;
					}
			}

			if (!this.getCrystalTypes().isEmpty() && !this.getCrystalTypes().contains(targetItem.getTemplate().getCrystalType()))
			{
				player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
				return false;
			}
			else if (targetItem.isArmor() && !this.getBodyParts().isEmpty() && !this.getBodyParts().contains(targetItem.getTemplate().getBodyPart()))
			{
				player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
				return false;
			}
			else
			{
				if (this._weaponType != WeaponType.NONE)
				{
					if (!targetItem.isWeapon() || targetItem.getItemType() != this._weaponType)
					{
						if (this._weaponType != WeaponType.CROSSBOW)
						{
							player.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_EXTRACTED);
							return false;
						}

						if (targetItem.getItemType() != WeaponType.CROSSBOW && targetItem.getItemType() != WeaponType.TWOHANDCROSSBOW)
						{
							player.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_EXTRACTED);
							return false;
						}
					}

					switch (this._handType)
					{
						case ONE_HANDED:
							if (targetItem.getTemplate().getBodyPart() != BodyPart.R_HAND)
							{
								player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
								return false;
							}
							break;
						case TWO_HANDED:
							if (targetItem.getTemplate().getBodyPart() != BodyPart.LR_HAND)
							{
								player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
								return false;
							}
					}

					switch (this._magicType)
					{
						case MAGICAL:
							if (!targetItem.getTemplate().isMagicWeapon())
							{
								player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
								return false;
							}
							break;
						case PHYISICAL:
							if (targetItem.getTemplate().isMagicWeapon())
							{
								player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
								return false;
							}
					}
				}

				if (this._armorType != ArmorType.NONE)
				{
					switch (this._armorType)
					{
						case SHIELD:
							if (!targetItem.isArmor() || targetItem.getItemType() != ArmorType.SHIELD)
							{
								player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
								return false;
							}
							break;
						case SIGIL:
							if (!targetItem.isArmor() || targetItem.getItemType() != ArmorType.SIGIL)
							{
								player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
								return false;
							}
					}
				}

				return true;
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_MODIFY_AN_EQUIPPED_ITEM_INTO_THE_APPEARANCE_OF_AN_UNEQUIPPABLE_ITEM_PLEASE_CHECK_RACE_GENDER_RESTRICTIONS_YOU_CAN_MODIFY_THE_APPEARANCE_IF_YOU_UNEQUIP_THE_ITEM);
			return false;
		}
	}

	public AppearanceHolder findVisualChange(Item targetItem)
	{
		Iterator<AppearanceHolder> var2 = this._allVisualIds.iterator();

		while (true)
		{
			AppearanceHolder holder;
			label69:
			while (true)
			{
				if (!var2.hasNext())
				{
					return null;
				}

				holder = var2.next();
				if (!targetItem.isArmor() || holder.getBodyPart() == BodyPart.NONE || targetItem.getTemplate().getBodyPart() == holder.getBodyPart())
				{
					if (holder.getWeaponType() == WeaponType.NONE)
					{
						break;
					}

					if (targetItem.isWeapon() && targetItem.getItemType() == holder.getWeaponType() || holder.getWeaponType() == WeaponType.CROSSBOW && (targetItem.getItemType() == WeaponType.CROSSBOW || targetItem.getItemType() == WeaponType.TWOHANDCROSSBOW))
					{
						switch (holder.getHandType())
						{
							case ONE_HANDED:
								if (targetItem.getTemplate().getBodyPart() != BodyPart.R_HAND)
								{
									continue;
								}
								break;
							case TWO_HANDED:
								if (targetItem.getTemplate().getBodyPart() != BodyPart.LR_HAND)
								{
									continue;
								}
						}

						switch (holder.getMagicType())
						{
							case MAGICAL:
								if (!targetItem.getTemplate().isMagicWeapon())
								{
									break;
								}
								break label69;
							case PHYISICAL:
								if (targetItem.getTemplate().isMagicWeapon())
								{
									break;
								}
							default:
								break label69;
						}
					}
				}
			}

			if (holder.getArmorType() == ArmorType.NONE)
			{
				return holder;
			}

			switch (holder.getArmorType())
			{
				case SHIELD:
					if (!targetItem.isArmor() || targetItem.getItemType() != ArmorType.SHIELD)
					{
						break;
					}

					return holder;
				case SIGIL:
					if (!targetItem.isArmor() || targetItem.getItemType() != ArmorType.SIGIL)
					{
						break;
					}

					return holder;
				default:
					return holder;
			}
		}
	}
}
