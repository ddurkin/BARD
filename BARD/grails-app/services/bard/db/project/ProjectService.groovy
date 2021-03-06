/* Copyright (c) 2014, The Broad Institute
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of The Broad Institute nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL The Broad Institute BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package bard.db.project

import acl.CapPermissionService
import bard.db.dictionary.Element
import bard.db.enums.Status
import bard.db.experiment.Experiment
import bard.db.experiment.PanelExperiment
import bard.db.people.Person
import bard.db.people.Role
import org.springframework.security.access.prepost.PreAuthorize

class ProjectService {
    CapPermissionService capPermissionService
    def springSecurityService
    final static String BARD_PROBE_URI = "http://www.bard.nih.gov/ontology/bard#BARD_0001682"
    final static String PUBCHEM_CID_URI = "http://www.bard.nih.gov/ontology/bard#BARD_0001205"

    List<Long> findApprovedProbeProjects() {

        final String PROBES_QUERY = '''
                SELECT DISTINCT project.id FROM Project project inner join project.contexts context
                inner join context.contextItems contextItem
                inner join contextItem.attributeElement element WHERE element.bardURI=? and
                project.ncgcWarehouseId is not null and ( project.projectStatus=? or project.projectStatus=?)
                '''

        final List<Long> projectIds = Project.executeQuery(PROBES_QUERY,
                [BARD_PROBE_URI, Status.APPROVED, Status.PROVISIONAL])

        return projectIds
    }

    List<Long> findApprovedProbeCompounds() {

        final String PROBES_QUERY = '''
                SELECT DISTINCT contextItem.extValueId FROM Project project inner join project.contexts context
                inner join context.contextItems contextItem
                inner join contextItem.attributeElement element WHERE element.bardURI=? and
                project.ncgcWarehouseId is not null and ( project.projectStatus=? or project.projectStatus=?)
                '''

        final List cids = Project.executeQuery(PROBES_QUERY,
                [PUBCHEM_CID_URI, Status.APPROVED, Status.PROVISIONAL]) as List<Long>

        return cids.collect { it as Long }
    }
    /**
     * Returns total number of probes. Please note that a probe could be reported either as a CID or an SID (https://www.pivotaltracker.com/story/show/62094690).
     * @return
     */
    Long totalNumberOfProbes() {

        final String TOTAL_PROBES_QUERY = '''
                SELECT count(*) as total FROM Project project inner join project.contexts context
                inner join context.contextItems contextItem
                inner join contextItem.attributeElement element WHERE element.bardURI=? and
                project.ncgcWarehouseId is not null and  ( project.projectStatus=? or project.projectStatus=?)
                '''

        final Long total = Project.executeQuery(TOTAL_PROBES_QUERY,
                [BARD_PROBE_URI, Status.APPROVED, Status.PROVISIONAL]).first()

        return total
    }

    @PreAuthorize("hasPermission(#id, 'bard.db.project.Project', admin) or hasRole('ROLE_BARD_ADMINISTRATOR')")
    Project updateOwnerRole(Long id, Role ownerRole) {
        Project project = Project.findById(id)
        project.ownerRole = ownerRole

        project.save(flush: true)

        capPermissionService.updatePermission(project, ownerRole)
        return Project.findById(id)
    }

    @PreAuthorize("hasPermission(#id, 'bard.db.project.Project', admin) or hasRole('ROLE_BARD_ADMINISTRATOR')")
    Project updateProjectStatus(Long id, Status newProjectStatus) {
        Project project = Project.findById(id)
        project.projectStatus = newProjectStatus
        if ((newProjectStatus.equals(Status.APPROVED) || newProjectStatus.equals(Status.PROVISIONAL))  && project.isDirty('projectStatus')) {
            Person currentUser = Person.findByUserName(springSecurityService.authentication.name)
            project.approvedBy = currentUser
            project.approvedDate = new Date()
        }
        project.save(flush: true)
        return Project.findById(id)
    }

    @PreAuthorize("hasPermission(#id, 'bard.db.project.Project', admin) or hasRole('ROLE_BARD_ADMINISTRATOR')")
    Project updateProjectDescription(Long id, String newProjectDescription) {
        Project project = Project.findById(id)
        project.description = newProjectDescription

        project.save(flush: true)
        return Project.findById(id)
    }

    @PreAuthorize("hasPermission(#id, 'bard.db.project.Project', admin) or hasRole('ROLE_BARD_ADMINISTRATOR')")
    Project updateProjectName(Long id, String newProjectName) {
        Project project = Project.findById(id)
        project.name = newProjectName

        project.save(flush: true)
        return Project.findById(id)
    }

    @PreAuthorize("hasPermission(#id, 'bard.db.project.Project', admin) or hasRole('ROLE_BARD_ADMINISTRATOR')")
    ProjectExperiment updateProjectStage(Long id, ProjectExperiment projectExperiment, Element newStage) {
        projectExperiment.stage = newStage
        projectExperiment.save(flush: true)
        return ProjectExperiment.findById(projectExperiment.id)

    }
    /**
     * remove experiment from project, if experiment has context, remove them. remove all steps associated with
     * this experiment, and step contexts
     * @param experiment
     * @param project
     */
    @PreAuthorize("hasPermission(#id, 'bard.db.project.Project', admin) or hasRole('ROLE_BARD_ADMINISTRATOR')")
    void removeExperimentFromProject(Experiment experiment, Long id) {
        Project project = Project.findById(id)
        def projectExperiment = ProjectSingleExperiment.findByExperimentAndProject(experiment, project)
        if (!projectExperiment) throw new UserFixableException("Can not find association between experiment " + experiment.id + " and project " + project.id)

        deleteProjectStepsByProjectExperiment(projectExperiment)

        projectExperiment.delete(flush: true)
    }

    /**
     * remove experiment from project, if experiment has context, remove them. remove all steps associated with
     * this experiment, and step contexts
     * @param panelExperiment
     * @param project
     */
    @PreAuthorize("hasPermission(#id, 'bard.db.project.Project', admin) or hasRole('ROLE_BARD_ADMINISTRATOR')")
    void removePanelExperimentFromProject(Long panelExperimentId, Long id) {
        PanelExperiment panelExperiment = PanelExperiment.findById(panelExperimentId)
        Project project = Project.findById(id)
        def projectExperiment = ProjectPanelExperiment.findByPanelExperimentAndProject(panelExperiment, project)
        if (!projectExperiment) throw new UserFixableException("Can not find association between experiment " + panelExperiment.id + " and project " + project.id)

        deleteProjectStepsByProjectExperiment(projectExperiment)

        projectExperiment.delete(flush: true)
    }

    /**
     * delete projectsteps having given projectexperiment as start point or end point
     * @param projectExperiment
     */
    private void deleteProjectStepsByProjectExperiment(ProjectExperiment projectExperiment) {
        // projectExperiment is start point of steps
        def projectSteps = ProjectStep.findAllByPreviousProjectExperiment(projectExperiment)

        if (projectSteps) {
            projectExperiment.followingProjectSteps.removeAll(projectSteps)
            deleteProjectSteps(projectSteps)
        }
        // projectExperiment is end point of steps
        projectSteps = ProjectStep.findAllByNextProjectExperiment(projectExperiment)
        if (projectSteps) {
            projectExperiment.precedingProjectSteps.removeAll(projectSteps)
            deleteProjectSteps(projectSteps)
        }
    }

    /**
     * delete projectstep and their contexts given a list of projectsteps
     * @param projectSteps
     */
    private void deleteProjectSteps(List<ProjectStep> projectSteps) {
        projectSteps.each { ProjectStep projectStep ->
            deleteProjectStep(projectStep, false)
        }
    }

    private void deleteProjectStep(ProjectStep projectStep, boolean isFlushTrue) {
        def stepContexts = StepContext.findByProjectStep(projectStep)
        if (stepContexts) {
            projectStep.stepContexts.removeAll(stepContexts)
            stepContexts.each { StepContext stepContext ->
                stepContext.delete()
            }
        }
        projectStep.delete(flush: isFlushTrue)
    }

    /**
     * remove edge
     * @param fromProjectExperiment
     * @param toProjectExperiment
     * @param project
     */
    @PreAuthorize("hasPermission(#id, 'bard.db.project.Project', admin) or hasRole('ROLE_BARD_ADMINISTRATOR')")
    void removeEdgeFromProject(ProjectExperiment fromProjectExperiment, ProjectExperiment toProjectExperiment, Long id) {
        Project project = Project.findById(id)

        if (!fromProjectExperiment || !toProjectExperiment)
            throw new UserFixableException("Experiment " + fromProjectExperiment.id + " and / or Experiment " + toProjectExperiment.id + " are / is not associated with project " + project.id)
        def projectStep = ProjectStep.findByPreviousProjectExperimentAndNextProjectExperiment(fromProjectExperiment, toProjectExperiment)
        if (!projectStep)
            throw new UserFixableException("Experiment " + fromProjectExperiment.id + " and Experiment " + toProjectExperiment.id + " are not linked")
        deleteProjectStep(projectStep, true)
    }

    /**
     * Associate an experiment with a project if it is not already associated
     * @param experiment
     * @param project
     */
    @PreAuthorize("hasPermission(#id, 'bard.db.project.Project', admin) or hasRole('ROLE_BARD_ADMINISTRATOR')")
    void addExperimentToProject(Experiment experiment, Long id, Element stage) {
        Project project = Project.findById(id)
        if (isExperimentAssociatedWithProject(experiment, project)) {
            throw new UserFixableException("Experiment " + experiment.id + " is already associated with Project " + project.id)
        }
        ProjectSingleExperiment pe = new ProjectSingleExperiment(experiment: experiment, project: project, stage: stage)
        project.addToProjectExperiments(pe)
        experiment.addToProjectExperiments(pe)
        pe.save()
        project.save()
        experiment.save()
    }

    @PreAuthorize("hasPermission(#id, 'bard.db.project.Project', admin) or hasRole('ROLE_BARD_ADMINISTRATOR')")
    void addPanelExperimentToProject(PanelExperiment panelExperiment, Long id, Element stage) {
        Project project = Project.findById(id)
        if (isPanelExperimentAssociatedWithProject(panelExperiment, project)) {
            throw new UserFixableException("Panel-Experiment " + panelExperiment.id + " is already associated with Project " + project.id)
        }
        ProjectPanelExperiment projectPanelExperiment = new ProjectPanelExperiment(panelExperiment: panelExperiment, project: project, stage: stage)
        project.addToProjectExperiments(projectPanelExperiment)
        projectPanelExperiment.save()
        project.save()
    }

    /**
     * Add link between two experiments that are associated with the project. The candidate link can not be duplicate with any existing one.
     * The candidate link can not result any cycle of graph.
     * @param fromProjectExperiment
     * @param toProjectExperiment
     * @param project
     */
    @PreAuthorize("hasPermission(#id, 'bard.db.project.Project', admin) or hasRole('ROLE_BARD_ADMINISTRATOR')")
    void linkProjectExperiment(ProjectExperiment fromProjectExperiment, ProjectExperiment toProjectExperiment, Long id) {
        if (!fromProjectExperiment || !toProjectExperiment) {
            throw new UserFixableException("Either or both project-experiment you were trying to link does not exist in the system.")
        }
        if (fromProjectExperiment.id == toProjectExperiment.id) {
            throw new UserFixableException("Link between same project-experiments is not allowed.")
        }
        Project project = Project.findById(id)
        if (fromProjectExperiment.project != project ||
                toProjectExperiment.project != project)
            throw new UserFixableException("Project-experiment " + fromProjectExperiment.id + " or project-experiment " + toProjectExperiment.id + " is not associated with project " + project.id)
        ProjectStep ps = ProjectStep.findByPreviousProjectExperimentAndNextProjectExperiment(fromProjectExperiment, toProjectExperiment)
        if (ps) // do not want to add one already exist
            throw new UserFixableException(("Link between " + fromProjectExperiment.id + " and " + toProjectExperiment.id + " does exist, can not be added again."))
        // check if there will be a cycle by adding this link, if there is, return, otherwise, add
        if (!isDAG(project, fromProjectExperiment, toProjectExperiment))
            throw new UserFixableException("Link " + fromProjectExperiment.id + " and " + toProjectExperiment.id + " results a cycle in the graph.")
        ProjectStep newPs = new ProjectStep(previousProjectExperiment: fromProjectExperiment, nextProjectExperiment: toProjectExperiment)
        newPs.save(flush: true)
        fromProjectExperiment.addToFollowingProjectSteps(newPs)
        toProjectExperiment.addToPrecedingProjectSteps(newPs)
    }

    /**
     * Check if an experiment is associated with a project or not
     * @param experiment
     * @param project
     * @return
     */
    boolean isExperimentAssociatedWithProject(Experiment experiment, Project project) {
        boolean isAssociated = false
        experiment.projectExperiments.each {
            ProjectExperiment pe ->
                if (pe.project.id == project?.id)
                    isAssociated = true
        }
        return isAssociated
    }

    /**
     * Checks if a panel-experiment is associated with a project or not
     * @param panelExperiment
     * @param project
     * @return
     */
    boolean isPanelExperimentAssociatedWithProject(PanelExperiment panelExperiment, Project project) {
        boolean isAssociated = false
        List<ProjectPanelExperiment> projectPanelExperiments = ProjectPanelExperiment.findAllByProject(project)
        projectPanelExperiments.each { ProjectPanelExperiment projectPanelExperiment ->
            if (projectPanelExperiment.panelExperiment.id == panelExperiment.id) {
                isAssociated = true
            }
        }
        return isAssociated
    }

    /**
     * Determine if the connected portion of projectexperiment graph is still a DAG (Directed acyclic graph) by adding a link between two ProjectExperiment.
     * Assume the connected portion of original graph should always be a DAG.
     * So start from the toNode, check if there will be a path to fromNode, if there is, return false, otherwise, return true.
     * NOTE: Later on, if we find out original graph can not be guaranteed to be a DAG due to data problem, this method need to be change to use
     * a more general cycle detection algorithm.
     * @param project
     * @param experiment
     * @return
     */
    boolean isDAG(Project project, ProjectExperiment fromPe, ProjectExperiment toPe) {
        List<Long> visiting = []
        List<Long> visited = []
        visiting.add(toPe.id)
        boolean isDag = true
        while (visiting.size() > 0) {
            Long current = visiting.remove(0)
            if (fromPe.id == current)
                return false
            if (visited.contains(current))
                continue
            processNode(current, visiting)
            visited.add(current)
        }
        return isDag
    }

    /**
     * Process current node by adding outgoing nodes connected with current nodes to the visiting queue
     * @param currentId
     * @param visiting
     */
    void processNode(Long currentId, List<Long> visiting) {
        ProjectExperiment pe = ProjectExperiment.findById(currentId)
        pe.followingProjectSteps.each {
            ProjectStep step ->
                visiting.add(step.nextProjectExperiment.id)
        }
    }
}
