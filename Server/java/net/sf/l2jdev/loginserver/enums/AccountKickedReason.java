package net.sf.l2jdev.loginserver.enums;

public enum AccountKickedReason {
   REASON_DATA_STEALER(1),
   REASON_GENERIC_VIOLATION(8),
   REASON_7_DAYS_SUSPENDED(16),
   REASON_PERMANENTLY_BANNED(32);

   private final int _code;

   private AccountKickedReason(int code) {
      this._code = code;
   }

   public int getCode() {
      return this._code;
   }
}
