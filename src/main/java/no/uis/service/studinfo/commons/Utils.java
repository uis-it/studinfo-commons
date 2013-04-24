package no.uis.service.studinfo.commons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Utils {

  private static final char ID_TOKEN_SEPARATOR = '_';
  
  private Utils() {
  }
  
  public static String formatTokens(Object... tokens) {
    StringBuilder sb = new StringBuilder();

    for (Object o : tokens) {
      if (sb.length() > 0) {
        sb.append(ID_TOKEN_SEPARATOR);
      }
      sb.append(o);
    }
    return sb.toString();
  }

  public static Collection<String> createStringArray(Object value, Collection<String> target, ToString ts) {
    if (!(value instanceof Collection)) {
      return Collections.emptyList();
    }
    Collection<?> coll = (Collection<?>)value;
    if (target == null) {
      target = new ArrayList<String>(coll.size());
    }
    for (Object o : coll) {
      String sval = ts == null ? String.valueOf(o) : ts.toString(o);
      if (sval != null) {
        target.add(sval);
      }
    }
    return target;
  }
  

}
