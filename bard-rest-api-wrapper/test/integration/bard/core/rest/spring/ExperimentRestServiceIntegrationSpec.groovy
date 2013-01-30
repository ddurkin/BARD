package bard.core.rest.spring

import bard.core.SearchParams
import bard.core.rest.helper.RESTTestHelper
import bard.core.rest.spring.assays.Assay
import grails.plugin.spock.IntegrationSpec
import spock.lang.Shared
import spock.lang.Timeout
import spock.lang.Unroll
import bard.core.rest.spring.experiment.*

/**
 * Tests for ProjectRestService
 */
@Mixin(RESTTestHelper)
@Unroll
class ExperimentRestServiceIntegrationSpec extends IntegrationSpec {
    ExperimentRestService experimentRestService
    CompoundRestService compoundRestService
    @Shared
    List<Long> TEST_CIDS = [5926293, 6197]
    @Shared
    List<Long> TEST_SIDS = [67101, 67121]
    @Shared
    List<Long> TEST_ADIDS = [5155, 5156]
    @Shared
    List<Long> TEST_EIDS = [13902, 14980]
    @Shared
    List<Long> TEST_EID_LONG_LIST = [460, 461, 197, 198, 4171, 3278, 3274, 3277, 2362, 2637]


    void "test activitiesByCIDs"() {
        given:
        final List<Long> cids = TEST_CIDS
        final SearchParams searchParams = new SearchParams(top: 10, skip: 0)
        when:
        final ExperimentData activitiesByCIDs = experimentRestService.activitiesByCIDs(cids, searchParams)

        then:
        assert activitiesByCIDs
        assert activitiesByCIDs.activities
        assert activitiesByCIDs.activities.size() == 10
    }

    void "test activitiesBySIDs"() {
        given:
        final List<Long> sids = TEST_SIDS
        final SearchParams searchParams = new SearchParams(top: 10, skip: 0)
        when:
        final ExperimentData activitiesBySIDs = experimentRestService.activitiesBySIDs(sids, searchParams)

        then:
        assert activitiesBySIDs
        assert activitiesBySIDs.activities
        assert activitiesBySIDs.activities.size() == 10
    }

    void "test activitiesByADIDs"() {
        given:
        final List<Long> adids = TEST_ADIDS
        final SearchParams searchParams = new SearchParams(top: 10, skip: 0)
        when:
        final ExperimentData activitiesByADIDs = experimentRestService.activitiesByADIDs(adids, searchParams)

        then:
        assert activitiesByADIDs
        assert activitiesByADIDs.activities
        assert activitiesByADIDs.activities.size() == 10
    }

    void "test activitiesByEIDs"() {
        given:
        final List<Long> eids = TEST_EIDS
        final SearchParams searchParams = new SearchParams(top: 10, skip: 0)
        when:
        final ExperimentData activitiesByEIDs = experimentRestService.activitiesByEIDs(eids, searchParams)

        then:
        assert activitiesByEIDs
        assert activitiesByEIDs.activities
        assert activitiesByEIDs.activities.size() == 10
    }



    void "test pull other values out of an experiment"() {

        when: "The get method is called with the given experiment ID: #experimentid"
        final ExperimentShow experiment = this.experimentRestService.getExperimentById(experimentid)
        then: "An experiment is returned with the expected information"
        assert experiment.getId() == experimentid
        List<Assay> assays = experiment.getAssays()
        assert assays
        List<Long> projectCollection = experiment.getProjectIdList()
        assert projectCollection
        int projectCount = experiment.getProjectCount()
        assert projectCollection.size() == projectCount
        where:
        label                | experimentid
        "Find an experiment" | TEST_EIDS.get(0)
    }

    void "testExperiment"() {
        given:
        final List<Long> experimentIds = TEST_EID_LONG_LIST
        when:
        final ExperimentSearchResult experimentResult = experimentRestService.searchExperimentsByIds(experimentIds)
        then:
        assert experimentResult
        assert experimentResult.experiments
        assert experimentResult.experiments.size() == experimentIds.size()
    }

    void "test step through entire Experiment #experimentId"() {

        when: "The get method is called with the given experiment ID: #experimentId"
        final ExperimentShow experimentShow = this.experimentRestService.getExperimentById(experimentId)
        then: "An experiment is returned with the expected information"
        assert experimentShow
        assert experimentShow.getId() == experimentId
        assertExperimentSearchResult(experimentShow)
        where:
        label                | experimentId
        "Find an experiment" | TEST_EIDS.get(0) // short expt (3000 values)
        "Find an experiment" | TEST_EIDS.get(1)  // longer expt (193717 values) -- runs for 8 minutes!
    }

    void "test retrieving the experimental data for known compounds in an experiment #label"() {
        given:
        final String etag = compoundRestService.newETag(label, cids);

        when: "We call the getFactest matehod"
        final ExperimentData experimentData = this.experimentRestService.activities(experimentid, etag);

        then: "We expect to get back a list of facets"
        assert experimentData
        assert experimentData.activities
        for (Activity activity : experimentData.activities) {
            assert activity
        }

        where:
        label                            | experimentid    | cids
        "Search with a list of CIDs"     | new Long(11795) | [16452494, 16014490]
        "Search with a single of CID"    | new Long(11795) | [16452494]
        "Search with another set of CID" | new Long(11795) | [16453200, 16446093]
    }


    void "tests getExperimentActivities #label"() {
        when: "We call the restExperimentService.activities method with the experiment"
        final ExperimentData experimentData = experimentRestService.activities(experimentId);

        then: "We expect activities for each of the experiments to be found"
        assert experimentData
        final List<Activity> activities = experimentData.activities
        assert activities
        where:
        label                                        | experimentId
        "For an existing experiment with activities" | TEST_EIDS.get(0)
    }

    void "tests new json #label"() {
        when: "We call the restExperimentService.activities method with the experiment"
        final ExperimentData experimentData = experimentRestService.activities(experimentId);

        then: "We expect activities for each of the experiments to be found"
        assert experimentData
        final List<Activity> activities = experimentData.activities
        Activity activity = activities.get(0)

        ResultData resultData = activity.getResultData()
        assert resultData
        ResultData resultData1 = activity.getResultData() //we call this twice because we want to make sure that the serialization happens only once
        assert resultData1
        assert resultData.getPriorityElements().size() == resultData1.getPriorityElements().size()
        final ConcentrationResponseSeries concentrationResponseSeries = resultData.getPriorityElements().get(0).getConcentrationResponseSeries()
        assert concentrationResponseSeries
        assert concentrationResponseSeries.getDictionaryDescription()
        assert concentrationResponseSeries.getDictionaryLabel()
        final Map<String, List<Double>> points = ConcentrationResponseSeries.toDoseResponsePoints(concentrationResponseSeries.concentrationResponsePoints)
        assert points
        where:
        label                                        | experimentId
        "For an existing experiment with activities" | TEST_EIDS.get(0)
    }

    void "testExperimentActivity"() {
        given:
        final Long experimentId = TEST_EIDS.get(0)
        when:
        final ExperimentData experimentData = experimentRestService.activities(experimentId)
        then:
        final List<Activity> activities = experimentData.activities
        assert activities
    }

    public void "testExperimentActivityWithCompounds"() {
        given: "That an etag is created from a list of compound IDs"
        final String etag = compoundRestService.newETag("foo cids", [644794L, 645320L, 646386L]);
        and: "That an experiment exists"
        final Long experimentId = 14743
        when: "Experiment with the compound etags"
        final ExperimentData experimentData = experimentRestService.activities(experimentId, etag)
        then:
        assert experimentData
        final List<Activity> activities = experimentData.activities
        assert activities
        assert !activities.isEmpty()
        for (Activity activity : activities) {
            assert activity
        }
        assert activities.size() == 3
    }
    /**
     * Step through the experiment
     * @param experiment
     */
    void assertExperimentSearchResult(final ExperimentShow experimentShow) {
        final ExperimentData experimentData = this.experimentRestService.activities(experimentShow.id)
        final List<Activity> activities = experimentData.activities
        assert activities
        for (Activity activity : activities) {
            assert activity
        }

    }
}