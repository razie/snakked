package razie;

import razie.base.AttrAccess;
import razie.base.AttrAccessImpl;

/** TODO remove when Java friendliness not needed */
public class JAA {
  public static AttrAccess of (Object... args) {
     return new AttrAccessImpl (args);
  }
}
