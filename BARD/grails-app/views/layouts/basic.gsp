<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title>BARD: <g:layoutTitle default="BioAssay Research Database"/></title>
    <link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
    <link href='https://fonts.googleapis.com/css?family=Lato:400,400italic,700,700italic,900,900italic,300,300italic'
          rel='stylesheet' type='text/css'>
    <g:layoutHead/>

    <r:require modules="basic,bootstrap,autocomplete,cart,idSearch,jquerynotifier,downtime"/>
    <%@ page import="bardqueryapi.IDSearchType" %>
    <r:layoutResources/>
    <ga:trackPageview/>
</head>

<body>
<noscript>
    <a href="http://www.enable-javascript.com/" target="javascript">
        <img src="${resource(dir: 'images', file: 'enable_js.png')}"
             alt="Please enable JavaScript to access the full functionality of this site."/>
    </a>
</noscript>

<header class="container-fluid" id="header">

    <div class="search-panel">

        <div class="container-fluid">
            <g:render template="/layouts/templates/downtime"/>


            <div class="row-fluid">
                <div class="span2"  style="min-width: 180px">
                    <strong class="logo"><a
                            href="${createLink(controller: 'BardWebInterface', action: 'index')}"  style="min-width: 207px">BARD BioAssay Research Database</a>
                    </strong>
                </div>

                <div class="span8"  style="min-width: 350px;">
                    <div class="search-block left-aligned">
                        <g:render template="/layouts/templates/searchBlock"/>
                    </div>
                    <div class="share-block social-networks">
                        <g:render template="/layouts/templates/socialMedia"/>
                    </div>
                </div>

                <div class="span2"></div>
                    <nav class="nav-panel" style="min-width: 150px; ">
                        <div class="right-aligned">
                            <g:render template="/layouts/templates/loginStrip"/>
                        </div>
                        <div class="right-aligned">
                            <g:render template="/layouts/templates/queryCart"/>
                        </div>

                        <sec:ifLoggedIn>
                            <div class="right-aligned">
                               <g:link controller="bardWebInterface" action="navigationPage" class="my-bard-button btn">My BARD</g:link>
                            </div>
                        </sec:ifLoggedIn>

                        <div class="navbar right-aligned hidden-phone">
                            <ul class="nav">
                                <g:render template="/layouts/templates/howtolinks"/>
                            </ul>
                        </div>

                    </nav>


            </div>
        </div>
    </div>

    <g:render template="/layouts/templates/IdSearchBox"></g:render>

    <g:if test="${flash.message}">
        <div class="alert">
            <button class="close" data-dismiss="alert">×</button>
            ${flash.message}
        </div>
    </g:if>

</header>

<div class="container-fluid" id="bard-container">
    <div class="row-fluid">
        <div class="span12">
            <div class="spinner-container">
                <div id="spinner" class="spinner" style="display:none; color: blue;"><g:message code="spinner.alt"
                                                                                                default=""/></div>
            </div>
            <g:layoutBody/>
        </div>
    </div>
</div>

<g:render template="/layouts/templates/footer"/>

<r:layoutResources/>
</body>
</html>
