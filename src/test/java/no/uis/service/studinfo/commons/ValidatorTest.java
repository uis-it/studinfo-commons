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
// CHECKSTYLE:OFF
package no.uis.service.studinfo.commons;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import no.uis.fsws.proxy.StudInfoImport.StudinfoType;
import no.uis.fsws.studinfo.data.FsSemester;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized2;
import org.junit.runners.Parameterized2.Parameters;
import org.junit.runners.Parameterized2.TestName;

@RunWith (Parameterized2.class)
public class ValidatorTest {

  private int faculty;
  private int year;
  private FsSemester semester;
  private StudinfoType infoType;
  private String lang;

  public ValidatorTest(int faculty, int year, FsSemester semester, StudinfoType infoType, String lang) {
    this.faculty = faculty;
    this.year = year;
    this.semester = semester;
    this.infoType = infoType;
    this.lang = lang;
  }
  
  @Parameters
  public static List<Object[]> configParams() {
    final String sFaculty = System.getProperty("studinfo.faculties", "-1");
    final String sYear = System.getProperty("studinfo.year");
    final String sInfoType = System.getProperty("studinfo.type");
    final String sLang = System.getProperty("studinfo.lang");
    final String sSemester = System.getProperty("studinfo.semester");

    if (sYear == null || sSemester == null) {
      return Collections.emptyList();
    }
    FsSemester semester = FsSemester.stringToUisSemester(sSemester);
    
    int year = Integer.parseInt(sYear);
    
    StudinfoType[] infoTypes = (sInfoType != null ? new StudinfoType[] {StudinfoType.valueOf(sInfoType)} : StudinfoType.values());
    String[] langs = (sLang != null ? sLang.split(",") : new String[] {"B", "E", "N"});
    String[] faculties = sFaculty.split(",");
    
    List<Object[]> params = new ArrayList<Object[]>();
    for (StudinfoType type : infoTypes) {
      for (String lang : langs) {
        if (type.equals(StudinfoType.KURS)) {
          params.add(new Object[] {-1, year, semester, type, lang});
        } else {
          for (String faculty : faculties) {
            params.add(new Object[] {Integer.valueOf(faculty), year, semester, type, lang});
          }
        }
      }
    }
    return params;
  }

  @TestName
  public static String getTestName(String method, Object[] params) {
    
    StringBuilder sb = new StringBuilder();
    sb.append(method);
    sb.append('(');
    for (int i = 0; i < params.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(params[i]);
    }
    sb.append(')');
    return sb.toString();
  }
  
  /**
   * The test is disabled by default. It can be enabled by setting the system properties {@code studinfo.year} and {@code studinfo.semester}. The year
   * is set with the system property {@code studinfo-validate.year}. The semester is set with the system property {@code studinfo-validate.semester}.
   */
  @Test
  public void validate() throws Exception {
    StudinfoValidator validator = new StudinfoValidator();
    List<String> messages = validator.fetchAndValidate(faculty, year, semester, lang, infoType);
    assertThat(new ListWrapper(messages), is(emptyCollection()));
  }
  
  private static class IsEmptyCollection extends BaseMatcher<Collection<?>> {

    @Override
    public boolean matches(Object item) {
      return (item != null && ((Collection<?>)item).isEmpty());
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("empty");
    }
  }

  private static Matcher<Collection<?>> emptyCollection() {
    return new IsEmptyCollection();
  }

  /**
  * This class overrides the {@link AbstractCollection#toString()} method so that the matcher puts every message on a line.
  */
  @SuppressWarnings("serial")
  private static class ListWrapper extends ArrayList<String> {
    
    public ListWrapper(List<String> wrappee) {
      super(wrappee);
    }
    
    @Override
    public String toString() {
      if (isEmpty()) {
        return "[]";
      }

      StringBuilder sb = new StringBuilder();
      sb.append("[\n");
      for (String elem : this) {
        sb.append(elem);
        sb.append("\n,\n");
      }
      sb.append(']');
      return sb.toString();
    }
  }
}
