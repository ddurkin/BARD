package bard.db.people

import bard.db.BardIntegrationSpec
import org.junit.Before
import spock.lang.Unroll


import static test.TestUtils.assertFieldValidationExpectations
import static test.TestUtils.createString

/**
 */
@Unroll
class RoleConstraintIntegrationSpec extends BardIntegrationSpec {
    Role domainInstance

    @Before
    void doSetup() {

    }

    void "test authority constraints #desc name: '#valueUnderTest'"() {
        given:
        domainInstance = Role.buildWithoutSave()
        final String field = 'authority'

        when: 'a value is set for the field under test'
        domainInstance[(field)] = valueUnderTest
        domainInstance.validate()

        then: 'verify valid or invalid for expected reason'
        assertFieldValidationExpectations(domainInstance, field, valid, errorCode)

        and: 'verify domain can be persisted to the db'
        if (valid) {
            domainInstance == domainInstance.save(flush: true)
        }

        where:
        desc               | valueUnderTest                             | valid | errorCode
        'null '            | null                                       | false | 'nullable'
        'blank value'      | ''                                         | false | 'blank'
        'blank value'      | '  '                                       | false | 'blank'
        'too long'         | createString(Role.AUTHORITY_NAME_SIZE + 1) | false | 'maxSize.exceeded'
        'exactly at limit' | createString(Role.AUTHORITY_NAME_SIZE)     | true  | null
    }

    void "test role constraints #desc display name: '#valueUnderTest'"() {
        given:
        domainInstance = Role.buildWithoutSave()
        final String field = 'displayName'

        when: 'a value is set for the field under test'
        domainInstance[(field)] = valueUnderTest
        domainInstance.validate()

        then: 'verify valid or invalid for expected reason'
        assertFieldValidationExpectations(domainInstance, field, valid, errorCode)

        and: 'verify domain can be persisted to the db'
        if (valid) {
            domainInstance == domainInstance.save(flush: true)
        }

        where:
        desc               | valueUnderTest                           | valid | errorCode
        'null '            | null                                     | true  | null
        'blank value'      | ''                                       | false | 'blank'
        'blank value'      | '  '                                     | false | 'blank'
        'too long'         | createString(Role.DISPLAY_NAME_SIZE + 1) | false | 'maxSize.exceeded'
        'exactly at limit' | createString(Role.DISPLAY_NAME_SIZE)     | true  | null
    }

    void "test modifiedBy constraints #desc name: '#valueUnderTest'"() {
        given:
        domainInstance = Role.buildWithoutSave()
        final String field = 'modifiedBy'

        when: 'a value is set for the field under test'
        domainInstance[(field)] = valueUnderTest
        domainInstance.validate()

        then: 'verify valid or invalid for expected reason'
        assertFieldValidationExpectations(domainInstance, field, valid, errorCode)

        and: 'verify domain can be persisted to the db'
        if (valid) {
            domainInstance == domainInstance.save(flush: true)
        }

        where:
        desc               | valueUnderTest                                | valid | errorCode
        'null '            | null                                          | true  | null
        'blank value'      | ''                                            | false | 'blank'
        'blank value'      | '  '                                          | false | 'blank'
        'too long'         | createString(Person.MODIFIED_BY_MAX_SIZE + 1) | false | 'maxSize.exceeded'
        'exactly at limit' | createString(Person.MODIFIED_BY_MAX_SIZE)     | true  | null
    }
}
