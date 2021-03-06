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

package bard.db.dictionary

import bard.db.enums.AddChildMethod
import bard.db.enums.ExpectedValueType
import bard.util.BardCacheUtilsService
import grails.buildtestdata.mixin.Build
import grails.converters.JSON
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll
import test.TestUtils

/**
 * Created with IntelliJ IDEA.
 * User: dlahr
 * Date: 4/24/13
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
@TestFor(ElementController)
@Build([Element, ElementHierarchy])
@Unroll
class ElementControllerUnitSpec extends Specification {
    private static final String pathDelimeter = "/"

    private static final EXPECTED_ADD_TERN_VIEW = "/element/addTerm"
    final String parentLabelField = 'parentLabel'
    Element parentElement

    def setup() {
        this.controller.elementService = Mock(ElementService.class)
        this.controller.bardCacheUtilsService = Mock(BardCacheUtilsService.class)
        this.parentElement = Element.build(label: 'parent label with spaces', addChildMethod: AddChildMethod.DIRECT)
        this.controller.buildElementPathsService = Spy(BuildElementPathsService, constructorArgs: [])
        this.controller.ontologyDataAccessService = Mock(OntologyDataAccessService)
        this.controller.modifyElementAndHierarchyService = Mock(ModifyElementAndHierarchyService)
    }

    void "test buildTopLevelHierarchyTree"() {

        given:
        final String label = "label"
        final String key = "key"
        final String description = "description"
        final boolean isFolder = false
        final boolean isLazy = false
        long elementId = Element.build(label: label).id
        when:
        controller.buildTopLevelHierarchyTree(false, "BARD", null)
        then:
        controller.elementService.createElementHierarchyTree(false, "BARD", null) >> {
            [[elementId: elementId, title: label, key: key, description: description, isFolder: isFolder, isLazy: isLazy]]
        }
        def controllerResponse = controller.response.contentAsString
        def jsonResult = JSON.parse(controllerResponse)
        assert jsonResult
        assert [elementId] == jsonResult.elementId
        assert [label] == jsonResult.title
        assert [false] == jsonResult.isFolder
        assert [false] == jsonResult.isLazy
    }

    void "test getChildrenAsJson"() {

        given:
        final String label = "label"
        final String key = "key"
        final String description = "description"
        final boolean isFolder = false
        final boolean isLazy = false
        long elementId = Element.build(label: label).id
        boolean doNotShowRetired = false
        when:
        controller.getChildrenAsJson(elementId, doNotShowRetired, null)
        then:
        controller.elementService.getChildNodes(_, _, _) >> {
            [[elementId: elementId,
                    title: label,
                    key: key,
                    description: description,
                    isFolder: isFolder,
                    isLazy: isLazy,
                    childMethodDescription: "child Description",
                    childMethod: AddChildMethod.DIRECT.toString(),
                    addClass: AddChildMethod.DIRECT.label
            ]]
        }
        def controllerResponse = controller.response.contentAsString
        def jsonResult = JSON.parse(controllerResponse)
        assert jsonResult
        assert [elementId] == jsonResult.elementId
        assert [label] == jsonResult.title
        assert [key] == jsonResult.key
        assert [false] == jsonResult.isFolder
        assert [false] == jsonResult.isLazy
        assert [AddChildMethod.DIRECT.label] == jsonResult.addClass
        assert ["child Description"] == jsonResult.childMethodDescription
        assert [AddChildMethod.DIRECT.toString()] == jsonResult.childMethod
    }

    void "test parent label constraints #desc TermCommand: '#valueUnderTest'"() {
        final String field = 'label'
        given:
        TermCommand termCommand =
                new TermCommand(parentLabel: valueUnderTest, label: "label",
                        description: "description", abbreviation: "abbr", synonyms: "abc,efg", curationNotes: "curationNotes")

        when: 'a value is set for the field under test'
        termCommand[(field)] = valueUnderTest
        termCommand.validate()
        then: 'verify valid or invalid for expected reason'
        TestUtils.assertFieldValidationExpectations(termCommand, field, valid, errorCode)


        where:
        desc          | valueUnderTest                                     | valid | errorCode
        'null '       | null                                               | false | 'nullable'
        'blank value' | ''                                                 | false | 'blank'
        'blank value' | '  '                                               | false | 'blank'
        'too long'    | TestUtils.createString(Element.LABEL_MAX_SIZE + 1) | false | 'maxSize.exceeded'
    }

    void "test label constraints #desc TermCommand: '#valueUnderTest'"() {

        given:
        Element.build(label: "label")
        TermCommand termCommand =
                new TermCommand(parentLabel: this.parentElement.label,
                        description: "description", abbreviation: "abbr", synonyms: "abc,efg", curationNotes: "curationNotes")
        termCommand.buildElementPathsService = Mock(BuildElementPathsService.class)
        when: 'a value is set for the field under test'
        termCommand[('label')] = valueUnderTest
        termCommand.validate()
        then: 'verify valid or invalid for expected reason'
        TestUtils.assertFieldValidationExpectations(termCommand, 'label', valid, errorCode)


        where:
        desc                          | valueUnderTest                                     | valid | errorCode
        'null '                       | null                                               | false | 'nullable'
        'blank value'                 | ''                                                 | false | 'blank'
        'blank value'                 | '  '                                               | false | 'blank'
        'too long'                    | TestUtils.createString(Element.LABEL_MAX_SIZE + 1) | false | 'maxSize.exceeded'
        'exactly at limit'            | TestUtils.createString(Element.LABEL_MAX_SIZE)     | true  | null
        'existing element with label' | "label"                                            | false | 'unique'
    }

    void "test parent description constraints #desc TermCommand: '#valueUnderTest'"() {
        final String field = 'parentDescription'
        given:
        TermCommand termCommand =
                new TermCommand(parentLabel: this.parentElement.label, description: "description",
                        label: "label", abbreviation: "abbr", synonyms: "abc,efg", curationNotes: "curationNotes")

        when: 'a value is set for the field under test'
        termCommand[(field)] = valueUnderTest
        termCommand.validate()

        then: 'verify valid or invalid for expected reason'
        TestUtils.assertFieldValidationExpectations(termCommand, field, valid, errorCode)


        where:
        desc               | valueUnderTest                                           | valid | errorCode
        'null '            | null                                                     | true  | null
        'blank value'      | ''                                                       | true  | null
        'blank value'      | '  '                                                     | true  | null
        'too long'         | TestUtils.createString(Element.DESCRIPTION_MAX_SIZE + 1) | false | 'maxSize.exceeded'
        'exactly at limit' | TestUtils.createString(Element.DESCRIPTION_MAX_SIZE)     | true  | null
    }

    void "test description constraints #desc TermCommand: '#valueUnderTest'"() {
        final String field = 'description'
        given:

        TermCommand termCommand =
                new TermCommand(parentLabel: this.parentElement.label, label: "label", abbreviation: "abbr", synonyms: "abc,efg", curationNotes: "curationNotes")

        when: 'a value is set for the field under test'
        termCommand[(field)] = valueUnderTest
        termCommand.validate()

        then: 'verify valid or invalid for expected reason'
        TestUtils.assertFieldValidationExpectations(termCommand, field, valid, errorCode)


        where:
        desc               | valueUnderTest                                           | valid | errorCode
        'null '            | null                                                     | false | 'nullable'
        'blank value'      | ''                                                       | false | 'blank'
        'blank value'      | '  '                                                     | false | 'blank'
        'too long'         | TestUtils.createString(Element.DESCRIPTION_MAX_SIZE + 1) | false | 'maxSize.exceeded'
        'exactly at limit' | TestUtils.createString(Element.DESCRIPTION_MAX_SIZE)     | true  | null
    }

    void "test curation notes constraints #desc TermCommand: Curation Notes '#valueUnderTest'"() {
        final String field = 'curationNotes'
        given:
        TermCommand termCommand =
                new TermCommand(parentLabel: this.parentElement.label, label: "label", description: "description", abbreviation: "abbr", synonyms: "abc,efg")

        when: 'a value is set for the field under test'
        termCommand[(field)] = valueUnderTest
        termCommand.validate()

        then: 'verify valid or invalid for expected reason'
        TestUtils.assertFieldValidationExpectations(termCommand, field, valid, errorCode)


        where:
        desc               | valueUnderTest                                           | valid | errorCode
        'null '            | null                                                     | false | 'nullable'
        'blank value'      | ''                                                       | false | 'blank'
        'blank value'      | '  '                                                     | false | 'blank'
        'too long'         | TestUtils.createString(Element.DESCRIPTION_MAX_SIZE + 1) | false | 'maxSize.exceeded'
        'exactly at limit' | TestUtils.createString(Element.DESCRIPTION_MAX_SIZE)     | true  | null
    }

    void "test Parent Label must exist constraints #desc TermCommand: '#valueUnderTest'"() {

        given:
        TermCommand termCommand =
                new TermCommand(label: "label", description: "description", abbreviation: "abbr", synonyms: "abc,efg", curationNotes: "curationNotes")

        when: 'a value is set for the field under test'
        termCommand[(this.parentLabelField)] = valueUnderTest
        termCommand.validate()

        then: 'verify valid or invalid for expected reason'
        TestUtils.assertFieldValidationExpectations(termCommand, this.parentLabelField, valid, errorCode)


        where:
        desc                          | valueUnderTest | valid | errorCode
        'Parent Label does not exist' | "parentl"      | false | 'termCommand.parentLabel.mustexist'

    }

    void "test Parent Label constraints #desc TermCommand: '#valueUnderTest'"() {

        given:
        Element.build(label: valueUnderTest)
        TermCommand termCommand =
                new TermCommand(label: "label", description: "description", abbreviation: "abbr", synonyms: "abc,efg", curationNotes: "curationNotes")

        when: 'a value is set for the field under test'
        termCommand[(this.parentLabelField)] = valueUnderTest
        termCommand.validate()

        then: 'verify valid or invalid for expected reason'
        TestUtils.assertFieldValidationExpectations(termCommand, this.parentLabelField, valid, errorCode)


        where:
        desc               | valueUnderTest                                 | valid | errorCode
        'exactly at limit' | TestUtils.createString(Element.LABEL_MAX_SIZE) | true  | null

    }

    void "add New Term Page"() {
        when:
        params.attributeElementId = 1
        this.controller.addTerm()
        then:
        assert EXPECTED_ADD_TERN_VIEW == view
        assert model.termCommand
    }

    void "Save Term Page Parent Label #desc"() {
        final String label = "label"
        given:
        TermCommand termCommand =
                new TermCommand(parentLabel: valueUnderTest.call(), label: label, description: "description", abbreviation: "abbr", synonyms: "abc,efg", curationNotes: "curationNotes")
        termCommand.buildElementPathsService = Mock(BuildElementPathsService.class)
        when:
        this.controller.saveTerm(termCommand)
        then:
        this.controller.elementService.addNewTerm(_) >> { Element.build(label: label) }
        assert EXPECTED_ADD_TERN_VIEW == view
        assert flashMessage == flash.message
        assert termCommand.success == valid
        TestUtils.assertFieldValidationExpectations(termCommand, this.parentLabelField, valid, errorCode)
        where:
        desc                                | valueUnderTest | valid | errorCode                           | flashMessage
        'null '                             | {
            null
        }                                                    | false | 'nullable'                          | ""
        'Parent Label does not exist'       | {
            "parentlabel2"
        }                                                    | false | 'termCommand.parentLabel.mustexist' | ""
        'Parent Label  exist'               | {
            "parent label with spaces"
        }                                                    | true  | null                                | "Proposed term label has been saved"
        'Parent Label with multiple spaces' | {
            "parent    label    with   spaces"
        }                                                    | true  | null                                | "Proposed term label has been saved"
    }

    void "Save Term Page label #desc"() {
        final String label = "label with spaces"
        given:
        Element.build(label: label)
        TermCommand termCommand =
                new TermCommand(parentLabel: this.parentElement.label, label: valueUnderTest,
                        description: "description", abbreviation: "abbr",
                        synonyms: "abc,efg", curationNotes: "curationNotes")
        termCommand.buildElementPathsService = Mock(BuildElementPathsService.class)
        when:
        this.controller.saveTerm(termCommand)
        then:
        this.controller.elementService.addNewTerm(_) >> { Element.build(label: valueUnderTest) }
        assert EXPECTED_ADD_TERN_VIEW == view
        TestUtils.assertFieldValidationExpectations(termCommand, "label", valid, errorCode)


        where:
        desc                           | valueUnderTest          | valid | errorCode
        'null '                        | null                    | false | 'nullable'
        'Label already exist'          | "label with spaces"     | false | 'unique'
        'Label does not already exist' | "label2"                | true  | null
        'Label with multiple spaces'   | "label   with   spaces" | false | 'unique'

    }

    void "simple test findElementsFromPathString"() {
        setup:
        final int num = 4

        StringBuilder builder = new StringBuilder()
        List<Element> expected = new ArrayList<Element>(num)
        for (int i = 0; i < num; i++) {
            Element e = Element.build()
            expected.add(e)
            builder.append(pathDelimeter).append(e.label)
        }
        builder.append(pathDelimeter)


        when:
        NewElementAndPath result = ElementController.findElementsFromPathString(builder.toString(), pathDelimeter)


        then:
        expected.size() - 1 == result.newPathElementList.size()
        for (int i = 0; i < expected.size() - 1; i++) {
            expected.get(i) == result.newPathElementList.get(i)
        }

        expected.get(expected.size() - 1).label == result.newElementLabel
    }


    void "test empty path"() {
        setup:
        String pathString = "$pathDelimeter$pathDelimeter"
        when:
        ElementController.findElementsFromPathString(pathString, pathDelimeter)

        then:
        thrown(EmptyPathException)
    }


    void "test invalid element label in path"() {
        setup:
        final int num = 4

        StringBuilder builder = new StringBuilder()
        Set<Element> elementSet = new HashSet<Element>()

        for (int i = 0; i < num / 2; i++) {
            Element e = Element.build()
            elementSet.add(e)
            builder.append(pathDelimeter).append(e.label)
        }

        elementSet.add(Element.build())
        builder.append(pathDelimeter).append("this is my random label - dave lahr")

        for (int i = 0; i < num / 2; i++) {
            Element e = Element.build()
            elementSet.add(e)
            builder.append(pathDelimeter).append(e.label)
        }
        builder.append(pathDelimeter)


        when:
        ElementController.findElementsFromPathString(builder.toString(), pathDelimeter)


        then:
        thrown(UnrecognizedElementLabelException)
    }


    void "test empty element label in middle of path"() {
        setup:
        final int num = 4

        StringBuilder builder = new StringBuilder()
        Set<Element> elementSet = new HashSet<Element>()

        for (int i = 0; i < num / 2; i++) {
            Element e = Element.build()
            elementSet.add(e)
            builder.append(pathDelimeter).append(e.label)
        }

        elementSet.add(Element.build())
        builder.append(pathDelimeter).append(" ")

        for (int i = 0; i < num / 2; i++) {
            Element e = Element.build()
            elementSet.add(e)
            builder.append(pathDelimeter).append(e.label)
        }
        builder.append(pathDelimeter)


        when:
        ElementController.findElementsFromPathString(builder.toString(), pathDelimeter)


        then:
        thrown(EmptyPathSectionException)
    }

    void "test listAjax"() {
        when:
        controller.listAjax()


        then:
        controller.buildElementPathsService.buildAll() >> { [] }
        controller.buildElementPathsService.createListSortedByString(_) >> {
            new ElementAndFullPathListAndMaxPathLength([], 0)
        }
        controller.elementService.convertPathsToSelectWidgetStructures(_) >> { ["results"] }

        assert response.text == '{"results":["results"]}'
    }

    void "test edit"() {
        given:
        Element element = Element.build()

        when:
        def response = controller.edit(element.id)

        then:
        controller.ontologyDataAccessService.getAllUnits() >> { [] }

        assert response.element == element
    }

    void "test update #testDescription"() {
        given:
        Element element = Element.build()
        Element unitElement = Element.build(label: 'unit')
        params.id = element.id
        params.label = label
        params.elementStatus = ElementStatus.Published
        params.unit = unitElement
        params.abbreviation = abbreviation
        params.synonyms = "updateSynonyms"
        params.expectedValueType = expectedValueType
        params.addChildMethod = AddChildMethod.RDM_REQUEST
        params.description = "updateDescription"
        params.externalURL = externalURL

        when:
        def response = controller.update()

        then:
        assert flash.message.contains(expectedFlashMessage)

        where:
        testDescription                                                | expectedValueType                   | externalURL | abbreviation   | label                                     | expectedFlashMessage
        'external-ontology with external url'                          | ExpectedValueType.EXTERNAL_ONTOLOGY | 'url'       | 'abbreviation' | 'updateLabel'                             | 'saved successfully'
        'external-ontology with empty external url'                    | ExpectedValueType.EXTERNAL_ONTOLOGY | ''          | 'abbreviation' | 'updateLabel'                             | 'Failed to update element'
        'external-ontology with empty external url'                    | ExpectedValueType.FREE_TEXT         | ''          | 'abbreviation' | 'updateLabel'                             | 'saved successfully'
        'abbreviation is empty, label<30, expectedValueType=none'      | ExpectedValueType.NONE              | ''          | ''             | 'updateLabel'                             | 'saved successfully'
        'abbreviation is empty, label>30, expectedValueType=none'      | ExpectedValueType.NONE              | ''          | ''             | 'label_size_is_grater_than_30_characters' | 'saved successfully'
        'abbreviation is not empty, label>30, expectedValueType<>none' | ExpectedValueType.NUMERIC           | ''          | 'abbreviation' | 'label_size_is_grater_than_30_characters' | 'saved successfully'
        'abbreviation is empty, label>30, expectedValueType<>none'     | ExpectedValueType.NUMERIC           | ''          | ''             | 'label_size_is_grater_than_30_characters' | 'Failed to update element'
    }

    void "test editHierarchy"() {
        given:
        Element element = Element.build()

        when:
        def response = controller.editHierarchy(element.id)

        then:
        controller.buildElementPathsService.build(_) >> { [] as Set }
        controller.buildElementPathsService.createListSortedByString(_) >> {
            new ElementAndFullPathListAndMaxPathLength([], 0)
        }

        assert response?.list?.size() == 0
        assert response.element == element
    }

    void "test editHierarchy with exception"() {
        given:
        Element element = Element.build()

        when:
        controller.editHierarchy(element.id)

        then:
        controller.buildElementPathsService.build(_) >> {
            throw new BuildElementPathsServiceLoopInPathException(elementAndFullPath: new ElementAndFullPath(element), nextTopElementHierarchy: ElementHierarchy.build())
        }
        controller.buildElementPathsService.createListSortedByString(_) >> {
            new ElementAndFullPathListAndMaxPathLength([], 0)
        }

        assert response.text.contains("A loop was found in one of the paths")
    }

    void "test addElementPath success"() {
        given:
        Element element = Element.build()
        params.elementId = element.id
        params.select2FullPath = "/${element.label}/"
        params.select2ElementId = element.id.toString()

        when:
        controller.addElementPath()

        then:
        controller.buildElementPathsService.build(_) >> { [] }
        controller.buildElementPathsService.createListSortedByString(_) >> {
            new ElementAndFullPathListAndMaxPathLength([], 0)
        }
        controller.modifyElementAndHierarchyService.updateHierarchyIfNeeded(_) >> { true }

        assert flash.message == null
        assert response.redirectUrl == "/element/editHierarchy/${element.id}"
    }

    void "test addElementPath error message"() {
        given:
        Element element = Element.build()
        params.elementId = element.id
        params.select2FullPath = "/${element.label}/"
        params.select2ElementId = element.id.toString()

        when:
        controller.addElementPath()

        then:
        controller.buildElementPathsService.build(_) >> { [] }
        controller.buildElementPathsService.createListSortedByString(_) >> {
            new ElementAndFullPathListAndMaxPathLength([], 0)
        }
        controller.modifyElementAndHierarchyService.updateHierarchyIfNeeded(_) >> { false }

        assert flash.message.contains('Failed to add a new element-path')
    }

    void "test addElementPath BuildElementPathsServiceLoopInPathException thrown"() {
        given:
        this.controller.buildElementPathsService = new BuildElementPathsService()
        Element element = Element.build()
        params.elementId = element.id
        params.select2FullPath = "/${element.label}/"
        params.select2ElementId = element.id.toString()

        when:
        controller.addElementPath()

        then:
        controller.buildElementPathsService.build(_) >> { [] }
        controller.buildElementPathsService.createListSortedByString(_) >> {
            new ElementAndFullPathListAndMaxPathLength([], 0)
        }
        controller.modifyElementAndHierarchyService.updateHierarchyIfNeeded(_) >> {
            throw new ModifyElementAndHierarchyLoopInPathException(pathWithLoop: [])
        }

        assert flash.message.contains("A loop was found in the proposed path")
    }

    void "test deleteElementPath success"() {
        given:
        Element element = Element.build()
        params.elementId = element.id.toString()
        params.elementPathToDelete = "/${element.label}/"

        when:
        controller.deleteElementPath()

        then:
        controller.buildElementPathsService.build(_) >> { [new ElementAndFullPath(element, pathDelimeter)] as Set }
        controller.modifyElementAndHierarchyService.updateHierarchyIfNeeded(_) >> { true }

        assert flash.message == null
        assert response.redirectUrl == "/element/editHierarchy/${element.id}"
    }

}
