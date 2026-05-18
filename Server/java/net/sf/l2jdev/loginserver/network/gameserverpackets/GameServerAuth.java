package net.sf.l2jdev.loginserver.network.gameserverpackets;

import java.util.Arrays;
import java.util.logging.Logger;
import net.sf.l2jdev.commons.network.base.BaseReadablePacket;
import net.sf.l2jdev.loginserver.GameServerTable;
import net.sf.l2jdev.loginserver.GameServerThread;
import net.sf.l2jdev.loginserver.config.LoginConfig;
import net.sf.l2jdev.loginserver.network.GameServerPacketHandler;
import net.sf.l2jdev.loginserver.network.loginserverpackets.AuthResponse;

public class GameServerAuth extends BaseReadablePacket {
   protected static final Logger LOGGER = Logger.getLogger(GameServerAuth.class.getName());
   GameServerThread _server;
   private final byte[] _hexId;
   private final int _desiredId;
   private final boolean _acceptAlternativeId;
   private final int _maxPlayers;
   private final int _port;
   private final String[] _hosts;

   public GameServerAuth(byte[] decrypt, GameServerThread server) {
      super(decrypt);
      this.readByte();
      this._server = server;
      this._desiredId = this.readByte();
      this._acceptAlternativeId = this.readByte() != 0;
      this.readByte();
      this._port = this.readShort();
      this._maxPlayers = this.readInt();
      int size = this.readInt();
      this._hexId = this.readBytes(size);
      size = 2 * this.readInt();
      this._hosts = new String[size];

      for (int i = 0; i < size; i++) {
         this._hosts[i] = this.readString();
      }

      if (this.handleRegProcess()) {
         AuthResponse ar = new AuthResponse(server.getGameServerInfo().getId());
         server.sendPacket(ar);
         server.setLoginConnectionState(GameServerPacketHandler.GameServerState.AUTHED);
      }
   }

   private boolean handleRegProcess() {
      GameServerTable gameServerTable = GameServerTable.getInstance();
      int id = this._desiredId;
      byte[] hexId = this._hexId;
      GameServerTable.GameServerInfo gsi = gameServerTable.getRegisteredGameServerById(id);
      if (gsi != null) {
         if (Arrays.equals(gsi.getHexId(), hexId)) {
            synchronized (gsi) {
               if (gsi.isAuthed()) {
                  this._server.forceClose(7);
                  return false;
               }

               this._server.attachGameServerInfo(gsi, this._port, this._hosts, this._maxPlayers);
            }
         } else {
            if (!LoginConfig.ACCEPT_NEW_GAMESERVER || !this._acceptAlternativeId) {
               this._server.forceClose(3);
               return false;
            }

            gsi = new GameServerTable.GameServerInfo(id, hexId, this._server);
            if (!gameServerTable.registerWithFirstAvailableId(gsi)) {
               this._server.forceClose(5);
               return false;
            }

            this._server.attachGameServerInfo(gsi, this._port, this._hosts, this._maxPlayers);
            gameServerTable.registerServerOnDB(gsi);
         }
      } else {
         if (!LoginConfig.ACCEPT_NEW_GAMESERVER) {
            this._server.forceClose(3);
            return false;
         }

         gsi = new GameServerTable.GameServerInfo(id, hexId, this._server);
         if (!gameServerTable.register(id, gsi)) {
            this._server.forceClose(4);
            return false;
         }

         this._server.attachGameServerInfo(gsi, this._port, this._hosts, this._maxPlayers);
         gameServerTable.registerServerOnDB(gsi);
      }

      return true;
   }
}
