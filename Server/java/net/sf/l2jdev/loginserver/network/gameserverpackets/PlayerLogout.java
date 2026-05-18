package net.sf.l2jdev.loginserver.network.gameserverpackets;

import java.util.logging.Logger;
import net.sf.l2jdev.commons.network.base.BaseReadablePacket;
import net.sf.l2jdev.loginserver.GameServerThread;

public class PlayerLogout extends BaseReadablePacket {
   protected static final Logger LOGGER = Logger.getLogger(PlayerLogout.class.getName());

   public PlayerLogout(byte[] decrypt, GameServerThread server) {
      super(decrypt);
      this.readByte();
      String account = this.readString();
      server.removeAccountOnGameServer(account);
   }
}
