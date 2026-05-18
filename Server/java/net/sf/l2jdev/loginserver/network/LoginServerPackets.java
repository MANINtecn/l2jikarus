package net.sf.l2jdev.loginserver.network;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;

public enum LoginServerPackets {
   INIT(0),
   LOGIN_FAIL(1),
   ACCOUNT_KICKED(2),
   LOGIN_OK(3),
   SERVER_LIST(4),
   PLAY_FAIL(6),
   PLAY_OK(7),
   PI_AGREEMENT_CHECK(17),
   PI_AGREEMENT_ACK(18),
   GG_AUTH(11),
   LOGIN_OPT_FAIL(13);

   private final int _id;

   private LoginServerPackets(int id) {
      this._id = id;
   }

   public void writeId(BaseWritablePacket packet) {
      packet.writeByte(this._id);
   }
}
