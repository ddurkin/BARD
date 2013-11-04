package bard.db.registration

import bard.db.BardIntegrationSpec
import bard.db.dictionary.Element
import bard.db.experiment.AssayContextExperimentMeasure
import bard.db.experiment.Experiment
import bard.db.experiment.ExperimentMeasure
import org.junit.After
import org.junit.Before
import spock.lang.Unroll

import static bard.db.registration.ExternalReference.MODIFIED_BY_MAX_SIZE
import static test.TestUtils.assertFieldValidationExpectations
import static test.TestUtils.createString

/**
 * Created with IntelliJ IDEA.
 * User: ddurkin
 * Date: 11/13/12
 * Time: 4:10 PM
 * To change this template use File | Settings | File Templates.
 */
@Unroll
class AssayContextExperimentMeasureConstraintIntegrationSpec extends BardIntegrationSpec {

    AssayContextExperimentMeasure domainInstance

    @Before
    void doSetup() {
        Assay assay = Assay.build()
        AssayContext assayContext = AssayContext.build(assay: assay)
        Experiment experiment = Experiment.build(assay: assay)
        ExperimentMeasure experimentMeasure = ExperimentMeasure.build(experiment: experiment, resultType: Element.build())
        domainInstance = AssayContextExperimentMeasure.buildWithoutSave(assayContext: assayContext, experimentMeasure: experimentMeasure)
    }

    @After
    void doAfter() {
        if (domainInstance.validate()) {
            domainInstance.save(flush: true)
        }
    }

    void "test assayContext constraints #desc assayContext: '#valueUnderTest'"() {

        final String field = 'assayContext'

        when:
        domainInstance[(field)] = valueUnderTest.call()
        domainInstance.validate()

        then:
        assertFieldValidationExpectations(domainInstance, field, valid, errorCode)

        where:
        desc                 | valueUnderTest                             | valid | errorCode
        'null not valid'     | { null }                                   | false | 'nullable'
        'valid assayContext' | { AssayContext.build().save(flush: true) } | true  | null

    }

    void "test experiment measure constraints #desc measure: '#valueUnderTest'"() {

        final String field = 'experimentMeasure'

        when:
        domainInstance[(field)] = valueUnderTest.call()
        domainInstance.validate()

        then:
        assertFieldValidationExpectations(domainInstance, field, valid, errorCode)

        where:
        desc             | valueUnderTest                        | valid | errorCode
        'null not valid' | { null }                              | false | 'nullable'
        'valid measure'  | { ExperimentMeasure.build().save(flush: true) } | true  | null

    }

    void "test modifiedBy constraints #desc modifiedBy: '#valueUnderTest'"() {

        final String field = 'modifiedBy'

        when:
        domainInstance[(field)] = valueUnderTest
        domainInstance.validate()

        then:
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

        when:
        domainInstance[(field)] = valueUnderTest
        domainInstance.validate()

        then:
        assertFieldValidationExpectations(domainInstance, field, valid, errorCode)

        where:
        desc             | valueUnderTest | valid | errorCode
        'null not valid' | null           | false | 'nullable'
        'date valid'     | new Date()     | true  | null
    }

    void "test lastUpdated constraints #desc lastUpdated: '#valueUnderTest'"() {
        final String field = 'lastUpdated'

        when:
        domainInstance[(field)] = valueUnderTest
        domainInstance.validate()

        then:
        assertFieldValidationExpectations(domainInstance, field, valid, errorCode)


        where:
        desc         | valueUnderTest | valid | errorCode
        'null valid' | null           | true  | null
        'date valid' | new Date()     | true  | null
    }
}