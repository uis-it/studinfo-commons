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

import no.usit.fsws.schemas.studinfo.Datoperiode;

/**
 * Converts a {@link Datoperiode} to a string. 
 * For converting the dates, the {@link CalendarNorwegianAdapter} is used.
 */
public class DatoperiodeConverter extends AbstractStringConverter<Datoperiode> {

  private static final String SPACE_HASH_SPACE = " - ";

  @Override
  protected String convert(Datoperiode value) {
    StringBuilder sb = new StringBuilder();
    if (value.isSetFradato()) {
      sb.append(value.getFradato().toXMLFormat());
    }
    sb.append(SPACE_HASH_SPACE);
    if (value.isSetTildato()) {
      sb.append(value.getTildato().toXMLFormat());
    }
    return sb.toString();
  }
}
