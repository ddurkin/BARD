package bard.db.registration

import bard.db.BardIntegrationSpec
import bard.db.enums.AssayStatus
import bard.db.enums.AssayType
import spock.lang.Unroll

/**
 * Created with IntelliJ IDEA.
 * User: ddurkin
 * Date: 8/21/12
 * Time: 9:18 AM
 * To change this template use File | Settings | File Templates.
 */
@Unroll
class AssayDefinitionServiceIntegrationSpec extends BardIntegrationSpec {

    AssayDefinitionService assayDefinitionService
    void "test update designed By"() {
        given:
        final Assay assay = Assay.build(assayName: 'assayName20', designedBy: "BARD")
        final String newDesignedBy = "CAP"
        when:
        final Assay updatedAssay = assayDefinitionService.updateDesignedBy(assay.id, newDesignedBy)
        then:
        assert newDesignedBy == updatedAssay.designedBy
    }
    void "test update assay name"() {
        given:
        final Assay assay = Assay.build(assayName: 'assayName20', assayStatus: AssayStatus.DRAFT)
        final String newAssayName = "New Assay Name"
        when:
        final Assay updatedAssay = assayDefinitionService.updateAssayName(assay.id, newAssayName)
        then:
        assert newAssayName == updatedAssay.assayName
    }
    void "test update assay status"() {
        given:
        final Assay assay = Assay.build(assayName: 'assayName10', assayStatus: AssayStatus.DRAFT)
        when:
        final Assay updatedAssay = assayDefinitionService.updateAssayStatus(assay.id, AssayStatus.APPROVED)
        then:
        assert AssayStatus.APPROVED == updatedAssay.assayStatus
    }
    void "test update assay type"() {
        given:
        final Assay assay = Assay.build(assayName: 'assayName10', assayType: AssayType.PANEL_GROUP)
        when:
        final Assay updatedAssay = assayDefinitionService.updateAssayType(assay.id, AssayType.TEMPLATE)
        then:
        assert AssayType.TEMPLATE == updatedAssay.assayType
    }

    void "test save new Assay"() {
        given:
        final Assay assay = Assay.build(assayName: 'assayName40', assayType: AssayType.PANEL_GROUP)
        when:
        final Assay updatedAssay = assayDefinitionService.saveNewAssay(assay)
        then:
        assert AssayType.TEMPLATE == updatedAssay.assayType
        //TODO: make sure the ACL tables are also populated
    }
}