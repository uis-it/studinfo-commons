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

import no.uis.fsws.studinfo.data.Vurdenhet;

/**
 * Converts a {@link Vurdenhet} to a string.
 */
public class VurdenhetConverter extends AbstractStringConverter<Vurdenhet> {

  @Override
  protected String convert(Vurdenhet v) {
    StringBuilder sb = new StringBuilder();
    if (v.isSetVurdstatus()) {
      sb.append(v.getVurdstatus());
      sb.append(": "); //$NON-NLS-1$
    }
    sb.append(v.getTidReell().getMonth());
    sb.append('/');
    sb.append(v.getTidReell().getYear());
    return sb.toString();
  }

}
