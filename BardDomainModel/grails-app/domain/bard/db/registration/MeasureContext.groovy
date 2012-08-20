package bard.db.registration

class MeasureContext {

    private static final int CONTEXT_NAME_MAX_SIZE = 128
    private static final int MODIFIED_BY_MAX_SIZE = 40

    String contextName
    Assay assay

    Set<Measure> measures = [] as Set<Measure>
    Set<MeasureContextItem> measureContextItems = [] as Set<MeasureContextItem>

    Date dateCreated
    Date lastUpdated
    String modifiedBy

    static hasMany = [measureContextItems: MeasureContextItem,
            measures: Measure]

    static mapping = {
        id(column: "MEASURE_CONTEXT_ID", generator: "sequence", params: [sequence: 'MEASURE_CONTEXT_ID_SEQ'])
    }

    static constraints = {
        contextName(maxSize: CONTEXT_NAME_MAX_SIZE, blank: false)
        assay()
        dateCreated(nullable: false)
        lastUpdated(nullable: true)
        modifiedBy(nullable: true, blank: false, maxSize: MODIFIED_BY_MAX_SIZE)
    }
}
