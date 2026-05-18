package net.sf.l2jdev.loginserver.network.gameserverpackets;

import net.sf.l2jdev.commons.network.base.BaseReadablePacket;
import net.sf.l2jdev.loginserver.GameServerThread;
import net.sf.l2jdev.loginserver.LoginController;
import net.sf.l2jdev.loginserver.SessionKey;
import net.sf.l2jdev.loginserver.network.loginserverpackets.PlayerAuthResponse;

public class PlayerAuthRequest extends BaseReadablePacket {
   public PlayerAuthRequest(byte[] decrypt, GameServerThread server) {
      super(decrypt);
      this.readByte();
      String account = this.readString();
      int playKey1 = this.readInt();
      int playKey2 = this.readInt();
      int loginKey1 = this.readInt();
      int loginKey2 = this.readInt();
      SessionKey sessionKey = new SessionKey(loginKey1, loginKey2, playKey1, playKey2);
      SessionKey storedKey = LoginController.getInstance().getKeyForAccount(account);
      if (storedKey != null && storedKey.equals(sessionKey)) {
         LoginController.getInstance().removeAuthedLoginClient(account);
         server.sendPacket(new PlayerAuthResponse(account, true));
      } else {
         server.sendPacket(new PlayerAuthResponse(account, false));
      }
   }
}
