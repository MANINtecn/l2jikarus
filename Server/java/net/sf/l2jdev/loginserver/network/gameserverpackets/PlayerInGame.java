package net.sf.l2jdev.loginserver.network.gameserverpackets;

import net.sf.l2jdev.commons.network.base.BaseReadablePacket;
import net.sf.l2jdev.loginserver.GameServerThread;

public class PlayerInGame extends BaseReadablePacket {
   public PlayerInGame(byte[] decrypt, GameServerThread server) {
      super(decrypt);
      this.readByte();
      int size = this.readShort();

      for (int i = 0; i < size; i++) {
         String account = this.readString();
         server.addAccountOnGameServer(account);
      }
   }
}
