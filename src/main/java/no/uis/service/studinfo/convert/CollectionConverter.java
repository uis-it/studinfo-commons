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

package no.uis.service.studinfo.convert;

import java.util.Collection;

/**
 * Converts a {@link Collection} to a comma-separated list of elements.
 * The elements are converted by the one registered for the type of each element.
 */
public class CollectionConverter extends AbstractStringConverter<Collection<?>> {

  @Override
  protected String convert(Collection<?> value) {
    StringBuilder sb = new StringBuilder();
    for (Object object : value) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(StringConverterUtil.convert(object));
    }
    return sb.toString();
  }

}
