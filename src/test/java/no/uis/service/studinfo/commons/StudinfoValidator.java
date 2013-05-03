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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import no.uis.fsws.studinfo.StudInfoImport;
import no.uis.fsws.studinfo.StudInfoImport.StudinfoType;
import no.uis.fsws.studinfo.data.FsSemester;
import no.uis.studinfo.commons.Studinfos;
import no.usit.fsws.studinfo.StudInfoService;

import org.apache.cxf.helpers.IOUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class StudinfoValidator {

  private static final int IOBUFFER_SIZE = 10000;
  static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StudinfoValidator.class);

  private StudInfoService fsWsStudInfo;
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    final StudinfoValidator validator = new StudinfoValidator();
    try {
      validator.validateMain(args);
    } catch(Exception ex) {
      log.error(null, ex);
    }
  }


  private void validateMain(String[] args) throws Exception {
    if (args.length != 2) {
      printUsage();
    } else {
      int year = Integer.parseInt(args[0]);
      FsSemester semester = FsSemester.stringToUisSemester(args[1]);
      List<String> messages = validateAll(year, semester, null, null);
      printMessages(messages);
    }
  }
  
  private List<String> validateAll(int year, FsSemester semester, StudinfoType[] infoTypes, String[] langs) throws Exception {
    
    List<String> messages = new ArrayList<String>();
    
    for (StudinfoType infoType : infoTypes) {
      switch(infoType) {
        case KURS:
          messages.addAll(validateCourses(semester, year, langs));
          break;
        case STUDIEPROGRAM:
          messages.addAll(validatePrograms(-1, semester, year, langs));
          break;
        case EMNE:
          messages.addAll(validateSubjects(-1, semester, year, langs));
          break;
      }
    }
    
    return messages;
}

  private void printMessages(List<String> messages) {
    for (String msg : messages) {
      System.out.println(msg);
    }
  }

  private List<String> validateSubjects(int faculty, FsSemester semester, int year, String[] langs) throws Exception {
    List<String> messages = new ArrayList<String>();
    for (String lang : langs) {
      messages.addAll(validateSubjects(Studinfos.FS_STED_UIS, faculty, year, semester, lang));
    }
    
    return messages;
  }

  private List<String> validatePrograms(int faculty, FsSemester semester, int year, String[] langs) throws Exception {
    List<String> messages = new ArrayList<String>();
    for (String lang : langs) {
      messages.addAll(validateStudyPrograms(faculty, Studinfos.FS_STED_UIS, year, semester, lang));
    }
    
    return messages;
  }

  private List<String> validateCourses(FsSemester semester, int year, String[] langs) throws Exception {
    List<String> messages = new ArrayList<String>();
    for (String lang : langs) {
      messages.addAll(validateCourses(Studinfos.FS_STED_UIS, year, semester, lang));
    }
    return messages;
  }

  private synchronized StudInfoService getBean() {
    if (fsWsStudInfo == null) {
      BeanFactory bf = new ClassPathXmlApplicationContext(new String[] {
        "fsStudInfoPropsContext.xml",
       "fsimportContext.xml",
       });
  
      fsWsStudInfo = bf.getBean("fsWsStudInfo", StudInfoService.class);
    }
    return fsWsStudInfo;
  }

  private void printUsage() {
    System.out.println(this.getClass().getName() + " YEAR <V\u00C5R | H\u00D8ST>");
  }

  private List<String> validateStudyPrograms(int institution, int faculty, int year, FsSemester semester, String language)
      throws Exception
  {
    String studieinfoXml = getBean().getStudieprogramSI(year,
        semester.toString(), StudInfoImport.INTEGER_1, null, institution, faculty, StudInfoImport.INTEGER_MINUS_1, null, language);
    
    return validate(studieinfoXml, StudinfoType.STUDIEPROGRAM, year, semester, language);
  }

  private List<String> validateSubjects(int institution, int faculty, int year, FsSemester semester, String language) throws Exception {
    String studieinfoXml = getBean().getEmneSI(Integer.valueOf(institution), null, null,
      faculty, StudInfoImport.INTEGER_MINUS_1, null, year, semester.toString(), language);

    return validate(studieinfoXml, StudinfoType.EMNE, year, semester, language);
  }

  private List<String> validateCourses(int institution, int year, FsSemester semester, String language)
      throws Exception
  {
    String studieinfoXml = getBean().getKursSI(Integer.valueOf(institution), StudInfoImport.INTEGER_MINUS_1, StudInfoImport.INTEGER_MINUS_1, StudInfoImport.INTEGER_MINUS_1, language);

    return validate(studieinfoXml, StudinfoType.KURS, year, semester, language);
  }

  protected List<String> validate(String studieinfoXml, StudinfoType infoType, int year, FsSemester semester, String language) throws Exception {
    
    // save xml
    File outFile = new File("target/out", infoType.toString() + year + semester + language + ".xml");
    if (outFile.exists()) {
      outFile.delete();
    } else {
      outFile.getParentFile().mkdirs();
    }
    File outBackup = new File("target/out", infoType.toString() + year + semester + language + "_orig.xml");
    Writer backupWriter = new OutputStreamWriter(new FileOutputStream(outBackup), IOUtils.UTF8_CHARSET);
    backupWriter.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    IOUtils.copy(new StringReader(studieinfoXml), backupWriter, IOBUFFER_SIZE);
    backupWriter.flush();
    backupWriter.close();
    
    TransformerFactory trFactory = TransformerFactory.newInstance();
    Source schemaSource = new StreamSource(getClass().getResourceAsStream("/fspreprocess.xsl"));
    Transformer stylesheet = trFactory.newTransformer(schemaSource);

    Source input = new StreamSource(new StringReader(studieinfoXml));
    
    Result result = new StreamResult(outFile);
    stylesheet.transform(input, result);
    
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(false);
    factory.setNamespaceAware(true);
    
    SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
    Schema schema = schemaFactory.newSchema(new Source[] {new StreamSource(new File("src/main/xsd/studinfo.xsd"))});

    factory.setSchema(schema);
    
    SAXParser parser = factory.newSAXParser();
    
    XMLReader reader = parser.getXMLReader();
    ValidationErrorHandler errorHandler = new ValidationErrorHandler(infoType, year, semester, language);
    reader.setErrorHandler(errorHandler);
    reader.setContentHandler(errorHandler);
    try {
      reader.parse(new InputSource(new InputStreamReader(new FileInputStream(outFile), IOUtils.UTF8_CHARSET)));
    } catch(SAXException ex) {
      // do nothing. The error is handled in the error handler
    }
    return errorHandler.getMessages();
  }

  public List<String> fetchAndValidate(int faculty, int year, FsSemester semester, String lang, StudinfoType infoType) throws Exception {
    List<String> result;
    switch(infoType) {
      case EMNE:
        result = validateSubjects(Studinfos.FS_STED_UIS, faculty, year, semester, lang);
        break;
      case KURS:
        result = validateCourses(Studinfos.FS_STED_UIS, year, semester, lang);
        break;
      case STUDIEPROGRAM:
        result = validateStudyPrograms(Studinfos.FS_STED_UIS, faculty, year, semester, lang);
        break;
      default:
        result = Collections.emptyList();
        break;
    }
    
    return result;
  }
  
}
