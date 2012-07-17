package dataexport.experiment

//import bard.db.experiment.Experiment


import bard.db.dictionary.Element
import dataexport.registration.MediaTypesDTO
import exceptions.NotFoundException
import groovy.xml.MarkupBuilder
import groovy.xml.StaxBuilder
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import bard.db.experiment.*

class ResultExportService {
    LinkGenerator grailsLinkGenerator
    MediaTypesDTO mediaTypes
    int maxResultsRecordsPerPage

    /**
     * Generate the results for a given experiment
     * @param markupBuilder - The markup builder to write
     * @param experimentId - The id of the experiment
     * @param offset - For paging
     * @return true if there are more results than can fit on a page
     */
    public boolean generateResults(final StaxBuilder markupBuilder, final Long experimentId, final int offset) {
        Experiment experiment = Experiment.get(experimentId)
        if (!experiment) {
            throw new NotFoundException("Could not find Experiment with Id ${experimentId}")
        }
        return generateResults(markupBuilder, experiment, offset)
    }
    /**
     * Given a resultId, find the result and generate an XML object
     * @param markupBuilder
     * @param resultId
     */
    public void generateResult(final MarkupBuilder markupBuilder, final Long resultId) {

        final Result result = Result.get(resultId)
        if (!result) {
            throw new NotFoundException("Could not find Result with Id ${resultId}")
        }
        generateResult(markupBuilder, result)

    }

    protected Map<String, String> generateAttributesForResult(final Result result) {
        Map<String, String> attributes = [:]

        if (result.readyForExtraction) {
            attributes.put('readyForExtraction', result.readyForExtraction)
        }
        if (result.valueDisplay) {
            attributes.put('valueDisplay', result.valueDisplay)
        }
        if (result.valueNum || result.valueNum.toString().isInteger()) {
            attributes.put('valueNum', result.valueNum.toString())
        }
        if (result.valueMin || result.valueMin.toString().isInteger()) {
            attributes.put('valueMin', result.valueMin.toString())
        }
        if (result.valueMax || result.valueMax.toString().isInteger()) {
            attributes.put('valueMax', result.valueMax.toString())
        }
        if (result.qualifier) {
            attributes.put('qualifier', result.qualifier)
        }
        if (result.resultStatus) {
            attributes.put('status', result.resultStatus)
        }
        return attributes
    }
    /**
     * Generate the result element
     * @param markupBuilder
     * @param result
     *
     */
    protected void generateResult(final MarkupBuilder markupBuilder, final Result result) {

        final Map<String, String> attributes = generateAttributesForResult(result)

        markupBuilder.result(attributes) {
            final Element resultType = result.resultType
            if (resultType) { //this is the result type
                resultTypeRef(label: resultType.label) {
                    final String href = grailsLinkGenerator.link(mapping: 'resultType', absolute: true, params: [id: resultType.id]).toString()
                    link(rel: 'related', href: "${href}", type: "${this.mediaTypes.resultTypeMediaType}")
                }
            }
            final Substance substances = result.substance
            if (substances) {
                substance(sid: substances.id.toString()) {
                }
            }

            final Set<ResultContextItem> resultContextItems = result.resultContextItems
            if (resultContextItems) {
                generateResultContextItems(markupBuilder, resultContextItems)
            }

            final Set<ResultHierarchy> hierarchies = [] as Set<ResultHierarchy>
            if (result.resultHierarchiesForParentResult) {
                hierarchies.addAll(result.resultHierarchiesForParentResult)
            }
            if (result.resultHierarchiesForResult) {
                hierarchies.addAll(result.resultHierarchiesForResult)
            }
            if (hierarchies) {
                generateResultHierarchies(markupBuilder, hierarchies)
            }
            generateResultLinks(markupBuilder, result)
        }

    }
    /**
     * Generate the links needed for an individual result element
     * @param markupBuilder
     * @param result
     */
    protected void generateResultLinks(def markupBuilder, final Result result) {

        final String experimentHref = grailsLinkGenerator.link(mapping: 'experiment', absolute: true, params: [id: result.experiment.id]).toString()

        markupBuilder.link(rel: 'up', title: 'Experiment', type: "${this.mediaTypes.experimentMediaType}",
                href: experimentHref) {
        }
        final String resultHref = grailsLinkGenerator.link(mapping: 'result', absolute: true, params: [id: result.id]).toString()
        markupBuilder.link(rel: 'edit', type: "${this.mediaTypes.resultMediaType}",
                href: resultHref) {
        }
    }
    /**
     * Generate the results for a given experiment
     * @param experimentId
     * @param numberOfResults
     * @param offset - Used for paging, marks the position of the current result element within the experiment
     * @return true if there are more results than can fit on a page
     */
    protected boolean generateResults(final StaxBuilder markupBuilder, final Experiment experiment, int offset) {

        int end = this.maxResultsRecordsPerPage + 1  //A trick to know if there are more records
        boolean hasMoreResults = false //This is used for paging, if there are more results than the threshold, add next link and return true

        List<Result> results = Result.findAllByExperimentAndReadyForExtraction(experiment, 'Ready', [sort: "id", order: "asc", offset: offset, max: end])
        final int numberOfResults = results.size()
        if (numberOfResults > this.maxResultsRecordsPerPage) {
            hasMoreResults = true
            results = results.subList(0, this.maxResultsRecordsPerPage) //we will leave one record behind but that is OK, since now we know there are more records
        }
        offset = this.maxResultsRecordsPerPage  //reset this to the max number of records
        markupBuilder.results(count: results.size()) {
            for (Result result : results) {
                final String resultHref = grailsLinkGenerator.link(mapping: 'result', absolute: true, params: [id: result.id]).toString()
                link(rel: 'related', type: "${this.mediaTypes.resultMediaType}", href: "${resultHref}")
            }
            generateResultsLinks(markupBuilder, experiment.id, hasMoreResults, offset)
        }
        return hasMoreResults
    }
    /**
     * Generate the links needed for the results element
     *
     * @param markupBuilder
     * @param experimentId
     * @param hasMoreResults
     * @param offset - Used for paging results, where to start the next time ou try to fetch results
     */
    protected void generateResultsLinks(final StaxBuilder markupBuilder, final Long experimentId, final boolean hasMoreResults, final int offset) {
        //if there are more records that can fit on a page then add the next link, with the offset parameter
        //being the end variable in this method
        if (hasMoreResults) {
            final String resultsHref = grailsLinkGenerator.link(mapping: 'results', absolute: true, params: [id: experimentId, offset: offset]).toString()
            markupBuilder.link(rel: 'next', type: "${this.mediaTypes.resultsMediaType}", href: "${resultsHref}")
        }
        final String experimentHref = grailsLinkGenerator.link(mapping: 'experiment', absolute: true, params: [id: experimentId]).toString()
        markupBuilder.link(rel: 'up', type: "${this.mediaTypes.experimentMediaType}", href: experimentHref)
    }
    /**
     *
     * @param markupBuilder
     * @param resultContextItems
     */
    protected void generateResultContextItems(def markupBuilder, final Set<ResultContextItem> resultContextItems) {
        markupBuilder.resultContextItems() {
            for (ResultContextItem resultContextItem : resultContextItems) {
                generateResultContextItem(markupBuilder, resultContextItem)
            }
        }
    }
    /**
     *
     * @param resultContextItem
     * @return
     */
    protected Map<String, String> generateAttributesForResultContextItem(final ResultContextItem resultContextItem) {
        Map<String, String> attributes = [:]
        attributes.put('resultContextItemId', resultContextItem.id?.toString())
        if (resultContextItem.parentGroup && resultContextItem.parentGroup.id.toString().isInteger()) {
            attributes.put('parentGroup', resultContextItem.parentGroup.id.toString())
        }

        if (resultContextItem.qualifier) {
            attributes.put('qualifier', resultContextItem.qualifier)
        }
        if (resultContextItem.valueDisplay) {
            attributes.put('valueDisplay', resultContextItem.valueDisplay)
        }
        if (resultContextItem.valueNum || resultContextItem.valueNum.toString().isInteger()) {
            attributes.put('valueNum', resultContextItem.valueNum.toString())
        }
        if (resultContextItem.valueMin || resultContextItem.valueMin.toString().isInteger()) {
            attributes.put('valueMin', resultContextItem.valueMin.toString())
        }
        if (resultContextItem.valueMax || resultContextItem.valueMax.toString().isInteger()) {
            attributes.put('valueMax', resultContextItem.valueMax.toString())
        }
        return attributes
    }
    /**
     *
     * @param markupBuilder
     * @param resultContextItem
     */
    protected void generateResultContextItem(def markupBuilder, final ResultContextItem resultContextItem) {

        final Map<String, String> attributes = generateAttributesForResultContextItem(resultContextItem)

        markupBuilder.resultContextItem(attributes) {

            if (resultContextItem.attribute) {
                final String attributeHref = grailsLinkGenerator.link(mapping: 'element', absolute: true, params: [id: "${resultContextItem.attribute.id}"]).toString()
                attribute(label: resultContextItem.attribute.label) {
                    link(rel: 'related', href: "${attributeHref}", type: "${this.mediaTypes.elementMediaType}")
                }
            }
            if (resultContextItem.valueControlled) {
                final String attributeHref = grailsLinkGenerator.link(mapping: 'element', absolute: true, params: [id: "${resultContextItem.valueControlled.id}"]).toString()

                valueControlled(label: resultContextItem.valueControlled.label) {
                    link(rel: 'related', href: "${attributeHref}", type: "${this.mediaTypes.elementMediaType}")
                }
            }
            if (resultContextItem.extValueId) {
                extValueId(resultContextItem.extValueId)
            }

        }
    }

    /**
     * Generate the RESULT_HIERARCHY element
     * @param markupBuilder
     * @param resultHierarchies
     */
    protected void generateResultHierarchies(def markupBuilder, final Set<ResultHierarchy> resultHierarchies) {
        markupBuilder.resultHierarchies() {
            for (ResultHierarchy resultHierarchy : resultHierarchies) {
                generateResultHierarchy(markupBuilder, resultHierarchy)
            }
        }
    }
    /**
     * Generate the RESULT_HIERARCHY element
     * @param markupBuilder
     * @param resultHierarchy
     */
    protected void generateResultHierarchy(def markupBuilder, final ResultHierarchy resultHierarchy) {
        Map<String, String> attributes = [:]
        if (resultHierarchy.parentResult) {
            attributes.put('parentResultId', resultHierarchy.parentResult.id.toString())
        }
        if (resultHierarchy.hierarchyType) {
            attributes.put('hierarchyType', resultHierarchy.hierarchyType.toString())
        }
        markupBuilder.resultHierarchy(attributes)
    }
}