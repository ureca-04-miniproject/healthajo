package healthajo.jdbc.table;

import healthajo.jdbc.core.Column;
import healthajo.jdbc.core.TableBase;
import java.time.LocalDateTime;

public class TReservation extends TableBase {
  public static final TReservation RESERVATION = new TReservation();

  public final Column<Long>      ID                 = new Column<>(getPrefix(), "id",                 Long.class);
  public final Column<Long>      USER_ID            = new Column<>(getPrefix(), "user_id",            Long.class);
  public final Column<Long>      SESSION_ID         = new Column<>(getPrefix(), "session_id",         Long.class);
  public final Column<Long>      PROGRAM_ID         = new Column<>(getPrefix(), "program_id",         Long.class);
  public final Column<Long>      MEMBERSHIP_ID      = new Column<>(getPrefix(), "membership_id",      Long.class);
  public final Column<String>    STATUS             = new Column<>(getPrefix(), "status",             String.class);
  public final Column<String>    CANCELLED_BY       = new Column<>(getPrefix(), "cancelled_by",       String.class);
  public final Column<LocalDateTime> RESERVED_AT        = new Column<>(getPrefix(), "reserved_at",        LocalDateTime.class);
  public final Column<LocalDateTime> CANCELLED_AT       = new Column<>(getPrefix(), "cancelled_at",       LocalDateTime.class);
  public final Column<String>    ATTENDANCE_STATUS  = new Column<>(getPrefix(), "attendance_status",  String.class);
  public final Column<LocalDateTime> ATTENDED_AT        = new Column<>(getPrefix(), "attended_at",        LocalDateTime.class);
  public final Column<String>    ATTENDED_BY        = new Column<>(getPrefix(), "attended_by",        String.class);

  private TReservation() {
    super("reservations");
  }

  private TReservation(String alias) {
    super("reservations", alias);
  }

  @Override
  public TReservation as(String alias) {
    return new TReservation(alias);
  }
}
