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

%{--<g:render template="message"/>--}%
<div class="row-fluid">
    <div class="offset1 span10">
        <g:render template="/common/errors" model="['errors': instance?.errors?.globalErrors ?: instance?.errors?.allErrors]"/>
    </div>
</div>
<g:set var="disabledInput" value="${reviewNewItem ? true : false}"/>

<input type="hidden" id="disabledInput" value="${disabledInput}"/>

<g:form class="form-horizontal" action="${action.toLowerCase()}">
<g:hiddenField name="contextOwnerId" value="${instance?.contextOwnerId}"/>
<g:hiddenField name="contextId" value="${instance?.contextId}"/>
<g:hiddenField name="contextClass" value="${instance?.contextClass}"/>
<g:hiddenField name="contextItemId" value="${instance?.contextItemId}"/>
<g:hiddenField name="version" value="${instance?.version}"/>


<g:hiddenField name="attributeElementText" value="${instance?.contextItem?.attributeElement?.label}"/>
<g:hiddenField name="valueElementText" value="${instance?.contextItem?.valueElement?.label}"/>
<g:hiddenField name="extValueText" value="(${instance?.extValueId}) ${instance?.valueDisplay}"/>


<div class="control-group ${hasErrors(bean: instance, field: 'attributeElementId', 'error')}">
    <label class="control-label" for="attributeElementId"><g:message
            code="contextItem.attributeElementId.label"/>:</label>

    <div class="controls">
        <g:hiddenField id="attributeElementId" name="attributeElementId" class="span11"
                       value="${instance?.attributeElementId}" disabled="${disabledInput}"/>
        <span class="help-inline"><g:fieldError field="attributeElementId" bean="${instance}"/></span>

    </div>
</div>

<div class="control-group">
    <label class="control-label" for="attributeDescription"><g:message code="contextItem.attributeDescription.label"/>:</label>
    <div class="controls">
        <g:textArea id="attributeDescription" name="attributeDescription" class="span11"
                    value="${instance?.contextItem?.attributeElement?.description}" disabled="true"/>
    </div>
</div>

<g:if test="${instance?.contextClass == 'AssayContext'}">
    <div class="control-group">
        <div class="controls">
            <label class="checkbox">
                <g:checkBox disabled="${disabledInput}" id="providedWithResults" name="providedWithResults"
                            checked="${instance.providedWithResults}"/>
                Value to be provided as part of result upload
            </label>
        </div>
    </div>

    <div id="valueConstraintContainer" style="display: none;">
        How should the values be constrained when results are loaded
        <div class="control-group ${hasErrors(bean: instance, field: 'valueConstraintType', 'error')}">

            <div class="controls">
                <label class="radio">
                    <g:radio disabled="${disabledInput}" name="valueConstraintType" id="valueConstraintFree"
                             value="Free" checked="${instance.valueConstraintType == 'Free'}"/> Not constrained
                </label>

                <label class="radio">
                    <g:radio disabled="${disabledInput}" name="valueConstraintType" id="valueConstraintList"
                             value="List"
                             checked="${instance.valueConstraintType == 'List'}"/> Should be selected from a list of acceptable values specified below
                            <br/><strong>NOTE:</strong> List items are added and deleted one at a time.  Items with same attribute are grouped into a list.
                </label>

                <label class="radio" id="valueConstraintRangeContainer">
                    <g:radio disabled="${disabledInput}" name="valueConstraintType" id="valueConstraintRange"
                             value="Range"
                             checked="${instance.valueConstraintType == 'Range'}"/> Should be within range specified below
                </label>
            </div>
            <span class="help-inline"><g:fieldError field="valueConstraintType" bean="${instance}"/></span>
        </div>
    </div>
</g:if>

<div id="elementValueContainer" style="display: none;">
    <g:if test="${disabledInput == false}">
        <div class="row-fluid">
            <div class="span10 offset1 alert alert-info">
                <p>This attribute expects a value from the dictionary, start typing to select a value.</p>

                <p>If you cannot find an existing value, please add a value to the dictionary.
                    <a href="${createLink(controller: "element", action: "selectParent")}" target="proposeTerm"
                       class="btn"
                       id="proposeANewTermButton">Propose a New Dictionary Term</a></p>
            </div>
        </div>
    </g:if>
    <div class="control-group ${hasErrors(bean: instance, field: 'valueElementId', 'error')}">

        <label class="control-label" for="valueElementId"><g:message
                code="contextItem.valueElementId.label"/>:</label>

        <div class="controls">
            <g:hiddenField class="span11 valueField" id="valueElementId" name="valueElementId"
                           value="${instance?.valueElementId}" disabled="${disabledInput}"/>

            <span class="help-inline"><g:fieldError field="valueElementId" bean="${instance}"/></span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label" for="valueDescription"><g:message code="contextItem.valueDescription.label"/>:</label>
        <div class="controls">
            <g:textArea id="valueDescription" name="valueDescription" class="span11"
                        value="${instance?.contextItem?.valueElement?.description}" disabled="true"/>
        </div>
    </div>
</div>

<div id="externalOntologyValueContainer">
    <g:if test="${disabledInput == false}">
        <div id="externalOntologyInfo" class="row-fluid">

        </div>

        <div id="externalOntologySearch" class="control-group">
            <label class="control-label" for="extValueId"><g:message code="contextItem.extValueSearch.label"/>:</label>

            <div class="controls">
                <g:hiddenField
                        class="span11 valueField" id="extValueSearch" name="extValueSearch"
                        value="${instance?.extValueId}"/>
            </div>
        </div>
    </g:if>


    <div class="control-group ${hasErrors(bean: instance, field: 'extValueId', 'error')}">
        <label class="control-label" for="extValueId"><g:message
                code="contextItem.extValueId.label"/>:</label>

        <div class="controls">

            <g:textField
                    class="span11 valueField" id="extValueId" name="extValueId" value="${instance?.extValueId}"
                    disabled="${disabledInput}"/>
            <span class="help-inline"><g:fieldError field="extValueId" bean="${instance}"/></span>
        </div>
    </div>
</div>

<div id="numericValueContainer" class="control-group ${hasErrors(bean: instance, field: 'qualifier', 'error')}">
    <g:if test="${disabledInput == false}">
        <div class="row-fluid">
            <div class="span6 offset2 alert alert-info">
                <p>This attribute expects a numeric value, please enter an integer, decimal or scientific notation, e.g. 1, 1.0 or 1E-3</p>
            </div>
        </div>
    </g:if>
    <label class="control-label" for="valueNum"><g:message code="contextItem.valueNum.label"/>:</label>

    <div class="control controls-row">
        <g:select class="offset1 span2 valueField" id="qualifier" name="qualifier"
                  from="${instance?.constraints.qualifier.inList}"
                  value="${instance?.qualifier != null ? instance?.qualifier : '= '}"
                  disabled="${disabledInput}"/>

        <g:textField class="span2 valueField" id="valueNum" name="valueNum"
                     placeholder="${message(code: "contextItem.valueNum.label")}"
                     value="${instance?.valueNum}" disabled="${disabledInput}"/>

        <g:textField class="span2 valueField" id="valueNumUnitId" name="valueNumUnitId"
                     value="${instance?.valueNumUnitId}"
                     disabled="${disabledInput}"/>
    </div>

</div>

<div class="row-fluid">
    <g:set var="numericFieldErrors"
           value="${instance?.errors?.fieldErrors.findAll { it.field in ['qualifier', 'valueNum', 'valueNumUnitId'] }}"/>
    <g:if test="${numericFieldErrors}">
        <div class="offset1 span10">
            <g:render template="/common/errors"
                      model="['errors': instance?.errors?.fieldErrors.findAll { it.field in ['qualifier', 'valueNum', 'valueNumUnitId'] }]"/>
        </div>
    </g:if>
</div>

<div id="numericRangeValueContainer"
     class="control-group ${hasErrors(bean: instance, field: 'qualifier', 'error')}">
    <g:if test="${disabledInput == false}">
        <div class="row-fluid">
            <div class="span6 offset2 alert alert-info">
                <p>Please enter a the min and max of the allowed range below</p>
            </div>
        </div>
    </g:if>

    <div class="control-group ${hasErrors(bean: instance, field: 'valueMin', 'error')}">
        <label class="control-label"><g:message code="contextItem.range.label"/>:</label>

        <div class="control controls-row">
            <g:textField class="offset1 span2 valueField" id="valueMin" name="valueMin"
                         placeholder="${message(code: "contextItem.valueMin.label")}" value="${instance?.valueMin}"
                         disabled="${disabledInput}"/>
            <span class="span1">TO</span>
            <g:textField class="span2 valueField" id="valueMax" name="valueMax"
                         placeholder="${message(code: "contextItem.valueMax.label")}" value="${instance?.valueMax}"
                         disabled="${disabledInput}"/>
        </div>
    </div>
</div>

<div id="freeTextValueContainer">
    <div class="control-group ${hasErrors(bean: instance, field: 'valueDisplay', 'error')}">
        <label class="control-label" for="valueDisplay"><g:message
                code="contextItem.valueDisplay.label"/>:</label>

        <div class="controls">
            <g:textField class="valueField span11" id="valueDisplay" name="valueDisplay"
                         value="${instance?.valueDisplay}"
                         disabled="${disabledInput}"/>
            <span class="help-inline"><g:fieldError field="valueDisplay" bean="${instance}"/></span>
        </div>
    </div>
</div>

<div class="row-fluid">
    <div id="noneValueContainer" class="offset2 span6 alert alert-info">
        <h4>Warning!</h4>

        <p>This attribute isn't allowed to have values assigned to it. Please select another attribute.</p>
    </div>
</div>

<div class="control-group">
    <div class="controls">

        <g:set var="contextItemParams" value="${[contextItemId: instance.contextItemId, contextId: instance?.contextId,
                contextClass: instance?.context.simpleClassName, contextOwnerId: instance?.contextOwnerId,
                groupBySection: instance?.context?.getContextType()?.id?.encodeAsURL()]}"></g:set>
        <g:if test="${reviewNewItem}">
            <g:link controller="${instance?.ownerController}" action="show"
                    id="${instance?.contextOwnerId}"
                    params="${contextItemParams}"
                    fragment="card-${instance?.contextId}"
                    class="btn">Back to Context</g:link>
            <g:link action="edit" class="btn" params="${contextItemParams}">Edit</g:link>
            <g:link action="create" class="btn btn-primary focus"
                    params="${contextItemParams}">Add Another Item</g:link>
        </g:if>
        <g:else>
            <g:link controller="${instance?.ownerController}" action="show"
                    id="${instance?.contextOwnerId}"
                    params="${contextItemParams}"
                    fragment="card-${instance?.contextId}"
                    class="btn">Cancel</g:link>
            <button type="submit" class="btn btn-primary">${action}</button>

        </g:else>
    </div>
</div>

<div class="row-fluid">
    <div class="span10 offset1">
        <h3>Current context:</h3>
        <g:render template="edit" model="${[context: instance.context,
                disableHeaderEdits: true,
                highlightedItemId: instance.contextItemId]}"/>
    </div>
</div>

<script id="externalOntologyIntegratedSearchTemplate" type="text/x-handlebars-template">
    <div class="span6 offset2 alert alert-info ">
        <p>An integrated lookup service exists for this External Ontology, start typing an identifier or some text and select a value.</p>

        <p>This lookup service reaches <strong>outside of this application</strong> and <strong>performance can vary</strong> for a number of reasons including network conditions and the underlying service.
        </p>

        <p>If the integrated search for the '{{attributeLabel}}' site doesn't meet your expectations, please search directly on the '{{attributeLabel}}'
        site.  <a href="{{attributeExternalUrl}}" target="external_ontology_site" class="btn">Open Site</a></p>

        <p>Then manually enter an External Ontology Id and a Display Value.</p>
    </div>

</script>
<script id="externalOntologyNoIntegratedSearchTemplate" type="text/x-handlebars-template">
    <div class="span6 offset2 alert alert-info ">
        <p>An integrated lookup service for {{attributeLabel}} does not currently exist.</p>

        <p>Please search directly on the {{attributeLabel}} site. <a href="{{attributeExternalUrl}}"
                                                                     target="external_ontology_site"
                                                                     class="btn">Open Site</a></p>

        <p>Then manually enter an External Ontology Id and a Display Value.</p>
    </div>
</script>
</g:form>
