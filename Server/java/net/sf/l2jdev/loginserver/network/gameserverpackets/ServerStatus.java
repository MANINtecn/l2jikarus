package net.sf.l2jdev.loginserver.network.gameserverpackets;

import java.util.logging.Logger;
import net.sf.l2jdev.commons.network.base.BaseReadablePacket;
import net.sf.l2jdev.loginserver.GameServerTable;
import net.sf.l2jdev.loginserver.GameServerThread;

public class ServerStatus extends BaseReadablePacket {
   protected static final Logger LOGGER = Logger.getLogger(ServerStatus.class.getName());
   public static final int SERVER_LIST_STATUS = 1;
   public static final int SERVER_TYPE = 2;
   public static final int SERVER_LIST_SQUARE_BRACKET = 3;
   public static final int MAX_PLAYERS = 4;
   public static final int TEST_SERVER = 5;
   public static final int SERVER_AGE = 6;
   public static final int STATUS_AUTO = 0;
   public static final int STATUS_GOOD = 1;
   public static final int STATUS_NORMAL = 2;
   public static final int STATUS_FULL = 3;
   public static final int STATUS_DOWN = 4;
   public static final int STATUS_GM_ONLY = 5;
   public static final int SERVER_NORMAL = 1;
   public static final int SERVER_RELAX = 2;
   public static final int SERVER_TEST = 4;
   public static final int SERVER_NOLABEL = 8;
   public static final int SERVER_CREATION_RESTRICTED = 16;
   public static final int SERVER_EVENT = 32;
   public static final int SERVER_FREE = 64;
   public static final int SERVER_AGE_ALL = 0;
   public static final int SERVER_AGE_15 = 15;
   public static final int SERVER_AGE_18 = 18;
   public static final int ON = 1;
   public static final int OFF = 0;

   public ServerStatus(byte[] decrypt, GameServerThread server) {
      super(decrypt);
      this.readByte();
      GameServerTable.GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(server.getServerId());
      if (gsi != null) {
         int size = this.readInt();

         for (int i = 0; i < size; i++) {
            int type = this.readInt();
            int value = this.readInt();
            switch (type) {
               case 1:
                  gsi.setStatus(value);
                  break;
               case 2:
                  gsi.setServerType(value);
                  break;
               case 3:
                  gsi.setShowingBrackets(value == 1);
                  break;
               case 4:
                  gsi.setMaxPlayers(value);
               case 5:
               default:
                  break;
               case 6:
                  gsi.setAgeLimit(value);
            }
         }
      }
   }
}
