package bard.core.rest.spring

import bard.core.rest.spring.assays.Assay
import bard.core.rest.spring.assays.AssayResult
import bard.core.rest.spring.assays.ExpandedAssay
import bard.core.rest.spring.compounds.CompoundResult
import bard.core.rest.spring.experiment.ExperimentData
import bard.core.rest.spring.experiment.ExperimentSearch
import bard.core.rest.spring.experiment.ExperimentSearchResult
import bard.core.rest.spring.experiment.ExperimentShow
import bard.core.rest.spring.project.ExpandedProjectResult
import bard.core.rest.spring.project.Project
import bard.core.rest.spring.project.ProjectResult
import grails.plugin.spock.IntegrationSpec
import spock.lang.Unroll

/**
 * Tests for RESTAssayService in JDO
 */
@Unroll
class RestCombinedServiceIntegrationSpec extends IntegrationSpec {
     ProjectRestService projectRestService
    ExperimentRestService experimentRestService
    CompoundRestService compoundRestService
    AssayRestService assayRestService
    /**
     *
     */
    void "test find compounds with experiment ID"() {
        given:
        final Long eid = 883
        when:
        final List<Long> cids = this.experimentRestService.compoundsForExperiment(eid)
        then:
        assert !cids.isEmpty()
        for (Long cid : cids) {
            println cid
        }
    }

    void "test findProjectsByAssayId #label=#adid"() {
        when:
        final ProjectResult projectResult = assayRestService.findProjectsByAssayId(adid)
        then:
        List<Project> projects = projectResult.projects
        and:
        assert projects
        assert !projects.isEmpty()
        and:

        for (Project project : projects) {
            assert project.getProjectId()
        }
        where:
        label      | adid
        "Assay ID" | 2868

    }

    /**
     * if you search for the this assay via the REST api, you get one experiment associated with the assay
     */
    void "test findExperimentsByAssayId #label"() {
        when: "We call the get method of the the RESTAssayService with an assay ids"
        List<ExperimentSearch> experiments = this.assayRestService.findExperimentsByAssayId(adid)
        then: "We expect to get back a list of assays"
        assert experiments


        where:
        label                           | adid
        "Search with a single assay id" | 644
    }
    /**
     *
     */
    void "test findAssaysByCID"() {
        given:
        Long cid = 999
        when:
        final AssayResult assayResult = this.compoundRestService.findAssaysByCID(cid)
        then:
        assert assayResult
        assert assayResult.assays

    }

    void "test findProjectsByCID #label"() {
        when:
        final ProjectResult projectResult = this.compoundRestService.findProjectsByCID(cid)
        then:
        final List<Project> projects = projectResult.projects
        assert !projects.isEmpty()

        where:
        label                       | cid
        "Find an existing compound" | 2722
    }


    void "test findCompoundsByExperimentId"() {
        given:
        Long eid = 2273
        when:
        CompoundResult compoundResult = this.experimentRestService.findCompoundsByExperimentId(eid)

        then:
        assert compoundResult
        assert compoundResult.compounds

    }


    void "test findExperimentsByProjectId"() {
        given:
        Long pid = 17
        when:
        List<Assay> assays = this.projectRestService.findAssaysByProjectId(pid)
        then:
        assert assays
    }

    void projToExptData() {

        given: "That we can retrieve the expts for project 274" //////////
        // This next section is the improved version, which goes directly from projects to experiments, rather than going through the assays explicitly.
        // Both approaches seem to produce the same results, but this alternate approach is more concise.

        final List<Long> cartProjectIdList = new ArrayList<Long>()
        cartProjectIdList.add(new Long(274))
        ExpandedProjectResult projectResult = this.projectRestService.searchProjectsByIds(cartProjectIdList)
        final List<Project> projects = projectResult.projects
        final List<ExperimentSearch> allExperiments = []
        for (final Project project : projects) {
            List<ExperimentSearch> experiments = this.projectRestService.findExperimentsByProjectId(project.id)
            allExperiments.addAll(experiments)
        }
        when: "We define an etag for a compound used in this project"  /////////////
        final List<Long> cartCompoundIdList = new ArrayList<Long>()
        cartCompoundIdList.add(new Long(5281847))
        String etag = this.compoundRestService.newETag((new Date()).toString(), cartCompoundIdList);


        then: "when we step through the value in the expt"    ////////

        int dataCount = 0
        for (ExperimentSearch experiment in allExperiments) {

            ExperimentData experimentData = this.experimentRestService.activities(experiment.id, etag)
            dataCount = dataCount + experimentData.activities.size()
        }

        // we expect to see some data, but we don't
        assert dataCount > 0   // this fails in V6, but not in V5
    }
    /**
     * if you search for the this assay via the REST api, you get one project associated with the assay: http://bard.nih.gov/api/latest/assays/588591/projects
     */
    /**
     * if you search for the this assay via the REST api, you get one experiment associated with the assay
     */
    void "test Get Assays with experiment #label"() {
        when: "We call the get method of the the RESTAssayService with an assay ids"
        final ExpandedAssay assay = this.assayRestService.getAssayById(adid)

        then: "We expect to get back a list of assays"
        assert assay
        List<ExperimentSearch> experiments = this.assayRestService.findExperimentsByAssayId(assay.id)
        assert experiments
        for (ExperimentSearch experiment : experiments) {
            assert experiment
        }
        where:
        label                           | adid
        "Search with a single assay id" | 644
    }

    void "test Get Assays with projects #label"() {
        when: "We call the get method of the the RESTAssayService with an assay ids"
        final ExpandedAssay assay = this.assayRestService.getAssayById(adid)

        then: "We expect to get back a list of assays"
        assert assay
        ProjectResult projectResult = assayRestService.findProjectsByAssayId(assay.id)
        final List<Project> projects = projectResult.projects
        assert projects
        for (Project project : projects) {
            assert project
        }


        where:
        label                           | adid
        "Search with a single assay id" | 644
    }

    void "test Project From Single Assay #label=#adid"() {
        given:
        ExpandedAssay assay = assayRestService.getAssayById(adid);
        when:
        ProjectResult projectResult = assayRestService.findProjectsByAssayId(assay.id);
        then:
        assert projectResult.projects
        and:
        for (Project project : projectResult.projects) {
            assert project.getId()
        }
        where:
        label      | adid
        "Assay ID" | 2868

    }

    void "test projects with experiments #label"() {

        when: "The get method is called with the given PID: #pid"
        Project project = this.projectRestService.getProjectById(pid)
        then: "A ProjectSearchResult is returned with the expected information"
        assert project
        assert pid == project.id
        assert project.experimentCount


        final List<ExperimentSearch> experiments = projectRestService.findExperimentsByProjectId(project.id)
        assert experiments
        for (ExperimentSearch experiment : experiments) {
            assert experiment
        }
        where:
        label                                  | pid
        "Find an existing ProjectSearchResult" | 179
    }

    void "test projects with assays #label"() {

        when: "The get method is called with the given PID: #pid"
        Project project = this.projectRestService.getProjectById(pid)
        then: "A ProjectSearchResult is returned with the expected information"
        assert project
        assert pid == project.id
        assert project.experimentCount

        final List<Assay> assays = projectRestService.findAssaysByProjectId(project.id)
        assert assays
        assert !assays.isEmpty()
        where:
        label                                  | pid
        "Find an existing ProjectSearchResult" | new Integer(179)
    }

    void "testExperimentsFromSingleProject"() {
        given:
        Project project = this.projectRestService.getProjectById(1);
        assert project

        when:
        List<ExperimentSearch> experiments = this.projectRestService.findExperimentsByProjectId(project.projectId)
        then:
        assert experiments
    }

    void "test pulling an arbitrary set of compounds out of an experiment and then looking at their experimental values #label"() {

        when: "The get method is called with the given experiment ID: #experimentid"
        final ExperimentShow experimentShow = this.experimentRestService.getExperimentById(experimentid)
        final List<Long> cids = this.experimentRestService.compoundsForExperiment(experimentid)
        then: "An experiment is returned with the expected information"
        assert experimentShow
        assert cids
        String etag = compoundRestService.newETag(label, cids);
        ExperimentData experimentData = this.experimentRestService.activities(experimentid, etag);
        assert experimentData.activities
        assert experimentData.activities.get(0)
        where:
        label                | experimentid  | numVals
        "Find an experiment" | new Long(883) | 2
    }

    void "test findAssaysByProjectId"() {
        given:
        Long pid = 17
        when:
        List<ExperimentSearch> experiments = this.projectRestService.findExperimentsByProjectId(pid)
        then:
        assert experiments
    }
    /**
     *
     */
    void "test findProjectsByExperimentId"() {
        given:
        final Long eid = 197
        when:
        final ProjectResult projectResult = experimentRestService.findProjectsByExperimentId(eid)

        then:
        assert projectResult
        assert projectResult.projects
    }

    void "test findExperimentsByCID"() {
        given:
        Long cid = 313619
        when:
        ExperimentSearchResult experimentResult = this.compoundRestService.findExperimentsByCID(cid)
        then:
        assert experimentResult
        assert experimentResult.experiments
    }
}