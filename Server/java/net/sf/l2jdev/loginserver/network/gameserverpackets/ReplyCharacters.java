package net.sf.l2jdev.loginserver.network.gameserverpackets;

import net.sf.l2jdev.commons.network.base.BaseReadablePacket;
import net.sf.l2jdev.loginserver.GameServerThread;
import net.sf.l2jdev.loginserver.LoginController;

public class ReplyCharacters extends BaseReadablePacket {
   public ReplyCharacters(byte[] decrypt, GameServerThread server) {
      super(decrypt);
      this.readByte();
      String account = this.readString();
      int chars = this.readByte();
      int charsToDel = this.readByte();
      long[] charsList = new long[charsToDel];

      for (int i = 0; i < charsToDel; i++) {
         charsList[i] = this.readLong();
      }

      LoginController.getInstance().setCharactersOnServer(account, chars, charsList, server.getServerId());
   }
}
