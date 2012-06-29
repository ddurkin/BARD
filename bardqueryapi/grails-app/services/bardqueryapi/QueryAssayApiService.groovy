package bardqueryapi

import grails.converters.JSON

class QueryAssayApiService {

    def grailsApplication
    QueryExecutorService queryExecutorService
    static String[] breakApartDistinctStrings(String inputString) {
        def outputList = inputString.split()
        return outputList.findAll { it.size() > 0 }.unique()
    }
    /**
     * v1/assays/{aid} - JSON representation of an assay, identified by its AID.
     * @param assayId
     */
    def findAssayByAid(String assayAidUrl) {
        final String url = grailsApplication.config.ncgc.server.root.url + assayAidUrl
         return queryExecutorService.executeGetRequestJSON(url, null)
    }
    /**
     * v1/assays/{aid}/targets - list of paths to protein targets, annotated for this assay
     * @param assayId
     */
    def findProteinTargetsByAssay(String assayAidUrl) {
        final String url = grailsApplication.config.ncgc.server.root.url + assayAidUrl + "/targets"
        println url
        return queryExecutorService.executeGetRequestJSON(url, null)
     }
    /**
     * v1/assays/{aid}/publications - list of paths to documents, annotated for this assay.
     * Currently documents are identified by Pubmed ID.
     * @param assayId
     */
    def findPublicationsByAssay(String assayAidUrl) {
        final String url = grailsApplication.config.ncgc.server.root.url + assayAidUrl + "/publications"
        println url
        return queryExecutorService.executeGetRequestJSON(url, null)
    }
    /**
     * v1/assays/{aid}/compounds - by default, a JSON list of paths to compounds identified by CID.
     * Given that some assays may have very large numbers of compounds, the resource will return a 413 (Request too large)
     * response if there are more than 1000 compounds to be returned.In such cases, obtain the number of compounds
     * (via the sample field of the assay entity).Compounds can be also be retrieved in
     *  SMILES or SDF format by specifying the appropriate chemical MIME type in the “ Accepts ” header.
     * @param assayId
     */
    def findCompoundsByAssay(String assayAidUrl, Map headers) {
        final String url = grailsApplication.config.ncgc.server.root.url + assayAidUrl + "/compounds"
        println url
        return queryExecutorService.executeGetRequestJSON(url, headers)
    }

    Integer getTotalAssayCompounds (Integer assayId) {
        final String assayResourceUrl = "${grailsApplication.config.ncgc.server.root.url}/bard/rest/v1/experiments/${assayId}"
        final wslite.json.JSONObject assayJson = queryExecutorService.executeGetRequestJSON(assayResourceUrl, null) //get the Assay instance
        final Integer totalCompounds = assayJson.compounds ?: 0
        return totalCompounds
    }

    /**
     * Returns a 'page' list of CIDs from an assay's compounds result set, based on Offset and Max
     * @param max
     * @param offset
     * @param assayId
     * @return List<String> of CIDs (size==max)
     */
    List<String> getAssayCompoundsResultset(Integer max, Integer offset, Integer assayId) {
        final String assayUrlPaging = "${grailsApplication.config.ncgc.server.root.url}/bard/rest/v1/experiments/${assayId}/compounds?skip=${offset}&top=${max}" //NCGS' max and offset
        final wslite.json.JSONObject assayCompoundsJson = queryExecutorService.executeGetRequestJSON(assayUrlPaging, null)
        if (assayCompoundsJson.collection) {
            List<String> compoundUrlList = assayCompoundsJson.collection.toList()
            //strip the CID from the ending of the compound resource url (e.g., /bard/rest/v1/compounds/661090 --> 661090)
            return compoundUrlList.collect { compoundResourceUrl ->
                compoundResourceUrl.split('/').toList().last()
            }
        }

        return []
    }
}
class MockQueryAssayApiService{
    def findAssays() {
        return """
[summary:0, deposited:null, data:null, aid:493232, type:1, assays:6, samples:1807, resourcePath:/bard/rest/v1/assays/493232, grantNo:R01 DA023915-02, category:2, publications:[[resourcePath:/bard/rest/v1/documents/17629964, title:Versatility of GPCR recognition by drugs: from biological implications to therapeutic relevance., pubmedId:17629964, abs:Most drugs acting on G-protein-coupled receptors (GPCRs) are classically defined as agonists, partial agonists or antagonists. This simplified classification seems sufficient to explain most of their therapeutic properties. The more recent description of inverse agonism has helped to revise theoretical models of GPCR function, but the therapeutic implications of the new concepts remain clearly restricted. Further complexity has arisen with demonstrations that a given receptor can adopt various conformations that support coupling with distinct G proteins. Because the related signaling pathways seem to be differentially affected by some ligands, the concept of 'functional selectivity' has been proposed, calling for a revision of the definitions of agonism and intrinsic efficacy. Evidence of complexity in G-protein coupling and examples of functional selectivity are accumulating, opening perspectives for drug development. Although such complexity should be regarded as an opportunity to gain pharmacological specificity, unraveling the physiological implications of these concepts is essential before their therapeutic relevance can be defined., doi:10.1016/j.tips.2007.06.001], [resourcePath:/bard/rest/v1/documents/18382464, title:Structural diversity of G protein-coupled receptors and significance for drug discovery., pubmedId:18382464, abs:G protein-coupled receptors (GPCRs) are the largest family of membrane-bound receptors and also the targets of many drugs. Understanding of the functional significance of the wide structural diversity of GPCRs has been aided considerably in recent years by the sequencing of the human genome and by structural studies, and has important implications for the future therapeutic potential of targeting this receptor family. This article aims to provide a comprehensive overview of the five main human GPCR families--Rhodopsin, Secretin, Adhesion, Glutamate and Frizzled/Taste2--with a focus on gene repertoire, general ligand preference, common and unique structural features, and the potential for future drug discovery., doi:10.1038/nrd2518], [resourcePath:/bard/rest/v1/documents/17959251, title:Modulation of pain transmission by G-protein-coupled receptors., pubmedId:17959251, abs:The heterotrimeric G-protein-coupled receptors (GPCR) represent the largest and most diverse family of cell surface receptors and proteins. GPCR are widely distributed in the peripheral and central nervous systems and are one of the most important therapeutic targets in pain medicine. GPCR are present on the plasma membrane of neurons and their terminals along the nociceptive pathways and are closely associated with the modulation of pain transmission. GPCR that can produce analgesia upon activation include opioid, cannabinoid, alpha2-adrenergic, muscarinic acetylcholine, gamma-aminobutyric acidB (GABAB), groups II and III metabotropic glutamate, and somatostatin receptors. Recent studies have led to a better understanding of the role of these GPCR in the regulation of pain transmission. Here, we review the current knowledge about the cellular and molecular mechanisms that underlie the analgesic actions of GPCR agonists, with a focus on their effects on ion channels expressed on nociceptive sensory neurons and on synaptic transmission at the spinal cord level., doi:10.1016/j.pharmthera.2007.09.003], [resourcePath:/bard/rest/v1/documents/18370232, title:Pharmacogenomics of G protein-coupled receptor signaling: insights from health and disease., pubmedId:18370232, abs:The identification and characterization of the processes of G protein-coupled receptor (GPCR) activation and inactivation have refined not only the study of the GPCRs but also the genomics of many accessory proteins necessary for these processes. This has accelerated progress in understanding the fundamental mechanisms involved in GPCR structure and function, including receptor transport to the membrane, ligand binding, activation and inactivation by GRK-mediated (and other) phosphorylation. The catalog of G(s)alpha and Gbeta subunit polymorphisms that result in complex phenotypes has complemented the effort to catalog the GPCRs and their variants. The study of the genomics of GPCR accessory proteins has also provided insight into pathways of disease, such as the contributions of regulator of G protein signaling (RGS) protein to hypertension and activator of G protein signaling (AGS) proteins to the response to hypoxia. In the case of the G protein-coupled receptor kinases (GRKs), identified originally in the retinal tissues that converge on rhodopsin, proteins such as GRK4 have been identified that have been subsequently associated with hypertension. Here, we review the structure and function of GPCR and associated proteins in the context of the gene families that encode them and the genetic disorders associated with their altered function. An understanding of the pharmacogenomics of GPCR signaling provides the basis for examining the GPCRs disrupted in monogenic disease and the pharmacogenetics of a given receptor system., doi:10.1007/978-1-59745-205-2_6], [resourcePath:/bard/rest/v1/documents/15217328, title:Desensitization of G protein-coupled receptors and neuronal functions., pubmedId:15217328, abs:G protein-coupled receptors (GPCRs) have proven to be the most highly favorable class of drug targets in modern pharmacology. Over 90% of nonsensory GPCRs are expressed in the brain, where they play important roles in numerous neuronal functions. GPCRs can be desensitized following activation by agonists by becoming phosphorylated by members of the family of G protein-coupled receptor kinases (GRKs). Phosphorylated receptors are then bound by arrestins, which prevent further stimulation of G proteins and downstream signaling pathways. Discussed in this review are recent progress in understanding basics of GPCR desensitization, novel functional roles, patterns of brain expression, and receptor specificity of GRKs and beta arrestins in major brain functions. In particular, screening of genetically modified mice lacking individual GRKs or beta arrestins for alterations in behavioral and biochemical responses to cocaine and morphine has revealed a functional specificity in dopamine and mu-opioid receptor regulation of locomotion and analgesia. An important and specific role of GRKs and beta arrestins in regulating physiological responsiveness to psychostimulants and morphine suggests potential involvement of these molecules in certain brain disorders, such as addiction, Parkinson's disease, mood disorders, and schizophrenia. Furthermore, the utility of a pharmacological strategy aimed at targeting this GPCR desensitization machinery to regulate brain functions can be envisaged., doi:10.1146/annurev.neuro.27.070203.144206], [resourcePath:/bard/rest/v1/documents/18423425, title:Orexin mediates the expression of precipitated morphine withdrawal and concurrent activation of the nucleus accumbens shell., pubmedId:18423425, abs:The lateral hypothalamic neuropeptide orexin (or hypocretin) is implicated in drug addiction. Although a role for orexin has been shown in reward and dependence, the molecular and neural mechanisms are unclear. We investigated the mechanism and neuroanatomic basis of orexin's role in morphine withdrawal., doi:10.1016/j.biopsych.2008.03.006], [resourcePath:/bard/rest/v1/documents/9419374, title:The hypocretins: hypothalamus-specific peptides with neuroexcitatory activity., pubmedId:9419374, abs:We describe a hypothalamus-specific mRNA that encodes preprohypocretin, the putative precursor of a pair of peptides that share substantial amino acid identities with the gut hormone secretin. The hypocretin (Hcrt) protein products are restricted to neuronal cell bodies of the dorsal and lateral hypothalamic areas. The fibers of these neurons are widespread throughout the posterior hypothalamus and project to multiple targets in other areas, including brainstem and thalamus. Hcrt immunoreactivity is associated with large granular vesicles at synapses. One of the Hcrt peptides was excitatory when applied to cultured, synaptically coupled hypothalamic neurons, but not hippocampal neurons. These observations suggest that the hypocretins function within the CNS as neurotransmitters., doi:null], [resourcePath:/bard/rest/v1/documents/9491897, title:Orexins and orexin receptors: a family of hypothalamic neuropeptides and G protein-coupled receptors that regulate feeding behavior., pubmedId:9491897, abs:The hypothalamus plays a central role in the integrated control of feeding and energy homeostasis. We have identified two novel neuropeptides, both derived from the same precursor by proteolytic processing, that bind and activate two closely related (previously) orphan G protein-coupled receptors. These peptides, termed orexin-A and -B, have no significant structural similarities to known families of regulatory peptides. prepro-orexin mRNA and immunoreactive orexin-A are localized in neurons within and around the lateral and posterior hypothalamus in the adult rat brain. When administered centrally to rats, these peptides stimulate food consumption. prepro-orexin mRNA level is up-regulated upon fasting, suggesting a physiological role for the peptides as mediators in the central feedback mechanism that regulates feeding behavior., doi:null], [resourcePath:/bard/rest/v1/documents/11394998, title:Genetic ablation of orexin neurons in mice results in narcolepsy, hypophagia, and obesity., pubmedId:11394998, abs:Orexins (hypocretins) are a pair of neuropeptides implicated in energy homeostasis and arousal. Recent reports suggest that loss of orexin-containing neurons occurs in human patients with narcolepsy. We generated transgenic mice in which orexin-containing neurons are ablated by orexinergic-specific expression of a truncated Machado-Joseph disease gene product (ataxin-3) with an expanded polyglutamine stretch. These mice showed a phenotype strikingly similar to human narcolepsy, including behavioral arrests, premature entry into rapid eye movement (REM) sleep, poorly consolidated sleep patterns, and a late-onset obesity, despite eating less than nontransgenic littermates. These results provide evidence that orexin-containing neurons play important roles in regulating vigilance states and energy homeostasis. Orexin/ataxin-3 mice provide a valuable model for studying the pathophysiology and treatment of narcolepsy., doi:null], [resourcePath:/bard/rest/v1/documents/19656173, title:Orexin/hypocretin signaling at the orexin 1 receptor regulates cue-elicited cocaine-seeking., pubmedId:19656173, abs:The orexin/hypocretin system has recently been implicated in reward-processing and addiction. We examined the involvement of the orexin system in cue-induced reinstatement of extinguished cocaine-seeking by administering the orexin 1 receptor antagonist SB-334867 (SB) or the orexin 2 receptor antagonist 4-pyridylmethyl (S)-tert-leucyl 6,7-dimethoxy-1,2,3,4-tetrahydroisoquinoline (4PT) prior to reinstatement testing. Male Sprague Dawley rats self-administered cocaine in 2-h sessions for 10 days, followed by extinction training. Reinstatement of cocaine-seeking was elicited by presentation of tone + light cues previously paired with cocaine infusions. SB (10, 20 and 30 mg/kg) dose-dependently decreased cue-induced reinstatement of cocaine-seeking without significantly affecting responding during late extinction. 4PT (10 and 30 mg/kg) did not significantly alter cue-induced reinstatement. In separate experiments, the highest doses of SB and 4PT had no significant effect on established cocaine self-administration, and 4PT reduced spontaneous activity in a locomotor test to a greater extent than SB. Finally, SB (30 mg/kg) had no effect on the acquisition of cocaine-paired cues during a Pavlovian cocaine-stimulus conditioning session in the operant chamber. Pretreatment with SB prior to the Pavlovian acquisition session had no effect on subsequent cue-induced reinstatement of cocaine-seeking elicited by those cues. However, pretreatment with SB prior to a second reinstatement session in the same animals significantly attenuated the expression of cue-induced reinstatement. These results show that orexin transmission at the orexin 1 receptor, but not the orexin 2 receptor, is necessary for the reinstatement of cocaine-seeking elicited by drug-paired cues and that orexin signaling is not critical for cocaine reinforcement or cocaine-stimulus conditioning., doi:10.1111/j.1460-9568.2009.06844.x], [resourcePath:/bard/rest/v1/documents/19596018, title:Orexin receptor antagonism prevents transcriptional and behavioral plasticity resulting from stimulant exposure., pubmedId:19596018, abs:Orexin is a key neurotransmitter of central arousal and reward circuits in the CNS. Two receptors respond to orexin signaling, Orexin 1 Receptor (OX1R) and Orexin 2 Receptor (OX2R) with partially overlapping brain distributions. Genetic and pharmacological studies suggest orexin receptor antagonists could provide therapeutic benefit for insomnia and other disorders in which sleep/wake cycles are disrupted. Preclinical data has also emerged showing that the orexin system is involved in the behavioral and neurological effects of drugs of abuse (Aston-Jones et al., 2009; Harris et al., 2005). Here we report sleep promoting effects of a recently described small molecule dual orexin receptor OX1R and OX2R antagonist. This dual orexin receptor antagonist (DORA) also inhibits the ability of subchronic amphetamine to produce behavioral sensitization measured 10 days following pre-treatment. Transcriptional profiling of isolated reward and arousal circuits from brains of behaviorally sensitized animals showed that the DORA blocked the significant alteration of gene expression levels in response to amphetamine exposure, particularly those associated with synaptic plasticity in the VTA. Further, DORA attenuates the ability of nicotine to induce reinstatement of extinguished responding for a reinforcer, demonstrating selectivity of the effect to reward pathways and not to food intake. In summary, these data demonstrate efficacy of a dual orexin receptor antagonist for promotion of sleep and suggest that pharmacological inhibition of the orexin system may play a role in both prevention of drug-induced plasticity and drug-relapse., doi:10.1016/j.neuropharm.2009.07.008], [resourcePath:/bard/rest/v1/documents/19363060, title:Blockade of orexin-1 receptors attenuates orexin-2 receptor antagonism-induced sleep promotion in the rat., pubmedId:19363060, abs:Orexins are peptides produced by lateral hypothalamic neurons that exert a prominent role in the maintenance of wakefulness by activating orexin-1 (OX1R) and orexin-2 (OX2R) receptor located in wake-active structures. Pharmacological blockade of both receptors by the dual OX1/2R antagonist (2R)-2-[(1S)-6,7-dimethoxy-1-{2-[4-(trifluoromethyl)phenyl]ethyl}-3,4-dihydroisoquinolin-2(1H)-yl]-N-methyl-2-phenylethanamide (almorexant) has been shown to promote sleep in animals and humans during their active period. However, the selective distribution of OX1R and OX2R in distinct neuronal circuits may result in a differential impact of these receptors in sleep-wake modulation. The respective role of OX1R and OX2R on sleep in correlation with monoamine release was evaluated in rats treated with selective antagonists alone or in combination. When administered in either phase of the light/dark cycle, the OX2R antagonist 1-(2,4-dibromophenyl)-3-[(4S,5S)-2,2-dimethyl-4-phenyl-1,3-dioxan-5-yl]urea (JNJ-10397049) decreased the latency for persistent sleep and increased nonrapid eye movement and rapid eye movement sleep time. Almorexant produced less hypnotic activity, whereas the OX1R antagonist 1-(6,8-difluoro-2-methylquinolin-4-yl)-3-[4-(dimethylamino)phenyl]urea (SB-408124) had no effect. Microdialysis studies showed that either OX2R or OX1/2R antagonism decreased extracellular histamine concentration in the lateral hypothalamus, whereas both OX1R and OX1/2R antagonists increased dopamine release in the prefrontal cortex. Finally, coadministration of the OX1R with the OX2R antagonist greatly attenuated the sleep-promoting effects of the OX2R antagonist. These results indicate that blockade of OX2R is sufficient to initiate and prolong sleep, consistent with the hypothesis of a deactivation of the histaminergic system. In addition, it is suggested that simultaneous inhibition of OX1R attenuates the sleep-promoting effects mediated by selective OX2R blockade, possibly correlated with dopaminergic neurotransmission., doi:10.1124/jpet.109.152009], [resourcePath:/bard/rest/v1/documents/10485925, title:Orexin A activates locus coeruleus cell firing and increases arousal in the rat., pubmedId:10485925, abs:The localization of orexin neuropeptides in the lateral hypothalamus has focused interest on their role in ingestion. The orexigenic neurones in the lateral hypothalamus, however, project widely in the brain, and thus the physiological role of orexins is likely to be complex. Here we describe an investigation of the action of orexin A in modulating the arousal state of rats by using a combination of tissue localization and electrophysiological and behavioral techniques. We show that the brain region receiving the densest innervation from orexinergic nerves is the locus coeruleus, a key modulator of attentional state, where application of orexin A increases cell firing of intrinsic noradrenergic neurones. Orexin A increases arousal and locomotor activity and modulates neuroendocrine function. The data suggest that orexin A plays an important role in orchestrating the sleep-wake cycle., doi:null], [resourcePath:/bard/rest/v1/documents/19207823, title:Role of the ventromedial hypothalamic orexin-1 receptors in regulation of gastric Acid secretion in conscious rats., pubmedId:19207823, abs:Orexins play an important role on the central nervous system to modulate gastric acid secretion. The orexin receptors are distributed within the hypothalamus, and expression of orexin-1 receptors (OX1R) is greatest in the anterior hypothalamus and ventromedial nucleus. Therefore, we hypothesised that ventromedial hypothalamic OX1R may be involved in the control of gastric acid secretion. To address this question, we examined the effects of orexin-A and a selective OX1R antagonist, SB-3345867, on gastric acid secretion in pyloric-ligated conscious rats. Intraventromedial injection of orexin-A (0.5-2 microg/microl) stimulated gastric acid secretion in a dose-dependent manner. This stimulatory effect of orexin-A persisted over 3 h. In some experiments, SB-3345867 (10 mg/kg i.p.) was administered 30 min before orexin-A or saline injections. We found that i.p. injection of SB-334867 suppressed stimulated gastric acid secretion induced by orexin-A (2 microg/microl). Atropine (5 mg/kg) also inhibited the stimulatory effect of central injection of orexin-A on acid secretion. In conclusion, the present study suggests that endogenous orexin-A acts on the ventromedial hypothalamus to stimulates acid secretion. This stimulatory effect is probably mediated through OX1R., doi:10.1111/j.1365-2826.2009.01824.x], [resourcePath:/bard/rest/v1/documents/10860858, title:Sensitivity of orexin-A binding to phospholipase C inhibitors, neuropeptide Y, and secretin., pubmedId:10860858, abs:The binding of [(125)I] orexin-A (Ox-A) to particulates from Chinese hamster ovary (CHO) cells expressing the cloned orexin-A receptor, or from rat forebrain areas, was sensitive to blockers of phosphatidylinositol-specific phospholipase C (PtdIns-PLC) U-73122 and ET-18-OCH(3), little affected by phospholipase A(2) inhibitor quinacrine, and not sensitive to D609, a xanthate inhibitor of phosphatidylcholine-selective PLC. Interaction of the receptor with a PtdIns-PLC was further indicated by a large sensitivity of the binding to Ca(2+). Up to 50% of the binding was sensitive to the G-protein nucleotide site agonist GTP-gamma-S. Ligand attachment to the orexin-A receptor thus depends on an association with both PtdIns-PLC and G-protein alpha-subunits. In all paradigms examined, the binding of [(125)I]orexin-A was competed by human/rat neuropeptide Y (hNPY) and porcine secretin with a potency similar to orexin-A (IC(50) range 30-100 nM). The rank order of potency for NPY-related peptides was hNPY > porcine peptide YY (pPYY) > (Leu(31), Pro(34)) human PYY > human PYY(3-36) > hNPY free acid > human pancreatic polypeptide. Among secretin-related peptides, the rank order of potency was porcine secretin > or = orexin-A > human pituitary adenylate cyclase-activating peptide > orexin-B > porcine vasoactive intestinal peptide. Among opioid peptides, rat beta-endorphin and camel delta-endorphin were much less active than NPY and secretin, and two enkephalins were inactive at 1 microM. In view of high abundance of NPY in forebrain, the above cross-reactivity could indicate a significant contribution of NPY to signaling via orexin-A receptors., doi:10.1006/bbrc.2000.2880]], source:The Scripps Research Institute Molecular Screening Center, updated:null, description:Source (MLPCN Center Name): The Scripps Research Institute Molecular Screening Center (SRIMSC)\n" +
                 "Affiliation: The Scripps Research Institute, TSRI\n" +
                 "Assay Provider: Patricia McDonald, TSRI\n" +
                 "Network: Molecular Library Probe Production Centers Network (MLPCN)\n" +
                 "Grant Proposal Number: R01 DA023915-02\n" +
                 "Grant Proposal PI: Patricia McDonald\n" +
                 "External Assay ID: CHOK1_ANT_HTRF_1536_3X%INH CSRUN IP-ONE\n" +
                 "\n" +
                 "Name: Counterscreen for antagonists of the orexin 1 receptor (OX1R; HCRTR1): Homogenous time resolved fluorescence (HTRF)-based cell-based assay to identify antagonists of the parental CHO-K1 cell line.\n" +
                 "\n" +
                 "Description:\n" +
                 "\n" +
                 "Heterotrimeric G-protein coupled receptors (GPCRs) are major targets for disease therapeutics, due in part to their broad tissue distribution, structural diversity, varied modes of action, and disease association (1-4). Most non-sensory GPCRs are expressed in the brain and regulate critical neuronal functions involved in feeding, sleep, mood, and addiction (5, 6). For example, in the lateral hypothalamic region of the brain, two orexin neuropeptides (orexin A and orexin B) derived from proteolytic processing of the same orexin precursor (7), signal through the Gq-coupled GPCRs OX1R and OX2R to stimulate food consumption (8, 9). OX1R binds orexin A selectively, while OX2R binds both orexin A and orexin B. Recently, signaling by orexin A through OX1R has been shown to play a critical role in cocaine-seeking behavior (10) and morphine withdrawal (6). Additional studies reveal OX1R involvement in behavioral plasticity (11), the sleep-wake cycle (12, 13), and gastric acid secretion (14), and that OX1R may bind other neuropeptides such as neuropeptide Y and secretin (15). As a result, the identification of a selective OX1R antagonist would serve as a useful tool for exploring orexin biology, and the role of OX1R in drug addiction.\n" +
                 "\n" +
                 "References:\n" +
                 "\n" +
                 "1. Bosier, B and Hermans, E, Versatility of GPCR recognition by drugs: from biological implications to therapeutic relevance. Trends Pharmacol Sci, 2007. 28(8): p. 438-46.\n" +
                 "2. Lagerstrom, MC and Schioth, HB, Structural diversity of G protein-coupled receptors and significance for drug discovery. Nat Rev Drug Discov, 2008. 7(4): p. 339-57.\n" +
                 "3. Pan, HL, Wu, ZZ, Zhou, HY, Chen, SR, Zhang, HM and Li, DP, Modulation of pain transmission by G-protein-coupled receptors. Pharmacol Ther, 2008. 117(1): p. 141-61.\n" +
                 "4. Thompson, MD, Cole, DE and Jose, PA, Pharmacogenomics of G protein-coupled receptor signaling: insights from health and disease. Methods Mol Biol, 2008. 448: p. 77-107.\n" +
                 "5. Gainetdinov, RR, Premont, RT, Bohn, LM, Lefkowitz, RJ and Caron, MG, Desensitization of G protein-coupled receptors and neuronal functions. Annu Rev Neurosci, 2004. 27: p. 107-44.\n" +
                 "6. Sharf, R, Sarhan, M and Dileone, RJ, Orexin mediates the expression of precipitated morphine withdrawal and concurrent activation of the nucleus accumbens shell. Biol Psychiatry, 2008. 64(3): p. 175-83.\n" +
                 "7. de Lecea, L, Kilduff, TS, Peyron, C, Gao, X, Foye, PE, Danielson, PE, Fukuhara, C, Battenberg, EL, Gautvik, VT, Bartlett, FS, 2nd, Frankel, WN, van den Pol, AN, Bloom, FE, Gautvik, KM and Sutcliffe, JG, The hypocretins: hypothalamus-specific peptides with neuroexcitatory activity. Proc Natl Acad Sci U S A, 1998. 95(1): p. 322-7.\n" +
                 "8. Sakurai, T, Amemiya, A, Ishii, M, Matsuzaki, I, Chemelli, RM, Tanaka, H, Williams, SC, Richardson, JA, Kozlowski, GP, Wilson, S, Arch, JR, Buckingham, RE, Haynes, AC, Carr, SA, Annan, RS, McNulty, DE, Liu, WS, Terrett, JA, Elshourbagy, NA, Bergsma, DJ and Yanagisawa, M, Orexins and orexin receptors: a family of hypothalamic neuropeptides and G protein-coupled receptors that regulate feeding behavior. Cell, 1998. 92(4): p. 573-85.\n" +
                 "9. Hara, J, Beuckmann, CT, Nambu, T, Willie, JT, Chemelli, RM, Sinton, CM, Sugiyama, F, Yagami, K, Goto, K, Yanagisawa, M and Sakurai, T, Genetic ablation of orexin neurons in mice results in narcolepsy, hypophagia, and obesity. Neuron, 2001. 30(2): p. 345-54.\n" +
                 "10. Smith, RJ, See, RE and Aston-Jones, G, Orexin/hypocretin signaling at the orexin 1 receptor regulates cue-elicited cocaine-seeking. Eur J Neurosci, 2009. 30(3): p. 493-503.\n" +
                 "11. Winrow, CJ, Tanis, KQ, Reiss, DR, Rigby, AM, Uslaner, JM, Uebele, VN, Doran, SM, Fox, SV, Garson, SL, Gotter, AL, Levine, DM, Roecker, AJ, Coleman, PJ, Koblan, KS and Renger, JJ, Orexin receptor antagonism prevents transcriptional and behavioral plasticity resulting from stimulant exposure. Neuropharmacology, 2009.\n" +
                 "12. Dugovic, C, Shelton, JE, Aluisio, LE, Fraser, IC, Jiang, X, Sutton, SW, Bonaventure, P, Yun, S, Li, X, Lord, B, Dvorak, CA, Carruthers, NI and Lovenberg, TW, Blockade of orexin-1 receptors attenuates orexin-2 receptor antagonism-induced sleep promotion in the rat. J Pharmacol Exp Ther, 2009. 330(1): p. 142-51.\n" +
                 "13. Hagan, JJ, Leslie, RA, Patel, S, Evans, ML, Wattam, TA, Holmes, S, Benham, CD, Taylor, SG, Routledge, C, Hemmati, P, Munton, RP, Ashmeade, TE, Shah, AS, Hatcher, JP, Hatcher, PD, Jones, DN, Smith, MI, Piper, DC, Hunter, AJ, Porter, RA and Upton, N, Orexin A activates locus coeruleus cell firing and increases arousal in the rat. Proc Natl Acad Sci U S A, 1999. 96(19): p. 10911-6.\n" +
                 "14. Eliassi, A, Nazari, M and Naghdi, N, Role of the ventromedial hypothalamic orexin-1 receptors in regulation of gastric Acid secretion in conscious rats. J Neuroendocrinol, 2009. 21(3): p. 177-82.\n" +
                 "15. Kane, JK, Tanaka, H, Parker, SL, Yanagisawa, M and Li, MD, Sensitivity of orexin-A binding to phospholipase C inhibitors, neuropeptide Y, and secretin. Biochem Biophys Res Commun, 2000. 272(3): p. 959-65.\n" +
                 "\n" +
                 "Keywords:\n" +
                 "\n" +
                 "orexin, Orexin 1 receptor, OX1, OX1R, OXR1, hypocretin-1 receptor, hypocretin, Hcrtr1, Hcrtr-1, GPCR, antagonism, inhibition, inhibit, inhibitor, addiction, relapse, cocaine, substance abuse, brain, CHO-K1 cells, CHO cells, inositol phosphate, IP, IP1, IP-One, IP one, IPOne, FRET, HTRF, TR-FRET, TRFRET, fluorescence, fluorescent, counterscreen, parental, triplicate, HTS, high throughput screen, 1536, Scripps Florida, The Scripps Research Institute Molecular Screening Center, SRIMSC, Molecular Libraries Probe Production Centers Network, MLPCN., classification:0, name:Counterscreen for antagonists of the orexin 1 receptor (OX1R; HCRTR1): Homogenous time resolved fluorescence (HTRF)-based cell-based assay to identify antagonists of the parental CHO-K1 cell line, targets:[]]""" as JSON
    }
}
