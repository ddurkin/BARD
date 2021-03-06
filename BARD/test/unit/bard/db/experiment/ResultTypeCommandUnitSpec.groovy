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

package bard.db.experiment

import bard.db.dictionary.Element
import bard.db.enums.HierarchyType
import bard.db.project.ExperimentController
import bard.db.project.ResultTypeCommand
import grails.buildtestdata.mixin.Build
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

import static test.TestUtils.assertFieldValidationExpectations

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(ExperimentController)
@Build([Experiment, Element, ExperimentMeasure])
@Unroll
class ResultTypeCommandUnitSpec extends ResultTypeCommandAbstractUnitSpec {


    void "test validate fields #desc"() {
        given:
        ResultTypeCommand resultTypeCommand = new ResultTypeCommand(requiredFields.call())

        when:
        resultTypeCommand[(field)] = valueUnderTest.call()?.id
        resultTypeCommand.validate()
        then:
        assertFieldValidationExpectations(resultTypeCommand, field, valid, errorCode)
        where:
        desc                                                      | valueUnderTest                    | valid | errorCode                                      | field                       | requiredFields
        'experiment id is null'                                   | { null }                          | false | 'nullable'                                     | "experimentId"              | { [resultTypeId: Element.build().id] }
        'experiment id is valid'                                  | { Experiment.build() }            | true  | null                                           | "experimentId"              | { [resultTypeId: Element.build().id] }

        'resultType id is valid'                                  | { Element.build() }               | true  | null                                           | "resultTypeId"              | { [experimentId: Experiment.build().id] }
        'resultType id is invalid'                                | { [id: 6000] }                    | false | 'command.resultTypeId.notexists'               | "resultTypeId"              | { [experimentId: Experiment.build().id] }

        'stats modifier id is null'                               | { null }                          | true  | null                                           | "statsModifierId"           | { [experimentId: Experiment.build().id, resultTypeId: Element.build().id] }
        'stats modifier id is valid'                              | { Element.build() }               | true  | null                                           | "statsModifierId"           | { [experimentId: Experiment.build().id, resultTypeId: Element.build().id] }

        'parentExperimentMeasure id no hierarchy'                 | { ExperimentMeasure.build() }     | false | 'command.parentMeasure.no.hierarchy'           | "parentExperimentMeasureId" | { [experimentId: Experiment.build().id, resultTypeId: Element.build().id] }
        'parentExperimentMeasure id with hierarchy'               | { ExperimentMeasure.build() }     | true  | null                                           | "parentExperimentMeasureId" | { [experimentId: Experiment.build().id, resultTypeId: Element.build().id, parentChildRelationship: HierarchyType.CALCULATED_FROM.id] }

        'parentChildRelationship id no parentExperimentMeasure'   | { HierarchyType.CALCULATED_FROM } | false | 'command.parentChildRelationship.no.hierarchy' | "parentChildRelationship"   | { [experimentId: Experiment.build().id, resultTypeId: Element.build().id] }
        'parentChildRelationship id with parentExperimentMeasure' | { HierarchyType.CALCULATED_FROM } | true  | null                                           | "parentChildRelationship"   | { [experimentId: Experiment.build().id, resultTypeId: Element.build().id, parentExperimentMeasureId: ExperimentMeasure.build().id] }

        'assay context id is null'                                | { null }                          | true  | null                                           | "contextIds"                | { [experimentId: Experiment.build().id, resultTypeId: Element.build().id] }
    }



    void "test createNewExperimentMeasure"() {
        given:
        final Element resultType = Element.build()
        final Experiment experiment = Experiment.build()
        final Element statsModifier = Element.build()
        final ExperimentMeasure parentExperimentMeasure = ExperimentMeasure.build()
        final HierarchyType hierarchyType = HierarchyType.CALCULATED_FROM
        Map m = [experimentService: this.experimentService,
                experimentId: experiment.id,
                resultTypeId: resultType.id,
                statsModifierId: statsModifier.id,
                parentExperimentMeasureId: parentExperimentMeasure.id,
                parentChildRelationship: hierarchyType.id
        ]
        ResultTypeCommand resultTypeCommand = new ResultTypeCommand(m)
        when:
        ExperimentMeasure experimentMeasure = resultTypeCommand.createNewExperimentMeasure()
        then:
        1 * experimentService.createNewExperimentMeasure(_, _, _, _, _, _, _) >> {
            new ExperimentMeasure(experiment: experiment,
                    priorityElement: false, resultType: resultType,
                    parent: parentExperimentMeasure, parentChildRelationship: hierarchyType,
                    statsModifier: statsModifier)
        }
        assert experimentMeasure.resultType == resultType
        assert experimentMeasure.experiment == experiment

        assert experimentMeasure.parent == parentExperimentMeasure
        assert experimentMeasure.parentChildRelationship == hierarchyType
        assert experimentMeasure.statsModifier == statsModifier
    }

    void "test getters"() {

        given:
        final Element resultType = Element.build()
        final Experiment experiment = Experiment.build()
        final Element statsModifier = Element.build()
        final ExperimentMeasure parentExperimentMeasure = ExperimentMeasure.build()
        final HierarchyType hierarchyType = HierarchyType.CALCULATED_FROM
        Map m = [experimentId: experiment.id,
                resultTypeId: resultType.id,
                statsModifierId: statsModifier.id,
                parentExperimentMeasureId: parentExperimentMeasure.id,
                parentChildRelationship: hierarchyType.id
        ]

        when:
        ResultTypeCommand resultTypeCommand = new ResultTypeCommand(m)
        then:
        assert resultTypeCommand.getResultType() == resultType
        assert resultTypeCommand.getExperiment() == experiment
        assert resultTypeCommand.getStatsModifier() == statsModifier
        assert resultTypeCommand.parentChildRelationship == hierarchyType.id
        assert resultTypeCommand.getParentExperimentMeasure() == parentExperimentMeasure


    }

    void "test createDataResponseMap"() {
        given:
        ResultTypeCommand resultTypeCommand = new ResultTypeCommand(experimentId: Experiment.build().id)

        when:
        Map m = resultTypeCommand.createDataResponseMap()
        then:
        assert m.resultTypeCommand instanceof ResultTypeCommand
        assert m.currentExperimentMeasures.isEmpty()
    }

    void "test createMeasure"() {
        given:
        Experiment experiment = Experiment.build()
        String modifiedBy = "Me"
        boolean priorityElement = true
        Element resultType = Element.build()
        ExperimentMeasure parent = ExperimentMeasure.build()
        HierarchyType hierarchyType = HierarchyType.CALCULATED_FROM
        Element statsModifier = Element.build()
        ResultTypeCommand resultTypeCommand = new ResultTypeCommand(experimentService: this.experimentService)
        when:
        ExperimentMeasure experimentMeasure =
            resultTypeCommand.createExperimentMeasure(experiment,
                    resultType, priorityElement, parent,
                    hierarchyType, statsModifier)
        then:
        1 * experimentService.createNewExperimentMeasure(_, _, _, _, _, _, _) >> {
            new ExperimentMeasure(experiment: experiment,
                    priorityElement: priorityElement, resultType: resultType,
                    modifiedBy: modifiedBy,
                    parent: parent, parentChildRelationship: hierarchyType,
                    statsModifier: statsModifier)
        }
        assert experimentMeasure.experiment == experiment
        assert experimentMeasure.modifiedBy == modifiedBy
        assert experimentMeasure.priorityElement == priorityElement
        assert experimentMeasure.resultType == resultType
        assert experimentMeasure.parent == parent
        assert experimentMeasure.parentChildRelationship == hierarchyType
        assert experimentMeasure.statsModifier == statsModifier
    }
}
