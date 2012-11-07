package bard.db.experiment

import bard.db.registration.AbstractContextConstraintIntegrationSpec
import org.junit.Before
import spock.lang.Unroll

import static test.TestUtils.assertFieldValidationExpectations

/**
 * Created with IntelliJ IDEA.
 * User: ddurkin
 * Date: 11/6/12
 * Time: 5:59 PM
 * To change this template use File | Settings | File Templates.
 */
@Unroll
class StepContextConstraintIntegrationSpec extends AbstractContextConstraintIntegrationSpec {

    @Before
    @Override
    void doSetup() {
        domainInstance = StepContext.buildWithoutSave()
    }

    void "test projectStep constraints #desc projectStep: '#valueUnderTest'"() {
        final String field = 'projectStep'

        when: 'a value is set for the field under test'
        domainInstance[(field)] = valueUnderTest.call()
        domainInstance.validate()

        then: 'verify valid or invalid for expected reason'
        assertFieldValidationExpectations(domainInstance, field, valid, errorCode)

        and: 'verify the domainspreadsheetmapping can be persisted to the db'
        if (valid) {
            domainInstance == domainInstance.save(flush: true)
        }

        where:
        desc             | valueUnderTest        | valid | errorCode
        'null not valid' | {null}                | false | 'nullable'
        'valid step'     | {ProjectStep.build()} | true  | null
    }
}