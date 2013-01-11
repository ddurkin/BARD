<%--
  Created by IntelliJ IDEA.
  User: gwalzer
  Date: 9/21/12
  Time: 10:55 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page import="bard.core.rest.spring.experiment.ActivityData; bard.core.rest.spring.experiment.PriorityElement; bardqueryapi.ActivityOutcome; bard.core.rest.spring.experiment.CurveFitParameters; bard.core.rest.spring.experiment.ConcentrationResponsePoint; bard.core.rest.spring.experiment.ConcentrationResponseSeries; results.ExperimentalValueType; results.ExperimentalValueUnit; results.ExperimentalValue; molspreadsheet.MolSpreadSheetCell; bard.core.interfaces.ExperimentValues" contentType="text/html;charset=UTF-8" %>

<p><b>Title: ${experimentDataMap?.experiment?.name}</b></p>

<p>
    <b>Assay ID : <g:link controller="bardWebInterface" action="showAssay"
                          id="${experimentDataMap?.experiment?.adid}" params='[searchString: "${searchString}"]'>
        ${experimentDataMap?.experiment?.adid}
    </g:link>
    </b>
</p>

<div class="row-fluid">
    <table class="table table-condensed">
        <thead>
        <tr>
            <th>SID</th>
            <th>CID</th>
            <th>Structure</th>
            <th>Outcome</th>
            <th>Potency</th>
            <th>Experiment Descriptors</th>
            <th>Child Elements</th>
            <g:if test="${!experimentDataMap?.activities?.isEmpty()}">
                <g:if test="${experimentDataMap?.activities?.get(0)?.resultData?.hasConcentrationResponseSeries()}">
                    <th>Concentration Response Series</th>
                    <th>Concentration Response Plot</th>
                </g:if>
            </g:if>
            <th>Misc Data</th>
        </tr>
        </thead>
        <g:each in="${experimentDataMap?.activities}" var="activity">
            <tr>
                <td>
                    <a href="http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?sid=${activity.sid}">
                        <img src="${resource(dir: 'images', file: 'pubchem.png')}" alt="PubChem"/>
                        ${activity.sid}</a>
                </td>
                <td>
                    <a href="${createLink(controller: 'bardWebInterface', action: 'showCompound', params: [cid: activity.cid, searchString: "${searchString}"])}">${activity.cid}</a>
                </td>
                <td style="min-width: 180px;">
                    <g:compoundOptions sid="${activity.sid}" cid="${activity.cid}" imageWidth="180"
                                       imageHeight="150"/>
                </td>
                <td>
                    <g:if test="${activity.outcome != null}">
                        <%
                            ActivityOutcome activityOutcome = ActivityOutcome.findActivityOutcome(activity.outcome.intValue())
                        %>
                        ${activityOutcome.label}
                    </g:if>
                </td>
                <td>
                    <g:if test="${activity.potency != null}">
                        <%
                            ExperimentalValue potency = new ExperimentalValue(new Double(activity.potency), false)
                        %>
                        ${potency.toString()}
                    </g:if>
                </td>
                <td>
                    <g:each in="${activity?.resultData?.rootElements}" var="rootElement">

                        <g:if test="${rootElement.toDisplay()}">${rootElement.toDisplay()} <br/></g:if>
                    </g:each>
                </td>
                <% PriorityElement priorityElement = activity?.resultData?.priorityElements.get(0) %>
                <td>
                    <g:each in="${priorityElement.childElements}" var="childElement">

                        <g:if test="${childElement.toDisplay()}">${childElement.toDisplay()}<br/></g:if>

                    </g:each>
                </td>
                <g:each in="${priorityElement.concentrationResponseSeries}"
                        var="concRespSeries">
                    <% List<ConcentrationResponsePoint> concentrationResponsePoints = concRespSeries.concentrationResponsePoints
                    Map m = ConcentrationResponseSeries.toDoseResponsePoints(concentrationResponsePoints)
                    List<Double> concentrations = m.concentrations
                    List<Double> activities = m.activities
                    %>
                    <% CurveFitParameters curveFitParameters = concRespSeries.curveFitParameters %>
                    <% List<ActivityData> miscDataList = concRespSeries.miscData %>
                    <td>
                        <g:each in="${concentrationResponsePoints}" var="concentrationResponsePoint">
                            <%
                                String responseValue = concentrationResponsePoint.value
                                Double concentrationValue = concentrationResponsePoint.testConcentration
                                String responseString = ""
                                String concentrationString = ""
                                if (responseValue != null) {
                                    ExperimentalValue resp = new ExperimentalValue(new Double(responseValue), false)
                                    responseString = resp.toString()
                                }
                                if (concentrationValue != null) {
                                    ExperimentalValue conc =
                                        new ExperimentalValue(concentrationValue,
                                                ExperimentalValueUnit.getByValue(concRespSeries.testConcentrationUnit),
                                                ExperimentalValueType.numeric)
                                    concentrationString = "@ " + conc.toString()
                                }
                            %>

                            ${responseString} ${concentrationString}
                            <br/>
                        </g:each>

                    </td>
                    <g:if test="${!concentrationResponsePoints.isEmpty()}">
                        <td>

                            <img alt="Plot for CID ${activity.cid}" title="Plot for CID ${activity.cid}"
                                 src="${createLink(controller: 'doseResponseCurve', action: 'doseResponseCurve',
                                         params: [sinf: curveFitParameters.sInf,
                                                 s0: curveFitParameters.s0,
                                                 ac50: priorityElement.getSlope(),
                                                 hillSlope: curveFitParameters.hillCoef,
                                                 concentrations: concentrations,
                                                 activities: activities,
                                                 yAxisLabel: "Place Holder",
                                                 xAxisLabel: "Concentration ${priorityElement.testConcentrationUnit}"
                                         ])}"/>
                            <br/>
                            <g:if test="${curveFitParameters != null}">
                                <p>
                                    Slope : ${priorityElement.slope} <br/>
                                    sInf : ${(new ExperimentalValue(curveFitParameters.sInf, false)).toString()}<br/>
                                    s0 : ${(new ExperimentalValue(curveFitParameters.s0, false)).toString()}<br/>
                                    HillSlope : ${(new ExperimentalValue(curveFitParameters.hillCoef, false)).toString()}<br/>
                                    ${priorityElement.toDisplay()}
                                </p>
                                <br/>
                                <br/>
                            </g:if>

                        </td>

                    </g:if>
                    <td>
                        <g:each in="${miscDataList}" var="miscData">
                            ${miscData.toDisplay()} <br/>
                        </g:each>
                    </td>
                </g:each>
            </tr>
        </g:each>
    </table>

    <div class="pagination">
        <g:paginate total="${experimentDataMap?.total ? experimentDataMap?.total : 0}" params='[id: "${params?.id}"]'/>
    </div>
</div>
