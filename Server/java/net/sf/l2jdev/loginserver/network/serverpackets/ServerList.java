package net.sf.l2jdev.loginserver.network.serverpackets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.loginserver.GameServerTable;
import net.sf.l2jdev.loginserver.network.LoginClient;

public class ServerList extends LoginServerPacket {
   protected static final Logger LOGGER = Logger.getLogger(ServerList.class.getName());
   private final List<ServerList.ServerData> _servers = new ArrayList<>(GameServerTable.getInstance().getRegisteredGameServers().size());
   private final int _lastServer;
   private Map<Integer, Integer> _charsOnServers;

   public ServerList(LoginClient client) {
      this._lastServer = client.getLastServer();

      for (GameServerTable.GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values()) {
         this._servers.add(new ServerList.ServerData(client, gsi));
      }

      for (int i = 0; this._charsOnServers == null && i++ < 10; this._charsOnServers = client.getCharsOnServ()) {
         try {
            Thread.sleep(50L);
         } catch (InterruptedException var4) {
         }
      }
   }

   @Override
   protected void writeImpl(LoginClient client, WritableBuffer buffer) {
      buffer.writeByte(4);
      buffer.writeByte(this._servers.size());
      buffer.writeByte(this._lastServer);

      for (ServerList.ServerData server : this._servers) {
         buffer.writeByte(server._serverId);
         buffer.writeByte(server._ip[0] & 255);
         buffer.writeByte(server._ip[1] & 255);
         buffer.writeByte(server._ip[2] & 255);
         buffer.writeByte(server._ip[3] & 255);
         buffer.writeInt(server._port);
         buffer.writeByte(server._ageLimit);
         buffer.writeByte(server._pvp ? 1 : 0);
         buffer.writeShort(server._currentPlayers);
         buffer.writeShort(server._maxPlayers);
         buffer.writeByte(server._status == 4 ? 0 : 1);
         buffer.writeInt(server._serverType);
         buffer.writeByte(server._brackets ? 1 : 0);
      }

      buffer.writeShort(164);
      if (this._charsOnServers != null) {
         for (ServerList.ServerData server : this._servers) {
            buffer.writeByte(server._serverId);
            buffer.writeByte(this._charsOnServers.getOrDefault(server._serverId, 0));
         }
      }
   }

   class ServerData {
      protected byte[] _ip;
      protected int _port;
      protected int _ageLimit;
      protected boolean _pvp;
      protected int _currentPlayers;
      protected int _maxPlayers;
      protected boolean _brackets;
      protected boolean _clock;
      protected int _status;
      protected int _serverId;
      protected int _serverType;

      ServerData(LoginClient client, GameServerTable.GameServerInfo gsi) {
         Objects.requireNonNull(ServerList.this);
         super();

         try {
            this._ip = InetAddress.getByName(gsi.getServerAddress(InetAddress.getByName(client.getIp()))).getAddress();
         } catch (UnknownHostException var5) {
            ServerList.LOGGER.warning(this.getClass().getSimpleName() + ": " + var5.getMessage());
            this._ip = new byte[4];
            this._ip[0] = 127;
            this._ip[1] = 0;
            this._ip[2] = 0;
            this._ip[3] = 1;
         }

         this._port = gsi.getPort();
         this._pvp = gsi.isPvp();
         this._serverType = gsi.getServerType();
         this._currentPlayers = gsi.getCurrentPlayerCount();
         this._maxPlayers = gsi.getMaxPlayers();
         this._ageLimit = 0;
         this._brackets = gsi.isShowingBrackets();
         this._status = client.getAccessLevel() >= 0 && (gsi.getStatus() != 5 || client.getAccessLevel() > 0) ? gsi.getStatus() : 4;
         this._serverId = gsi.getId();
      }
   }
}
