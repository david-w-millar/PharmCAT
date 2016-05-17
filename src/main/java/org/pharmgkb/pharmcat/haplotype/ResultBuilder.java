package org.pharmgkb.pharmcat.haplotype;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.pharmgkb.pharmcat.definition.model.DefinitionFile;
import org.pharmgkb.pharmcat.definition.model.NamedAllele;
import org.pharmgkb.pharmcat.definition.model.VariantLocus;
import org.pharmgkb.pharmcat.haplotype.model.DiplotypeMatch;
import org.pharmgkb.pharmcat.haplotype.model.GeneCall;
import org.pharmgkb.pharmcat.haplotype.model.HaplotyperResult;
import org.pharmgkb.pharmcat.haplotype.model.Metadata;
import org.pharmgkb.pharmcat.haplotype.model.Variant;


/**
 * This class collects results for the {@link Haplotyper}.
 *
 * @author Mark Woon
 */
public class ResultBuilder {
  private static final Joiner sf_vcfAlleleJoiner = Joiner.on(",");
  private DefinitionReader m_definitionReader;
  private HaplotyperResult m_result = new HaplotyperResult();
  private SimpleDateFormat m_dateFormat = new SimpleDateFormat("MM/dd/yy");


  public ResultBuilder(@Nonnull DefinitionReader definitionReader) {
    Preconditions.checkNotNull(definitionReader);
    m_definitionReader = definitionReader;
  }

  public HaplotyperResult build() {
    return m_result;
  }


  public ResultBuilder forFile(@Nonnull Path vcfFile) {
    Preconditions.checkNotNull(vcfFile);
    Preconditions.checkArgument(vcfFile.toString().endsWith(".vcf"));
    Preconditions.checkArgument(Files.isRegularFile(vcfFile));

    Metadata md = new Metadata();
    //md.setCpicAnnotatorBuild();
    //md.setCpicDataBuild();
    md.setDate(new Date());
    //md.setGenomeAssembly();
    md.setInputFile(vcfFile.getName(vcfFile.getNameCount() - 1).toString());
    m_result.setMetadata(md);

    return this;
  }


  protected ResultBuilder gene(@Nonnull String gene, @Nonnull MatchData matchData,
      @Nonnull List<DiplotypeMatch> matches) {
    Preconditions.checkNotNull(gene);
    Preconditions.checkNotNull(matches);

    DefinitionFile tsvFile = m_definitionReader.getDefinitionFile(gene);
    String definitionVersion = tsvFile.getContentVersion() + " (" + m_dateFormat.format(tsvFile.getModificationDate()) + ")";
    String chromosome = tsvFile.getChromosome();

    Set<String> matchableHaps = matchData.getHaplotypes().stream()
        .map(NamedAllele::getName)
        .collect(Collectors.toSet());
    Set<String> uncallableHaplotypes = m_definitionReader.getHaplotypes(gene).stream()
        .map(NamedAllele::getName)
        .filter(n -> !matchableHaps.contains(n))
        .collect(Collectors.toSet());

    GeneCall geneCall = new GeneCall(definitionVersion, chromosome, gene, matchData, uncallableHaplotypes);

    // get haplotype/diplotype info
    for (DiplotypeMatch dm : matches) {
      geneCall.addDiplotype(dm);
    }

    // get position info
    for (VariantLocus variant : matchData.getPositions()) {
      SampleAllele allele = matchData.getSampleAllele(variant.getVcfPosition());
      String call;
      String vcfAlleles = sf_vcfAlleleJoiner.join(allele.getVcfAlleles());
      if (allele.isPhased()) {
        call = allele.getAllele1() + "|" + allele.getAllele2();
      } else {
        call = allele.getAllele1() + "/" + allele.getAllele2();
      }
      geneCall.add(new Variant(variant.getPosition(), variant.getRsid(), call, variant.getVcfPosition(), vcfAlleles));
    }

    m_result.addDiplotypeCall(geneCall);

    return this;
  }
}