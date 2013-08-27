<%@ page import="bardqueryapi.JavaScriptUtility" %>
<g:hiddenField name="totalProjects" id="totalProjects" value="${nhits}"/>

<div data-role="header">
    <h1>Projects</h1>
</div><!-- /header -->

<div data-role="content">
    <g:if test="${nhits > 0}">
        <ul class="unstyled results">
            <g:each var="projectAdapter" in="${projectAdapters}">
                <li>
                %{--<h3>--}%
                    <g:link action="showProject" id="${projectAdapter.id}"
                            params='[searchString: "${searchString}"]'>${projectAdapter.name} <small>(Project ID: ${projectAdapter.capProjectId})</small></g:link>
                %{--</h3>--}%
                    <g:if test="${projectAdapter?.getNumberOfExperiments()}">
                        <dl>
                            <dt>Number Of Experiments:</dt>
                            <dd><span class="badge badge-info">${projectAdapter.getNumberOfExperiments()}</span></dd>
                        </dl>
                    </g:if>
                </li>
            </g:each>
        </ul>

        <div id="paginateBar" class="pagination">
            <g:paginate total="${nhits ? nhits : 0}" params='[searchString: "${searchString}"]'/>
        </div>
    </g:if>
    <g:else>
        <div class="tab-message">No search results found</div>
    </g:else>
</div>