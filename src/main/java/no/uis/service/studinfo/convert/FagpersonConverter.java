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

import no.uis.service.studinfo.data.Fagperson;

/**
 * Converts a {@link Fagperson} to a String.
 * The result has the form <code><i>firstName</i> <i>lastName</i> (<i>role</i>)</code>.
 */
public class FagpersonConverter extends AbstractStringConverter<Fagperson> {

  @Override
  protected String convert(Fagperson value) {
    StringBuilder sb = new StringBuilder();
    sb.append(value.getPersonnavn().getFornavn());
    sb.append(' ');
    sb.append(value.getPersonnavn().getEtternavn());
    sb.append(" ("); //$NON-NLS-1$
    sb.append(value.getPersonrolle());
    sb.append(')');
    return sb.toString();
  }

}
