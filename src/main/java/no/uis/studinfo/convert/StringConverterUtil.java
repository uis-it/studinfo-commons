/*
 Copyright 2010-2013 University of Stavanger, Norway

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

package no.uis.studinfo.convert;

import java.util.HashMap;
import java.util.Map;

/**
 * Facilitates conversion from objects to string.
 * You can register string converters for a given class which are use by this utility.
 */
public class StringConverterUtil {

  private static StringConverterUtil singleton;
  
  private Map<Class<?>, StringConverter> converters = new HashMap<Class<?>, StringConverter>();
  private StringConverter defaultConverter = new AbstractStringConverter<Object>() {
    @Override
    protected String convert(Object value) {
      return value.toString();
    }
  };
  
  private StringConverterUtil() {
  }
  
  private void register(StringConverter converter, Class<?> clazz) {
    converters.put(clazz, converter == null ? defaultConverter : converter);
  }

  protected String convertToString(Object value) {
    if (value != null) {
      return convertToString(value, value.getClass());
    }
    return convertToString(value, Object.class);
  }
  
  protected String convertToString(Object value, Class<?> valueType) {

    return getConverter(valueType).convertToString(value);
  }
  
  private StringConverter getConverter(Class<?> valueType) {
    if (valueType != null) {
      StringConverter converter = converters.get(valueType);
      if (converter == null) {
        for (Class<?> iface : valueType.getInterfaces()) {
          converter = converters.get(iface);
          if (converter != null) {
            break;
          }
        }
      }
      if (converter != null) {
        return converter;
      } else {
        return getConverter(valueType.getSuperclass());
      }
    }
    return defaultConverter;
  }
  
  public static String convert(Object value) {
    return getInstance().convertToString(value);
  }
  
  public static String convert(Object value, Class<?> valueType) {
    return getInstance().convertToString(value, valueType);
  }

  private static StringConverterUtil getInstance() {
    if (singleton == null) {
      synchronized(StringConverterUtil.class) {
        if (singleton == null) {
          singleton = new StringConverterUtil();
        }
      }
    }
    return singleton;
  }

  /**
   * Register a converter for a given class. 
   * @param converter if {@code null}, the default converter is used for the given class.
   * @param valueType the type for which the converter is registered
   */
  public static void registerConverter(StringConverter converter, Class<?> valueType) {
    getInstance().register(converter, valueType);
  }
}
