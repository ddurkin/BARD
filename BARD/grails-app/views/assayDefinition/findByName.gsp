<%@ page import="bard.db.registration.*" %>
<!DOCTYPE html>
<html>
<head>
<r:require modules="core,bootstrap"/>
<meta name="layout" content="basic"/>
<link rel="stylesheet" href="${resource(dir: 'css', file: 'bootstrap-plus.css')}" type="text/css">
<title>CAP - Find assay by name</title>
<r:script>
	$(document).ready(function() {
		var autoOpts = {
			source: "/BARD/assayJSon/getNames",
			minLength: 2
		}
	 	$( "#name" ).autocomplete(autoOpts);
	 	$( "#results_accordion" ).accordion({ autoHeight: false });
	})
</r:script>

</head>
<body>
	<div class="row-fluid">
	    <div class="span12">
	    	<div class="hero-unit-v1">
	        	<h4>Search assay by name</h4>
	        </div>
	    </div>
	</div>

    <g:if test="${flash.message}">
	    <div class="row-fluid">
		    <div class="span12">
		        <div class="ui-widget">
		            <div class="ui-state-error ui-corner-all" style="margin-top: 20px; padding: 0 .7em;">
		                <p><span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>
		                    <strong>${flash.message}</strong>
		            </div>
		        </div>
		    </div>
	    </div>
    </g:if>

    <div class="row-fluid">
	    <div class="span12">
	    	<div class="bs-docs" style="padding: 20px 20px 20px;">
	        	<g:form action="findByName" class="form-inline">
	        		<label class="control-label" for="assayName">Enter Assay Name:</label>
	        		<input type="text" size="50" id="name" name='assayName' value="${params.assayName}">
					<g:submitButton name="search" value="Search" class="btn btn-primary"/>
				</g:form>
	        </div>
	    </div>
	</div>

	<g:if test="${assays}">
	<div class="row-fluid">
	    <div class="span12" id="results_accordion">
	    		<h3>Assays (${assays.size()})</h3>
				<div>
					<g:if test="${assays.size() > 0}">
						<div>
							<table class="gridtable">
								<thead>
									<tr>
										<g:sortableColumn property="id" title="${message(code: 'assay.id.label', default: 'ID')}" />
										<g:sortableColumn property="assayName" title="${message(code: 'assay.assayName.label', default: 'Assay Name')}" />
										<g:sortableColumn property="designedBy" title="${message(code: 'assay.designedBy.label', default: 'Designed By')}" />
									</tr>
								</thead>
								<tbody>
								<g:each in="${assays}" status="i" var="assayInstance">
									<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
										<td>${fieldValue(bean: assayInstance, field: "id")}</td>
										<td><g:link action="show" id="${assayInstance.id}">${fieldValue(bean: assayInstance, field: "assayName")}</g:link></td>
										<td>${fieldValue(bean: assayInstance, field: "designedBy")}</td>
									</tr>
								</g:each>
								</tbody>
							</table>
						</div>
					</g:if>
				</div>
	    </div>
	</div>
	</g:if>
</body>
</html>