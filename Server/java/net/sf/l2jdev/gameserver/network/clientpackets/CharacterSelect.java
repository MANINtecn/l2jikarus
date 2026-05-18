package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.custom.DualboxCheckConfig;
import net.sf.l2jdev.gameserver.config.custom.FactionSystemConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflinePlayConfig;
import net.sf.l2jdev.gameserver.data.holders.TimedHuntingZoneHolder;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.data.sql.OfflinePlayTable;
import net.sf.l2jdev.gameserver.data.xml.AdminData;
import net.sf.l2jdev.gameserver.data.xml.SecondaryAuthData;
import net.sf.l2jdev.gameserver.managers.AntiFeedManager;
import net.sf.l2jdev.gameserver.managers.MapRegionManager;
import net.sf.l2jdev.gameserver.managers.PunishmentManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportWhereType;
import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerSelect;
import net.sf.l2jdev.gameserver.model.events.returns.TerminateReturn;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentAffect;
import net.sf.l2jdev.gameserver.model.punishment.PunishmentType;
import net.sf.l2jdev.gameserver.model.variables.PlayerVariables;
import net.sf.l2jdev.gameserver.network.ConnectionState;
import net.sf.l2jdev.gameserver.network.Disconnection;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.holders.CharacterInfoHolder;
import net.sf.l2jdev.gameserver.network.serverpackets.CharSelected;
import net.sf.l2jdev.gameserver.network.serverpackets.LeaveWorld;
import net.sf.l2jdev.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerClose;

public class CharacterSelect extends ClientPacket
{
	protected static final Logger LOGGER_ACCOUNTING = Logger.getLogger("accounting");
	protected int _charSlot;
	protected int _unk1;
	protected int _unk2;
	protected int _unk3;
	protected int _unk4;

	@Override
	protected void readImpl()
	{
		this._charSlot = this.readInt();
		this._unk1 = this.readShort();
		this._unk2 = this.readInt();
		this._unk3 = this.readInt();
		this._unk4 = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = this.getClient();
		if (client.getFloodProtectors().canSelectCharacter())
		{
			if (SecondaryAuthData.getInstance().isEnabled() && !client.getSecondaryAuth().isAuthed())
			{
				client.getSecondaryAuth().openDialog();
			}
			else
			{
				if (client.getPlayerLock().tryLock())
				{
					try
					{
						if (client.getPlayer() == null)
						{
							CharacterInfoHolder info = client.getCharSelection(this._charSlot);
							if (info == null)
							{
								return;
							}

							Player player = World.getInstance().getPlayer(info.getObjectId());
							if (player != null)
							{
								if (OfflinePlayConfig.RESTORE_AUTO_PLAY_OFFLINERS && player.isAutoPlaying())
								{
									OfflinePlayTable.getInstance().removeOfflinePlay(player);
								}

								Disconnection.of(player).storeAndDelete();
							}

							if (PunishmentManager.getInstance().hasPunishment(info.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.BAN) || PunishmentManager.getInstance().hasPunishment(client.getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.BAN) || PunishmentManager.getInstance().hasPunishment(client.getIp(), PunishmentAffect.IP, PunishmentType.BAN) || (info.getAccessLevel() < 0))
							{
								client.close(ServerClose.STATIC_PACKET);
								return;
							}

							if (client.getCharSelection(this._charSlot).getAccessLevel() < 100)
							{
								if (player != null && player.hasPremiumStatus() && DualboxCheckConfig.DUALBOX_CHECK_MAX_OFFLINEPLAY_PREMIUM_PER_IP > 0 && !AntiFeedManager.getInstance().tryAddPlayer(4, player, DualboxCheckConfig.DUALBOX_CHECK_MAX_OFFLINEPLAY_PREMIUM_PER_IP))
								{
									NpcHtmlMessage msg = new NpcHtmlMessage();
									msg.setFile(null, "data/html/mods/IPRestriction.htm");
									msg.replace("%max%", String.valueOf(AntiFeedManager.getInstance().getLimit(client, DualboxCheckConfig.DUALBOX_CHECK_MAX_OFFLINEPLAY_PREMIUM_PER_IP)));
									client.sendPacket(msg);
									return;
								}

								if (DualboxCheckConfig.DUALBOX_CHECK_MAX_PLAYERS_PER_IP > 0 && !AntiFeedManager.getInstance().tryAddClient(0, client, DualboxCheckConfig.DUALBOX_CHECK_MAX_PLAYERS_PER_IP))
								{
									NpcHtmlMessage msg = new NpcHtmlMessage();
									msg.setFile(null, "data/html/mods/IPRestriction.htm");
									msg.replace("%max%", String.valueOf(AntiFeedManager.getInstance().getLimit(client, DualboxCheckConfig.DUALBOX_CHECK_MAX_PLAYERS_PER_IP)));
									client.sendPacket(msg);
									return;
								}
							}

							if (FactionSystemConfig.FACTION_SYSTEM_ENABLED && FactionSystemConfig.FACTION_BALANCE_ONLINE_PLAYERS)
							{
								if (info.isGood() && World.getInstance().getAllGoodPlayers().size() >= World.getInstance().getAllEvilPlayers().size() + FactionSystemConfig.FACTION_BALANCE_PLAYER_EXCEED_LIMIT)
								{
									NpcHtmlMessage msg = new NpcHtmlMessage();
									msg.setFile(null, "data/html/mods/Faction/ExceededOnlineLimit.htm");
									msg.replace("%more%", FactionSystemConfig.FACTION_GOOD_TEAM_NAME);
									msg.replace("%less%", FactionSystemConfig.FACTION_EVIL_TEAM_NAME);
									client.sendPacket(msg);
									return;
								}

								if (info.isEvil() && World.getInstance().getAllEvilPlayers().size() >= World.getInstance().getAllGoodPlayers().size() + FactionSystemConfig.FACTION_BALANCE_PLAYER_EXCEED_LIMIT)
								{
									NpcHtmlMessage msg = new NpcHtmlMessage();
									msg.setFile(null, "data/html/mods/Faction/ExceededOnlineLimit.htm");
									msg.replace("%more%", FactionSystemConfig.FACTION_EVIL_TEAM_NAME);
									msg.replace("%less%", FactionSystemConfig.FACTION_GOOD_TEAM_NAME);
									client.sendPacket(msg);
									return;
								}
							}

							Player cha = client.load(this._charSlot);
							if (cha == null)
							{
								return;
							}

							CharInfoTable.getInstance().addName(cha);
							if (cha.isGM() && GeneralConfig.GM_STARTUP_INVISIBLE && AdminData.getInstance().hasAccess("admin_invisible", cha.getAccessLevel()))
							{
								cha.setInvisible(true);
							}

							PlayerVariables vars = cha.getVariables();
							String restore = vars.getString("RESTORE_LOCATION", "");
							if (!restore.isEmpty())
							{
								String[] split = restore.split(";");
								cha.setXYZ(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
								vars.remove("RESTORE_LOCATION");
							}
							else
							{
								TimedHuntingZoneHolder zone = cha.getTimedHuntingZone();
								if (zone != null)
								{
									Location exit = zone.getExitLocation();
									cha.setXYZ(exit != null ? exit : MapRegionManager.getInstance().getTeleToLocation(cha, TeleportWhereType.TOWN));
									vars.remove("LAST_HUNTING_ZONE_ID");
								}
							}

							cha.setClient(client);
							client.setPlayer(cha);
							cha.setOnlineStatus(true, true);
							if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_SELECT, Containers.Players()))
							{
								TerminateReturn terminate = EventDispatcher.getInstance().notifyEvent(new OnPlayerSelect(cha, cha.getObjectId(), cha.getName(), client), Containers.Players(), TerminateReturn.class);
								if (terminate != null && terminate.terminate())
								{
									Disconnection.of(cha).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
									return;
								}
							}

							client.setConnectionState(ConnectionState.ENTERING);
							client.sendPacket(new CharSelected(cha, client.getSessionId().playOkID1));
						}
					}
					finally
					{
						client.getPlayerLock().unlock();
					}

					LOGGER_ACCOUNTING.info("Logged in, " + client);
				}
			}
		}
	}
}
