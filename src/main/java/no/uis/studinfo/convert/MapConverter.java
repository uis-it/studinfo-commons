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

import java.util.Map;

/**
 * Converts a Map to a string.
 * Internally the StringConverterUtil is used on the map's entry set. 
 */
public class MapConverter extends AbstractStringConverter<Map<?, ?>> {

  @Override
  protected String convert(Map<?, ?> value) {
    return StringConverterUtil.convert(value.entrySet());
  }
}
