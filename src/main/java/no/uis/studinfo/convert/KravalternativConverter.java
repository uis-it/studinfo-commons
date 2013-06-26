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

import no.uis.fsws.studinfo.data.Kravalternativ;

/**
 * Converts a {@link Kravalternativ} to a string.
 */
public class KravalternativConverter extends AbstractStringConverter<Kravalternativ> {

  @Override
  protected String convert(Kravalternativ value) {
    return value.getValue();
  }
}
