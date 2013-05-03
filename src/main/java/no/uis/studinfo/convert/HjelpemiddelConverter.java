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

import no.uis.fsws.studinfo.data.Hjelpemiddel;

/**
 * Convert a {@link Hjelpemiddel} to a String.
 * Format: <code><i>hjelpemiddelnavn</i> (<i>hjelpemiddelmerknad</i>)</code>.
 * @see Hjelpemiddel#getHjelpemiddelnavn()
 * @see Hjelpemiddel#getHjelpemiddelmerknad()
 */
public class HjelpemiddelConverter extends AbstractStringConverter<Hjelpemiddel> {

  @Override
  protected String convert(Hjelpemiddel value) {
    StringBuilder sb = new StringBuilder();

    sb.append(StringConverterUtil.convert(value.getHjelpemiddelnavn()));
    if (value.isSetHjelpemiddelmerknad() && !value.getHjelpemiddelmerknad().isEmpty()) {
      sb.append(" ("); //$NON-NLS-1$
      sb.append(StringConverterUtil.convert(value.getHjelpemiddelmerknad()));
      sb.append(')');
    }
    return sb.toString();
  }
}
