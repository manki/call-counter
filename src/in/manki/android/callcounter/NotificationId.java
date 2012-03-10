package in.manki.android.callcounter;

public enum NotificationId {

  LOW_BALANCE(0);

  private final int id;

  private NotificationId(int id) {
    this.id = id;
  }

  public int get() {
    return id;
  }
}
