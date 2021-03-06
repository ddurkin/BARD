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

package bard.db.guidance.owner

import bard.db.dictionary.Element
import bard.db.enums.ExpectedValueType
import bard.db.project.Project
import bard.db.project.ProjectContext
import bard.db.project.ProjectContextItem
import bard.db.registration.Assay
import bard.db.registration.AssayContext
import bard.db.registration.AssayContextItem
import grails.buildtestdata.mixin.Build
import spock.lang.Specification
import spock.lang.Unroll

import static bard.db.guidance.owner.MinimumOfOneBiologyGuidanceRule.ASSAY_ONE_BIOLOGY_ATTRIBUTE_REQUIRED
import static bard.db.guidance.owner.MinimumOfOneBiologyGuidanceRule.PROJECT_ONE_BIOLOGY_ATTRIBUTE_REQUIRED

/**
 * Created by ddurkin on 1/29/14.
 */
@Build([Assay, AssayContext, AssayContextItem, Project, ProjectContext, ProjectContextItem, Element])
@Unroll
class MinimumOfOneBiologyGuidanceRuleUnitSpec extends Specification {


    void 'test guidance for Assay #desc'() {
        given:
        final Assay assay = Assay.build()
        List<List<Map>> itemMaps = attributeElementValueElementMaps.call()
        //[[attribute1 element pairMaps], [value1 element pairMaps]]
        // putting each item in it's own context
        if (itemMaps) {
            itemMaps.each { List<Map> pairMaps ->
                final AssayContext assayContext = AssayContext.build(assay: assay)
                final Element attribute = Element.findByLabel(pairMaps.first().label) ?: Element.build(pairMaps.first())
                final Element valueElement = pairMaps.last() ? (Element.findByLabel(pairMaps.last()?.label) ?: Element.build(pairMaps.last())) : null
                assayContext.addContextItem(AssayContextItem.build(attributeElement: attribute, valueElement: valueElement, valueDisplay: valueDisplay))
            }
        }

        when:
        final List<String> actualGuidanceMessages = new MinimumOfOneBiologyGuidanceRule(assay).guidance.message

        then:
        actualGuidanceMessages == expectedGuidanceMessages

        where:
        desc                                                        | attributeElementValueElementMaps                                                                                | valueDisplay | expectedGuidanceMessages
        'biology required (true)'                                   | { null }                                                                                                        | null         | [ASSAY_ONE_BIOLOGY_ATTRIBUTE_REQUIRED]
        'biology required (false)'                                  | { [[[label: 'biology'], []]] }                                                                                  | null         | []
        'biology required with valueElement==small-molecule format' | { [[[label: 'assay format', expectedValueType: ExpectedValueType.ELEMENT], [label: 'small-molecule format']]] } | 'not null'   | []
        'more than 1 biology ok'                                    | { [[[label: 'biology'], []], [[label: 'biology'], []]] }                                                        | null         | []
    }

    void 'test guidance for project #desc'() {
        given:
        final Project project = Project.build()
        List<Map> itemMaps = attributeElementMaps.call()
        // putting each item in it's own context
        if (itemMaps) {
            itemMaps.each { Map map ->
                final ProjectContext projectContext = ProjectContext.build(project: project)
                project.addToContexts(projectContext)
                final Element attribute = Element.findByLabel(map.label) ?: Element.build(map)
                projectContext.addContextItem(ProjectContextItem.build(attributeElement: attribute, valueDisplay: null))
            }
        }

        when:
        final List<String> actualGuidanceMessages = new MinimumOfOneBiologyGuidanceRule(project).getGuidance().message

        then:
        actualGuidanceMessages == expectedGuidanceMessages

        where:
        desc                     | attributeElementMaps                         | expectedGuidanceMessages
        'biology required'       | { null }                                     | [PROJECT_ONE_BIOLOGY_ATTRIBUTE_REQUIRED]
        'biology required'       | { [[label: 'biology']] }                     | []
        'more than 1 biology ok' | { [[label: 'biology'], [label: 'biology']] } | []
    }
}
