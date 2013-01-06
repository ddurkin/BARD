<%
/**
 * wizard refresh flow action
 *
 * When a page (/ partial) is rendered, any DOM event handlers need to be
 * (re-)attached. The af:ajaxButton, af:ajaxSubmitJs and af:redirect tags
 * supports calling a JavaScript after the page has been rendered by passing
 * the 'afterSuccess' argument.
 *
 * Example:	af:redirect afterSuccess="onPage();"
 * 		af:redirect afterSuccess="console.log('redirecting...');"
 *
 * Generally one would expect this code to add jQuery event handlers to
 * DOM objects in the rendered page (/ partial).
 *
 * @author	Jeroen Wesbeek <work@osx.eu>
 * @package AjaxFlow
 */
%>
<script type="text/javascript">
		$(document).ready(function() {			
			initializePageOne();			
		})
        function onPage() {
                if (console) {
                        console.log('calling onPage() which can be used to attach generic javascript handlers to DOM elements of a rendered page / partial');
                }
                var sectionPath = $("#sectionPath").val();
    			console.log('sectionPath var = ' + sectionPath);
    			var cardAssayContextId = $("#cardAssayContextId").val();
    			console.log('cardAssayContextId var = ' + cardAssayContextId);
                initializePageOne();
                initializePageThree();
        }

        function initializePageOne(){
        	if (console) {
                console.log('calling initializePageOne()');
        	}
        	var autoOpts = {
        			minLength: 2,
        			//source: "/BARD/ontologyJSon/getDescriptors",
        			source: function( request, response ) {            			
            			var sectionPath = $("#sectionPath").val();
            			console.log('Auto-complete sectionPath var = ' + sectionPath);
            			var cardAssayContextId = $("#cardAssayContextId").val();
            			console.log('Auto-complete cardAssayContextId var = ' + cardAssayContextId);
            			$("#path").val(sectionPath);
            			$("#assayContextId").val(cardAssayContextId);
            			var cardAssaySection = $("#path").val();
            			console.log('cardAssaySection var = ' + cardAssaySection);
            			var assayContextId = $("#assayContextId").val();
            			console.log('assayContextId var = ' + assayContextId);
        				$.getJSON( 
                				"/BARD/ontologyJSon/getDescriptors",
                				{
                            		term: request.term,
                            		section: sectionPath
                            	}, 
                            	response 
                        );
            		},

        			select: function( event, ui ) {
                        $( "#currentValue" ).val( ui.item.value );
                        $( "#elementId" ).val( ui.item.elementId );                        
                    }
                			
        	}
        	$( "#attributeTextField" ).autocomplete(autoOpts);
        }

        function initializePageThree(){
        	if (console) {
                console.log('calling initializePagThree()');
        	}
        	var autoOpts = {
        			minLength: 2,
        			//source: "/BARD/ontologyJSon/getDescriptors",
        			source: function( request, response ) {
            			var cardAssaySection = $("#path").val()
            			cardAssaySection = "assay protocol> assay component"
            			var elementId = $("#elementId").val();
            			console.log('cardAssaySection var = ' + cardAssaySection);
            			console.log('elementId var = ' + elementId);
        				$.getJSON( 
                				"/BARD/ontologyJSon/getValueDescriptors",
                				{
                            		term: request.term,
                            		section: cardAssaySection,
                            		attributeId: 153
                            	}, 
                            	response 
                        );
            		},

        			select: function( event, ui ) {
                        $( "#currentChoice" ).val( ui.item.value );
                        $( "#unit" ).val( ui.item.unit );
                    }
                			
        	}
        	$( "#valueTextField" ).autocomplete(autoOpts);
        }
</script>
