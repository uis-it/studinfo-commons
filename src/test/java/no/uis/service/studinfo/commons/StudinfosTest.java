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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import no.uis.fsws.studinfo.data.FsSemester;
import no.uis.fsws.studinfo.data.FsStudieinfo;
import no.uis.fsws.studinfo.data.FsYearSemester;
import no.uis.fsws.studinfo.data.Studieprogram;
import no.uis.fsws.studinfo.impl.EmptyStudinfoImport;
import no.uis.fsws.studinfo.impl.SkippingAmpersandParser;
import no.uis.fsws.studinfo.impl.StudInfoImportImpl;
import no.uis.studinfo.commons.Studinfos;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class StudinfosTest {

  private Resource testData;
  private Resource transformer;
  private ResourceLoader loader = new DefaultResourceLoader();
  
  @Before
  public void init() throws Exception {
    testData = loader.getResource("file:src/test/data/b-data-future.xml");
    assumeNotNull(testData);
    
    transformer = loader.getResource("classpath:/fspreprocess.xsl");
    assumeNotNull(transformer);
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void test() throws Exception {
    StudInfoImportImpl importer = new ImportMock();
    importer.setTransformerUrl(transformer.getURL());
    importer.setXmlSourceParser(SkippingAmpersandParser.class.getName());
    
    FsStudieinfo sinfo = importer.fetchStudyPrograms(217, -1, 2013, FsSemester.VAR.toString(), true, "B");

    assumeNotNull(sinfo);
    
    assumeThat(sinfo.getStudieprogram(), hasItems(notNullValue(Studieprogram.class)));
    
    Studieprogram bdata = sinfo.getStudieprogram().get(0);
    assumeThat(bdata.getStudieprogramkode(), is("B-DATA"));
    
    Studinfos.cleanUtdanningsplan(bdata.getUtdanningsplan(), "B-DATA", new FsYearSemester(2013, FsSemester.VAR), 6);
    
    assertThat(bdata.getUtdanningsplan().getKravSammensettingListe(), is(notNullValue()));
    assertThat(bdata.getUtdanningsplan().getKravSammensettingListe().size(), is(3));
  }
  
  private class ImportMock extends EmptyStudinfoImport {
    @Override
    protected Reader fsGetStudieprogram(int institution, int faculty, int year, String semester, boolean includeEP, String language) {
      try {
        return new InputStreamReader(testData.getInputStream(), "ISO-8859-1");
      } catch(IOException e) {
        assumeNoException(e);
      }
      return null;
    }
    
    @Override
    protected Reader fsGetEmne(int institution, int faculty, int year, String semester, String language) {
      throw new UnsupportedOperationException();
    }
    
    @Override
    protected Reader fsGetKurs(int institution, String language) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected Reader fsGetEmne(int institution, String emnekode, String versjonskode, int year, String semester, String language)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    protected Reader fsGetStudieprogram(String studieprogramkode, int year, String semester, boolean includeEP, String language) {
      throw new UnsupportedOperationException();
    }
  }
}

