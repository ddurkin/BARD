%{-- Copyright (c) 2014, The Broad Institute
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
 --}%

<%@ page defaultCodec="none" %>
<%@ page import="bard.core.rest.spring.util.StructureSearchParams" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>BARD: Structure Search</title>
    <script type="text/javascript" src="${request.contextPath}/js/dojo-min/dojoConfig.js"></script>
    <script type="text/javascript" src="${request.contextPath}/js/dojo-min/dojo/dojo.js"></script>
    <script type="text/javascript"
            src="${request.contextPath}/js/jsDraw/JSDraw3.1.3/Scilligence.JSDraw2.Pro.js"></script>

    <script type="text/javascript" src="<g:createLink controller="jsDraw" action="license"/>"></script>
    <r:require modules="jquery, jquery-ui, jquery-theme, core, bootstrap, jsDrawEditor"/>
    <r:script disposition='head'>
        window.bardAppContext = "${request.contextPath}";
    </r:script>
    <r:layoutResources/>
</head>

<body>
<div class="row-fluid">
%{--Save the username in the page--}%
    <sec:ifLoggedIn>
        <p style="display: none" id="jsDrawUsername"><sec:username/></p>
    </sec:ifLoggedIn>
    <sec:ifNotLoggedIn>
        <p style="display: none" id="jsDrawUsername">anonymous</p>
    </sec:ifNotLoggedIn>

%{--AJAX is handling this--}%
    <form class="form-inline" style="margin-top: 3px; margin-left: 3px">
        <input type="text" id="convertSmilesTextfield" name="smiles" class="span8" rows="1"
               placeholder="${message(code: 'jsDraw.convertSmiles')}"/>

        <button type="submit" id="smilesToMolBtn" class="btn">Import</button>

        <label id="smilesImportErrorLabel" style="margin-left: 5px;"/>
    </form>
</div>

<div class="row-fluid">
    <div class="span12">
        <div id="jsDrawEditorDiv" skin='w8' style="border:1px solid gray; margin-left: auto; margin-right: auto;"></div>
        <g:form controller="bardWebInterface" action="searchResults">
        %{--Use this field to hold the smiles + search-type value--}%
            <g:hiddenField name="searchString" id="searchString" value=""/>
            <g:hiddenField name="similaritySearchTypeValue" id="similaritySearchTypeValue"
                           value="${StructureSearchParams.Type.Similarity}"/>

            <div style="text-align: left;">
                <g:radioGroup name="structureSearchType"
                              values="${StructureSearchParams.Type.values()}"
                              value="${StructureSearchParams.Type.Substructure}"
                              labels="${StructureSearchParams.Type.values()}">
                    <label class="radio inline">
                        ${it.radio} ${it.label}
                    </label>
                </g:radioGroup>

                <span><g:textField name="cutoff" value="90" id="cutoff" disabled="disabled"/>[%]</span>
            </div>
            <br/>

            <div style="text-align: left;">
                <g:submitButton name="search" value="Search" class="btn btn-primary span1" id="searchButton"/>
            </div>
        </g:form>
    </div>
</div>
<r:layoutResources/>
</body>
</html>
