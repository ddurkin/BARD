<table>
    <tbody>
    <tr>
        <g:if test="${!resultSize}">
            <td>
                <p class="text-error">N/A</p>
            </td>
        </g:if>
        <g:else>
        %{--Experiment's result-set--}%
            <g:each in="${results}" var="result">
                <td align="center" nowrap="wrap">
                %{--A curve--}%
                    <g:if test="${result instanceof bardqueryapi.ConcentrationResponseSeriesValue}">
                        <g:set var="concentrationSeries"
                               value="${result.value.concentrations}"/>
                        <g:set var="activitySeries" value="${result.value.activities}"/>
                        <table>
                            <tr>
                                <td align="center">
                                    <g:curvePlot
                                            concentrationSeries="${concentrationSeries}"
                                            activitySeries="${activitySeries}"
                                            curveFitParameters="${result.curveFitParameters}"
                                            slope="${result.slope}"
                                            responseUnit="${result.responseUnit}"
                                            testConcentrationUnit="${result.testConcentrationUnit}"/>
                                </td>
                                <td align="center">
                                    <g:curveValues
                                            title="${result.title}"
                                            concentrationSeries="${concentrationSeries}"
                                            activitySeries="${activitySeries}"
                                            responseUnit="${result.responseUnit}"
                                            testConcentrationUnit="${result.testConcentrationUnit}"/>
                                </td>
                            </tr>
                        </table>
                    </g:if>
                %{--A key/value pair result--}%
                    <g:elseif test="${result instanceof bardqueryapi.PairValue}">
                        <g:set var="pair" value="${result.value}"/>
                        <table style="border-style:solid; border-width:1px 1px 1px 1px; border-color:#000000; padding: 3px; margin: 3px;">
                            <tr>
                                <td align="center">
                                    <b><small>${pair.left}</small></b>

                                    <p><small>${pair.right}</small></p>
                                </td>
                            </tr>
                        </table>
                    </g:elseif>
                </td>
            </g:each>
        </g:else>
    </tr>
    </tbody>
</table>