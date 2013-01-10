//use GLOBAL params here
var spinnerImageLink = '<img src="/bardwebclient/static/images/loading_icon.gif" alt="loading" title="loading" height="16" width="16"/>';
var bigSpinnerImage = '<div class="tab-message"><img src="/bardwebclient/static/images/ajax-loader.gif" alt="loading" title="loading"/></div>';
var errorImageTwitterBootstrap = '<img src=""  class="icon-exclamation-sign" alt="error" height="16" width="16" />';

//matches a number followed by a zero or more spaces, followed by max of one comma
//The question mark and the colon after the opening round bracket are the special syntax that
// you can use to tell the regex engine that this pair of brackets should not create a backreference
//You do that to optimize this regular expression
//see http://www.regular-expressions.info/brackets.html
var NUMBER_MATCHING_REGEX = /^\s*\d+\s*(?:,?\s*\d+\s*)*$/;
var TAB_ICON_CLASS = 'badge badge-important';

$(document).ready(function () {

    //set up main form submission
    $('#searchForm').submit(function (event) {
        event.preventDefault();	// prevent the default action behaviour to happen
        var searchString = $("#searchString").val();
        handleMainFormSubmit(searchString);
        return false; //do not submit form the normal way, use Ajax instead

    });

    //set up filter form submissions, for all 3 tabs
    //We are using on because we want to do late binding
    //these forms would not exist when the document first loads
    $(document).on("submit", "#AssayFacetForm", function (event) {
        event.preventDefault();	// prevent the default action behaviour to happen
        var searchString = $("#searchString").val();
        handleFilteredQuery(searchString, 'AssayFacetForm', 'AssayFacetForm', 'assaysTab', 'totalAssays', 'assays', 'Assay Definitions ');
        return false; //do not submit form the normal way, use Ajax instead

    });
    $(document).on("submit", "#ProjectFacetForm", function (event) {
        event.preventDefault();	// prevent the default action behaviour to happen
        var searchString = $("#searchString").val();
        handleFilteredQuery(searchString, 'ProjectFacetForm', 'ProjectFacetForm', 'projectsTab', 'totalProjects', 'projects', 'Projects ');
        return false; //do not submit form the normal way, use Ajax instead
    });
    $(document).on("submit", "#CompoundFacetForm", function (event) {
        event.preventDefault();	// prevent the default action behaviour to happen
        var searchString = $("#searchString").val();
        handleFilteredQuery(searchString, 'CompoundFacetForm', 'CompoundFacetForm', 'compoundsTab', "totalCompounds", 'compounds', 'Compounds ');
        return false; //do not submit form the normal way, use Ajax instead
    });


    //bind buttons to reset filters
    $(document).on("click", "#AssayFacetForm_ResetButton", function () {
        resetAllFilters('AssayFacetForm');
    });
    $(document).on("click", "#ProjectFacetForm_ResetButton", function () {
        resetAllFilters('ProjectFacetForm');
    });
    $(document).on("click", "#CompoundFacetForm_ResetButton", function () {
        resetAllFilters('CompoundFacetForm');
    });


    //=== Handle Paging. We bind to all of the paging css classes on the anchor tag ===
    $(document).on("click", "a.step,a.nextLink,a.prevLink", function (event) {
        event.preventDefault();	// prevent the default action behaviour to happen
        var url = $(this).attr('href');

        handlePaging(url)
    });

    // If there's a string in the search box, then submit it (used for redirect from home page)
    if ($("#searchString").val() != null) {
        $('#searchForm').submit();
    }


    //=== Handle the hide/show of facets ===
    $(document).on("click", "a.facetDiv", function (event) {
        event.preventDefault();	// prevent the default action behaviour to happen
        var facetId = $(this).attr('div_id'); //get the current div
        var facetText = $(this).text();//Get the current text, either 'Less' or 'More'
        if (facetText == 'More') { //If the text is 'More' set to 'Less' and change the style to 'display:block'
            $("#" + facetId).css('display', 'block');
            $(this).text("Less");
        }
        if (facetText == 'Less') {//If the text is 'Less' set to 'More' and change the style to 'display:none'
            $("#" + facetId).css('display', 'none');
            $(this).text("More");
        }
    });

});
function resetAllFilters(facetForm) {
    //Uncheck all filters for the current form
    $('#' + facetForm + ' input[type="checkbox"]').attr('checked', false);
    //Resubmit the form
    $('#' + facetForm).submit();
}
/**
 * Handle paging using Ajax
 * TODO: Only paging on the main form is supported.
 * Paging with filters is not yet supported
 * @param url
 */
function handlePaging(url) {
    var assayIndex = url.indexOf("Assays");
    var compoundIndex = url.indexOf("Compounds");
    var projectIndex = url.indexOf("Projects");
    var structureSearchIndex = url.indexOf("Structure");

    //to find the right search to perform
    if (assayIndex >= 0) {
        handleSearch(url, 'AssayFacetForm', 'assaysTab', 'totalAssays', 'Assay Definitions ', 'assays');
    }
    else if (compoundIndex >= 0) {
        handleSearch(url, 'CompoundFacetForm', 'compoundsTab', 'totalCompounds', 'Compounds ', 'compounds');
    }
    else if (projectIndex >= 0) {
        handleSearch(url, 'ProjectFacetForm', 'projectsTab', 'totalProjects', 'Projects ', 'projects');
    }
    else if (structureSearchIndex >= 0) {
        handleStructureSearch(url, 'searchForm');
    }
}
/**
 * Handle structure searches { exact, Substructure, superstructure and similarity searches}
 */
function handleStructureSearch(url, currentFormId) {
    var searchForm = "#" + currentFormId;


    $.ajax({
        url:url,
        type:'POST',
        data:$(searchForm).serialize(),
        cache:false,
        //timeout: 10000,
        beforeSend:function () {
            resetTabsForStructureSearches();
            $("#compounds").html(bigSpinnerImage);
        },
        success:function (data) {
            $("#compounds").html(data);
            var compoundTotal = ' Compounds <span class="'+ TAB_ICON_CLASS +'">' + $("#totalCompounds").val() + '</span>';

            $("#compoundsTab").html(compoundTotal);
            $("#compounds").tab('show');
        },
        error:function (request, status, error) {
            $("#compoundsTab").html('Compounds ' + errorImageTwitterBootstrap);
            $("#compounds").html(error);
        },
        complete:function () {
            $("#compounds").trigger('search.complete');
        }
    });
}
/**
 *
 * @param controllerAction - The name of the controller action that would handle this request e.g 'searchAssays'
 * @param tabId - The ID of the tab where the results should be displayed  e.g 'assaysTab'
 * @param totalHitsForResourceId  - The ID of the hidden field that would hold the total number of hits
 * @param prefixOfTextToAppearOnTab - The start of the string to display on the tab e.g 'Assay Definitions '
 * @param updateDiv - Where the results will be displayed
 */
function handleSearch(controllerAction, currentFormId, tabId, totalHitsForResourceId, prefixOfTextToAppearOnTab, updateDiv) {
    var tabDivElement = '#' + tabId;
    var totalHitsElement = '#' + totalHitsForResourceId;
    var updateDivId = '#' + updateDiv;
    var searchForm = "#" + currentFormId;
    $.ajax({
        url:controllerAction,
        type:'POST',
        data:$(searchForm).serialize(),
        cache:false,
        //timeout: 10000,
        beforeSend:function () {
            $(tabDivElement).html(prefixOfTextToAppearOnTab + spinnerImageLink);
            $(updateDivId).html(bigSpinnerImage);
        },
        success:function (data) {
            $(updateDivId).html(data);
            var totalHits = $(totalHitsElement).val();
            var total = prefixOfTextToAppearOnTab;
            if (totalHits > 0) {
                total += ' <span class="'+ TAB_ICON_CLASS +'">' + totalHits + '</span>';
            }
            else {
                total += ' (0)'
            }

            $(tabDivElement).html(total);
        },
        error:function (request, status, error) {
            $(tabDivElement).html(prefixOfTextToAppearOnTab + errorImageTwitterBootstrap);
            $(updateDivId).html(error);
        },
        complete:function () {
            $(updateDivId).trigger('search.complete');
        }
    });
}

/**
 * Handles filtered form submission
 * @param searchString - The search string started by the user
 * @param facetFormType - The facet form type
 * @param currentFormId - The id of the currently selected form
 * @param currentTabId  -   The id of the currently selected tab
 * @param numberOfHitsDivId  - The id of the div where we would display the total number of hits
 * @param updateId    - The id of the div where we would display the results of the ajax call
 * @param tabDisplayPrefix   The prefix for the string that we would display on the currently selected tab (for example 'Assay Definitions')
 */
function handleFilteredQuery(searchString, facetFormType, currentFormId, currentTabId, numberOfHitsDivId, updateId, tabDisplayPrefix) {

    var controllerAction = findTheAppropriateControllerActionForRequest(searchString, facetFormType);
    if (controllerAction == 'structureSearch') {
        handleStructureSearch('/bardwebclient/bardWebInterface/searchStructures', currentFormId)
    }
    else if (controllerAction != 'EMPTY') {
        handleSearch('/bardwebclient/bardWebInterface/' + controllerAction, currentFormId, currentTabId, numberOfHitsDivId, tabDisplayPrefix, updateId);
    }

}
/**
 * Based on the search String and the facetFormType figure out the Controller action to call
 * @param searchString
 * @param facetFormType
 * @return {String}
 */
function findTheAppropriateControllerActionForRequest(searchString, facetFormType) {

    var searchType = findSearchType(searchString);
    return findTheAppropriateControllerActionFromFacetType(searchType, facetFormType)
}
function findTheAppropriateControllerActionFromFacetType(searchType, facetFormType) {
    if (!$.trim(searchType).length) { //if empty search string
        return "EMPTY"
    }
    if (!$.trim(facetFormType).length) {  //if empty facet form type
        return "EMPTY"
    }

    switch (searchType.toUpperCase()) {
        case 'FREE_TEXT':
            if (facetFormType == 'AssayFacetForm') {
                return "searchAssays"
            }
            if (facetFormType == 'ProjectFacetForm') {
                return "searchProjects"
            }
            if (facetFormType == 'CompoundFacetForm') {
                return "searchCompounds"
            }
            break;
        case 'ID':
        case 'CID':
        case 'ADID':
        case 'PID':
            if (facetFormType == 'AssayFacetForm') {
                return 'searchAssaysByIDs'
            }
            if (facetFormType == 'ProjectFacetForm') {
                return 'searchProjectsByIDs'
            }
            if (facetFormType == 'CompoundFacetForm') {
                return 'searchCompoundsByIDs'
            }
            break;
        case 'STRUCTURE':
            if (facetFormType == 'CompoundFacetForm') {
                return  'structureSearch'
            }
            break;
    }
    return "EMPTY"

}
function showTab(tabId) {
    var tab = "#" + tabId;
    $(tab).tab('show');
}
/**
 * Handles form submission from the main form
 * @param searchString  - The string entered into the main form's search box
 */
function handleMainFormSubmit(searchString) {

    var searchType = findSearchType(searchString);

    switch (searchType.toUpperCase()) {
        case 'FREE_TEXT':
            handleAllFreeTextSearches();
            break;
        case 'ADID':
            activateCurrentTab('assaysTab');
            showTab("assays");
            handleSearch('/bardwebclient/bardWebInterface/searchAssaysByIDs', 'searchForm', 'assaysTab', 'totalAssays', 'Assay Definitions ', 'assays');
            break;
        case 'CID':
            activateCurrentTab('compoundsTab');
            showTab("compounds");
            handleSearch('/bardwebclient/bardWebInterface/searchCompoundsByIDs', 'searchForm', 'compoundsTab', 'totalCompounds', 'Compounds ', 'compounds');
            break;
        case 'PID':
            activateCurrentTab('projectsTab');
            // $("#projects").tab('show');
            showTab("projects");
            handleSearch('/bardwebclient/bardWebInterface/searchProjectsByIDs', 'searchForm', 'projectsTab', 'totalProjects', 'Projects ', 'projects');
            break;
        case 'ID':
            //TODO: Right now we are treating Id searches like regular searches
            //i.e we send the ids to all 3 resources
            //We intend to change to a modal view, where a user picks
            //the type of id (like we do with structure searches) so that we
            //can only send the query to the resource of interest
            handleAllIdSearches();
            break;
        case 'STRUCTURE':
            handleStructureSearch('/bardwebclient/bardWebInterface/searchStructures', 'searchForm');
            break;
    }
}
/**
 * Handle all free text searches
 */
function handleAllFreeTextSearches() {
    handleSearch('/bardwebclient/bardWebInterface/searchAssays', 'searchForm', 'assaysTab', 'totalAssays', 'Assay Definitions ', 'assays');
    handleSearch('/bardwebclient/bardWebInterface/searchCompounds', 'searchForm', 'compoundsTab', 'totalCompounds', 'Compounds ', 'compounds');
    handleSearch('/bardwebclient/bardWebInterface/searchProjects', 'searchForm', 'projectsTab', 'totalProjects', 'Projects ', 'projects');
}

/**
 * Handle all ID searches
 */
function handleAllIdSearches() {
    handleSearch('/bardwebclient/bardWebInterface/searchAssaysByIDs', 'searchForm', 'assaysTab', 'totalAssays', 'Assay Definitions ', 'assays');
    handleSearch('/bardwebclient/bardWebInterface/searchCompoundsByIDs', 'searchForm', 'compoundsTab', 'totalCompounds', 'Compounds ', 'compounds');
    handleSearch('/bardwebclient/bardWebInterface/searchProjectsByIDs', 'searchForm', 'projectsTab', 'totalProjects', 'Projects ', 'projects');
}
/**
 * Make Tabs inactive
 * @param tabId - The ID of the tab
 * @param tabListId - The ID of the tab list li element
 * @param resourceId - The ID of the resource (assays, compounds or projects)
 * @param tabDisplayPrefix - The prefix to display on tab
 */
function deActivateTabs(tabId, tabListId, resourceId, tabDisplayPrefix) {
    var tabIdElement = '#' + tabId;
    var resourceIdElement = '#' + resourceId;
    var tabListIdElement = '#' + tabListId;

    $(tabIdElement).html(tabDisplayPrefix);
    $(resourceIdElement).html('');
    $(tabListIdElement).removeClass('active');
}
/**
 * Make Tabs active
 * @param tabId - The ID of the tab
 * @param tabListId - The ID of the tab list li element
 * @param resourceId - The ID of the resource (assays, compounds or projects)
 * @param tabDisplayPrefix - The prefix to display on tab
 */
function activateTabs(tabId, tabListId, resourceId, tabDisplayPrefix) {
    var tabIdElement = '#' + tabId;
    var tabListIdElement = '#' + tabListId;
    var resourceIdElement = '#' + resourceId;

    $(tabIdElement).html(tabDisplayPrefix + spinnerImageLink);
    $(resourceIdElement).html('');
    $(tabListIdElement).addClass('active');
}
/**
 * Reset the tabs for structure searches
 * Make the counts zero for each tab, before you start the search
 */
function resetTabsForStructureSearches() {
    activateCurrentTab('compoundsTab');
}

/**
 * Reset the tabs for structure searches
 * Make the counts zero for each tab, before you start the search
 */
function activateCurrentTab(currentTab) {
    switch (currentTab) {
        case 'compoundsTab':
            activateTabs('compoundsTab', 'compoundsTabLi', 'compounds', "Compounds ");
            deActivateTabs('assaysTab', 'assaysTabLi', 'assays', 'Assay Definitions (0)');
            deActivateTabs('projectsTab', 'projectsTabLi', 'projects', 'Projects (0)');
            break;
        case 'assaysTab':
            deActivateTabs('compoundsTab', 'compoundsTabLi', 'compounds', "Compounds (0)");
            activateTabs('assaysTab', 'assaysTabLi', 'assays', 'Assay Definitions ');
            deActivateTabs('projectsTab', 'projectsTabLi', 'projects', 'Projects (0)');
            break;
        case 'projectsTab':
            deActivateTabs('compoundsTab', 'compoundsTabLi', 'compounds', "Compounds (0)");
            deActivateTabs('assaysTab', 'assaysTabLi', 'assays', 'Assay Definitions (0)');
            activateTabs('projectsTab', 'projectsTabLi', 'projects', 'Projects ');
            break;
        default:
            activateCurrentTab('compoundsTab');
            break;
    }
}
/**
 * Find the search type based on the search string supplied by the user
 * @return {String} - One of 'FREE_TEXT', 'STRUCTURE', 'EMPTY' or 'ID'
 */
function findSearchType(searchString) {

    if (!$.trim(searchString).length) {  //if this is an empty string, do nothing
        return "EMPTY";
    }
    if (searchString.match(NUMBER_MATCHING_REGEX)) {//this is an id match
        return "ID";
    }
    //we want to find out if this is a Structure search
    var searchStringSplit = searchString.split(":");
    var searchType = searchStringSplit[0];
    if (searchStringSplit.length == 2 && $.trim(searchStringSplit[1]).length) { //has to be of the form Exact:CCC so there must be 2 things in the array
        searchType = searchType.toLowerCase();
        var stringAfterColon = $.trim(searchStringSplit[1]);
        switch (searchType) { //must be one of these to qualify as a structure search
            case 'exact':
            case 'substructure':
            case 'superstructure':
            case 'similarity':
                return 'STRUCTURE';
            case 'adid': //this is an assay search with ids
                if (stringAfterColon.match(NUMBER_MATCHING_REGEX)) {//this is an id match
                    return 'ADID';
                }
                break;
            case 'cid':  //this is a compound search with ids
                if (stringAfterColon.match(NUMBER_MATCHING_REGEX)) {//this is an id match
                    return 'CID'
                }
                break;
            case 'pid':  //this is an project search with ids
                if (stringAfterColon.match(NUMBER_MATCHING_REGEX)) {//this is an id match
                    return 'PID'
                }
                break;
        }
    } else if (searchStringSplit.length == 2 && !$.trim(searchStringSplit[1]).length) { //e.g Exact: with no smile string
        return "EMPTY";
    }
    return "FREE_TEXT"; //this a Free Text Search
}