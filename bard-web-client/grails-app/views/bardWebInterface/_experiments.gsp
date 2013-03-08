<%@ page import="bard.core.interfaces.ExperimentValues;" %>
<div>
    <g:each var="experiment" in="${experiments}" status="i">
        <div>
            <div class="accordion" id="accordionDescription">
                <div class="accordion-group">
                    <div class="accordion-heading">
                        <h4>
                            ${experiment.name} <small>(Experiment ID: ${experiment.capExptId})</small>
                        </h4>

                        <p><span class="label label-info">${experiment.getRole()}</span>
                            ${experiment.compounds} compound(s) /
                            ${experiment.substances} substance(s) tested
                        </p>

                        <p><span>
                            <g:if test="${searchString}">
                                <g:link controller="bardWebInterface" action="showExperiment" id="${experiment.bardExptId}"
                                        params='[searchString: "${searchString}"]'>View Results</g:link>
                            </g:if>
                            <g:else>
                                <g:link controller="bardWebInterface" action="showExperiment" id="${experiment.bardExptId}">View Results</g:link>
                            </g:else>

                            <g:if test="${showAssaySummary}">
                                 <div><g:render template="assaySummary"
                                               model="[assayAdapter: experiment?.getAssay()]"/></div>
                            </g:if>
                            |
                            <a data-toggle="collapse" data-parent="#accordionDescription" class="resultsdescriptions"
                               href="#accordionDescriptionContent_${i}">View Description</a>
                        </span></p>
                    </div>

                    <div id="accordionDescriptionContent_${i}" class="accordion-body collapse">
                        <div class="accordion-inner"><g:textBlock>${experiment.description}</g:textBlock></div>
                    </div>
                </div>
            </div>
        </div>
    </g:each>
</div>