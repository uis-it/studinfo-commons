/*
 Copyright 2013 University of Stavanger, Norway

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

package no.uis.service.studinfo.commons;

import no.uis.service.studinfo.data.FsYearSemester;

public class StudinfoContext {
  private final FsYearSemester currentSemester;
  
  /**
   * start semester for the current semester.  
   */
  private final FsYearSemester startSemester;

  private final String language;

  public StudinfoContext(FsYearSemester currentSemester, String language) {
    this.currentSemester = currentSemester;
    this.startSemester = Studinfos.getStartYearSemester(currentSemester);
    this.language = language;
  }

  public FsYearSemester getCurrentYearSemester() {
    return currentSemester;
  }
  
  public FsYearSemester getStartYearSemester() {
    return startSemester;
  }
  
  public String getLanguage() {
    return language;
  }
}
