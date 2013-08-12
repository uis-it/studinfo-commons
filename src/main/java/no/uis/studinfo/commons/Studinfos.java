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

package no.uis.studinfo.commons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import no.uis.fsws.studinfo.data.FsSemester;
import no.uis.fsws.studinfo.data.FsYearSemester;
import no.uis.fsws.studinfo.util.ContextPath;
import no.uis.studinfo.convert.StringConverterUtil;
import no.usit.fsws.schemas.studinfo.Emne;
import no.usit.fsws.schemas.studinfo.Emnekombinasjon;
import no.usit.fsws.schemas.studinfo.KravSammensetting;
import no.usit.fsws.schemas.studinfo.Obligoppgave;
import no.usit.fsws.schemas.studinfo.ProgramEmne;
import no.usit.fsws.schemas.studinfo.Studieprogram;
import no.usit.fsws.schemas.studinfo.Utdanningsplan;
import no.usit.fsws.schemas.studinfo.Vurdkombinasjon;
import no.usit.fsws.schemas.studinfo.Vurdordning;

/**
 * Utility class for study info. 
 */
public final class Studinfos {

  /**
   * FS code for UiS.
   */
  public static final int FS_STED_UIS = 217;
  
  /**
   * Default fraction for "vurderingskombinasjon".
   */
  public static final String DEFAULT_VURDKOMB_BROK = "1/1";
  
  /**
   * Property string "validFrom".
   */
  public static final String PROP_VALID_FROM = "validFrom";
  
  /**
   * Property string "skipSemesters".
   */
  public static final String PROP_SKIP_SEMESTERS = "skipSemesters";

  /**
   * Property name "privatist".
   */
  public static final String PROP_PRIVATIST = "privatist";
  
  /**
   * Property name "obligund".
   */
  public static final String PROP_OBLIGUND = "obligund";
  
  /**
   * Property name "forkunnskaper".
   */
  public static final String PROP_FORKUNNSKAPER = "forkunnskaper";
  
  /**
   * Property name "alternatives".
   */
  public static final String PROP_ALTERNATIVES = "alternatives";
  
  /**
   * Property name "text".
   */
  public static final String PROP_TEXT = "text";
  
  private static final int YEAR_LEN = 4;
  private static final int DEFAULT_MAX_SEMESTER = 10;
  private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Studinfos.class);

  private static ContextPath contextPath = new ContextPath();

  private Studinfos() {
  }

  public static void cleanUtdanningsplan(Utdanningsplan uplan, String programCode, FsYearSemester currentSemester,
      int maxSemesters)
  {
    contextPath.init(programCode);
    try {
      if (uplan != null && uplan.isSetKravSammensettingListe() && uplan.getKravSammensettingListe().isSetKravSammensetting()) {
        cleanKravSammensettings(uplan.getKravSammensettingListe().getKravSammensetting(), currentSemester, maxSemesters);
      }
    } finally {
      contextPath.remove();
    }
  }

  /**
   * Calculating the difference between {@code ys1} and {@code ys2}. Like ys1 - ys2.
   * 
   * @return the difference in semesters
   */
  public static int getDiffSemesters(FsYearSemester ys1, FsYearSemester ys2) {

    int diff = 2 * (ys1.getYear() - ys2.getYear());

    diff += ys1.getSemester().ordinal() - ys2.getSemester().ordinal();

    return diff;
  }

  public static FsYearSemester getValidFrom(KravSammensetting ks) {
    int validFromYear = 0;
    FsSemester validFromSemester = null;
    if (ks.isSetArstallGjelderFra()) {
      validFromYear = ks.getArstallGjelderFra().getYear();
      validFromSemester = ks.getTerminkodeGjelderFra();
    } else if (ks.isSetTerminGjelderFra()) {
      String terminGjelderFra = ks.getTerminGjelderFra();
      try {
        validFromYear = Integer.parseInt(terminGjelderFra.substring(0, YEAR_LEN));
        char semesterChar = terminGjelderFra.charAt(YEAR_LEN);
        validFromSemester = semesterChar == 'H' ? FsSemester.HOST : (semesterChar == 'V' ? FsSemester.VAR : null);
      } catch(NumberFormatException | IndexOutOfBoundsException e) {
        log.warn(contextPath.getPath() + ": " + terminGjelderFra, e);
      }
    }
    return new FsYearSemester(validFromYear, validFromSemester);
  }

  private static void cleanKravSammensettings(List<KravSammensetting> kravSammensetting, FsYearSemester currentSemester,
      final int maxSemesters)
  {
    Iterator<KravSammensetting> iter = kravSammensetting.iterator();
    while (iter.hasNext()) {
      KravSammensetting kravSammen = iter.next();
      FsYearSemester yearSemester = getValidFrom(kravSammen);
      Integer skipSemesters = getDiffSemesters(currentSemester, yearSemester);

      kravSammen.addProperty(PROP_VALID_FROM, yearSemester);
      kravSammen.addProperty(PROP_SKIP_SEMESTERS, skipSemesters);

      boolean doRemove = skipSemesters < 0 || maxSemesters <= skipSemesters;
      if (!doRemove) {
        doRemove = cleanKravSammensetting(kravSammen, maxSemesters, skipSemesters);
      }
      if (doRemove) {
        iter.remove();
      }
    }
  }

  private static boolean cleanKravSammensetting(KravSammensetting kravSammen, int maxSemesters, int skipSemesters) {

    return cleanEmneKombinasjon(kravSammen.getEmnekombinasjon(), 0, skipSemesters) == 0;
  }

  /**
   * @return number of valid emner
   */
  private static int cleanEmneKombinasjon(Emnekombinasjon ek, int offset, final int skipSemesters) {

    int nboEmne = 0;
    contextPath.push(ek.getEmnekombinasjonskode());
    int newOffset = offset;
    if (ek.isSetEmneListe() && ek.getEmneListe().isSetEmne()) {
      Iterator<ProgramEmne> iter = ek.getEmneListe().getEmne().iterator();
      int nEmne = 0;
      while (iter.hasNext()) {
        ProgramEmne emne = iter.next();
        contextPath.push(emne.getEmneid().getEmnekode());
        if (!isValidEmne(emne, newOffset, skipSemesters)) {
          iter.remove();
        }
        contextPath.pop();
        nEmne++;
      }
      nboEmne += nEmne;
    }

    if (ek.isSetEmnekombinasjonListe() && ek.getEmnekombinasjonListe().isSetEmnekombinasjon()) {
      if (ek.isSetTerminnrRelativStart()) {
        newOffset += ek.getTerminnrRelativStart().intValue() - 1;
      }
      for (Emnekombinasjon subEk : ek.getEmnekombinasjonListe().getEmnekombinasjon()) {
        nboEmne += cleanEmneKombinasjon(subEk, newOffset, skipSemesters);
      }
    }

    contextPath.pop();
    return nboEmne;
  }

  private static boolean isValidEmne(ProgramEmne emne, int terminOffset, final int skipSemesters) {
    if (emne.isSetUndterminFra()) {
      int ufra = emne.getUndterminFra().intValue();
      int util = emne.isSetUndterminTil() ? emne.getUndterminTil().intValue() : ufra;
      int udefault = emne.isSetUndterminDefault() ? emne.getUndterminDefault().intValue() : 0;

      if (udefault == 0) {
        udefault = ufra;
      }
      ufra += terminOffset;
      util += terminOffset;
      udefault += terminOffset;

      if (util <= skipSemesters) {
        return false;
      }
    }
    return true;
  }

  /**
   * Remove "obligatorsk undervisning", old subjects. If vurdering is set, "only elements with vurdering == true are considered.
   * 
   * @return List of codes that are removed from the vurderingsordning (compulsory ones)
   */
  public static List<String> cleanVurderingsordning(Emne emne, FsYearSemester currentYearSemester) {
    List<String> excludeCodes;
    if (emne.isSetVurdordningListe()) {
      // There is no attribute on vurdkombinasjon that tells us if it is compulsory
      if (emne.isSetObligund() && emne.getObligund().isSetObligoppgave()) {
        List<Obligoppgave> obligund = emne.getObligund().getObligoppgave();
        excludeCodes = new ArrayList<String>(obligund.size());
        for (Obligoppgave oo : obligund) {
          excludeCodes.add(oo.getNr());
        }
      } else {
        excludeCodes = Collections.emptyList();
      }

      if (emne.isSetVurdordningListe() && emne.getVurdordningListe().isSetVurdordning()) {
        Iterator<Vurdordning> iter = emne.getVurdordningListe().getVurdordning().iterator();
        while (iter.hasNext()) {
          Vurdordning vo = iter.next();
          if (excludeCodes.contains(vo.getVurdordningid())) {
            iter.remove();
          } else if (vo.isSetVurdkombinasjon()) {
            Vurdkombinasjon vkomb = vo.getVurdkombinasjon();
            cleanVurderingskombinasjon(vo, vkomb, currentYearSemester, excludeCodes);
          }
        }
      }
    } else {
      excludeCodes = Collections.emptyList();
    }

    return excludeCodes;
  }

  private static void cleanVurderingsKombinasjon(List<Vurdkombinasjon> vurdkombinasjon, FsYearSemester currentYearSemester,
      List<String> excludeCodes)
  {
    Iterator<Vurdkombinasjon> iter = vurdkombinasjon.iterator();
    while (iter.hasNext()) {
      Vurdkombinasjon vkomb = iter.next();
      cleanVurderingskombinasjon(iter, vkomb, currentYearSemester, excludeCodes);
    }
  }

  private static void cleanVurderingskombinasjon(Object parentObject, Vurdkombinasjon vkomb, FsYearSemester currentYearSemester,
      List<String> excludeCodes)
  {
    boolean doClean = excludeCodes.contains(vkomb.getVurdkombkode()) || isOldVkomb(vkomb, currentYearSemester);

    if (!doClean && vkomb.isSetVurdering()) {
      doClean = !vkomb.isVurdering();
    }

    if (doClean) {
      if (parentObject instanceof Vurdordning) {
        Vurdordning vo = (Vurdordning)parentObject;
        vo.setVurdkombinasjon(null);
      } else if (parentObject instanceof Iterator) {
        Iterator<?> iter = (Iterator<?>)parentObject;
        iter.remove();
      }
    } else {
      if (!vkomb.isSetBrok()) {
        vkomb.setBrok(DEFAULT_VURDKOMB_BROK);
      }
      if (vkomb.isSetVurdkombinasjonListe() && vkomb.getVurdkombinasjonListe().isSetVurdkombinasjon()) {
        cleanVurderingsKombinasjon(vkomb.getVurdkombinasjonListe().getVurdkombinasjon(), currentYearSemester, excludeCodes);
      }
    }
  }

  private static boolean isOldVkomb(Vurdkombinasjon vkomb, FsYearSemester currentYearSemester) {
    FsYearSemester sistegang = vkomb.getSistegang();
    if (sistegang != null) {
      int skipSemesters = getDiffSemesters(currentYearSemester, sistegang);
      return skipSemesters >= 0;
    }
    return false;
  }

  public static boolean isSetAndTrue(Boolean b) {
    if (b != null) {
      return b.booleanValue();
    }
    return false;
  }

  public static FsYearSemester getStartYearSemester(FsYearSemester currentSemester) {

    int year = currentSemester.getYear();
    FsSemester semester = currentSemester.getSemester();

    if (semester.equals(FsSemester.VAR)) {
      semester = FsSemester.HOST;
      year--;
    }
    return new FsYearSemester(year, semester);
  }

  public static int numberOfSemesters(Studieprogram program) {
    if (program.isSetVarighet()) {
      return program.getVarighet().getSemesters();
    }

    int maxSemester = 0;
    if (program.isSetUtdanningsplan() && program.getUtdanningsplan().isSetKravSammensettingListe() && program.getUtdanningsplan().getKravSammensettingListe().isSetKravSammensetting()) {
      for (KravSammensetting krav : program.getUtdanningsplan().getKravSammensettingListe().getKravSammensetting()) {
        maxSemester = maxSemester(krav.getEmnekombinasjon(), maxSemester);
      }
    }
    return maxSemester == 0 ? DEFAULT_MAX_SEMESTER : maxSemester;
  }

  public static Map<String, Object> forkunnskap(Emne emne) {
    Map<String, Object> forkunnskap = null;
    if (emne.isSetFormelleForkunnskaper() || emne.isSetAbsForkunnskaperFritekst()) {
      forkunnskap = new HashMap<String, Object>();
      if (emne.isSetFormelleForkunnskaper()) {
        forkunnskap.put(PROP_ALTERNATIVES, emne.getFormelleForkunnskaper());
        emne.setFormelleForkunnskaper(null);
      }
      if (emne.isSetAbsForkunnskaperFritekst()) {
        forkunnskap.put(PROP_TEXT, emne.getAbsForkunnskaperFritekst());
        emne.setAbsForkunnskaperFritekst(null);
      }
    }
    return forkunnskap;
  }

  public static Map<String, Object> anbefalteForkunnskaper(Emne emne) {
    Map<String, Object> anbForkunn = null;
    if (emne.isSetAnbefalteForkunnskaper() || emne.isSetAnbForkunnskaperFritekst()) {
      anbForkunn = new HashMap<String, Object>();

      if (emne.isSetAnbefalteForkunnskaper()) {
        anbForkunn.put(PROP_FORKUNNSKAPER, emne.getAnbefalteForkunnskaper());
        emne.setAnbefalteForkunnskaper(null);
      }
      if (emne.isSetAnbForkunnskaperFritekst()) {
        anbForkunn.put(PROP_TEXT, emne.getAnbForkunnskaperFritekst());
        emne.setAnbForkunnskaperFritekst(null);
      }
    }
    return anbForkunn;
  }

  public static Map<String, Object> obligund(Emne emne) {
    Map<String, Object> obligund = null;
    if (emne.isSetObligund() || emne.isSetObligUndaktTilleggsinfo()) {
      obligund = new HashMap<String, Object>();
      if (emne.isSetObligund()) {
        obligund.put(PROP_OBLIGUND, StringConverterUtil.convert(emne.getObligund()));
        emne.setObligund(null);
      }
      if (emne.isSetObligUndaktTilleggsinfo()) {
        obligund.put(PROP_TEXT, emne.getObligUndaktTilleggsinfo());
        emne.setObligUndaktTilleggsinfo(null);
      }
    }
    return obligund;
  }

  public static Map<String, Object> apenFor(Emne emne) {
    Map<String, Object> apenFor = new HashMap<String, Object>();
    apenFor.put(PROP_PRIVATIST, Studinfos.isSetAndTrue(emne.isStatusPrivatist()));
    emne.setStatusPrivatist(null);
    if (emne.isSetApentForTillegg() || Studinfos.isSetAndTrue(emne.isStatusPrivatist())) {
      if (emne.isSetApentForTillegg()) {
        apenFor.put(PROP_TEXT, emne.getApentForTillegg());
        emne.setApentForTillegg(null);
      }

    } else if (emne.isSetInngarIStudieprogramListe()) {
      String value = StringConverterUtil.convert(emne.getInngarIStudieprogramListe());
      apenFor.put(PROP_TEXT, value);
    }
    return apenFor;
  }

  private static int maxSemester(Emnekombinasjon emnekombinasjon, int maxSemester) {
    int newMax = maxSemester;
    if (emnekombinasjon.isSetEmneListe() && emnekombinasjon.getEmneListe().isSetEmne()) {
      for (ProgramEmne emne : emnekombinasjon.getEmneListe().getEmne()) {
        int currMax = 0;
  
        if (emne.isSetUndterminTil()) {
          currMax = emne.getUndterminTil().intValue();
        } else if (emne.isSetUndterminFra()) {
          currMax = emne.getUndterminFra().intValue();
        }
        if (currMax > newMax) {
          newMax = currMax;
        }
      }
    }
    if (emnekombinasjon.isSetEmnekombinasjonListe() && emnekombinasjon.getEmnekombinasjonListe().isSetEmnekombinasjon()) {
      for (Emnekombinasjon ekChild : emnekombinasjon.getEmnekombinasjonListe().getEmnekombinasjon()) {
        newMax = maxSemester(ekChild, newMax);
      }
    }
    return newMax;
  }
}
