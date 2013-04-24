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

import no.uis.service.studinfo.data.Semesterperiode;

/**
 * Convert a {@link Semesterperiode} to a string.
 */
public class SemesterperiodeConverter extends AbstractStringConverter<Semesterperiode> {

  @Override
  protected String convert(Semesterperiode value) {
    StringBuilder sb = new StringBuilder();
    if (value.isSetForstegang()) {
      sb.append(value.getForstegang());
    }
    sb.append('-');
    if (value.isSetSistegang()) {
      sb.append(value.getSistegang());
    }
    return sb.toString();
  }
}
