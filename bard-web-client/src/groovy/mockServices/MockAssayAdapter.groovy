package mockServices
import bard.core.interfaces.*
import bard.core.rest.spring.assays.BardAnnotation
import bard.core.rest.spring.assays.Annotation
import bard.core.rest.spring.assays.Context
import bard.core.rest.spring.assays.MinimumAnnotation
import bard.core.rest.spring.util.NameDescription
/**
 * Created with IntelliJ IDEA.
 * User: jasiedu
 * Date: 3/10/13
 * Time: 8:36 PM
 * To change this template use File | Settings | File Templates.
 */
class MockAssayAdapter implements AssayAdapterInterface {
    String name
    Long assayId
    AssayCategory category
    String protocol
    String comments
    String description
    AssayType assayType
    AssayRole assayRole
    String source
    Long aid

    @Override
    String getHighlight() {
        return "Score: 2.0, Matched Field: Name"
    }

    MockAssayAdapter() {
        super()
    }

    public BardAnnotation getAnnotations() {
        BardAnnotation annotation = new BardAnnotation()
        Annotation comp = new Annotation(key: "target", value: "WEE1", display: "WEE1", entity: "assay")
        Context context = new Context(name: "Biology", contextItems: [comp])
        annotation.contexts.add(context)
        return annotation
    }

    @Override
    List<String> getKeggDiseaseNames() {
        return ["Neurodegenerative disease", "Neurodegenerative disease", "Neurodegenerative disease"]
    }

    @Override
    List<String> getKeggDiseaseCategories() {
        return ["Amyotrophic lateral sclerosis (ALS)", "Lou Gehrig's disease", "Progressive supranuclear palsy (PSP)"]
    }

    @Override
    Map<String, List<String>> getKeggAnnotations() {
        Map<String, List<String>> annos = new HashMap<String, List<String>>()
        annos.put(EntityNamedSources.KEGGDiseaseCategoryAnnotationSource, ["Amyotrophic lateral sclerosis (ALS)", "Lou Gehrig's disease", "Progressive supranuclear palsy (PSP)"])
        annos.put(EntityNamedSources.KEGGDiseaseNameAnnotationSource, ["Neurodegenerative disease", "Neurodegenerative disease", "Neurodegenerative disease"])
        return annos;
    }

    @Override
    MinimumAnnotation getMinimumAnnotation() {
        return new MinimumAnnotation(assayType: "signal transduction assay", assayFormat: "cell-based format",
                detectionMethodType: "luminescence method", assayFootprint: "1536-well plate")
    }

    @Override
    Long getCapAssayId() {
        return this.assayId ?: 233  //To change body of implemented methods use File | Settings | File Templates.
    }

    Double getScore() {
        return 1.0
    }

    NameDescription getMatchingField() {
        return new NameDescription(name: "Test Name", description: "Test Desc")
    }

    String getTitle() {
        return name
    }

    Long getBardAssayId() {
        return this.assayId
    }

    @Override
    AssayType getType() {
        return this.assayType ?: AssayType.Confirmatory  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    AssayRole getRole() {
        return assayRole ?: AssayRole.SecondaryConfirmation  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    AssayCategory getCategory() {
        return category ?: AssayCategory.MLPCN
    }

    String getLastUpdatedDate() {
        return new Date().toString()
    }

    @Override
    String getDescription() {
        return this.description ?: '''
                    The Y-family of DNA polymerases, such as Pol eta, are specifically involved in DNA repair.
                    Pol eta copies undamaged DNA with a lower fidelity than other DNA-directed polymerases.
                    However, it accurately replicates UV-damaged DNA; when thymine dimers are present, this polymerase inserts the
                    complementary nucleotides in the newly synthesized DNA, thereby bypassing the lesion and suppressing the mutagenic
                    effect of UV-induced DNA damage. Pol eta has the ability to bypass cisplatinated DNA adducts in vitro, and it
                    has been suggested that pol eta-dependent bypass of the cisplatin lesion in vivo leads to increased tumor resistance.
                    Thus, while pol eta's (and most likely iota's) normal function is to protect humans against the deleterious consequences
                    of DNA damage, under certain conditions they can have deleterious effects on human health.
                    As a consequence, we propose to utilize a high throughput replication assay to
                    identify small molecule inhibitors of pol eta.

                    In a collaboration between the National Institute of Child Health & Human Development (NICHD) and NIH Chemical
                    Genomics Center, a high-throughput, fluorescent screen was developed to screen the NIH Molecular Libraries Small
                    Molecule Repository (MLSMR). This screen is used to identify inhibitors of pol eta.

                    NIH Chemical Genomics Center [NCGC]
                    NIH Molecular Libraries Probe Centers Network [MLPCN]

                    MLPCN Grant: MH090825
                    Assay Submitter (PI): Roger Woodgate, NICHD)
                '''
    }

    @Override
    Long getId() {
        return assayId ?: 22
    }

    @Override
    String getProtocol() {
        return this.protocol ?: "Protocol"
    }

    @Override
    String getComments() {
        return this.comments ?: "Comments"
    }

    @Override
    Long getAid() {
        return this.aid ?: 244
    }

    @Override
    String getSource() {
        return this.source ?: "Source"  //To change body of implemented methods use File | Settings | File Templates.
    }
    @Override
    String getAssayStatus() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getDesignedBy() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getAssayTypeString() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }
}