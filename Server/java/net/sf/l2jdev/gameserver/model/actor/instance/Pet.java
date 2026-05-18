package net.sf.l2jdev.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.NpcConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.config.RatesConfig;
import net.sf.l2jdev.gameserver.data.enums.EvolveLevel;
import net.sf.l2jdev.gameserver.data.sql.CharSummonTable;
import net.sf.l2jdev.gameserver.data.sql.SummonEffectTable;
import net.sf.l2jdev.gameserver.data.xml.ExperienceData;
import net.sf.l2jdev.gameserver.data.xml.PetDataTable;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.handler.IItemHandler;
import net.sf.l2jdev.gameserver.handler.ItemHandler;
import net.sf.l2jdev.gameserver.managers.CursedWeaponsManager;
import net.sf.l2jdev.gameserver.managers.FortSiegeManager;
import net.sf.l2jdev.gameserver.managers.ItemManager;
import net.sf.l2jdev.gameserver.managers.ItemsOnGroundManager;
import net.sf.l2jdev.gameserver.model.PetData;
import net.sf.l2jdev.gameserver.model.PetLevelData;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.holders.creature.PetEvolveHolder;
import net.sf.l2jdev.gameserver.model.actor.stat.PetStat;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.groups.PartyDistributionType;
import net.sf.l2jdev.gameserver.model.item.Weapon;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.itemcontainer.PetInventory;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.EffectScope;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.ExChangeNpcState;
import net.sf.l2jdev.gameserver.network.serverpackets.ExStorageMaxCount;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.StopMove;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.PetInventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.PetItemList;
import net.sf.l2jdev.gameserver.taskmanagers.DecayTaskManager;

public class Pet extends Summon
{
	protected static final Logger LOGGER_PET = Logger.getLogger(Pet.class.getName());
	public static final String ADD_SKILL_SAVE = "INSERT INTO character_pet_skills_save (petObjItemId,skill_id,skill_level,skill_sub_level,remaining_time,buff_index) VALUES (?,?,?,?,?,?)";
	public static final String RESTORE_SKILL_SAVE = "SELECT petObjItemId,skill_id,skill_level,skill_sub_level,remaining_time,buff_index FROM character_pet_skills_save WHERE petObjItemId=? ORDER BY buff_index ASC";
	public static final String DELETE_SKILL_SAVE = "DELETE FROM character_pet_skills_save WHERE petObjItemId=?";
	public static final String SELECT_PET_SKILLS = "SELECT * FROM pet_skills WHERE petObjItemId=?";
	public static final String INSERT_PET_SKILLS = "INSERT INTO pet_skills (petObjItemId, skillId, skillLevel) VALUES (?,?,?) ON DUPLICATE KEY UPDATE skillId=VALUES(skillId), skillLevel=VALUES(skillLevel), petObjItemId=VALUES(petObjItemId)";
	public static final String DELETE_PET_SKILLS = "DELETE FROM pet_skills WHERE petObjItemId=?";
	public static final String SELECT_EVOLVED_PETS = "SELECT * FROM pet_evolves WHERE itemObjId=?";
	public static final String UPDATE_EVOLVED_PETS = "REPLACE INTO pet_evolves (`itemObjId`, `index`, `level`) VALUES (?, ?, ?)";
	protected int _curFed;
	protected final PetInventory _inventory;
	private final boolean _mountable;
	private final int _controlObjectId;
	private boolean _respawned;
	private int _petType = 0;
	private int _curWeightPenalty = 0;
	private long _expBeforeDeath = 0L;
	private PetData _data;
	private PetLevelData _leveldata;
	private EvolveLevel _evolveLevel = EvolveLevel.None;
	private Future<?> _feedTask;

	private void deletePetEvolved()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps1 = con.prepareStatement("DELETE FROM pet_evolves WHERE itemObjId=?");)
		{
			ps1.setInt(1, this._controlObjectId);
			ps1.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, "Could not delete pet evolve data " + this._controlObjectId, var9);
		}
	}

	public void restorePetEvolvesByItem()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps2 = con.prepareStatement("SELECT * FROM pet_evolves WHERE itemObjId=?");)
		{
			ps2.setInt(1, this._controlObjectId);

			try (ResultSet rset = ps2.executeQuery())
			{
				if (rset.next())
				{
					this.setEvolveLevel(EvolveLevel.values()[rset.getInt("level")]);
				}
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "Could not restore pet evolve for playerId: " + this.getObjectId(), var12);
		}
	}

	public void storeEvolvedPets(int evolveLevel, int index, int controlItemObjId)
	{
		this.deletePetEvolved();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("REPLACE INTO pet_evolves (`itemObjId`, `index`, `level`) VALUES (?, ?, ?)");)
		{
			stmt.setInt(1, controlItemObjId);
			stmt.setInt(2, index);
			stmt.setInt(3, evolveLevel);
			stmt.execute();
		}
		catch (SQLException var12)
		{
			LOGGER.log(Level.WARNING, "Could not store evolved pets: ", var12);
		}

		this.getOwner().setPetEvolve(controlItemObjId, new PetEvolveHolder(index, evolveLevel, this.getName(), this.getLevel(), this.getExpForThisLevel()));
	}

	public void storePetSkills(int skillId, int skillLevel)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps2 = con.prepareStatement("INSERT INTO pet_skills (petObjItemId, skillId, skillLevel) VALUES (?,?,?) ON DUPLICATE KEY UPDATE skillId=VALUES(skillId), skillLevel=VALUES(skillLevel), petObjItemId=VALUES(petObjItemId)");)
		{
			ps2.setInt(1, this._controlObjectId);
			ps2.setInt(2, skillId);
			ps2.setInt(3, skillLevel);
			ps2.execute();
		}
		catch (Exception var11)
		{
			LOGGER.log(Level.WARNING, "Could not store pet skill data: ", var11);
		}
	}

	public void restoreSkills()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps1 = con.prepareStatement("SELECT * FROM pet_skills WHERE petObjItemId=?"); PreparedStatement ps2 = con.prepareStatement("DELETE FROM pet_skills WHERE petObjItemId=?");)
		{
			ps1.setInt(1, this._controlObjectId);

			try (ResultSet rset = ps1.executeQuery())
			{
				while (rset.next())
				{
					Skill skill = SkillData.getInstance().getSkill(rset.getInt("skillId"), rset.getInt("skillLevel"));
					if (skill != null)
					{
						this.addSkill(skill);
					}
				}
			}

			ps2.setInt(1, this._controlObjectId);
			ps2.executeUpdate();
		}
		catch (Exception var15)
		{
			LOGGER.log(Level.WARNING, "Could not restore " + this + " skill data: " + var15.getMessage(), var15);
		}
	}

	public PetLevelData getPetLevelData()
	{
		if (this._leveldata == null)
		{
			this._leveldata = PetDataTable.getInstance().getPetLevelData(this.getTemplate().getId(), this.getStat().getLevel());
		}

		return this._leveldata;
	}

	public PetData getPetData()
	{
		if (this._data == null)
		{
			this._data = PetDataTable.getInstance().getPetData(this.getTemplate().getId());
		}

		this.setPetType(this._data.getDefaultPetType());
		return this._data;
	}

	public void setPetData(PetLevelData value)
	{
		this._leveldata = value;
	}

	public static synchronized Pet spawnPet(NpcTemplate template, Player owner, Item control)
	{
		Pet existingPet = World.getInstance().getPet(owner.getObjectId());
		if (existingPet != null)
		{
			existingPet.unSummon(owner);
		}

		Pet pet = restore(control, template, owner);
		if (pet != null)
		{
			pet.restoreSkills();
			pet.restorePetEvolvesByItem();
			pet.setTitle(owner.getName());
			World.getInstance().addPet(owner.getObjectId(), pet);
		}

		return pet;
	}

	public Pet upgrade(NpcTemplate template)
	{
		this.unSummon(this.getOwner());
		Pet pet = restore(this.getControlItem(), template, this.getOwner());
		if (pet != null)
		{
			pet.restoreSkills();
			pet.restorePetEvolvesByItem();
			pet.setTitle(this.getOwner().getName());
			World.getInstance().addPet(this.getOwner().getObjectId(), pet);
		}

		return pet;
	}

	public Pet(NpcTemplate template, Player owner, Item control)
	{
		this(template, owner, control, template.getDisplayId() == 12564 ? owner.getLevel() : 1);
	}

	public Pet(NpcTemplate template, Player owner, Item control, int level)
	{
		super(template, owner);
		this.setInstanceType(InstanceType.Pet);
		this._controlObjectId = control.getObjectId();
		this.getStat().setLevel(Math.max(level, PetDataTable.getInstance().getPetMinLevel(template.getId())));
		this._inventory = new PetInventory(this);
		this._inventory.restore();
		int npcId = template.getId();
		this._mountable = PetDataTable.isMountable(npcId);
		this.getPetData();
		this.getPetLevelData();
	}

	@Override
	public PetStat getStat()
	{
		return (PetStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		this.setStat(new PetStat(this));
	}

	public boolean isRespawned()
	{
		return this._respawned;
	}

	@Override
	public int getSummonType()
	{
		return 2;
	}

	@Override
	public int getControlObjectId()
	{
		return this._controlObjectId;
	}

	public Item getControlItem()
	{
		return this.getOwner().getInventory().getItemByObjectId(this._controlObjectId);
	}

	public int getCurrentFed()
	{
		return this._curFed;
	}

	public void setCurrentFed(int num)
	{
		if (num <= 0)
		{
			this.sendPacket(new ExChangeNpcState(this.getObjectId(), 100));
		}
		else if (this._curFed <= 0 && num > 0)
		{
			this.sendPacket(new ExChangeNpcState(this.getObjectId(), 101));
		}

		this._curFed = num > this.getMaxFed() ? this.getMaxFed() : num;
	}

	@Override
	public Item getActiveWeaponInstance()
	{
		if (this._inventory != null)
		{
			for (Item item : this._inventory.getItems())
			{
				if (item.getItemLocation() == ItemLocation.PET_EQUIP && (item.getTemplate().getBodyPart() == BodyPart.R_HAND || item.getTemplate().getBodyPart() == BodyPart.LR_HAND))
				{
					return item;
				}
			}
		}

		return null;
	}

	@Override
	public Weapon getActiveWeaponItem()
	{
		Item weapon = this.getActiveWeaponInstance();
		return weapon == null ? null : (Weapon) weapon.getTemplate();
	}

	@Override
	public Item getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public PetInventory getInventory()
	{
		return this._inventory;
	}

	@Override
	public boolean destroyItem(ItemProcessType process, int objectId, long count, WorldObject reference, boolean sendMessage)
	{
		Item item = this._inventory.destroyItem(process, objectId, count, this.getOwner(), reference);
		if (item == null)
		{
			if (sendMessage)
			{
				this.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			}

			return false;
		}
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(item);
		this.sendPacket(petIU);
		if (sendMessage)
		{
			if (count > 1L)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_X_S2_DISAPPEARED);
				sm.addItemName(item.getId());
				sm.addLong(count);
				this.sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(item.getId());
				this.sendPacket(sm);
			}
		}

		return true;
	}

	@Override
	public boolean destroyItemByItemId(ItemProcessType process, int itemId, long count, WorldObject reference, boolean sendMessage)
	{
		Item item = this._inventory.destroyItemByItemId(process, itemId, count, this.getOwner(), reference);
		if (item == null)
		{
			if (sendMessage)
			{
				this.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			}

			return false;
		}
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(item);
		this.sendPacket(petIU);
		if (sendMessage)
		{
			if (count > 1L)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_X_S2_DISAPPEARED);
				sm.addItemName(item.getId());
				sm.addLong(count);
				this.sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(item.getId());
				this.sendPacket(sm);
			}
		}

		return true;
	}

	@Override
	public void doPickupItem(WorldObject object)
	{
		if (!this.isDead())
		{
			this.getAI().setIntention(Intention.IDLE);
			this.broadcastPacket(new StopMove(this));
			if (!object.isItem())
			{
				LOGGER_PET.warning(this + " trying to pickup wrong target." + object);
				this.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				boolean follow = this.getFollowStatus();
				Item target = (Item) object;
				if (CursedWeaponsManager.getInstance().isCursed(target.getId()))
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1);
					smsg.addItemName(target.getId());
					this.sendPacket(smsg);
				}
				else if (!FortSiegeManager.getInstance().isCombat(target.getId()))
				{
					SystemMessage smsg = null;
					synchronized (target)
					{
						if (!target.isSpawned())
						{
							this.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}

						if (!target.getDropProtection().tryPickUp(this))
						{
							this.sendPacket(ActionFailed.STATIC_PACKET);
							smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1);
							smsg.addItemName(target);
							this.sendPacket(smsg);
							return;
						}

						if ((this.isInParty() && this.getParty().getDistributionType() == PartyDistributionType.FINDERS_KEEPERS || !this.isInParty()) && !this.getOwner().getInventory().validateCapacity(target))
						{
							this.sendPacket(ActionFailed.STATIC_PACKET);
							this.sendPacket(SystemMessageId.YOUR_GUARDIAN_S_INVENTORY_IS_FULL_REMOVE_SOMETHING_FROM_IT_AND_TRY_AGAIN);
							return;
						}

						if (target.getOwnerId() != 0 && target.getOwnerId() != this.getOwner().getObjectId() && !this.getOwner().isInLooterParty(target.getOwnerId()))
						{
							if (target.getId() == 57)
							{
								smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1_ADENA);
								smsg.addLong(target.getCount());
							}
							else if (target.getCount() > 1L)
							{
								smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S2_S1_S);
								smsg.addItemName(target);
								smsg.addLong(target.getCount());
							}
							else
							{
								smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1);
								smsg.addItemName(target);
							}

							this.sendPacket(ActionFailed.STATIC_PACKET);
							this.sendPacket(smsg);
							return;
						}

						if (target.getItemLootShedule() != null && (target.getOwnerId() == this.getOwner().getObjectId() || this.getOwner().isInLooterParty(target.getOwnerId())))
						{
							target.resetOwnerTimer();
						}

						target.pickupMe(this);
						if (GeneralConfig.SAVE_DROPPED_ITEM)
						{
							ItemsOnGroundManager.getInstance().removeObject(target);
						}
					}

					if (target.getTemplate().hasExImmediateEffect())
					{
						IItemHandler handler = ItemHandler.getInstance().getHandler(target.getEtcItem());
						if (handler == null)
						{
							LOGGER.warning("No item handler registered for item ID: " + target.getId() + ".");
						}
						else
						{
							handler.onItemUse(this, target, false);
						}

						ItemManager.destroyItem(ItemProcessType.NONE, target, this.getOwner(), null);
						this.broadcastStatusUpdate();
					}
					else
					{
						if (target.getId() == 57)
						{
							smsg = new SystemMessage(SystemMessageId.YOUR_GUARDIAN_HAS_PICKED_UP_S1_ADENA);
							smsg.addLong(target.getCount());
							this.sendPacket(smsg);
						}
						else if (target.getEnchantLevel() > 0)
						{
							smsg = new SystemMessage(SystemMessageId.YOUR_GUARDIAN_HAS_PICKED_UP_S1_S2);
							smsg.addInt(target.getEnchantLevel());
							smsg.addItemName(target);
							this.sendPacket(smsg);
						}
						else if (target.getCount() > 1L)
						{
							smsg = new SystemMessage(SystemMessageId.YOUR_GUARDIAN_HAS_PICKED_UP_S1_X_S2);
							smsg.addLong(target.getCount());
							smsg.addItemName(target);
							this.sendPacket(smsg);
						}
						else
						{
							smsg = new SystemMessage(SystemMessageId.YOUR_GUARDIAN_HAS_PICKED_UP_S1);
							smsg.addItemName(target);
							this.sendPacket(smsg);
						}

						if (this.getOwner().isInParty() && this.getOwner().getParty().getDistributionType() != PartyDistributionType.FINDERS_KEEPERS)
						{
							this.getOwner().getParty().distributeItem(this.getOwner(), target);
						}
						else
						{
							Item item = this.getOwner().getInventory().addItem(ItemProcessType.PICKUP, target, this.getOwner(), this);
							if (item != null)
							{
								this.getOwner().sendPacket(new PetItemList(this.getInventory().getItems()));
							}
						}
					}

					this.getAI().setIntention(Intention.IDLE);
					if (follow)
					{
						this.followOwner();
					}
				}
			}
		}
	}

	@Override
	public void deleteMe(Player owner)
	{
		super.deleteMe(owner);
	}

	@Override
	public boolean doDie(Creature killer)
	{
		Player owner = this.getOwner();
		if (owner != null && !owner.isInDuel() && (!this.isInsideZone(ZoneId.PVP) || this.isInsideZone(ZoneId.SIEGE)))
		{
			this.deathPenalty();
		}

		if (!super.doDie(killer, true))
		{
			return false;
		}
		this.stopFeed();
		this.storeMe();

		for (Skill skill : this.getAllSkills())
		{
			this.storePetSkills(skill.getId(), skill.getLevel());
		}

		DecayTaskManager.getInstance().add(this);
		if (owner != null)
		{
			BuffInfo buffInfo = owner.getEffectList().getBuffInfoBySkillId(49300);
			owner.getEffectList().add(new BuffInfo(owner, owner, SkillData.getInstance().getSkill(49300, buffInfo == null ? 1 : Math.min(buffInfo.getSkill().getLevel() + 1, 10)), false, null, null));
		}

		return true;
	}

	@Override
	public void doRevive()
	{
		this.getOwner().removeReviving();
		super.doRevive();
		DecayTaskManager.getInstance().cancel(this);
		this.startFeed();
		if (!this.isHungry())
		{
			this.setRunning();
		}

		this.getAI().setIntention(Intention.ACTIVE);
	}

	@Override
	public void doRevive(double revivePower)
	{
		this.restoreExp(revivePower);
		this.doRevive();
	}

	public Item transferItem(ItemProcessType process, int objectId, long count, Inventory target, Player actor, WorldObject reference)
	{
		Item oldItem = this._inventory.getItemByObjectId(objectId);
		Item playerOldItem = target.getItemByItemId(oldItem.getId());
		Item newItem = this._inventory.transferItem(process, objectId, count, target, actor, reference);
		if (newItem == null)
		{
			return null;
		}
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		if (oldItem.getCount() > 0L && oldItem != newItem)
		{
			petIU.addModifiedItem(oldItem);
		}
		else
		{
			petIU.addRemovedItem(oldItem);
		}

		this.sendInventoryUpdate(petIU);
		if (playerOldItem != null && newItem.isStackable())
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(newItem);
			this.getOwner().sendInventoryUpdate(iu);
		}

		return newItem;
	}

	public void destroyControlItem(Player owner, boolean evolve)
	{
		World.getInstance().removePet(owner.getObjectId());

		try
		{
			Item removedItem;
			if (evolve)
			{
				removedItem = owner.getInventory().destroyItem(ItemProcessType.FEE, this._controlObjectId, 1L, this.getOwner(), this);
			}
			else
			{
				removedItem = owner.getInventory().destroyItem(ItemProcessType.DESTROY, this._controlObjectId, 1L, this.getOwner(), this);
				if (removedItem != null)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
					sm.addItemName(removedItem);
					owner.sendPacket(sm);
				}
			}

			if (removedItem == null)
			{
				LOGGER.warning("Couldn't destroy pet control item for " + owner + " pet: " + this + " evolve: " + evolve);
			}
			else
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addRemovedItem(removedItem);
				owner.sendInventoryUpdate(iu);
				owner.broadcastUserInfo();
			}
		}
		catch (Exception var9)
		{
			LOGGER_PET.log(Level.WARNING, "Error while destroying control item: " + var9.getMessage(), var9);
		}

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id = ?");)
		{
			statement.setInt(1, this._controlObjectId);
			statement.execute();
		}
		catch (Exception var12)
		{
			LOGGER_PET.log(Level.SEVERE, "Failed to delete Pet [ObjectId: " + this.getObjectId() + "]", var12);
		}
	}

	public void dropAllItems()
	{
		try
		{
			for (Item item : this._inventory.getItems())
			{
				this.dropItemHere(item);
			}
		}
		catch (Exception var3)
		{
			LOGGER_PET.log(Level.WARNING, "Pet Drop Error: " + var3.getMessage(), var3);
		}
	}

	public void dropItemHere(Item item, boolean protect)
	{
		Item dropit = this._inventory.dropItem(ItemProcessType.DROP, item.getObjectId(), item.getCount(), this.getOwner(), this);
		if (dropit != null)
		{
			if (protect)
			{
				dropit.getDropProtection().protect(this.getOwner());
			}

			LOGGER_PET.finer("Item id to drop: " + dropit.getId() + " amount: " + dropit.getCount());
			dropit.dropMe(this, this.getX(), this.getY(), this.getZ() + 100);
		}
	}

	public void dropItemHere(Item dropit)
	{
		this.dropItemHere(dropit, false);
	}

	@Override
	public boolean isMountable()
	{
		return this._mountable;
	}

	public static Pet restore(Item control, NpcTemplate template, Player owner)
	{
		try
		{
			Pet var17;
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT item_obj_id, name, level, curHp, curMp, exp, sp, fed FROM pets WHERE item_obj_id=?");)
			{
				statement.setInt(1, control.getObjectId());

				Pet pet;
				try (ResultSet rset = statement.executeQuery())
				{
					if (!rset.next())
					{
						return new Pet(template, owner, control);
					}

					pet = new Pet(template, owner, control, rset.getInt("level"));
					pet._respawned = true;
					pet.setName(rset.getString("name"));
					long exp = rset.getLong("exp");
					PetLevelData info = PetDataTable.getInstance().getPetLevelData(pet.getId(), pet.getLevel());
					if (info != null && exp < info.getPetMaxExp())
					{
						exp = info.getPetMaxExp();
					}

					pet.getStat().setExp(exp);
					pet.getStat().setLevel(rset.getInt("level"));
					pet.getStat().setSp(rset.getInt("sp"));
					pet.getStatus().setCurrentHp(rset.getInt("curHp"));
					pet.getStatus().setCurrentMp(rset.getInt("curMp"));
					pet.getStatus().setCurrentCp(pet.getMaxCp());
					if (rset.getDouble("curHp") < 1.0)
					{
						pet.setCurrentHpMp(pet.getMaxHp(), pet.getMaxMp());
					}

					pet.setEvolveLevel(pet.getPetData().getEvolveLevel());
					pet.setCurrentFed(rset.getInt("fed"));
				}

				var17 = pet;
			}

			return var17;
		}
		catch (Exception var16)
		{
			LOGGER_PET.log(Level.WARNING, "Could not restore pet data for owner: " + owner + " - " + var16.getMessage(), var16);
			return null;
		}
	}

	@Override
	public void setRestoreSummon(boolean value)
	{
		this._restoreSummon = value;
	}

	@Override
	public void stopSkillEffects(SkillFinishType type, int skillId)
	{
		super.stopSkillEffects(type, skillId);
		Collection<SummonEffectTable.SummonEffect> effects = SummonEffectTable.getInstance().getPetEffects().get(this.getControlObjectId());
		if (effects != null && !effects.isEmpty())
		{
			for (SummonEffectTable.SummonEffect effect : effects)
			{
				if (effect.getSkill().getId() == skillId)
				{
					SummonEffectTable.getInstance().getPetEffects().get(this.getControlObjectId()).remove(effect);
				}
			}
		}
	}

	@Override
	public void storeMe()
	{
		if (this._controlObjectId != 0)
		{
			if (!PlayerConfig.RESTORE_PET_ON_RECONNECT)
			{
				this._restoreSummon = false;
			}

			String req;
			if (!this._respawned)
			{
				req = "INSERT INTO pets (name,level,curHp,curMp,exp,sp,fed,ownerId,restore,item_obj_id) VALUES (?,?,?,?,?,?,?,?,?,?)";
			}
			else
			{
				req = "UPDATE pets SET name=?,level=?,curHp=?,curMp=?,exp=?,sp=?,fed=?,ownerId=?,restore=? WHERE item_obj_id = ?";
			}

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement(req);)
			{
				statement.setString(1, this.getName());
				statement.setInt(2, this.getStat().getLevel());
				statement.setDouble(3, this.getStatus().getCurrentHp());
				statement.setDouble(4, this.getStatus().getCurrentMp());
				statement.setLong(5, this.getStat().getExp());
				statement.setLong(6, this.getStat().getSp());
				statement.setInt(7, this._curFed);
				statement.setInt(8, this.getOwner().getObjectId());
				statement.setString(9, String.valueOf(this._restoreSummon));
				statement.setInt(10, this._controlObjectId);
				statement.executeUpdate();
				this._respawned = true;
				if (this._restoreSummon)
				{
					CharSummonTable.getInstance().getPets().put(this.getOwner().getObjectId(), this.getControlObjectId());
				}
				else
				{
					CharSummonTable.getInstance().getPets().remove(this.getOwner().getObjectId());
				}
			}
			catch (Exception var10)
			{
				LOGGER_PET.log(Level.SEVERE, "Failed to store Pet [ObjectId: " + this.getObjectId() + "] data", var10);
			}

			Item itemInst = this.getControlItem();
			if (itemInst != null && itemInst.getEnchantLevel() != this.getStat().getLevel())
			{
				itemInst.setEnchantLevel(this.getStat().getLevel());
				itemInst.updateDatabase();
			}
		}
	}

	@Override
	public void storeEffect(boolean storeEffects)
	{
		if (PlayerConfig.SUMMON_STORE_SKILL_COOLTIME)
		{
			SummonEffectTable.getInstance().getPetEffects().getOrDefault(this.getControlObjectId(), Collections.emptyList()).clear();

			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps1 = con.prepareStatement("DELETE FROM character_pet_skills_save WHERE petObjItemId=?");
				PreparedStatement ps2 = con.prepareStatement("INSERT INTO character_pet_skills_save (petObjItemId,skill_id,skill_level,skill_sub_level,remaining_time,buff_index) VALUES (?,?,?,?,?,?)");)
			{
				ps1.setInt(1, this._controlObjectId);
				ps1.execute();
				int buffIndex = 0;
				Set<Long> storedSkills = new HashSet<>();
				if (storeEffects)
				{
					for (BuffInfo info : this.getEffectList().getEffects())
					{
						if (info != null)
						{
							Skill skill = info.getSkill();
							if (!skill.isDeleteAbnormalOnLeave() && skill.isSharedWithSummon() && skill.getAbnormalType() != AbnormalType.LIFE_FORCE_OTHERS && (!skill.isToggle() || skill.isNecessaryToggle()) && (!skill.isDance() || PlayerConfig.ALT_STORE_DANCES) && storedSkills.add(skill.getReuseHashCode()))
							{
								ps2.setInt(1, this._controlObjectId);
								ps2.setInt(2, skill.getId());
								ps2.setInt(3, skill.getLevel());
								ps2.setInt(4, skill.getSubLevel());
								ps2.setInt(5, info.getTime());
								ps2.setInt(6, ++buffIndex);
								ps2.addBatch();
								SummonEffectTable.getInstance().getPetEffects().computeIfAbsent(this.getControlObjectId(), _ -> ConcurrentHashMap.newKeySet()).add(new SummonEffectTable.SummonEffect(skill, info.getTime()));
							}
						}
					}

					ps2.executeBatch();
				}
			}
			catch (Exception var16)
			{
				LOGGER.log(Level.WARNING, "Could not store pet effect data: ", var16);
			}
		}
	}

	@Override
	public void restoreEffects()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps1 = con.prepareStatement("SELECT petObjItemId,skill_id,skill_level,skill_sub_level,remaining_time,buff_index FROM character_pet_skills_save WHERE petObjItemId=? ORDER BY buff_index ASC");
			PreparedStatement ps2 = con.prepareStatement("DELETE FROM character_pet_skills_save WHERE petObjItemId=?");)
		{
			if (!SummonEffectTable.getInstance().getPetEffects().containsKey(this.getControlObjectId()))
			{
				ps1.setInt(1, this._controlObjectId);

				try (ResultSet rset = ps1.executeQuery())
				{
					while (rset.next())
					{
						int effectCurTime = rset.getInt("remaining_time");
						Skill skill = SkillData.getInstance().getSkill(rset.getInt("skill_id"), rset.getInt("skill_level"));
						if (skill != null && skill.hasEffects(EffectScope.GENERAL))
						{
							SummonEffectTable.getInstance().getPetEffects().computeIfAbsent(this.getControlObjectId(), _ -> ConcurrentHashMap.newKeySet()).add(new SummonEffectTable.SummonEffect(skill, effectCurTime));
						}
					}
				}
			}

			ps2.setInt(1, this._controlObjectId);
			ps2.executeUpdate();
		}
		catch (Exception var28)
		{
			LOGGER.log(Level.WARNING, "Could not restore " + this + " active effect data: " + var28.getMessage(), var28);
		}
		finally
		{
			if (SummonEffectTable.getInstance().getPetEffects().get(this.getControlObjectId()) != null)
			{
				for (SummonEffectTable.SummonEffect se : SummonEffectTable.getInstance().getPetEffects().get(this.getControlObjectId()))
				{
					if (se != null)
					{
						se.getSkill().applyEffects(this, this, false, se.getEffectCurTime());
					}
				}
			}
		}
	}

	public synchronized void stopFeed()
	{
		if (this._feedTask != null)
		{
			this._feedTask.cancel(false);
			this._feedTask = null;
		}
	}

	public synchronized void startFeed()
	{
		this.stopFeed();
		if (!this.isDead() && this.getOwner().getPet() == this)
		{
			this._feedTask = ThreadPool.scheduleAtFixedRate(new Pet.FeedTask(), 10000L, 10000L);
		}
	}

	@Override
	public synchronized void unSummon(Player owner)
	{
		this.stopFeed();
		this.stopHpMpRegeneration();
		super.unSummon(owner);
		if (!this.isDead())
		{
			if (this._inventory != null)
			{
				this._inventory.deleteMe();
			}

			World.getInstance().removePet(owner.getObjectId());
		}
	}

	public void restoreExp(double restorePercent)
	{
		if (this._expBeforeDeath > 0L)
		{
			this.getStat().addExp(Math.round((this._expBeforeDeath - this.getStat().getExp()) * restorePercent / 100.0));
			this._expBeforeDeath = 0L;
		}
	}

	private void deathPenalty()
	{
		int level = this.getStat().getLevel();
		double percentLost = -0.07 * level + 6.5;
		long lostExp = Math.round((this.getStat().getExpForLevel(level + 1) - this.getStat().getExpForLevel(level)) * percentLost / 100.0);
		this._expBeforeDeath = this.getStat().getExp();
		this.getStat().addExp(-lostExp);
	}

	@Override
	public synchronized void addExpAndSp(double addToExp, double addToSp)
	{
		if (this.getId() == 12564)
		{
			this.getStat().addExpAndSp(addToExp * RatesConfig.SINEATER_XP_RATE);
		}
		else
		{
			this.getStat().addExpAndSp(addToExp * RatesConfig.PET_XP_RATE);
		}
	}

	@Override
	public long getExpForThisLevel()
	{
		return this.getLevel() >= ExperienceData.getInstance().getMaxPetLevel() ? 0L : this.getStat().getExpForLevel(this.getLevel());
	}

	@Override
	public long getExpForNextLevel()
	{
		return this.getLevel() >= ExperienceData.getInstance().getMaxPetLevel() - 1 ? 0L : this.getStat().getExpForLevel(this.getLevel() + 1);
	}

	@Override
	public int getLevel()
	{
		return this.getStat().getLevel();
	}

	public int getMaxFed()
	{
		return this.getStat().getMaxFeed();
	}

	@Override
	public int getCriticalHit()
	{
		return this.getStat().getCriticalHit();
	}

	@Override
	public int getMAtk()
	{
		return this.getStat().getMAtk();
	}

	@Override
	public int getMDef()
	{
		return this.getStat().getMDef();
	}

	@Override
	public int getSkillLevel(int skillId)
	{
		if (this.getKnownSkill(skillId) == null)
		{
			return 0;
		}
		int level = this.getLevel();
		return level > 70 ? 7 + (level - 70) / 5 : level / 10;
	}

	public void updateRefOwner(Player owner)
	{
		int oldOwnerId = this.getOwner().getObjectId();
		this.setOwner(owner);
		World.getInstance().removePet(oldOwnerId);
		World.getInstance().addPet(oldOwnerId, this);
	}

	public int getInventoryLimit()
	{
		return NpcConfig.INVENTORY_MAXIMUM_PET;
	}

	public void refreshOverloaded()
	{
		int maxLoad = this.getMaxLoad();
		if (maxLoad > 0)
		{
			long weightproc = (this.getCurrentLoad() - this.getBonusWeightPenalty()) * 1000 / maxLoad;
			int newWeightPenalty;
			if (weightproc < 500L || this.getOwner().getDietMode())
			{
				newWeightPenalty = 0;
			}
			else if (weightproc < 666L)
			{
				newWeightPenalty = 1;
			}
			else if (weightproc < 800L)
			{
				newWeightPenalty = 2;
			}
			else if (weightproc < 1000L)
			{
				newWeightPenalty = 3;
			}
			else
			{
				newWeightPenalty = 4;
			}

			if (this._curWeightPenalty != newWeightPenalty)
			{
				this._curWeightPenalty = newWeightPenalty;
				if (newWeightPenalty > 0)
				{
					this.addSkill(SkillData.getInstance().getSkill(4270, newWeightPenalty));
					this.setOverloaded(this.getCurrentLoad() >= maxLoad);
				}
				else
				{
					this.removeSkill(this.getKnownSkill(4270), true);
					this.setOverloaded(false);
				}
			}
		}
	}

	@Override
	public void updateAndBroadcastStatus(int value)
	{
		this.refreshOverloaded();
		super.updateAndBroadcastStatus(value);
	}

	@Override
	public boolean isHungry()
	{
		return this._curFed < this.getPetData().getHungryLimit() / 100.0F * this.getPetLevelData().getPetMaxFeed();
	}

	public boolean isUncontrollable()
	{
		return this._curFed <= 0;
	}

	@Override
	public int getWeapon()
	{
		Item weapon = this._inventory.getPaperdollItem(5);
		return weapon != null ? weapon.getId() : 0;
	}

	@Override
	public int getArmor()
	{
		Item weapon = this._inventory.getPaperdollItem(6);
		return weapon != null ? weapon.getId() : 0;
	}

	public int getJewel()
	{
		Item weapon = this._inventory.getPaperdollItem(4);
		return weapon != null ? weapon.getId() : 0;
	}

	@Override
	public short getSoulShotsPerHit()
	{
		return this.getPetLevelData().getPetSoulShot();
	}

	@Override
	public short getSpiritShotsPerHit()
	{
		return this.getPetLevelData().getPetSpiritShot();
	}

	@Override
	public void setName(String name)
	{
		Item controlItem = this.getControlItem();
		if (controlItem != null)
		{
			if (controlItem.getCustomType2() == (name == null ? 1 : 0))
			{
				controlItem.setCustomType2(name != null ? 1 : 0);
				controlItem.updateDatabase();
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(controlItem);
				this.getOwner().sendInventoryUpdate(iu);
			}
		}
		else
		{
			LOGGER.warning("Pet control item null, for pet: " + this.toString());
		}

		super.setName(name);
	}

	public boolean canEatFoodId(int itemId)
	{
		return this._data.getFood().contains(itemId);
	}

	@Override
	public boolean isPet()
	{
		return true;
	}

	@Override
	public Pet asPet()
	{
		return this;
	}

	@Override
	public double getRunSpeed()
	{
		return super.getRunSpeed() * (this.isUncontrollable() ? 0.5 : 1.0);
	}

	@Override
	public double getWalkSpeed()
	{
		return super.getWalkSpeed() * (this.isUncontrollable() ? 0.5 : 1.0);
	}

	@Override
	public double getMovementSpeedMultiplier()
	{
		return super.getMovementSpeedMultiplier() * (this.isUncontrollable() ? 0.5 : 1.0);
	}

	@Override
	public double getMoveSpeed()
	{
		if (this.isInsideZone(ZoneId.WATER))
		{
			return this.isRunning() ? this.getSwimRunSpeed() : this.getSwimWalkSpeed();
		}
		return this.isRunning() ? this.getRunSpeed() : this.getWalkSpeed();
	}

	public int getPetType()
	{
		return this._petType;
	}

	public void setPetType(int petType)
	{
		this._petType = petType;
	}

	public int getEvolveLevel()
	{
		return this._evolveLevel.ordinal();
	}

	public void setEvolveLevel(EvolveLevel evolveLevel)
	{
		this._evolveLevel = evolveLevel;
	}

	public void useEquippableItem(Item item, boolean abortAttack)
	{
		if (item != null)
		{
			ItemLocation itemLocation = item.getItemLocation();
			if (itemLocation == ItemLocation.INVENTORY || itemLocation == ItemLocation.PAPERDOLL || itemLocation == ItemLocation.PET || itemLocation == ItemLocation.PET_EQUIP)
			{
				boolean isEquiped = item.isEquipped();
				int oldInvLimit = this.getInventoryLimit();
				SystemMessage sm = null;
				List<Item> items;
				if (isEquiped)
				{
					if (item.getEnchantLevel() > 0)
					{
						sm = new SystemMessage(SystemMessageId.S1_S2_UNEQUIPPED);
						sm.addInt(item.getEnchantLevel());
						sm.addItemName(item);
					}
					else
					{
						sm = new SystemMessage(SystemMessageId.S1_UNEQUIPPED);
						sm.addItemName(item);
					}

					this.sendPacket(sm);
					BodyPart bodyPart = BodyPart.fromItem(item);
					if (bodyPart != BodyPart.DECO && bodyPart != BodyPart.BROOCH_JEWEL && bodyPart != BodyPart.AGATHION && bodyPart != BodyPart.ARTIFACT)
					{
						items = this._inventory.unEquipItemInBodySlotAndRecord(bodyPart);
					}
					else
					{
						items = this._inventory.unEquipItemInSlotAndRecord(item.getLocationSlot());
					}
				}
				else
				{
					items = this._inventory.equipItemAndRecord(item);
					if (item.isEquipped())
					{
						if (item.getEnchantLevel() > 0)
						{
							sm = new SystemMessage(SystemMessageId.S1_S2_EQUIPPED);
							sm.addInt(item.getEnchantLevel());
							sm.addItemName(item);
						}
						else
						{
							sm = new SystemMessage(SystemMessageId.S1_EQUIPPED);
							sm.addItemName(item);
						}

						this.sendPacket(sm);
						item.decreaseMana(false);
						BodyPart bodyPart = item.getTemplate().getBodyPart();
						if (bodyPart == BodyPart.R_HAND || bodyPart == BodyPart.LR_HAND)
						{
							this.rechargeShots(true, true, false);
						}
					}
					else
					{
						this.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
					}
				}

				PetInventoryUpdate petIU = new PetInventoryUpdate();
				petIU.addItems(items);
				this.sendInventoryUpdate(petIU);
				if (abortAttack)
				{
					this.abortAttack();
				}

				if (this.getInventoryLimit() != oldInvLimit)
				{
					this.getOwner().sendPacket(new ExStorageMaxCount(this.getOwner()));
				}
			}
		}
	}

	class FeedTask implements Runnable
	{
		FeedTask()
		{
			Objects.requireNonNull(Pet.this);
			super();
		}

		@Override
		public void run()
		{
			try
			{
				Summon pet = Pet.this.getOwner().getPet();
				BuffInfo buffInfo = Pet.this.getOwner() != null ? Pet.this.getOwner().getEffectList().getBuffInfoBySkillId(49300) : null;
				int buffLvl = buffInfo == null ? 0 : buffInfo.getSkill().getLevel();
				int feedCons = buffLvl != 0 ? this.getFeedConsume() + this.getFeedConsume() / 100 * buffLvl * 50 : this.getFeedConsume();
				if (Pet.this.getOwner() == null || pet == null || pet.getObjectId() != Pet.this.getObjectId())
				{
					Pet.this.stopFeed();
					return;
				}

				if (Pet.this._curFed > feedCons)
				{
					Pet.this.setCurrentFed(Pet.this._curFed - feedCons);
				}
				else
				{
					Pet.this.setCurrentFed(0);
				}

				Pet.this.broadcastStatusUpdate();
				Set<Integer> foodIds = Pet.this.getPetData().getFood();
				if (foodIds.isEmpty())
				{
					if (Pet.this.isUncontrollable())
					{
						if (Pet.this.getTemplate().getId() == 16050 && Pet.this.getOwner() != null)
						{
							Pet.this.getOwner().setPkKills(Math.max(0, Pet.this.getOwner().getPkKills() - Rnd.get(1, 6)));
						}

						Pet.this.sendPacket(SystemMessageId.YOUR_GUARDIAN_IS_RUNNING_AWAY_FROM_YOU);
						Pet.this.deleteMe(Pet.this.getOwner());
					}
					else if (Pet.this.isHungry())
					{
						Pet.this.sendPacket(SystemMessageId.YOUR_GUARDIAN_WILL_RUN_AWAY_SHORTLY);
					}

					return;
				}

				Item food = null;

				for (int id : foodIds)
				{
					food = Pet.this.getOwner().getInventory().getItemByItemId(id);
					if (food != null && Pet.this.getOwner().getAutoUseSettings().getAutoSupplyItems().contains(id))
					{
						break;
					}
				}

				if (food != null && Pet.this.isHungry() && Pet.this.getOwner().getAutoUseSettings().getAutoSupplyItems().contains(food.getId()) && !pet.isInsideZone(ZoneId.PEACE))
				{
					IItemHandler handler = ItemHandler.getInstance().getHandler(food.getEtcItem());
					if (handler != null)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_GUARDIAN_WAS_HUNGRY_SO_IT_HAS_EATEN_S1);
						sm.addItemName(food.getId());
						Pet.this.sendPacket(sm);
						handler.onItemUse(Pet.this.getOwner(), food, false);
					}
				}

				if (Pet.this.isUncontrollable())
				{
					Pet.this.sendPacket(SystemMessageId.THE_GUARDIAN_IS_HUNGRY_AND_CANNOT_BE_CONTROLLED_IT_NEEDS_A_COMBAT_RATION);
				}
			}
			catch (Exception var9)
			{
				Pet.LOGGER_PET.log(Level.SEVERE, "Pet [ObjectId: " + Pet.this.getObjectId() + "] a feed task error has occurred", var9);
			}
		}

		private int getFeedConsume()
		{
			return Pet.this.isAttackingNow() ? Pet.this.getPetLevelData().getPetFeedBattle() : Pet.this.getPetLevelData().getPetFeedNormal();
		}
	}
}
