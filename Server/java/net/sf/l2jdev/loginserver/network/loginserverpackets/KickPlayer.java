package net.sf.l2jdev.loginserver.network.loginserverpackets;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;

public class KickPlayer extends BaseWritablePacket {
   public KickPlayer(String account) {
      this.writeByte(4);
      this.writeString(account);
   }
}
