package org.pharmgkb.pharmcat.haplotype;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pharmgkb.pharmcat.TestUtil;
import org.pharmgkb.pharmcat.definition.model.NamedAllele;
import org.pharmgkb.pharmcat.definition.model.VariantLocus;
import org.pharmgkb.pharmcat.haplotype.model.DiplotypeMatch;
import org.pharmgkb.pharmcat.haplotype.model.HaplotypeMatch;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


/**
 * JUnit test for {@link DiplotypeMatcher}.
 *
 * @author Mark Woon
 */
public class DiplotypeMatcherTest {
  private static VariantLocus[] s_positions;
  private static List<NamedAllele> s_haplotypes;


  @BeforeClass
  public static void beforeClass() {

    // initialize test variants
    VariantLocus var1 = new VariantLocus(1, "g.1T>A");
    VariantLocus var2 = new VariantLocus(2, "g.2T>A");
    VariantLocus var3 = new VariantLocus(3, "g.3T>A");

    s_positions = new VariantLocus[] { var1, var2, var3 };

    // initialize test haplotypes
    NamedAllele hap1 = new NamedAllele("*1", "*1", new String[] { "A", "C", "C" });
    hap1.finalize(s_positions);

    NamedAllele hap2 = new NamedAllele("*4a", "*4a", new String[] { "G", null, null });
    hap2.finalize(s_positions);

    NamedAllele hap3 = new NamedAllele("*4b", "*4b", new String[] { "G", "T", "T" });
    hap3.finalize(s_positions);

    NamedAllele hap4 = new NamedAllele("*17", "*17", new String[] { null, "T", "T" });
    hap4.finalize(s_positions);

    s_haplotypes = Lists.newArrayList(hap1, hap2, hap3, hap4);

    /*
            | 1 | 2 | 3 |
        *1  | A | C | C |
        *4a | G |   |   |
        *4b | G | T | T |
        *17 |   | T | T |
    */
  }


  @Test
  public void test1() throws Exception {

    List<SampleAllele> alleles = Arrays.asList(
        new SampleAllele("chr1", 1, "A", "G", false, Lists.newArrayList("A", "G")),
        new SampleAllele("chr1", 2, "C", "C", false, Lists.newArrayList("C", "T")),
        new SampleAllele("chr1", 3, "C", "C", false, Lists.newArrayList("C", "T"))
    );

    List<DiplotypeMatch> pairMatches = computeHaplotypes(alleles);
    List<String> expectedMatches = Lists.newArrayList("*1/*4a");
    TestUtil.assertDiplotypePairs(expectedMatches, pairMatches);
  }


  @Test
  public void test2() throws Exception {

    List<SampleAllele> alleles = Arrays.asList(
        new SampleAllele("chr1", 1, "A", "G", false, Lists.newArrayList("A", "G")),
        new SampleAllele("chr1", 2, "C", "T", false, Lists.newArrayList("C", "T")),
        new SampleAllele("chr1", 3, "C", "T", false, Lists.newArrayList("C", "T"))
    );

    List<DiplotypeMatch> pairMatches = computeHaplotypes(alleles);
    List<String> expectedMatches = Lists.newArrayList("*1/*4b", "*1/*17", "*1/*4a", "*4a/*17");
    TestUtil.assertDiplotypePairs(expectedMatches, pairMatches);
  }


  @Test
  public void test3() throws Exception {

    List<SampleAllele> alleles = Arrays.asList(
        new SampleAllele("chr1", 1, "A", "A", false, Lists.newArrayList("A", "G")),
        new SampleAllele("chr1", 2, "C", "T", false, Lists.newArrayList("C", "T")),
        new SampleAllele("chr1", 3, "C", "T", false, Lists.newArrayList("C", "T"))
    );

    List<DiplotypeMatch> pairMatches = computeHaplotypes(alleles);
    List<String> expectedMatches = Lists.newArrayList("*1/*17");
    TestUtil.assertDiplotypePairs(expectedMatches, pairMatches);
  }


  @Test
  public void test4() throws Exception {

    List<SampleAllele> alleles = Arrays.asList(
        new SampleAllele("chr1", 1, "G", "G", false, Lists.newArrayList("A", "G")),
        new SampleAllele("chr1", 2, "T", "T", false, Lists.newArrayList("C", "T")),
        new SampleAllele("chr1", 3, "C", "T", false, Lists.newArrayList("C", "T"))
    );

    List<DiplotypeMatch> pairMatches = computeHaplotypes(alleles);
    List<String> expectedMatches = Lists.newArrayList("*4a/*4b", "*4a/*17", "*4a/*4a");
    TestUtil.assertDiplotypePairs(expectedMatches, pairMatches);
  }


  @Test
  public void test5() throws Exception {

    List<SampleAllele> alleles = Arrays.asList(
        new SampleAllele("chr1", 1, "G", "G", false, Lists.newArrayList("A", "G")),
        new SampleAllele("chr1", 2, "C", "T", false, Lists.newArrayList("C", "T")),
        new SampleAllele("chr1", 3, "C", "T", false, Lists.newArrayList("C", "T"))
    );
    List<DiplotypeMatch> pairMatches = computeHaplotypes(alleles);
    List<String> expectedMatches = Lists.newArrayList("*4a/*4b", "*4a/*17", "*4a/*4a");
    TestUtil.assertDiplotypePairs(expectedMatches, pairMatches);
  }


  private List<DiplotypeMatch> computeHaplotypes(List<SampleAllele> alleles) {

    SortedMap<String, SampleAllele> sampleAlleleMap = alleles.stream()
        .collect(Collectors.toMap(s -> "chr1:" + s.getPosition(),
        Function.identity(), new NonnullMergeFunction<>(), TreeMap::new));

    MatchData dataset = new MatchData(sampleAlleleMap, "chr1", s_positions);
    dataset.marshallHaplotypes(s_positions, s_haplotypes);
    dataset.generateSamplePermutations();

    return new DiplotypeMatcher(dataset).compute();
  }


  public static class NonnullMergeFunction<T> implements BinaryOperator<T> {

    @Override
    public T apply(T o, T o2) {
      throw new RuntimeException(String.format("Duplicate key %s", o));
    }
  }


  @Test
  public void testComparePermutations() throws Exception {

    VariantLocus var1 = new VariantLocus(1, "g.1T>A");
    VariantLocus var2 = new VariantLocus(2, "g.2T>A");
    VariantLocus var3 = new VariantLocus(3, "g.3T>A");
    VariantLocus var4 = new VariantLocus(4, "g.3T>A");
    VariantLocus[] variants = new VariantLocus[] { var1, var2, var3, var4 };

    NamedAllele hap1 = new NamedAllele("*1", "*1", new String[] { "T", "A", "C", "C" });
    hap1.finalize(variants);

    NamedAllele hap2 = new NamedAllele("*2", "*2", new String[] { null, "T", "C", null });
    hap2.finalize(variants);

    NamedAllele hap3 = new NamedAllele("*3", "*3", new String[] { null, null, "GG", null });
    hap3.finalize(variants);

    Set<String> permutations = Sets.newHashSet(
        "1:T;2:A;3:C;4:C;",
        "1:T;2:A;3:C;4:G;",
        "1:T;2:T;3:C;4:C;",
        "1:T;2:T;3:C;4:G;"
    );
    SortedMap<String, SampleAllele> sampleAlleleMap = new TreeMap<>();
    sampleAlleleMap.put("chr1:1", new SampleAllele("chr1", 1, "T", "T", true, Lists.newArrayList("T")));
    sampleAlleleMap.put("chr1:2", new SampleAllele("chr1", 2, "A", "T", false, Lists.newArrayList("T")));
    sampleAlleleMap.put("chr1:3", new SampleAllele("chr1", 3, "C", "C", false, Lists.newArrayList("C")));
    sampleAlleleMap.put("chr1:4", new SampleAllele("chr1", 4, "C", "G", false, Lists.newArrayList("C")));

    MatchData dataset = new MatchData(sampleAlleleMap, "chr1", variants);
    dataset.marshallHaplotypes(variants, Lists.newArrayList(hap1, hap2, hap3));
    dataset.generateSamplePermutations();
    assertThat(dataset.getPermutations(), equalTo(permutations));

    DiplotypeMatcher diplotypeMatcher = new DiplotypeMatcher(dataset);

    SortedSet<HaplotypeMatch> matches = diplotypeMatcher.comparePermutations();
    assertEquals(2, matches.size());
    Iterator<HaplotypeMatch> it = matches.iterator();
    assertEquals(hap1, it.next().getHaplotype());
    assertEquals(hap2, it.next().getHaplotype());
  }
}
