package bard.db.registration

import bard.db.dictionary.Element
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.junit.Before
import spock.lang.Specification
import grails.buildtestdata.mixin.Build

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */


@TestFor(AssayDefinitionController)
@TestMixin(DomainClassUnitTestMixin)
@Build([Assay,Element,AssayContext, AssayContextMeasure])
class AssayDefinitionControllerUnitSpec extends Specification {

    Assay assay

    @Before
    void setup() {
        assay = Assay.build(assayName:'Test')
        assert assay.validate()
    }

    void 'test show'() {

        when:
        params.id = assay.id
        def model = controller.show()

        then:
        model.assayInstance == assay
    }

    void 'testFindById()'() {

        when:
        params.assayId = "${assay.id}"
        controller.findById()

        then:
        "/assayDefinition/show/${assay.id}" == controller.response.redirectedUrl
    }

	void 'testFindByName'() {
        when:
		params.assayName = assay.assayName
		controller.findByName()

        then:
        "/assayDefinition/show/${assay.id}" == controller.response.redirectedUrl
	}

    void 'test editMeasure'() {
        when:
            params.id = assay.id
            def model = controller.show()

        then:
            model.assayInstance == assay
    }

    void 'test add measure'() {
        when:
            mockDomain(Element)
            Element resultType = Element.build()
            Element statistic = Element.build()
            Element.saveAll([resultType, statistic])

            params.id = assay.id
            params.resultTypeId = resultType.id
            params.statisticId = statistic.id

            def assayContextService = mockFor(AssayContextService)
            assayContextService.demand.addMeasure(1) {assayInstance, parentMeasure, rt, sm, entryUnit ->
                assert assayInstance == assay
                assert parentMeasure == null
                assert rt == resultType
                assert sm == statistic
                assert entryUnit == null

                return Measure.build()
            }
            controller.setAssayContextService(assayContextService.createMock())
            controller.addMeasure()

        then:
        response.redirectedUrl == '/assayDefinition/editMeasure/'+assay.id

        when:
        assayContextService.verify()

        then:
        notThrown(Exception.class)
    }

    void 'test delete measure'() {
        when:
        mockDomain(Measure)
        def measure = Measure.build(assay: assay)

        then:
        Measure.count == 1

        when:
        params.id = assay.id
        params.measureId = measure.id
        controller.deleteMeasure()

        then:
        response.redirectedUrl == '/assayDefinition/editMeasure/'+assay.id
        Measure.count == 0
    }

    void 'test associate context'() {
        when:
        assay = Assay.build(assayName:'Test')
        def context = AssayContext.build(assay: assay)
        def measure = Measure.build(assay: assay)
        params.id = assay.id
        params.measureId = measure.id
        params.assayContextId = context.id

        def assayContextService = mockFor(AssayContextService)
        assayContextService.demand.associateContext(1) {Measure measureParam, AssayContext contextParam ->
            assert measureParam == measure
            assert contextParam == context
        }
        controller.setAssayContextService(assayContextService.createMock())
        controller.associateContext()

        then:
        response.redirectedUrl == '/assayDefinition/editMeasure/'+assay.id

        when:
        assayContextService.verify()

        then:
        notThrown(Exception.class)
    }

    void 'test disassociate context'() {
        when:
        assay = Assay.build(assayName:'Test')
        AssayContext context = AssayContext.build(assay: assay)
        Measure measure = Measure.build(assay: assay)
        def contextMeasure = AssayContextMeasure.build(measure: measure, assayContext: context)
        context.addToAssayContextMeasures(contextMeasure)
        measure.addToAssayContextMeasures(contextMeasure)

        params.id = assay.id
        params.measureId = measure.id
        params.assayContextId = context.id
        def assayContextService = mockFor(AssayContextService)
        assayContextService.demand.disassociateContext(1) {Measure measureParam, AssayContext contextParam ->
            assert measureParam == measure
            assert contextParam == context
        }
        controller.setAssayContextService(assayContextService.createMock())
        controller.disassociateContext()

        then:
        response.redirectedUrl == '/assayDefinition/editMeasure/'+assay.id

        when:
        assayContextService.verify()

        then:
        notThrown(Exception.class)
    }
}