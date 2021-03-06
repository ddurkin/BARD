/* Copyright (c) 2014, The Broad Institute
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of The Broad Institute nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL The Broad Institute BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package bard.db.project

import bard.db.dictionary.Element
import bard.db.enums.ExpectedValueType
import bard.db.guidance.Guidance
import bard.db.model.AbstractContextUnitSpec
import bard.db.registration.AssayContext
import bard.db.registration.AssayContextItem
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import org.junit.Before
import spock.lang.Unroll

import static bard.db.guidance.context.BiologyShouldHaveOneSupportingReferencePerContextRule.BIOLOGICAL_PROCESS_SHOULD_HAVE_ONE_BIOLOGICAL_PROCESS_TERM_MSG
import static bard.db.guidance.context.BiologyShouldHaveOneSupportingReferencePerContextRule.MOLECULAR_TARGETS_SHOULD_HAVE_ONE_BIOLOGICAL_PROCESS_TERM_MSG
import static bard.db.guidance.context.OneBiologyAttributePerContextRule.ONE_BIOLOGY_ATTRIBUTE

/**
 * Created with IntelliJ IDEA.
 * User: ddurkin
 * Date: 8/8/13
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */
@Build([Project, ProjectContext, ProjectContextItem, Element])
@Mock([Project, ProjectContext, ProjectContextItem, Element])
@Unroll
class ProjectContextUnitSpec extends AbstractContextUnitSpec<ProjectContext> {
    @Before
    @Override
    void doSetup() {
        domainInstance = ProjectContext.buildWithoutSave()
    }
    void 'test guidance #desc'() {
        given:
        final ProjectContext projectContext = ProjectContext.build()

        if (attributeElementMaps) {
            attributeElementMaps.eachWithIndex { Map attributeMap, Integer index ->
                attributeMap.expectedValueType = ExpectedValueType.ELEMENT
                final Element attribute = Element.findByLabel(attributeMap.label) ?: Element.build(attributeMap)
                final ProjectContextItem projectContextItem = ProjectContextItem.buildWithoutSave(attributeElement: attribute)
                final Map valueMap = valueElementMaps[index]
                if (valueMap) {
                    final Element value = Element.findByLabel(valueMap.label) ?: Element.build(valueMap)
                    projectContextItem.valueElement = value
                }
                projectContext.addContextItem(projectContextItem)
            }
        }

        when:
        List<Guidance> actualGuidanceMessages = projectContext.guidance.message

        then:
        actualGuidanceMessages == expectedGuidanceMessages

        where:
        desc                                                                     | attributeElementMaps                                                                   | valueElementMaps                                               | expectedGuidanceMessages
        "2 biology attributes no good"                                           | [[label: 'biology'], [label: 'biology']]                                               | [[label: 'biological process'], [label: 'biological process']] | [ONE_BIOLOGY_ATTRIBUTE, BIOLOGICAL_PROCESS_SHOULD_HAVE_ONE_BIOLOGICAL_PROCESS_TERM_MSG]
        "no biology attribute ok"                                                | []                                                                                     | []                                                             | []

        "biology='biological process', should have supporting term ok"           | [[label: 'biology'], [label: 'GO biological process term']]                            | [[label: 'biological process']]                                | []
        "biology='biological process', should have supporting term ok"           | [[label: 'biology'], [label: 'NCBI BioSystems term']]                                  | [[label: 'biological process']]                                | []
        "biology='biological process', with more than 1 supporting term no good" | [[label: 'biology'], [label: 'NCBI BioSystems term'], [label: 'NCBI BioSystems term']] | [[label: 'biological process']]                                | [BIOLOGICAL_PROCESS_SHOULD_HAVE_ONE_BIOLOGICAL_PROCESS_TERM_MSG]
        "biology='biological process', without supporting term no good"          | [[label: 'biology']]                                                                   | [[label: 'biological process']]                                | [BIOLOGICAL_PROCESS_SHOULD_HAVE_ONE_BIOLOGICAL_PROCESS_TERM_MSG]

        "biology='molecular target' should have supporting term ok"              | [[label: 'biology'], [label: 'GO gene-product ID']]                                    | [[label: 'molecular target']]                                  | []
        "biology='molecular target' should have supporting term ok"              | [[label: 'biology'], [label: 'GenBank ID']]                                            | [[label: 'molecular target']]                                  | []
        "biology='molecular target' should have supporting term ok"              | [[label: 'biology'], [label: 'NCBI accession number']]                                 | [[label: 'molecular target']]                                  | []
        "biology='molecular target' should have supporting term ok"              | [[label: 'biology'], [label: 'UniProt accession number']]                              | [[label: 'molecular target']]                                  | []
        "biology='molecular target' should have supporting term ok"              | [[label: 'biology'], [label: 'gene Entrez GI']]                                        | [[label: 'molecular target']]                                  | []
        "biology='molecular target' should have supporting term ok"              | [[label: 'biology'], [label: 'protein Entrez GI']]                                     | [[label: 'molecular target']]                                  | []
        "biology='molecular target' with more than 1 supporting term no good"    | [[label: 'biology'], [label: 'GO gene-product ID'], [label: 'GO gene-product ID']]     | [[label: 'molecular target']]                                  | [MOLECULAR_TARGETS_SHOULD_HAVE_ONE_BIOLOGICAL_PROCESS_TERM_MSG]
        "biology='molecular target' should have supporting term"                 | [[label: 'biology']]                                                                   | [[label: 'molecular target']]                                  | [MOLECULAR_TARGETS_SHOULD_HAVE_ONE_BIOLOGICAL_PROCESS_TERM_MSG]

        "biology='macromolecule' should have supporting term"                    | [[label: 'biology']]                                                                   | [[label: 'macromolecule']]                                     | [MOLECULAR_TARGETS_SHOULD_HAVE_ONE_BIOLOGICAL_PROCESS_TERM_MSG]
        "biology='antibody' should have supporting term"                         | [[label: 'biology']]                                                                   | [[label: 'antibody']]                                          | [MOLECULAR_TARGETS_SHOULD_HAVE_ONE_BIOLOGICAL_PROCESS_TERM_MSG]
        "biology='gene' should have supporting term"                             | [[label: 'biology']]                                                                   | [[label: 'gene']]                                              | [MOLECULAR_TARGETS_SHOULD_HAVE_ONE_BIOLOGICAL_PROCESS_TERM_MSG]
        "biology='nucleotide' should have supporting term"                       | [[label: 'biology']]                                                                   | [[label: 'nucleotide']]                                        | [MOLECULAR_TARGETS_SHOULD_HAVE_ONE_BIOLOGICAL_PROCESS_TERM_MSG]
        "biology='peptide' should have supporting term"                          | [[label: 'biology']]                                                                   | [[label: 'peptide']]                                           | [MOLECULAR_TARGETS_SHOULD_HAVE_ONE_BIOLOGICAL_PROCESS_TERM_MSG]
        "biology='protein-DNA complex' should have supporting term"              | [[label: 'biology']]                                                                   | [[label: 'protein-DNA complex']]                               | [MOLECULAR_TARGETS_SHOULD_HAVE_ONE_BIOLOGICAL_PROCESS_TERM_MSG]
        "biology='protein-reporter complex' should have supporting term"         | [[label: 'biology']]                                                                   | [[label: 'protein-reporter complex']]                          | [MOLECULAR_TARGETS_SHOULD_HAVE_ONE_BIOLOGICAL_PROCESS_TERM_MSG]
        "biology='protein-reporter complex' should have supporting term"         | [[label: 'biology']]                                                                   | [[label: 'protein']]                                           | [MOLECULAR_TARGETS_SHOULD_HAVE_ONE_BIOLOGICAL_PROCESS_TERM_MSG]
    }

    void 'test rules wired into assayContext '() {
        when: 'we have an assayContext'
        ProjectContext context = new ProjectContext()

        then: 'we should see only the rules we expect'
        context.guidanceRules*.class*.simpleName == ['OneBiologyAttributePerContextRule', 'BiologyShouldHaveOneSupportingReferencePerContextRule']
    }
}
