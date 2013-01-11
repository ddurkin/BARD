package bard.db.project

import grails.buildtestdata.mixin.Build
import org.junit.Before
import spock.lang.Specification
import spock.lang.Unroll

import static bard.db.project.ProjectStep.EDGE_NAME_MAX_SIZE
import static bard.db.project.ProjectStep.MODIFIED_BY_MAX_SIZE
import static test.TestUtils.assertFieldValidationExpectations
import static test.TestUtils.createString

/**
 * Created with IntelliJ IDEA.
 * User: ddurkin
 * Date: 8/23/12
 * Time: 12:07 AM
 * To change this template use File | Settings | File Templates.
 */
@Build(ProjectStep)
@Unroll
class ProjectStepConstraintUnitSpec extends Specification {

    ProjectStep domainInstance

    @Before
    void doSetup() {
        domainInstance = ProjectStep.buildWithoutSave()
    }

    void "test nextProjectExperiment constraints #desc"() {

        final String field = 'nextProjectExperiment'

        when:
        domainInstance[(field)] = valueUnderTest.call()
        domainInstance.validate()

        then:
        assertFieldValidationExpectations(domainInstance, field, valid, errorCode)

        where:
        desc                          | valueUnderTest              | valid | errorCode
        'null not valid'              | {null}                      | false | 'nullable'
        'valid nextProjectExperiment' | {ProjectExperiment.build()} | true  | null

    }

    void "test previousProjectExperiment constraints #desc"() {

        final String field = 'previousProjectExperiment'

        when:
        domainInstance[(field)] = valueUnderTest.call()
        domainInstance.validate()

        then:
        assertFieldValidationExpectations(domainInstance, field, valid, errorCode)

        where:
        desc                              | valueUnderTest              | valid | errorCode
        'null not valid'                  | {null}                      | false | 'nullable'
        'valid previousProjectExperiment' | {ProjectExperiment.build()} | true  | null

    }

    void "test edgeName constraints #desc edgeName: '#valueUnderTest'"() {

        final String field = 'edgeName'

        when: 'a value is set for the field under test'
        domainInstance[(field)] = valueUnderTest
        domainInstance.validate()

        then: 'verify valid or invalid for expected reason'
        assertFieldValidationExpectations(domainInstance, field, valid, errorCode)

        where:
        desc               | valueUnderTest                       | valid | errorCode
        'too long'         | createString(EDGE_NAME_MAX_SIZE + 1) | false | 'maxSize.exceeded'
        'blank valid'      | ''                                   | false | 'blank'
        'blank valid'      | '  '                                 | false | 'blank'

        'exactly at limit' | createString(EDGE_NAME_MAX_SIZE)     | true  | null
        'null valid'       | null                                 | true  | null
    }

    void "test modifiedBy constraints #desc modifiedBy: '#valueUnderTest'"() {

        final String field = 'modifiedBy'

        when: 'a value is set for the field under test'
        domainInstance[(field)] = valueUnderTest
        domainInstance.validate()

        then: 'verify valid or invalid for expected reason'
        assertFieldValidationExpectations(domainInstance, field, valid, errorCode)

        where:
        desc               | valueUnderTest                         | valid | errorCode
        'too long'         | createString(MODIFIED_BY_MAX_SIZE + 1) | false | 'maxSize.exceeded'
        'blank valid'      | ''                                     | false | 'blank'
        'blank valid'      | '  '                                   | false | 'blank'

        'exactly at limit' | createString(MODIFIED_BY_MAX_SIZE)     | true  | null
        'null valid'       | null                                   | true  | null
    }

    void "test dateCreated constraints #desc dateCreated: '#valueUnderTest'"() {
        final String field = 'dateCreated'

        when: 'a value is set for the field under test'
        domainInstance[(field)] = valueUnderTest
        domainInstance.validate()

        then: 'verify valid or invalid for expected reason'
        assertFieldValidationExpectations(domainInstance, field, valid, errorCode)

        where:
        desc             | valueUnderTest | valid | errorCode
        'null not valid' | null           | false | 'nullable'
        'date valid'     | new Date()     | true  | null
    }

    void "test lastUpdated constraints #desc lastUpdated: '#valueUnderTest'"() {
        final String field = 'lastUpdated'

        when: 'a value is set for the field under test'
        domainInstance[(field)] = valueUnderTest
        domainInstance.validate()

        then: 'verify valid or invalid for expected reason'
        assertFieldValidationExpectations(domainInstance, field, valid, errorCode)

        where:
        desc         | valueUnderTest | valid | errorCode
        'null valid' | null           | true  | null
        'date valid' | new Date()     | true  | null
    }

}