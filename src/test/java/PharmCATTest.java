import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pharmgkb.pharmcat.PharmCAT;
import org.pharmgkb.pharmcat.VcfTestUtils;
import org.pharmgkb.pharmcat.reporter.ReportContext;
import org.pharmgkb.pharmcat.reporter.model.VariantReport;
import org.pharmgkb.pharmcat.reporter.model.result.GeneReport;
import org.pharmgkb.pharmcat.reporter.model.result.GuidelineReport;

import static org.junit.Assert.*;


/**
 * Test the data generated from a full run of the PharmCAT matcher and reporter.
 *
 * @author Ryan Whaley
 */
public class PharmCATTest {

  private static final String sf_astrolabeOutput = "##Test Astrolabe output\n" +
      "#ROI_label\tdiplotype labels\tdiplotype activity\tdiplotype calling notes\tjaccard\tpart\tpValue\tROI notes\tspecial case\tnomenclature version\n" +
      "CYP2D6\tCYP2D6*1/CYP2D6*4\t?/?\t\t0.6\t0.75\tp: 0.0\t\t\tv1.9-2017_02_09\n";
  private static final String sf_diplotypesTemplate = "\nmatcher: %s\nreporter: %s\nprint (displayCalls): %s\nlookup: %s";
  private static PharmCAT s_pharmcat;
  private static Path s_tempAstroPath;
  private static ReportContext s_context;

  @BeforeClass
  public static void prepare() throws IOException {

    s_tempAstroPath = Files.createTempFile("astrolabe", ".tsv");
    try (FileWriter fw = new FileWriter(s_tempAstroPath.toFile())) {
      fw.write(sf_astrolabeOutput);
    }

    Path tempDirPath = Files.createTempDirectory(MethodHandles.lookup().lookupClass().getName());
    s_pharmcat = new PharmCAT(tempDirPath, null, null);
  }

  @Test
  public void testCyp2c19_1() throws Exception {
    generalTest("test.cyp2c19.s4s17het", new String[]{
            "cyp2c19/s4s17het.vcf"
        },
        false);

    assertCalledGenes("CYP2C19");
    testCalls(DipType.PRINT,  "CYP2C19", "*1/*4B");

    testMatchedGroups("citalopram", 1);
    testMatchedGroups("ivacaftor", 0);
  }

  @Test
  public void testCyp2c19noCall() throws Exception {
    generalTest("test.cyp2c19.noCall", new String[]{
            "cyp2c19/noCall.vcf"
        },
        false);

    assertFalse(s_context.getGeneReport("CYP2C19").isCalled());

    testMatchedGroups("citalopram", 0);
    testMatchedGroups("ivacaftor", 0);
  }

  @Test
  public void testCyp2c19s11s35() throws Exception {
    generalTest("test.cyp2c19.s11s35", new String[]{
            "cyp2c19/s11s35.vcf"
        },
        false);

    assertCalledGenes("CYP2C19");
    testCalls(DipType.PRINT, "CYP2C19", "*11/*35");

    testMatchedGroups("citalopram", 1);
    testMatchedGroups("escitalopram", 1);
    testMatchedGroups("imipramine", 1);
    testMatchedGroups("clomipramine", 1);
  }

  @Test
  public void testCyp2c19s11s35rs4244285missing() throws Exception {
    generalTest("test.cyp2c19.s11s35rs4244285missing", new String[]{
            "cyp2c19/s11s35rs4244285missing.vcf"
        },
        false);

    assertCalledGenes("CYP2C19");
    testCalls(DipType.PRINT, "CYP2C19", "*1/*2");
    assertMissingVariant("CYP2C19", "rs4244285");

    testMatchedGroups("citalopram", 1);
    testMatchedGroups("escitalopram", 1);
    testMatchedGroups("imipramine", 1);
    testMatchedGroups("clomipramine", 1);
  }

  @Test
  public void testCyp2c19s2hets11missing() throws Exception {
    generalTest("test.cyp2c19.s2hets11missing", new String[]{
            "cyp2c19/s2hets11missing.vcf"
        },
        false);

    assertCalledGenes("CYP2C19");
    testCalls(DipType.PRINT, "CYP2C19", "*1/*2");
    assertMissingVariant("CYP2C19", "rs58973490");
    assertUncallableAllele("CYP2C19", "*11");

    testMatchedGroups("citalopram", 1);
    testMatchedGroups("escitalopram", 1);
    testMatchedGroups("imipramine", 1);
    testMatchedGroups("clomipramine", 1);
  }

  @Test
  public void testCyp2c19s2hets11s35missing() throws Exception {
    generalTest("test.cyp2c19.s2hets11s35missing", new String[]{
            "cyp2c19/s2hets11s35missing.vcf"
        },
        false);

    assertCalledGenes("CYP2C19");
    testCalls(DipType.PRINT, "CYP2C19", "*1/*2");
    assertMissingVariant("CYP2C19", "rs12769205");
    assertMissingVariant("CYP2C19", "rs58973490");
    assertUncallableAllele("CYP2C19", "*11");
    assertUncallableAllele("CYP2C19", "*35");

    testMatchedGroups("citalopram", 1);
    testMatchedGroups("escitalopram", 1);
    testMatchedGroups("imipramine", 1);
    testMatchedGroups("clomipramine", 1);
  }

  @Test
  public void testCyp2c19s2homos11het() throws Exception {
    generalTest("test.cyp2c19.s2homos11het", new String[]{
            "cyp2c19/s2homos11het.vcf"
        },
        false);

    assertCalledGenes("CYP2C19");
    testCalls(DipType.PRINT, "CYP2C19", "*2/*2");

    testMatchedGroups("citalopram", 1);
    testMatchedGroups("escitalopram", 1);
    testMatchedGroups("imipramine", 1);
    testMatchedGroups("clomipramine", 1);
  }

  @Test
  public void testCyp2c19s2homos11s17het() throws Exception {
    generalTest("test.cyp2c19.s2homos11s17het", new String[]{
            "cyp2c19/s2homos11s17het.vcf"
        },
        false);

    assertFalse(s_context.getGeneReport("CYP2C19").isCalled());

    testMatchedGroups("citalopram", 0);
    testMatchedGroups("escitalopram", 0);
    testMatchedGroups("imipramine", 0);
    testMatchedGroups("clomipramine", 0);
  }

  @Test
  public void testCyp2c19s2s11het() throws Exception {
    generalTest("test.cyp2c19.s2s11het", new String[]{
            "cyp2c19/s2s11het.vcf"
        },
        false);

    assertCalledGenes("CYP2C19");
    testCalls(DipType.PRINT, "CYP2C19", "*1/*2");

    testMatchedGroups("citalopram", 1);
    testMatchedGroups("escitalopram", 1);
    testMatchedGroups("imipramine", 1);
    testMatchedGroups("clomipramine", 1);
  }

  @Test
  public void testCyp2c19s2s11hetrs12769205missing() throws Exception {
    generalTest("test.cyp2c19.s2s11hetrs12769205missing", new String[]{
            "cyp2c19/s2s11hetrs12769205missing.vcf"
        },
        false);

    assertCalledGenes("CYP2C19");
    testCalls(DipType.PRINT, "CYP2C19", "*1/*2");
    assertMissingVariant("CYP2C19", "rs12769205");

    testMatchedGroups("citalopram", 1);
    testMatchedGroups("escitalopram", 1);
    testMatchedGroups("imipramine", 1);
    testMatchedGroups("clomipramine", 1);
  }

  @Test
  public void testCyp2c19s2s2s11s17het() throws Exception {
    generalTest("test.cyp2c19.s2s11s17het", new String[]{
            "cyp2c19/s2s11s17het.vcf"
        },
        false);

    assertCalledGenes("CYP2C19");
    testCalls(DipType.PRINT, "CYP2C19", "*2/*17");

    testMatchedGroups("citalopram", 1);
    testMatchedGroups("escitalopram", 1);
    testMatchedGroups("imipramine", 1);
    testMatchedGroups("clomipramine", 1);
  }

  @Test
  public void testCyp2c19s2s11s17rs12769205missing() throws Exception {
    generalTest("test.cyp2c19.s2s11s17rs12769205missing", new String[]{
            "cyp2c19/s2s11s17rs12769205missing.vcf"
        },
        false);

    assertCalledGenes("CYP2C19");
    testCalls(DipType.PRINT, "CYP2C19", "*2/*17");
    assertMissingVariant("CYP2C19", "rs12769205");

    testMatchedGroups("citalopram", 1);
    testMatchedGroups("escitalopram", 1);
    testMatchedGroups("imipramine", 1);
    testMatchedGroups("clomipramine", 1);
  }

  @Test
  public void testCyp2c19_astrolabe() throws Exception {
    generalTest("test.cyp2c19.s1s4b", new String[]{
        "cyp2c19/s4s17het.vcf"
        },
        true);

    assertCalledGenes("CYP2C19", "CYP2D6");

    testCalls(DipType.PRINT, "CYP2D6", "*1/*4");
    testCalls(DipType.PRINT, "CYP2C19", "*1/*4B");

    assertTrue(s_context.getGeneReport("CYP2D6").isAstrolabeCall());
  }

  @Test
  public void testCftrRegInc() throws Exception {
    generalTest("test.cftr.reg_inc", new String[]{
            "cftr/G542XF508del.vcf"
        },
        false);

    assertCalledGenes("CFTR");
    testCalls(DipType.PRINT, "CFTR", "F508del(CTT)/G542X");
    testCalls(DipType.LOOKUP, "CFTR", "CFTR:F508del(CTT)/Other");

    assertTrue("Missing incidental allele", s_context.getGeneReports().stream().anyMatch(GeneReport::isIncidental));
  }

  @Test
  public void testCftrRefRef() throws Exception {
    generalTest("test.cftr.ref_ref", new String[]{
            "cftr/refref.vcf"
        },
        false);

    assertCalledGenes("CFTR");
    testCalls(DipType.PRINT, "CFTR", "No CPIC variants found");
    testCalls(DipType.LOOKUP, "CFTR", "CFTR:Other/Other");

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testCftrF508() throws Exception {
    generalTest("test.cftr.refF508del", new String[]{
            "cftr/refF508del.vcf"
        },
        false);

    assertCalledGenes("CFTR");
    testCalls(DipType.PRINT, "CFTR", "F508del(CTT) (heterozygous)");
    testCalls(DipType.LOOKUP, "CFTR", "CFTR:F508del(CTT)/Other");

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testCftrF508HomCTT() throws Exception {
    generalTest("test.cftr.F508delHom_CTT", new String[]{
            "cftr/F508delF508del.vcf"
        },
        false);

    assertCalledGenes("CFTR");
    testCalls(DipType.PRINT, "CFTR", "F508del(CTT)/F508del(CTT)");
    testCalls(DipType.LOOKUP, "CFTR", "CFTR:F508del(CTT)/F508del(CTT)");

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  /**
   * This is the same test case as {@link PharmCATTest#testCftrF508()} but the position lines have been sorted
   * lexigraphically. This should not affect the calling and should lead to same diplotype output.
   */
  @Test
  public void testCftrF508Sorted() throws Exception {
    generalTest("test.cftr.refF508del_sorted", new String[]{
            "cftr/refF508del_sorted.vcf"
        },
        false);

    assertCalledGenes("CFTR");
    testCalls(DipType.PRINT, "CFTR", "F508del(CTT) (heterozygous)");
    testCalls(DipType.LOOKUP, "CFTR", "CFTR:F508del(CTT)/Other");

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testCftrI507Missing() throws Exception {
    generalTest("test.cftr.refI507missing", new String[]{
            "cftr/refI507missing.vcf"
        },
        false);
    
    assertMissingVariant("CFTR", "rs121908745");

    assertCalledGenes("CFTR");
    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testSlco1b1Test1() throws Exception {
    generalTest("test.slco1b1.17.21", new String[]{
            "SLCO1B1/s17s21.vcf"
        },
        false);

    assertCalledGenes("SLCO1B1");
    testCalls(DipType.PRINT, "SLCO1B1", "*17/*21");
    testCalls(DipType.LOOKUP, "SLCO1B1", "SLCO1B1:*17/*21");

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testSlco1b1HomWild() throws Exception {
    generalTest("test.slco1b1.hom.wild", new String[]{
            "SLCO1B1/s1as1a.vcf"
        },
        false);

    assertCalledGenes("SLCO1B1");
    testCalls(DipType.PRINT, "SLCO1B1", "*1A/*1A");
    testCalls(DipType.LOOKUP, "SLCO1B1", "SLCO1B1:*1A/*1A");

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testSlco1b1HomVar() throws Exception {
    generalTest("test.slco1b1.hom.var", new String[]{
            "SLCO1B1/s5s15.vcf"
        },
        false);

    assertCalledGenes("SLCO1B1");
    testCalls(DipType.PRINT, "SLCO1B1", "*5/*15");
    testCalls(DipType.LOOKUP, "SLCO1B1", "SLCO1B1:*5/*15");

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testSlco1b1Test3() throws Exception {
    generalTest("test.slco1b1.1a.15", new String[]{
            "SLCO1B1/s1as15.vcf"
        },
        false);

    assertCalledGenes("SLCO1B1");
    testCalls(DipType.PRINT, "SLCO1B1", "*1A/*15");
    testCalls(DipType.LOOKUP, "SLCO1B1", "SLCO1B1:*1A/*15");

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testSlco1b1TestMissing() throws Exception {
    generalTest("test.slco1b1.missing", new String[]{
            "DPYD/s1s1.vcf",
            "TPMT/s1s1.vcf"
        },
        false);

    assertCalledGenes("DPYD", "TPMT");
    assertFalse(s_context.getGeneReport("SLCO1B1").isCalled());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testDpydS1S2B() throws Exception {
    generalTest("test.slco1b1.missing", new String[]{
            "DPYD/s1s2b.vcf"
        },
        false);

    assertCalledGenes("DPYD");
    assertTrue(s_context.getGeneReport("DPYD").isCalled());
    testCalls(DipType.PRINT, "DPYD", "Reference/c.1905+1G>A");
    testCalls(DipType.LOOKUP, "DPYD", "DPYD:Any normal function variant or no variant detected/c.1905+1G>A");

    testMatchedGroups("fluorouracil", 1);
    testMatchedGroups("capecitabine", 1);

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testSlco1b1TestMulti() throws Exception {
    generalTest("test.slco1b1.multi", new String[]{
            "SLCO1B1/multi.vcf"
        },
        false);

    GeneReport geneReport = s_context.getGeneReport("SLCO1B1");
    assertNotNull(geneReport);
    assertFalse(geneReport.isCalled());

    testCalls(DipType.PRINT, "SLCO1B1", "rs4149056T/rs4149056C");
    testCalls(DipType.LOOKUP, "SLCO1B1", "SLCO1B1:*1A/*5");

    testMatchedGroups("simvastatin", 1);

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1PhasedMulti() throws Exception {
    generalTest("test.ugt1a1.phased.multi", new String[]{
            "UGT1A1/s1s60s80phased.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*1/*60");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*1/*60");

    assertTrue(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1UnphasedMulti() throws Exception {
    generalTest("test.ugt1a1.unphased.multi", new String[]{
            "UGT1A1/s1s60s80unphased.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*60 (heterozygous)");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*1/*1");

    assertFalse(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1S1S28S60S80() throws Exception {
    generalTest("test.ugt1a1.s1s28s60s80unphased", new String[]{
            "UGT1A1/s1s28s60s80unphased.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*80+*28 (heterozygous)", "*28 (heterozygous)", "*60 (heterozygous)");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*1/*80");

    assertFalse(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1S28S37() throws Exception {
    generalTest("test.ugt1a1.s28s37", new String[]{
            "UGT1A1/s28s37.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*28 (heterozygous)", "*37 (heterozygous)");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*80/*80");

    assertFalse(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1s28s80phased() throws Exception {
    generalTest("test.ugt1a1.s28s80phased", new String[]{
            "UGT1A1/s28s80phased.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*1/*28+*80");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*1/*80");

    assertTrue(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1s28s80s6s60phased() throws Exception {
    generalTest("test.ugt1a1.s28s80s6s60phased", new String[]{
            "UGT1A1/s28s80s6s60phased.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*6+*60/*28+*80");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*80/*80");

    assertTrue(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1s28s80s6s60unphased() throws Exception {
    generalTest("test.ugt1a1.s28s80s6s60unphased", new String[]{
            "UGT1A1/s28s80s6s60unphased.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*80+*28 (heterozygous)","*28 (heterozygous)","*6 (heterozygous)","*60 (heterozygous)");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*80/*80");

    assertFalse(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1s28s80unphased() throws Exception {
    generalTest("test.ugt1a1.s28s80unphased", new String[]{
            "UGT1A1/s28s80unphased.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*80+*28 (heterozygous)", "*28 (heterozygous)");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*1/*80");

    assertFalse(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1s6s6() throws Exception {
    generalTest("test.ugt1a1.s6s6", new String[]{
            "UGT1A1/s6s6.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*6/*6");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*6/*6");

    assertTrue(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1s6s60s80s28MissingPhased() throws Exception {
    generalTest("test.ugt1a1.s6s60s80s28MissingPhased", new String[]{
            "UGT1A1/s6s60s80s28missingphased.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*6/*28+*37+*60+*80");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*80/*80");

    assertTrue(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1s6s60s80s28MissingUnphased() throws Exception {
    generalTest("test.ugt1a1.s6s60s80s28MissingUnphased", new String[]{
            "UGT1A1/s6s60s80s28missingunphased.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*6 (heterozygous)","*60 (heterozygous)","*80+*28 (heterozygous)","*80+*37 (heterozygous)");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*80/*80");

    assertFalse(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1s80s28missing() throws Exception {
    generalTest("test.ugt1a1.s80s28missing", new String[]{
            "UGT1A1/s80s28missing.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*80+*28 (heterozygous)", "*80+*37 (heterozygous)");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*1/*80");

    assertFalse(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1na12717() throws Exception {
    generalTest("test.ugt1a1.na12717", new String[]{
            "UGT1A1/NA12717_UGT1A1.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*80+*28 (heterozygous)", "*60 (heterozygous)", "*28 (heterozygous)");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*1/*80");

    assertFalse(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1na18868() throws Exception {
    generalTest("test.ugt1a1.na18868", new String[]{
            "UGT1A1/NA18868_UGT1A1.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*80+*28 (heterozygous)", "*60 (heterozygous)", "*60 (homozygous)", "*28 (heterozygous)");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*1/*80");

    assertFalse(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1na19785() throws Exception {
    generalTest("test.ugt1a1.na19785", new String[]{
            "UGT1A1/NA19785_UGT1A1.vcf"
        },
        false);

    GeneReport geneReport = s_context.getGeneReport("UGT1A1");
    assertNotNull(geneReport);
    assertFalse(geneReport.isCalled());

    assertFalse(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1s28homMissing() throws Exception {
    generalTest("test.ugt1a1.s28s28unphaseds60s80miss", new String[]{
            "UGT1A1/s28s28unphaseds60s80miss.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*28+*80/*28+*80");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*80/*80");

    // sample is effectively phased since all positions homozygous
    assertTrue(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  /**
   * Example of a UGT1A1 report that looks odd. The matcher calls *28/*60 and *60/*60 which then gets translated to the
   * print diplotypes listed below. It looks odd to have both "*60 (homozygous)" and "*60 (heterozygous)". This happens
   * because diplotype calls get translated to the zygosity format one at a time and can't "look ahead" to other
   * matches to check for homozygous calls that could obviate a heterozygous call display.
   *
   * Leaving this here for now but could be addressed in a future release.
   */
  @Test
  public void testUgt1a1s28s60Hom() throws Exception {
    generalTest("test.ugt1a1.s28s60hom", new String[]{
            "UGT1A1/s28s60hom.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*28 (heterozygous)", "*60 (homozygous)", "*60 (heterozygous)");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*1/*80");

    // sample is effectively phased since all positions homozygous
    assertFalse(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1s27s28unphaseds80s60missing() throws Exception {
    generalTest("test.ugt1a1.s27s28unphaseds80s60missing", new String[]{
            "UGT1A1/s27s28unphaseds80s60missing.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*27 (heterozygous)", "*28 (heterozygous)", "*80+*28 (heterozygous)");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*80/*80");

    assertFalse(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1s28hets60homounphaseds80missing() throws Exception {
    generalTest("test.ugt1a1.s28hets60homounphaseds80missing", new String[]{
            "UGT1A1/s28hets60homounphaseds80missing.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*28 (heterozygous)", "*60 (heterozygous)", "*60 (homozygous)", "*80+*28 (heterozygous)");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*1/*80");

    assertFalse(s_context.getGeneReport("UGT1A1").isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1HG00436() throws Exception {
    generalTest("test.ugt1a1.HG00436", new String[]{
            "UGT1A1/HG00436.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*1/*27+*28+*60+*80");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*1/*80");

    GeneReport geneReport = s_context.getGeneReport("UGT1A1");
    assertTrue(geneReport.isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1s1s80s27s60s28missingphased() throws Exception {
    generalTest("test.ugt1a1.s1s80s27s60s28missingphased", new String[]{
            "UGT1A1/s1s80s27s60s28missingphased.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    assertUncallableAllele("UGT1A1", "*28");
    assertUncallableAllele("UGT1A1", "*36");
    assertUncallableAllele("UGT1A1", "*37");
    assertMissingVariant("UGT1A1", "chr2", 233760233);
    testCalls(DipType.PRINT, "UGT1A1", "*1/*27+*28+*37+*60+*80");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*1/*80");

    GeneReport geneReport = s_context.getGeneReport("UGT1A1");
    assertTrue(geneReport.isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1s1s60s80s6phased() throws Exception {
    generalTest("test.ugt1a1.s1s60s80s6phased", new String[]{
            "UGT1A1/s1s60s80s6phased.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*1/*6+*60");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*1/*80");

    GeneReport geneReport = s_context.getGeneReport("UGT1A1");
    assertTrue(geneReport.isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1s1s60s80s28s6phased() throws Exception {
    generalTest("test.ugt1a1.s1s60s80s28s6phased", new String[]{
            "UGT1A1/s1s60s80s28s6phased.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*1/*6+*28+*60+*80");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*1/*80");

    GeneReport geneReport = s_context.getGeneReport("UGT1A1");
    assertTrue(geneReport.isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testUgt1a1s1s37s80s60phased() throws Exception {
    generalTest("test.ugt1a1.s1s37s80s60phased", new String[]{
            "UGT1A1/s1s37s80s60phased.vcf"
        },
        false);

    assertCalledGenes("UGT1A1");
    testCalls(DipType.PRINT, "UGT1A1", "*1/*37+*60+*80");
    testCalls(DipType.LOOKUP, "UGT1A1", "UGT1A1:*1/*80");

    GeneReport geneReport = s_context.getGeneReport("UGT1A1");
    assertTrue(geneReport.isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testCyp3a5Missing3Message() throws Exception {
    String gene = "CYP3A5";

    generalTest("test.cyp3a5.s3missing", new String[]{
            "cyp3a5/s1s1rs776746missing.vcf"
        },
        false);

    assertCalledGenes(gene);
    testCalls(DipType.PRINT, gene, "*1/*1");
    testCalls(DipType.LOOKUP, gene, "CYP3A5:*1/*1");

    // rs776746 should be missing from this report
    assertNotNull(s_context.getGeneReport(gene).getVariantReports());
    assertMissingVariant("CYP3A5", "rs776746");

    // the guideline should have a matching message
    assertTrue(s_context.getGuidelineReports().stream()
        .filter(r -> r.getRelatedDrugs().contains("tacrolimus"))
        .allMatch(r -> r.getMessages().size() > 0));

    assertTrue(s_context.getGeneReport(gene).isPhased());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testTpmtStar1s() throws Exception {
    generalTest("test.tpmt.star1s", new String[]{
            "TPMT/s1ss1ss3.vcf"
        },
        false);

    assertCalledGenes("TPMT");
    testCalls(DipType.PRINT, "TPMT", "*1/*3A");
    testCalls(DipType.LOOKUP, "TPMT", "TPMT:*1/*3A");

    GeneReport tpmtReport = s_context.getGeneReport("TPMT");
    assertEquals(30, tpmtReport.getVariantReports().size());
    assertEquals(1, tpmtReport.getVariantOfInterestReports().size());

    Predicate<VariantReport> singlePosition = r -> r.getDbSnpId() != null && r.getDbSnpId().equals("rs2842934");
    assertTrue(tpmtReport.getVariantOfInterestReports().stream().anyMatch(singlePosition));
    assertTrue(tpmtReport.getVariantOfInterestReports().stream().filter(singlePosition).allMatch(r -> r.getCall().equals("G|G")));

    assertEquals(0, tpmtReport.getHighlightedVariants().size());

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }

  @Test
  public void testTpmtS15OffData() throws Exception {
    generalTest("test.tpmt.s15offdata", new String[] {
            "TPMT/s15offdata.vcf"
        },
        false);

    assertUncalledGenes("TPMT");
    GeneReport report = s_context.getGeneReport("TPMT");
    assertTrue(report.getVariantReports().stream().filter(r -> r.getPosition() == 18133890).allMatch(VariantReport::isMismatch));
  }


  @Test
  public void testCombined() throws Exception {
    generalTest("test.combined", new String[]{
            "DPYD/s1s1.vcf",
            "UGT1A1/s1s1.vcf",
            "TPMT/s1s1.vcf",
            "cyp3a5/s1s7.vcf",
            "cftr/refref.vcf",
            "cyp2c19/s2s2.vcf",
            "cyp2c9/s2s3.vcf",
            "SLCO1B1/s1as1a.vcf",
            "VKORC1/-1639A-1639A.vcf",
            "cyp4f2/s1s1.vcf",
            "IFNL3/rs12979860CC.vcf"
        },
        true);

    assertCalledGenes("DPYD", "UGT1A1", "TPMT", "CYP3A5", "CFTR", "CYP2C19",
        "CYP2C9", "SLCO1B1", "VKORC1", "CYP4F2", "IFNL3", "CYP2D6");
    testCalls(DipType.PRINT, "TPMT", "*1/*1");
    testCalls(DipType.PRINT, "DPYD", "No CPIC decreased or no function variant with strong or moderate evidence found");
    testCalls(DipType.PRINT, "CYP2C19", "*2/*2");
    testCalls(DipType.LOOKUP, "TPMT", "TPMT:*1/*1");
    testCalls(DipType.PRINT, "CYP2D6", "*1/*4");
    testCalls(DipType.PRINT, "UGT1A1", "*1/*1");

    assertTrue("Should be no incidental alleles", s_context.getGeneReports().stream().noneMatch(GeneReport::isIncidental));
  }


  /**
   * Runs the PharmCAT tool for the given example gene call data
   */
  private void generalTest(String name, String[] geneCalls, boolean includeAstrolabe) throws Exception {
    Path tempVcfPath = Files.createTempFile(name, ".vcf");
    try (FileWriter fw = new FileWriter(tempVcfPath.toFile())) {
      fw.write(VcfTestUtils.writeVcf(geneCalls));
    } catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }

    Path astrolabePath = includeAstrolabe ? s_tempAstroPath : null;
    s_pharmcat.execute(tempVcfPath, astrolabePath, null);
    s_context = s_pharmcat.getReporter().getContext();

    assertEquals(14, s_context.getGeneReports().size());
    assertEquals(32, s_context.getGuidelineReports().size());
  }

  /**
   * Test the different types of diplotype calls that come out of the reporter
   * @param type what type of diplotype to test
   * @param gene the gene to get diplotypes for
   * @param calls the calls to match against
   */
  private void testCalls(DipType type, String gene, String... calls) {
    GeneReport geneReport = s_context.getGeneReport(gene);

    Collection<String> dips = type == DipType.PRINT ?
        geneReport.printDisplayCalls()
        : new ArrayList<>(geneReport.getDiplotypeLookupKeys());

    assertEquals(gene + " call count doesn't match " + String.join(";", dips), calls.length, dips.size());

    Arrays.stream(calls)
        .forEach(c -> assertTrue(c + " not in "+type+" for " + gene + ":" + dips + printDiagnostic(geneReport), dips.contains(c)));
  }

  private static String printDiagnostic(GeneReport geneReport) {
    return String.format(
        sf_diplotypesTemplate,
        geneReport.getMatcherDiplotypes().toString(),
        geneReport.getReporterDiplotypes().toString(),
        geneReport.printDisplayCalls(),
        geneReport.getDiplotypeLookupKeys()
    );
  }

  /**
   * Check to see if all the given genes have been called
   */
  private void assertCalledGenes(String... genes) {
    assertTrue(genes != null && genes.length > 0);

    Arrays.stream(genes)
        .forEach(g -> assertTrue(g + " is not called", s_context.getGeneReport(g).isCalled()));
  }

  /**
   * Check to see if none ofthe given genes have been called
   */
  private void assertUncalledGenes(String... genes) {
    assertTrue(genes != null && genes.length > 0);

    Arrays.stream(genes)
        .forEach(g -> assertFalse(g + " is called", s_context.getGeneReport(g).isCalled()));
  }

  private void testMatchedGroups(String drugName, int count) {
    Stream<GuidelineReport> guidelineStream = s_context.getGuidelineReports().stream()
        .filter(r -> r.getRelatedDrugs().contains(drugName));

    if (count > 0) {
      assertTrue(
          drugName + " does not have matching group count of " + count,
          guidelineStream.allMatch(r -> r.getMatchingGroups().size() == count));
    }
    else {
      assertTrue(
          guidelineStream.allMatch(g -> g.getMatchingGroups() == null || g.getMatchingGroups().size() == 0));
    }
  }

  /**
   * Assert that a variant identified by RSID is in the gene report but marked as missing. Don't use this method if the 
   * variant has no RSID, 
   * @param geneSymbol a Gene symbol
   * @param rsid the dbSNP RSID of the variant
   */
  private void assertMissingVariant(String geneSymbol, String rsid) {
    GeneReport gene = s_context.getGeneReport(geneSymbol);
    VariantReport variant = gene.getVariantReports().stream()
        .filter(v -> v.getDbSnpId() != null)
        .filter(v -> v.getDbSnpId().equals(rsid))
        .findFirst()
        .orElseThrow(() -> new RuntimeException(rsid + " record should exist but does not"));
    assertTrue(variant.isMissing());
  }

  /**
   * Assert that a variant identified by chromosome and position is in the gene report but marked as missing
   * @param geneSymbol a Gene symbol
   * @param chr a chromosome name in the form "chr##"
   * @param position the position of the variant
   */
  private void assertMissingVariant(String geneSymbol, String chr, int position) {
    GeneReport gene = s_context.getGeneReport(geneSymbol);
    VariantReport variant = gene.getVariantReports().stream()
        .filter(v -> v.getChr().equals(chr) && v.getPosition() == position)
        .findFirst()
        .orElseThrow(() -> new RuntimeException(chr + ":" + position + " record should exist but does not"));
    assertTrue(variant.isMissing());
  }

  /**
   * Assert that the given allele un uncallable
   * @param geneSymbol a Gene symbol
   * @param allele the name of an allele (e.g. "*2")
   */
  private void assertUncallableAllele(String geneSymbol, String allele) {
    GeneReport gene = s_context.getGeneReport(geneSymbol);
    assertTrue(gene.getUncalledHaplotypes().contains(allele));
  }
  
  private enum DipType {
    PRINT, // the diplotype that is displayed to the end-user
    LOOKUP // the diplotype used to lookup annotations
  }
}
