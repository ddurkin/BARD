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

<%@ page import="bard.db.enums.ContextType; bard.db.registration.DocumentKind; bard.db.model.AbstractContextOwner; bard.db.project.*" %>
<!DOCTYPE html>
<html>
<g:render template="howToHeader" model="[title:'BARD Architecture']"/>
<body>
<div class="search-panel">
    <div class="container-fluid">
        <div class="row-fluid">
            <aside class="span2"></aside>
            <article class="span8 head-holder"><h2>Architecture &amp; Design</h2></article>
            <aside class="span2"></aside>
        </div>

    </div>
</div>

<article class="hero-block">
    <div class="container-fluid">
        <div class="lowkey-hero-area">
            <div class="row-fluid">
                <aside class="span2"></aside>
                <article class="span8">

                    <h3>
                        Architecture
                    </h3>

                    <p>
                        BARD has been developed to meet the differing needs of both data generators and data consumers.  It uses a component-based architecture with components connected by RESTful web services.
                    </p>

                    <p>
                    <dl class='unindentedDefinition'>
                        <dt>Data dictionary component</dt>
                        <dd>Used by dictionary curators to manage BARD’s hierarchical dictionary of terms</dd>
                        <br/>
                        <dt>Catalog of assay protocols component</dt>
                        <dd>Used by data generators to register assays and upload result data</dd>
                        <br/>
                        <dt>Warehouse component</dt>
                        <dd>Provides persistent storage of result data in a form that is fast and simple to query via a REST API.  Relies on the controlled terms from the dictionary for effective and accurate searching.  Links to data from GO and other sources.  Used by query tool components and by informatics data consumers.  The API can be extended using plug-ins contributed by the community.</dd>
                        <br/>
                        <dt>Query tool components</dt>
                        <dd>Both the 'web query' and 'desktop client' provide methods for novice and experienced users to browse and find the information they need.</dd>
                    </dl>

                </p>

                    <IMG style="margin: 25px auto 25px;" SRC="${resource(dir: 'images/bardHomepage', file: 'BARD_architecture.png')}" ALIGN="top">

                    <h3>
                        Technology Stack
                    </h3>

                    <p>
                        BARD has been developed to be as open source as possible, using commercial components only in limited cases where an open source solution did not meet our needs.  After the public launch, the BARD source code will be made available to the community for extension and re-use.
                    </p>

                    <IMG SRC="${resource(dir: 'images/bardHomepage', file: 'BARD_technology.png')}" ALIGN="bottom">


                </article>
                <aside class="span2"></aside>
            </div>
        </div>
    </div>
</article>



</body>
</html>
