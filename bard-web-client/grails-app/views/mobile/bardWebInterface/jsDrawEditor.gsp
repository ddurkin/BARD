<%@ page import="bard.core.rest.spring.util.StructureSearchParams" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>JSDraw Structure Editor</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <r:require modules="jquery, jquery-ui, jquery-theme"/>
    <r:layoutResources/>
    <link rel="stylesheet" type="text/css"
          href="${request.contextPath}/css/jquery.mobile-1.2.0/jquery.mobile.structure-1.2.0.css"/>
    <link rel="stylesheet" type="text/css"
          href="${request.contextPath}/css/jquery.mobile-1.2.0/jquery.mobile.structure-1.2.0.min.css"/>
    <link rel="stylesheet" type="text/css"
          href="${request.contextPath}/css/jquery.mobile-1.2.0/jquery.mobile.theme-1.2.0.css"/>
    <link rel="stylesheet" type="text/css"
          href="${request.contextPath}/css/jquery.mobile-1.2.0/jquery.mobile.theme-1.2.0.min.css"/>
    <link rel="stylesheet" type="text/css"
          href="${request.contextPath}/css/jquery.mobile-1.2.0/jquery.mobile-1.2.0.css"/>
    <link rel="stylesheet" type="text/css"
          href="${request.contextPath}/css/jquery.mobile-1.2.0/jquery.mobile-1.2.0.min.css"/>
    <script type="text/javascript" src="${request.contextPath}/js/jquery.mobile-1.2.0/jquery.mobile-1.2.0.js"></script>
    <script type="text/javascript"
            src="${request.contextPath}/js/jquery.mobile-1.2.0/jquery.mobile-1.2.0.min.js"></script>
    <link rel="stylesheet" type="text/css" href="${request.contextPath}/css/bard-mobile.css"/>
    <script type="text/javascript" src="${request.contextPath}/js/jqueryMobileInit.js"></script>

    <script type="text/javascript" src="${request.contextPath}/js/dojo-min/dojo/dojo.js"></script>
    <script type="text/javascript" src="${request.contextPath}/js/jsDraw/Scilligence.JSDraw2.js"></script>
    <script type="text/javascript" src="${request.contextPath}/js/jsDraw/license.js"></script>
    <script>
        dojo.addOnLoad(function () {
            var jsDrawEditor = new JSDraw2.Editor("jsDrawEditorDiv", {popup:false});
            var width = dojo.window.getBox().w * 0.95;
            var height = dojo.window.getBox().h * 0.8;
            if (width && height && jsDrawEditor) {
                jsDrawEditor.setSize(width, height)
            }
        });
    </script>
</head>

<body>
<div data-role="page" id="jsDrawEditorMobile">
    <div data-role="content" style="text-align: center">

        <div id="jsDrawEditorDiv" skin='w8' style="width: 480px; height: 600px;border:1px solid gray"></div>

        <div style="text-align: left">
            <g:radioGroup name="structureSearchType"
                          values="${StructureSearchParams.Type.values()}"
                          value="${StructureSearchParams.Type.Substructure}"
                          labels="${StructureSearchParams.Type.values()}">
                <label class="radio inline">
                    ${it.radio} ${it.label}
                </label>
            </g:radioGroup>
        </div>

        <div>
            <button value="Search" name="search" id="searchButtonMobile" data-theme="b" type="submit"
                    class="ui-btn-hidden"
                    aria-disabled="false" data-inline="true" data-theme="b">Search</button>
        </div>
    </div><!-- /content -->

</div><!-- /page -->
<r:layoutResources/>
</body>
</html>