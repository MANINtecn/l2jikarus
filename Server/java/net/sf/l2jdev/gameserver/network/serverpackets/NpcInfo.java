package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.NpcConfig;
import net.sf.l2jdev.gameserver.config.custom.MultilingualSupportConfig;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.data.xml.NpcNameLocalisationData;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Team;
import net.sf.l2jdev.gameserver.model.actor.instance.Doppelganger;
import net.sf.l2jdev.gameserver.model.actor.instance.Guard;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.skill.AbnormalVisualEffect;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.NpcStringId;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.NpcInfoType;

public class NpcInfo extends AbstractMaskPacket<NpcInfoType>
{
	private final Npc _npc;
	private final byte[] _masks = new byte[]
	{
		0,
		12,
		12,
		0,
		0
	};
	private int _initSize = 0;
	private int _blockSize = 0;
	private int _clanCrest = 0;
	private int _clanLargeCrest = 0;
	private int _allyCrest = 0;
	private int _allyId = 0;
	private int _clanId = 0;
	private int _statusMask = 0;
	private final Set<AbnormalVisualEffect> _abnormalVisualEffects;

	public NpcInfo(Npc npc)
	{
		this._npc = npc;
		this._abnormalVisualEffects = npc.getEffectList().getCurrentAbnormalVisualEffects();
		this.addComponentType(NpcInfoType.ATTACKABLE, NpcInfoType.RELATIONS, NpcInfoType.ID, NpcInfoType.POSITION, NpcInfoType.ALIVE, NpcInfoType.RUNNING);
		if (npc.getHeading() > 0)
		{
			this.addComponentType(NpcInfoType.HEADING);
		}

		if (npc.getStat().getPAtkSpd() > 0 || npc.getStat().getMAtkSpd() > 0)
		{
			this.addComponentType(NpcInfoType.ATK_CAST_SPEED);
		}

		if (npc.getRunSpeed() > 0.0)
		{
			this.addComponentType(NpcInfoType.SPEED_MULTIPLIER);
		}

		if (npc.getLeftHandItem() > 0 || npc.getRightHandItem() > 0)
		{
			this.addComponentType(NpcInfoType.EQUIPPED);
		}

		if (npc.getTeam() != Team.NONE)
		{
			if (GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT != null && GeneralConfig.RED_TEAM_ABNORMAL_EFFECT != null)
			{
				this.addComponentType(NpcInfoType.ABNORMALS);
			}
			else
			{
				this.addComponentType(NpcInfoType.TEAM);
			}
		}

		if (npc.getDisplayEffect() > 0)
		{
			this.addComponentType(NpcInfoType.DISPLAY_EFFECT);
		}

		if (npc.isInsideZone(ZoneId.WATER) || npc.isFlying())
		{
			this.addComponentType(NpcInfoType.SWIM_OR_FLY);
		}

		if (npc.isFlying())
		{
			this.addComponentType(NpcInfoType.FLYING);
		}

		if (npc.getCloneObjId() > 0)
		{
			this.addComponentType(NpcInfoType.CLONE);
		}

		if (npc.getMaxHp() > 0L)
		{
			this.addComponentType(NpcInfoType.MAX_HP);
		}

		if (npc.getMaxMp() > 0)
		{
			this.addComponentType(NpcInfoType.MAX_MP);
		}

		if (npc.getCurrentHp() <= npc.getMaxHp())
		{
			this.addComponentType(NpcInfoType.CURRENT_HP);
		}

		if (npc.getCurrentMp() <= npc.getMaxMp())
		{
			this.addComponentType(NpcInfoType.CURRENT_MP);
		}

		if (npc.getTemplate().isUsingServerSideName())
		{
			this.addComponentType(NpcInfoType.NAME);
		}

		if (npc.getTemplate().isShowName())
		{
			this.addComponentType(NpcInfoType.SHOW_NAME);
		}

		if (npc.getTemplate().isUsingServerSideTitle() || npc.isMonster() && (NpcConfig.SHOW_NPC_LEVEL || NpcConfig.SHOW_NPC_AGGRESSION) || npc.isChampion() || npc.isTrap())
		{
			this.addComponentType(NpcInfoType.TITLE);
		}

		if (npc.getNameString() != null)
		{
			this.addComponentType(NpcInfoType.NAME_NPCSTRINGID);
		}

		if (npc.getTitleString() != null)
		{
			this.addComponentType(NpcInfoType.TITLE_NPCSTRINGID);
		}

		if (this._npc.getReputation() != 0)
		{
			this.addComponentType(NpcInfoType.REPUTATION);
		}

		if (!this._abnormalVisualEffects.isEmpty() || npc.isInvisible())
		{
			this.addComponentType(NpcInfoType.ABNORMALS);
		}

		if (npc.getEnchantEffect() > 0)
		{
			this.addComponentType(NpcInfoType.ENCHANT);
		}

		if (npc.getTransformationDisplayId() > 0)
		{
			this.addComponentType(NpcInfoType.TRANSFORMATION);
		}

		if (npc.isShowSummonAnimation())
		{
			this.addComponentType(NpcInfoType.SUMMONED);
		}

		if (npc.getClanId() > 0 && npc.isTargetable() && npc.isShowName())
		{
			Clan clan = ClanTable.getInstance().getClan(npc.getClanId());
			if (clan != null && (npc instanceof Doppelganger || npc.getTemplate().getId() == 34156 || !npc.isMonster() && npc.isInsideZone(ZoneId.PEACE)))
			{
				this._clanId = clan.getId();
				this._clanCrest = clan.getCrestId();
				this._clanLargeCrest = clan.getCrestLargeId();
				this._allyCrest = clan.getAllyCrestId();
				this._allyId = clan.getAllyId();
				this.addComponentType(NpcInfoType.CLAN);
			}
		}

		this.addComponentType(NpcInfoType.PET_EVOLUTION_ID);
		if (npc.getPvpFlag() > 0)
		{
			this.addComponentType(NpcInfoType.PVP_FLAG);
		}

		if (npc.isInCombat())
		{
			this._statusMask |= 1;
		}

		if (npc.isDead())
		{
			this._statusMask |= 2;
		}

		if (npc.isTargetable())
		{
			this._statusMask |= 4;
		}

		if (npc.isShowName())
		{
			this._statusMask |= 8;
		}

		if (this._statusMask != 0)
		{
			this.addComponentType(NpcInfoType.VISUAL_STATE);
		}
	}

	@Override
	protected byte[] getMasks()
	{
		return this._masks;
	}

	@Override
	protected void onNewMaskAdded(NpcInfoType component)
	{
		this.calcBlockSize(this._npc, component);
	}

	private void calcBlockSize(Npc npc, NpcInfoType type)
	{
		switch (type)
		{
			case ATTACKABLE:
			case RELATIONS:
				this._initSize = this._initSize + type.getBlockLength();
				break;
			case TITLE:
				this._initSize = this._initSize + type.getBlockLength() + npc.getTitle().length() * 2;
				break;
			case NAME:
				this._blockSize = this._blockSize + type.getBlockLength() + npc.getName().length() * 2;
				break;
			default:
				this._blockSize = this._blockSize + type.getBlockLength();
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (!this._npc.isDecayed())
		{
			String[] localisation = null;
			if (MultilingualSupportConfig.MULTILANG_ENABLE)
			{
				Player player = client.getPlayer();
				if (player != null)
				{
					String lang = player.getLang();
					if (lang != null && !lang.equals("en"))
					{
						localisation = NpcNameLocalisationData.getInstance().getLocalisation(lang, this._npc.getId());
						if (localisation != null)
						{
							if (!this.containsMask(NpcInfoType.NAME))
							{
								this.addComponentType(NpcInfoType.NAME);
							}

							this._blockSize = this._blockSize - this._npc.getName().length() * 2;
							this._blockSize = this._blockSize + localisation[0].length() * 2;
							if (!localisation[1].equals(""))
							{
								if (!this.containsMask(NpcInfoType.TITLE))
								{
									this.addComponentType(NpcInfoType.TITLE);
								}

								String title = this._npc.getTitle();
								this._initSize = this._initSize - title.length() * 2;
								if (title.equals(""))
								{
									this._initSize = this._initSize + localisation[1].length() * 2;
								}
								else
								{
									this._initSize = this._initSize + title.replace(NpcData.getInstance().getTemplate(this._npc.getId()).getTitle(), localisation[1]).length() * 2;
								}
							}
						}
					}
				}
			}

			ServerPackets.NPC_INFO.writeId(this, buffer);
			buffer.writeInt(this._npc.getObjectId());
			buffer.writeByte(this._npc.isShowSummonAnimation() ? 2 : 0);
			buffer.writeShort(40);
			buffer.writeBytes(this._masks);
			buffer.writeByte(this._initSize);
			if (this.containsMask(NpcInfoType.ATTACKABLE))
			{
				buffer.writeByte(this._npc.isAttackable() && !(this._npc instanceof Guard));
			}

			if (this.containsMask(NpcInfoType.RELATIONS))
			{
				buffer.writeLong(0L);
			}

			if (this.containsMask(NpcInfoType.TITLE))
			{
				String title = this._npc.getTitle();
				if (localisation != null && !localisation[1].equals(""))
				{
					if (title.equals(""))
					{
						title = localisation[1];
					}
					else
					{
						title = title.replace(NpcData.getInstance().getTemplate(this._npc.getId()).getTitle(), localisation[1]);
					}
				}

				buffer.writeString(title);
			}

			buffer.writeShort(this._blockSize);
			if (this.containsMask(NpcInfoType.ID))
			{
				buffer.writeInt(this._npc.getTemplate().getDisplayId() + 1000000);
			}

			if (this.containsMask(NpcInfoType.POSITION))
			{
				buffer.writeInt(this._npc.getX());
				buffer.writeInt(this._npc.getY());
				buffer.writeInt(this._npc.getZ());
			}

			if (this.containsMask(NpcInfoType.HEADING))
			{
				buffer.writeInt(this._npc.getHeading());
			}

			if (this.containsMask(NpcInfoType.VEHICLE_ID))
			{
				buffer.writeInt(0);
			}

			if (this.containsMask(NpcInfoType.ATK_CAST_SPEED))
			{
				buffer.writeInt(this._npc.getPAtkSpd());
				buffer.writeInt(this._npc.getMAtkSpd());
			}

			if (this.containsMask(NpcInfoType.SPEED_MULTIPLIER))
			{
				buffer.writeFloat((float) this._npc.getStat().getMovementSpeedMultiplier());
				buffer.writeFloat((float) this._npc.getStat().getAttackSpeedMultiplier());
			}

			if (this.containsMask(NpcInfoType.EQUIPPED))
			{
				buffer.writeInt(this._npc.getRightHandItem());
				buffer.writeInt(0);
				buffer.writeInt(this._npc.getLeftHandItem());
			}

			if (this.containsMask(NpcInfoType.ALIVE))
			{
				buffer.writeByte(!this._npc.isDead());
			}

			if (this.containsMask(NpcInfoType.RUNNING))
			{
				buffer.writeByte(this._npc.isRunning());
			}

			if (this.containsMask(NpcInfoType.SWIM_OR_FLY))
			{
				buffer.writeByte(this._npc.isInsideZone(ZoneId.WATER) ? 1 : (this._npc.isFlying() ? 2 : 0));
			}

			if (this.containsMask(NpcInfoType.TEAM))
			{
				buffer.writeByte(this._npc.getTeam().getId());
			}

			if (this.containsMask(NpcInfoType.ENCHANT))
			{
				buffer.writeInt(this._npc.getEnchantEffect());
			}

			if (this.containsMask(NpcInfoType.FLYING))
			{
				buffer.writeInt(this._npc.isFlying());
			}

			if (this.containsMask(NpcInfoType.CLONE))
			{
				buffer.writeInt(this._npc.getCloneObjId());
			}

			if (this.containsMask(NpcInfoType.PET_EVOLUTION_ID))
			{
				buffer.writeInt(0);
			}

			if (this.containsMask(NpcInfoType.DISPLAY_EFFECT))
			{
				buffer.writeInt(this._npc.getDisplayEffect());
			}

			if (this.containsMask(NpcInfoType.TRANSFORMATION))
			{
				buffer.writeInt(this._npc.getTransformationDisplayId());
			}

			if (this.containsMask(NpcInfoType.CURRENT_HP))
			{
				buffer.writeLong((long) this._npc.getCurrentHp());
			}

			if (this.containsMask(NpcInfoType.CURRENT_MP))
			{
				buffer.writeInt((int) this._npc.getCurrentMp());
			}

			if (this.containsMask(NpcInfoType.MAX_HP))
			{
				buffer.writeLong(this._npc.getMaxHp());
			}

			if (this.containsMask(NpcInfoType.MAX_MP))
			{
				buffer.writeInt(this._npc.getMaxMp());
			}

			if (this.containsMask(NpcInfoType.SUMMONED))
			{
				buffer.writeByte(0);
			}

			if (this.containsMask(NpcInfoType.FOLLOW_INFO))
			{
				buffer.writeInt(0);
				buffer.writeInt(0);
			}

			if (this.containsMask(NpcInfoType.NAME))
			{
				buffer.writeString(localisation != null ? localisation[0] : this._npc.getName());
			}

			if (this.containsMask(NpcInfoType.NAME_NPCSTRINGID))
			{
				NpcStringId nameString = this._npc.getNameString();
				buffer.writeInt(nameString != null ? nameString.getId() : -1);
			}

			if (this.containsMask(NpcInfoType.TITLE_NPCSTRINGID))
			{
				NpcStringId titleString = this._npc.getTitleString();
				buffer.writeInt(titleString != null ? titleString.getId() : -1);
			}

			if (this.containsMask(NpcInfoType.PVP_FLAG))
			{
				buffer.writeByte(this._npc.getPvpFlag());
			}

			if (this.containsMask(NpcInfoType.REPUTATION))
			{
				buffer.writeInt(this._npc.getReputation());
			}

			if (this.containsMask(NpcInfoType.CLAN))
			{
				buffer.writeInt(this._clanId);
				buffer.writeInt(this._clanCrest);
				buffer.writeInt(this._clanLargeCrest);
				buffer.writeInt(this._allyId);
				buffer.writeInt(this._allyCrest);
			}

			if (this.containsMask(NpcInfoType.VISUAL_STATE))
			{
				buffer.writeByte(this._statusMask);
			}

			if (this.containsMask(NpcInfoType.SHOW_NAME))
			{
				buffer.writeByte(1);
			}

			if (this.containsMask(NpcInfoType.ABNORMALS))
			{
				buffer.writeInt(0);
				Team team = GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT != null && GeneralConfig.RED_TEAM_ABNORMAL_EFFECT != null ? this._npc.getTeam() : Team.NONE;
				buffer.writeShort(this._abnormalVisualEffects.size() + (this._npc.isInvisible() ? 1 : 0) + (team != Team.NONE ? 1 : 0));

				for (AbnormalVisualEffect abnormalVisualEffect : this._abnormalVisualEffects)
				{
					buffer.writeShort(abnormalVisualEffect.getClientId());
				}

				if (this._npc.isInvisible())
				{
					buffer.writeShort(AbnormalVisualEffect.STEALTH.getClientId());
				}

				if (team == Team.BLUE)
				{
					if (GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT != null)
					{
						buffer.writeShort(GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT.getClientId());
					}
				}
				else if (team == Team.RED && GeneralConfig.RED_TEAM_ABNORMAL_EFFECT != null)
				{
					buffer.writeShort(GeneralConfig.RED_TEAM_ABNORMAL_EFFECT.getClientId());
				}
			}
		}
	}
}
