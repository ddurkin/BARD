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

<%@ page import="bard.db.command.BardCommand" %>
<!DOCTYPE html>
<html>
<head>
    <r:require
            modules="core,bootstrap"/>
    <meta name="layout" content="basic"/>
    <title>My Submissions</title>
</head>

<body>
<div class="container-fluid">

    <div class="row-fluid">
        <div class="span12">
            <div class="navbar navbar-inverse navbar-static-top">
                <div>
                    <ul>
                        <g:if test="${isBardAdmin || isManager}">
                            <li>
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                    Admin
                                </a>
                                <g:if test="${isBardAdmin}">
                                    <ul>
                                        <li class=" controller"><g:link controller="downTimeScheduler"
                                                                        action="create">Schedule Down Time</g:link></li>
                                        <li class="controller"><g:link controller="downTimeScheduler"
                                                                       action="list">View Down Times</g:link></li>
                                        <li class="controller"><g:link controller="person"
                                                                       action="list">List Person Table</g:link></li>
                                        <li class="controller"><g:link controller="assayDefinition"
                                                                       action="assayComparisonReport">Compare Assays</g:link></li>
                                        <li class="controller"><g:link controller="splitAssayDefinition"
                                                                       action="show">Split Assays</g:link></li>
                                        <li class="controller"><g:link controller="config"
                                                                       action="index">Override API URL</g:link></li>
                                        <li class="controller"><g:link controller="role"
                                                                       action="list">List Teams</g:link></li>
                                        <li class="controller"><g:link controller="role"
                                                                       action="create">Create new Team</g:link></li>
                                        <li class="controller"><g:link
                                                controller="offlineValidation">Offline Validation</g:link></li>
                                    </ul>
                                </g:if>
                                <g:if test="${isManager}">
                                    <ul>
                                        <li class="controller"><g:link controller="role"
                                                                       action="myTeams">My Teams</g:link></li>
                                    </ul>
                                </g:if>
                            </li>
                        </g:if>

                        <li>
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                Assay Definitions

                            </a>
                            <ul>
                                <li class="controller"><g:link controller="assayDefinition"
                                                               action="myAssays">My Assay Definitions</g:link></li>
                            %{--//You should belong to at least one team to create--}%
                                <g:if test="${BardCommand.userRoles()}">
                                    <li class="controller"><g:link controller="assayDefinition"
                                                                   action="create">Create Assay Definition</g:link></li>
                                </g:if>
                                <li class="controller"><g:link controller="assayDefinition"
                                                               action="assayComparisonReport">Compare Assay Definitions</g:link></li>
                            </ul>
                        </li>
                        <li>
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                Projects

                            </a>
                            <ul>
                                <li class="controller"><g:link controller="project"
                                                               action="myProjects">My Projects</g:link></li>
                            %{--//You should belong to at least one team to create--}%
                                <g:if test="${BardCommand.userRoles()}">
                                    <li class="controller"><g:link controller="project"
                                                                   action="create">Create a New Project</g:link></li>
                                </g:if>
                            </ul>
                        </li>
                        <li>
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                Experiments

                            </a>
                            <ul>
                                <li class="controller"><g:link controller="experiment"
                                                               action="myExperiments">My Experiments</g:link></li>
                                <g:if test="${ownsExperiments}">
                                    <li class="controller"><g:link controller="moveExperiments"
                                                                   action="show">Move Experiments</g:link></li>
                                </g:if>

                                <li class="controller"><g:link controller="jobs"
                                                               action="index">My import jobs</g:link></li>
                                <li class="controller"><g:link controller="panelExperiment"
                                                               action="create">Create a New Panel-Experiment</g:link></li>
                            </ul>
                        </li>
                        <sec:ifAnyGranted roles="ROLE_BARD_ADMINISTRATOR,ROLE_CURATOR">
                            <li>
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                    Elements

                                </a>
                                <ul>
                                    <li class="controller"><g:link controller="element"
                                                                   action="select">Edit</g:link></li>
                                </ul>

                            </li>
                        </sec:ifAnyGranted>
                        <li>
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                Panels

                            </a>
                            <ul>
                                <li class="controller"><g:link controller="panel"
                                                               action="myPanels">My Panels</g:link></li>
                            %{--//You should belong to at least one team to create--}%
                                <g:if test="${BardCommand.userRoles()}">
                                    <li class="controller"><g:link controller="panel"
                                                                   action="create">Create New Panel</g:link></li>
                                </g:if>
                            </ul>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
