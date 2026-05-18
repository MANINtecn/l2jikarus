package net.sf.l2jdev.loginserver.network.loginserverpackets;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;

public class InitLS extends BaseWritablePacket {
   public InitLS(byte[] publickey) {
      this.writeByte(0);
      this.writeInt(262);
      this.writeInt(publickey.length);
      this.writeBytes(publickey);
   }
}
