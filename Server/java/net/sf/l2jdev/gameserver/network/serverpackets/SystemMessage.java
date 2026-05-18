package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Arrays;
import java.util.Objects;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.custom.MultilingualSupportConfig;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.enums.player.ElementalSpiritType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class SystemMessage extends ServerPacket
{
	private static final SystemMessage.SMParam[] EMPTY_PARAM_ARRAY = new SystemMessage.SMParam[0];
	public static final byte TYPE_FACTION_NAME = 24;
	public static final byte TYPE_BYTE = 20;
	public static final byte TYPE_POPUP_ID = 16;
	public static final byte TYPE_CLASS_ID = 15;
	public static final byte TYPE_SYSTEM_STRING = 13;
	public static final byte TYPE_PLAYER_NAME = 12;
	public static final byte TYPE_DOOR_NAME = 11;
	public static final byte TYPE_INSTANCE_NAME = 10;
	public static final byte TYPE_ELEMENT_NAME = 9;
	public static final byte TYPE_ZONE_NAME = 7;
	public static final byte TYPE_LONG_NUMBER = 6;
	public static final byte TYPE_CASTLE_NAME = 5;
	public static final byte TYPE_SKILL_NAME = 4;
	public static final byte TYPE_ITEM_NAME = 3;
	public static final byte TYPE_NPC_NAME = 2;
	public static final byte TYPE_INT_NUMBER = 1;
	public static final byte TYPE_TEXT = 0;
	private SystemMessage.SMParam[] _params;
	private final SystemMessageId _smId;
	private int _paramIndex;

	public SystemMessage(int id)
	{
		this._smId = SystemMessageId.getSystemMessageId(id);
		this._params = this._smId.getParamCount() > 0 ? new SystemMessage.SMParam[this._smId.getParamCount()] : EMPTY_PARAM_ARRAY;
	}

	public SystemMessage(SystemMessageId smId)
	{
		if (smId == null)
		{
			throw new NullPointerException("SystemMessageId cannot be null!");
		}
		this._smId = smId;
		this._params = smId.getParamCount() > 0 ? new SystemMessage.SMParam[smId.getParamCount()] : EMPTY_PARAM_ARRAY;
	}

	public SystemMessage(String text)
	{
		if (text == null)
		{
			throw new NullPointerException();
		}
		this._smId = SystemMessageId.getSystemMessageId(SystemMessageId.S1_2.getId());
		this._params = new SystemMessage.SMParam[1];
		this.addString(text);
	}

	public int getId()
	{
		return this._smId.getId();
	}

	public SystemMessageId getSystemMessageId()
	{
		return this._smId;
	}

	private void append(SystemMessage.SMParam param)
	{
		if (this._paramIndex >= this._params.length)
		{
			this._params = Arrays.copyOf(this._params, this._paramIndex + 1);
			this._smId.setParamCount(this._paramIndex + 1);
			if (param.getType() != 16)
			{
				PacketLogger.info("Wrong parameter count '" + (this._paramIndex + 1) + "' for SystemMessageId: " + this._smId);
			}
		}

		this._params[this._paramIndex++] = param;
	}

	public SystemMessage addString(String text)
	{
		this.append(new SystemMessage.SMParam((byte) 0, text));
		return this;
	}

	public SystemMessage addCastleId(int number)
	{
		this.append(new SystemMessage.SMParam((byte) 5, number));
		return this;
	}

	public SystemMessage addInt(int number)
	{
		this.append(new SystemMessage.SMParam((byte) 1, number));
		return this;
	}

	public SystemMessage addLong(long number)
	{
		this.append(new SystemMessage.SMParam((byte) 6, number));
		return this;
	}

	public SystemMessage addPcName(Player pc)
	{
		this.append(new SystemMessage.SMParam((byte) 12, pc.getAppearance().getVisibleName()));
		return this;
	}

	public SystemMessage addDoorName(int doorId)
	{
		this.append(new SystemMessage.SMParam((byte) 11, doorId));
		return this;
	}

	public SystemMessage addNpcName(Npc npc)
	{
		return this.addNpcName(npc.getTemplate());
	}

	public SystemMessage addNpcName(Summon npc)
	{
		return this.addNpcName(npc.getId());
	}

	public SystemMessage addNpcName(NpcTemplate template)
	{
		return template.isUsingServerSideName() ? this.addString(template.getName()) : this.addNpcName(template.getId());
	}

	public SystemMessage addNpcName(int id)
	{
		this.append(new SystemMessage.SMParam((byte) 2, 1000000 + id));
		return this;
	}

	public SystemMessage addItemName(Item item)
	{
		return this.addItemName(item.getId());
	}

	public SystemMessage addItemName(ItemTemplate item)
	{
		return this.addItemName(item.getId());
	}

	public SystemMessage addItemName(int id)
	{
		ItemTemplate item = ItemData.getInstance().getTemplate(id);
		if (item.getDisplayId() != id)
		{
			return this.addString(item.getName());
		}
		this.append(new SystemMessage.SMParam((byte) 3, id));
		return this;
	}

	public SystemMessage addZoneName(int x, int y, int z)
	{
		this.append(new SystemMessage.SMParam((byte) 7, new int[]
		{
			x,
			y,
			z
		}));
		return this;
	}

	public SystemMessage addSkillName(Skill skill)
	{
		return skill.getId() != skill.getDisplayId() ? this.addString(skill.getName()) : this.addSkillName(skill.getId(), skill.getLevel(), skill.getSubLevel());
	}

	public SystemMessage addSkillName(int id)
	{
		return this.addSkillName(id, 1, 0);
	}

	public SystemMessage addSkillName(int id, int lvl, int subLevel)
	{
		this.append(new SystemMessage.SMParam((byte) 4, new int[]
		{
			id,
			lvl,
			subLevel
		}));
		return this;
	}

	public SystemMessage addAttribute(int type)
	{
		this.append(new SystemMessage.SMParam((byte) 9, type));
		return this;
	}

	public SystemMessage addSystemString(int type)
	{
		this.append(new SystemMessage.SMParam((byte) 13, type));
		return this;
	}

	public SystemMessage addClassId(int type)
	{
		this.append(new SystemMessage.SMParam((byte) 15, type));
		return this;
	}

	public SystemMessage addFactionName(int factionId)
	{
		this.append(new SystemMessage.SMParam((byte) 24, factionId));
		return this;
	}

	public SystemMessage addPopup(int target, int attacker, int damage)
	{
		this.append(new SystemMessage.SMParam((byte) 16, new int[]
		{
			target,
			attacker,
			damage
		}));
		return this;
	}

	public SystemMessage addByte(int time)
	{
		this.append(new SystemMessage.SMParam((byte) 20, time));
		return this;
	}

	public SystemMessage addInstanceName(int type)
	{
		this.append(new SystemMessage.SMParam((byte) 10, type));
		return this;
	}

	public SystemMessage addElementalSpiritName(byte elementType)
	{
		this.append(new SystemMessage.SMParam((byte) 0, ElementalSpiritType.of(elementType).getName()));
		return this;
	}

	public SystemMessage.SMParam[] getParams()
	{
		return this._params;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SYSTEM_MESSAGE.writeId(this, buffer);
		if (MultilingualSupportConfig.MULTILANG_ENABLE)
		{
			Player player = client.getPlayer();
			if (player != null)
			{
				String lang = player.getLang();
				if (lang != null && !lang.equals("en"))
				{
					SystemMessageId.SMLocalisation sml = this._smId.getLocalisation(lang);
					if (sml != null)
					{
						Object[] params = new Object[this._paramIndex];

						for (int i = 0; i < this._paramIndex; i++)
						{
							params[i] = this._params[i].getValue();
						}

						buffer.writeShort(SystemMessageId.S1_2.getId());
						buffer.writeByte(1);
						buffer.writeByte((byte) 0);
						buffer.writeString(sml.getLocalisation(params));
						return;
					}
				}
			}
		}

		buffer.writeShort(this.getId());
		buffer.writeByte(this._params.length);

		for (SystemMessage.SMParam param : this._params)
		{
			if (param == null)
			{
				PacketLogger.warning("Found null parameter for SystemMessageId " + this._smId);
			}
			else
			{
				buffer.writeByte(param.getType());
				switch (param.getType())
				{
					case 0:
					case 12:
						buffer.writeString(param.getStringValue());
						break;
					case 1:
					case 2:
					case 3:
					case 11:
						buffer.writeInt(param.getIntValue());
						break;
					case 4:
					{
						int[] array = param.getIntArrayValue();
						buffer.writeInt(array[0]);
						buffer.writeShort(array[1]);
						buffer.writeShort(array[2]);
						break;
					}
					case 5:
					case 10:
					case 13:
					case 15:
						buffer.writeShort(param.getIntValue());
						break;
					case 6:
						buffer.writeLong(param.getLongValue());
						break;
					case 7:
					case 16:
					{
						int[] array = param.getIntArrayValue();
						buffer.writeInt(array[0]);
						buffer.writeInt(array[1]);
						buffer.writeInt(array[2]);
					}
					case 8:
					case 14:
					case 17:
					case 18:
					case 19:
					case 21:
					case 22:
					case 23:
					default:
						break;
					case 9:
					case 20:
					case 24:
						buffer.writeByte(param.getIntValue());
				}
			}
		}
	}

	public class SMParam
	{
		private final byte _type;
		private final Object _value;

		public SMParam(byte type, Object value)
		{
			Objects.requireNonNull(SystemMessage.this);
			super();
			this._type = type;
			this._value = value;
		}

		public byte getType()
		{
			return this._type;
		}

		public Object getValue()
		{
			return this._value;
		}

		public String getStringValue()
		{
			return (String) this._value;
		}

		public int getIntValue()
		{
			return (Integer) this._value;
		}

		public long getLongValue()
		{
			return (Long) this._value;
		}

		public int[] getIntArrayValue()
		{
			return (int[]) this._value;
		}
	}
}
