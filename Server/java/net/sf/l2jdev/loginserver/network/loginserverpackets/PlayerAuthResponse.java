package net.sf.l2jdev.loginserver.network.loginserverpackets;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;

public class PlayerAuthResponse extends BaseWritablePacket {
   public PlayerAuthResponse(String account, boolean response) {
      this.writeByte(3);
      this.writeString(account);
      this.writeByte(response ? 1 : 0);
   }
}
