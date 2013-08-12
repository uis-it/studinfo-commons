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

import java.util.Collections;
import java.util.List;

import no.usit.fsws.schemas.studinfo.Semester;
import no.usit.fsws.schemas.studinfo.Undsemester;

/**
 * Converts a {@link Undsemester} to a string.
 */
public class UndsemesterConverter extends AbstractStringConverter<Undsemester> {

  @Override
  protected String convert(Undsemester value) {
    List<Semester> semesterList = value.isSetSemesterListe() && value.getSemesterListe().isSetSemester() 
        ? value.getSemesterListe().getSemester() : Collections.<Semester>emptyList();
        
    boolean includeNumber = semesterList.size() > 1;

    StringBuilder sb = new StringBuilder();

    for (Semester semester : semesterList) {
      if (sb.length() > 0) {
        sb.append(", "); //$NON-NLS-1$
      }
      if (includeNumber) {
        sb.append(semester.getNr());
        sb.append(':');
      }
      sb.append(semester.getValue());
    }
    boolean needHyphen = true;
    boolean hasValidPeriod = value.isSetForstegang() || value.isSetSistegang();
    if (hasValidPeriod) {
      sb.append('(');
    }
    if (value.isSetForstegang()) {
      sb.append(value.getForstegang());
      sb.append('-');
      needHyphen = false;
    }
    if (value.isSetSistegang()) {
      if (needHyphen) {
        sb.append('-');
      }
      sb.append(value.getSistegang());
    }
    if (hasValidPeriod) {
      sb.append(')');
    }

    return sb.toString();
  }
}
