/*
 Copyright 2013 University of Stavanger, Norway

 Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package no.uis.studinfo.commons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import no.uis.studinfo.convert.StringConverter;
import no.uis.studinfo.convert.StringConverterUtil;

/**
 * Collection of utility functions. 
 * Bad habit :-(  
 */
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

  public static Collection<String> createStringArray(Object value, Collection<String> target, StringConverter converter) {
    if (!(value instanceof Collection)) {
      return Collections.emptyList();
    }
    Collection<?> coll = (Collection<?>)value;
    Collection<String> t = target;
    if (t == null) {
      t = new ArrayList<String>(coll.size());
    }
    for (Object o : coll) {
      String sval = converter == null ? StringConverterUtil.convert(o) : converter.convertToString(o);
      if (sval != null) {
        t.add(sval);
      }
    }
    return t;
  }
}
