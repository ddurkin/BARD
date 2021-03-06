/* Copyright (c) 2014, The Broad Institute
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
 */

package scenarios

import pages.ContextItemPage
import pages.EditContextPage
import pages.ViewExperimentPage
import spock.lang.Unroll
import base.BardFunctionalSpec

import common.Constants
import common.TestData

import db.Experiment

/**
 * This class holds all the test functions of Experiment Annotations including context card and context item add/edit/delete
 * @author Muhammad.Rafique
 * Date Created: 2013/11/27
 */
@Unroll
class ExperimentAnnotationSpec extends BardFunctionalSpec {
	def section = "contexts-header"
	def editContextGroup = "Unclassified"
	def contextCard = "Test Card"
	def dbContextType = "Unclassified"

	def setup() {
		logInSomeUser()
	}

	def "Test Add and Delete Context Card in Experiment"(){
		given:"Navigate to Show Experiment page"
		to ViewExperimentPage

		when:"At View Experiment Definition Page, Fetching Contexts Info from UI and DB for validation"
		at ViewExperimentPage
		def uiContentsBefore = getUIContexts(section)
		def dbContentsBefore = Experiment.getExperimentContext(dbContextType, TestData.experimentId)

		then:"Verifying Context Info with UI & DB"
		assert uiContentsBefore.size() == dbContentsBefore.size()
		assert uiContentsBefore.sort() == dbContentsBefore.sort()

		and:"Navigating to Edit Experiment Context Page"
		navigateToEditContext(section)

		when:"At Edit Experiment Context Page, Fetching Contexts Info from UI and DB for validation"
		at EditContextPage
		while(isContext(editContextGroup, contextCard)){
			deleteContext(editContextGroup, contextCard)
		}

		then:"Navigating to Context Item Page"
		addNewContextCard(editContextGroup, contextCard)

		and:"Verifying Context added successfully"
		assert isContext(editContextGroup, contextCard)

		when:"Context is added, Fetching Contexts Info from UI and DB for validation"
		def uiContentsAfterAdd = getUIContexts(editContextGroup)
		def dbContentsAfterAdd = Experiment.getExperimentContext(dbContextType, TestData.experimentId)

		then:"Verifying Context Info with UI & DB"
		assert uiContentsAfterAdd.sort() == dbContentsAfterAdd.sort()

		and:"Navigating to View Experiment Definition Page"
		finishEditing.buttonPrimary.click()

		when:"At View Experiment Definition Page, Fetching Contexts Info from UI and DB for validation"
		at ViewExperimentPage
		uiContentsAfterAdd = getUIContexts(section)
		dbContentsAfterAdd = Experiment.getExperimentContext(dbContextType, TestData.experimentId)

		then:"Verifying Context Info with UI & DB"
		assert uiContentsAfterAdd.sort() == dbContentsAfterAdd.sort()

		and:"Cleaning up Context Items"
		navigateToEditContext(section)
		at EditContextPage
		while(isContext(editContextGroup, contextCard)){
			deleteContext(editContextGroup, contextCard)

			when:"Context is cleaned up, Fetching Contexts Info from UI and DB for validation"
			def uiContentsAfterDelete = getUIContexts(editContextGroup)
			def dbContentsAfterDelete = Experiment.getExperimentContext(dbContextType, TestData.experimentId)

			then:"Verifying Context Info with UI & DB"
			assert uiContentsAfterDelete.sort() == dbContentsAfterDelete.sort()
		}
		and:"Navigating to View Experiment Definition Page"
		finishEditing.buttonPrimary.click()

		when:"At View Experiment Definition Page, Fetching Contexts Info from UI and DB for validation"
		at ViewExperimentPage
		def uiContents = getUIContexts(section)
		def dbContents = Experiment.getExperimentContext(dbContextType, TestData.experimentId)

		then:"Verifying Context Info with UI & DB"
		assert uiContents.sort() == dbContents.sort()

		report ""
	}

	def "Test Edit Context Card in Experiment"(){
		given:"Navigate to Show Experiment page"
		to ViewExperimentPage

		when:"At View Experiment Definition Page, Fetching Contexts Info from UI and DB for validation"
		at ViewExperimentPage
		def editedContext = contextCard+Constants.edited
		def uiContentsBefore = getUIContexts(section)
		def dbContentsBefore = Experiment.getExperimentContext(dbContextType, TestData.experimentId)

		then:"Verifying Context Info with UI & DB"
		assert uiContentsBefore.size() == dbContentsBefore.size()
		assert uiContentsBefore.sort() == dbContentsBefore.sort()

		and:"Navigating to Edit Experiment Context Page"
		navigateToEditContext(section)

		when:"At Edit Experiment Context Page, Fetching Contexts Info from UI and DB for validation"
		at EditContextPage
		while(isContext(editContextGroup, contextCard)){
			deleteContext(editContextGroup, contextCard)
		}

		then:"Add new Context Card and then Edit it"
		if(!isContext(editContextGroup, contextCard)){
			addNewContextCard(editContextGroup, contextCard)
			editContext(editContextGroup, contextCard, editedContext)
		}else{
			editContext(editContextGroup, contextCard, editedContext)
		}

		and:"Verifying Context edited successfully"
		assert isContext(editContextGroup, editedContext)

		when:"Context is edited, Fetching Contexts Info from UI and DB for validation"
		def uiContentsAfterAdd = getUIContexts(editContextGroup)
		def dbContentsAfterAdd = Experiment.getExperimentContext(dbContextType, TestData.experimentId)

		then:"Verifying Context Info with UI & DB"
		assert uiContentsAfterAdd.sort() == dbContentsAfterAdd.sort()

		and:"Navigating to View Experiment Definition Page"
		finishEditing.buttonPrimary.click()

		when:"At View Experiment Definition Page, Fetching Contexts Info from UI and DB for validation"
		at ViewExperimentPage
		uiContentsAfterAdd = getUIContexts(section)
		dbContentsAfterAdd = Experiment.getExperimentContext(dbContextType, TestData.experimentId)

		then:"Verifying Context Info with UI & DB"
		assert uiContentsAfterAdd.sort() == dbContentsAfterAdd.sort()

		and:"Cleaning up Context Items"
		navigateToEditContext(section)
		at EditContextPage
		while(isContext(editContextGroup, editedContext)){
			deleteContext(editContextGroup, editedContext)

			when:"Context is cleaned up, Fetching Contexts Info from UI and DB for validation"
			def uiContentsAfterDelete = getUIContexts(editContextGroup)
			def dbContentsAfterDelete = Experiment.getExperimentContext(dbContextType, TestData.experimentId)

			then:"Verifying Context Info with UI & DB"
			assert uiContentsAfterDelete.sort() == dbContentsAfterDelete.sort()
		}
		and:"Navigating to View Experiment Definition Page"
		finishEditing.buttonPrimary.click()

		when:"At View Experiment Definition Page, Fetching Contexts Info from UI and DB for validation"
		at ViewExperimentPage
		def uiContents = getUIContexts(section)
		def dbContents = Experiment.getExperimentContext(dbContextType, TestData.experimentId)

		then:"Verifying Context Info with UI & DB"
		assert uiContents.sort() == dbContents.sort()

		report ""
	}

	def "Test Add #TestName Type Context Item in Experiment"(){
		given:"Navigate to Show Experiment page"
		to ViewExperimentPage

		when:"At View Experiment Definition Page, Fetching Contexts Info from UI and DB for validation"
		at ViewExperimentPage

		def uiContentsBefore = getUIContexts(section)
		def dbContentsBefore = Experiment.getExperimentContext(dbContextType, TestData.experimentId)

		then:"Verifying Context Info with UI & DB"
		assert uiContentsBefore.size() == dbContentsBefore.size()
		assert uiContentsBefore.sort() == dbContentsBefore.sort()

		and:"Navigating to Edit Experiment Context Page"
		navigateToEditContext(section)

		when:"At Edit Experiment Context Page, Fetching Contexts Info from UI and DB for validation"
		at EditContextPage
		while(isContext(editContextGroup, contextCard)){
			deleteContext(editContextGroup, contextCard)
		}
		then:"Add Context Card, Before adding context item"
		addNewContextCard(editContextGroup, contextCard)
		assert isContext(editContextGroup, contextCard)

		and:"Navigating to Context Item Page"
		navigateToAddContextItem(editContextGroup, contextCard)

		when: "At Context Item Page"
		at ContextItemPage

		then:"Adding New Context Item"
		if(TestName == "Element"){
			addElementContextItem(inputData, true, false)
		}else if(TestName == "FreeText"){
			addFreeTextItem(inputData, true, false)
		}else if(TestName == "NumericValue"){
			addNumericValueItem(inputData, true, false)
		}else if(TestName == "ExOntologyIntegtegrated"){
			addExternalOntologyItem(inputData, true, false, true)
		}
		else if(TestName == "ExOntologyNoIntegtegrated"){
			addExternalOntologyItem(inputData, true, false, false)
		}

		and:"Verifying Context Item added successfully"
		at EditContextPage
		assert isContextItem(editContextGroup, contextCard, contextItem)

		when:"Context Item  is added, Fetching Contexts Info from UI and DB for validation"
		def uiContentsAfterAdd = getUIContextItems(editContextGroup, contextCard)
		def dbContentsAfterAdd = Experiment.getExperimentContextItem(TestData.experimentId, dbContextType, contextCard)

		then:"Verifying Context Info with UI & DB"
		assert uiContentsAfterAdd.sort() == dbContentsAfterAdd.sort()

		and:"Navigating to View Experiment Definition Page"
		finishEditing.buttonPrimary.click()

		when:"At View Experiment Definition Page, Fetching Contexts Info from UI and DB for validation"
		at ViewExperimentPage
		uiContentsAfterAdd = getUIContextItems(section, contextCard)
		dbContentsAfterAdd = Experiment.getExperimentContextItem(TestData.experimentId, dbContextType, contextCard)

		then:"Verifying Context Info with UI & DB"
		assert uiContentsAfterAdd.sort() == dbContentsAfterAdd.sort()

		and:"Cleaning up Context Items"
		navigateToEditContext(section)
		at EditContextPage
		while(isContextItem(editContextGroup, contextCard, contextItem)){
			deleteContextItem(editContextGroup, contextCard, contextItem)

			when:"Context Item  is cleaned up, Fetching Contexts Info from UI and DB for validation"
			def uiContentsAfterDelete = getUIContextItems(editContextGroup, contextCard)
			def dbContentsAfterDelete = Experiment.getExperimentContextItem(TestData.experimentId, dbContextType, contextCard)

			then:"Verifying Context Info with UI & DB"
			assert uiContentsAfterDelete.sort() == dbContentsAfterDelete.sort()
		}
		and:"Cleaning up Contexts"
		while(isContext(editContextGroup, contextCard)){
			deleteContext(editContextGroup, contextCard)
		}

		and:"Navigating to View Experiment Definition Page"
		finishEditing.buttonPrimary.click()

		and:"At View Experiment Definition Page"
		at ViewExperimentPage
		assert !isContext(section, contextCard)

		report "$TestName"

		where:
		TestName					| inputData						| contextItem
		"Element"					| TestData.contexts.Element		| TestData.contexts.Element.attribute
		"FreeText"					| TestData.contexts.FreeText	| TestData.contexts.FreeText.attribute
		"NumericValue"				| TestData.contexts.Numeric		| TestData.contexts.Numeric.attribute
		"ExOntologyIntegtegrated"	| TestData.contexts.ExtOntology	| TestData.contexts.ExtOntology.attribute

	}

	def "Test Edit #TestName Type Context Item in Experiment"(){
		given:"Navigate to Show Experiment page"
		to ViewExperimentPage

		when:"At View Experiment Definition Page, Fetching Contexts Info from UI and DB for validation"
		at ViewExperimentPage
		def uiContentsBefore = getUIContextItems(section, contextCard)
		def dbContentsBefore = Experiment.getExperimentContextItem(TestData.experimentId, dbContextType, contextCard)

		then:"Verifying Context Info with UI & DB"
		assert uiContentsBefore.size() == dbContentsBefore.size()
		assert uiContentsBefore.sort() == dbContentsBefore.sort()

		and:"Navigating to Edit Experiment Context Page"
		navigateToEditContext(section)

		when:"At Edit Experiment Context Page, Fetching Contexts Info from UI and DB for validation"
		at EditContextPage
		while(isContext(editContextGroup, contextCard)){
			deleteContext(editContextGroup, contextCard)
		}

		then:"At Edit Experiment Context Page, Fetching Contexts Info from UI and DB for validation"
		at EditContextPage
		addNewContextCard(editContextGroup, contextCard)
		assert isContext(editContextGroup, contextCard)

		and:"If context Item does nto exists, add new before updating"
		if(!isContextItem(editContextGroup, contextCard, contextItem)){
			navigateToAddContextItem(editContextGroup, contextCard)
			and:"Navigating to Context Item Page"
			at ContextItemPage
			if(TestName == "Element"){
				addElementContextItem(inputData, true, false)
			}else if(TestName == "FreeText"){
				addFreeTextItem(inputData, true, false)
			}else if(TestName == "NumericValue"){
				addNumericValueItem(inputData, true, false)
			}else if(TestName == "ExOntologyIntegtegrated"){
				addExternalOntologyItem(inputData, true, false, true)
			}
			else if(TestName == "ExOntologyNoIntegtegrated"){
				addExternalOntologyItem(inputData, true, false, false)
			}
			and:"Navigating to Edit Context Page"
			at EditContextPage
			assert isContextItem(editContextGroup, contextCard, contextItem)
			navigateToUpdateContextItem(editContextGroup, contextCard, contextItem)
		}else{
			navigateToUpdateContextItem(editContextGroup, contextCard, contextItem)
		}

		when: "At Context Item Page"
		at ContextItemPage

		then:"Updating Context Item"
		if(TestName == "Element"){
			addElementContextItem(inputData, false, false)
		}else if(TestName == "FreeText"){
			addFreeTextItem(inputData, false, false)
		}else if(TestName == "NumericValue"){
			addNumericValueItem(inputData, false, false)
		}else if(TestName == "ExOntologyIntegtegrated"){
			addExternalOntologyItem(inputData, false, false, true)
		}
		else if(TestName == "ExOntologyNoIntegtegrated"){
			addExternalOntologyItem(inputData, false, false, false)
		}

		and:"Verifying Context Item updated successfully"
		at EditContextPage
		assert isContextItem(editContextGroup, contextCard, contextItem)

		when:"Context Item  is updated, Fetching Contexts Info from UI and DB for validation"
		def uiContentsAfterAdd = getUIContextItems(editContextGroup, contextCard)
		def dbContentsAfterAdd = Experiment.getExperimentContextItem(TestData.experimentId, dbContextType, contextCard)

		then:"Verifying Context Info with UI & DB"
		assert uiContentsAfterAdd.sort() == dbContentsAfterAdd.sort()

		and:"Navigating to View Experiment Definition Page"
		finishEditing.buttonPrimary.click()

		when:"At View Experiment Definition Page, Fetching Contexts Info from UI and DB for validation"
		at ViewExperimentPage
		uiContentsAfterAdd = getUIContextItems(section, contextCard)
		dbContentsAfterAdd = Experiment.getExperimentContextItem(TestData.experimentId, dbContextType, contextCard)

		then:"Verifying Context Info with UI & DB"
		assert uiContentsAfterAdd.sort() == dbContentsAfterAdd.sort()

		and:"Cleaning up Context Items"
		navigateToEditContext(section)
		at EditContextPage
		while(isContextItem(editContextGroup, contextCard, contextItem)){
			deleteContextItem(editContextGroup, contextCard, contextItem)

			when:"Context Item  is cleaned up, Fetching Contexts Info from UI and DB for validation"
			def uiContentsAfterDelete = getUIContextItems(editContextGroup, contextCard)
			def dbContentsAfterDelete = Experiment.getExperimentContextItem(TestData.experimentId, dbContextType, contextCard)

			then:"Verifying Context Info with UI & DB"
			assert uiContentsAfterDelete.sort() == dbContentsAfterDelete.sort()
		}
		and:"Cleaning up Contexts"
		while(isContext(editContextGroup, contextCard)){
			deleteContext(editContextGroup, contextCard)
		}

		and:"Navigating to View Experiment Definition Page"
		finishEditing.buttonPrimary.click()

		and:"At View Experiment Definition Page"
		at ViewExperimentPage
		assert !isContext(section, contextCard)

		report "$TestName"

		where:
		TestName					| inputData						| contextItem
		"Element"					| TestData.contexts.Element		| TestData.contexts.Element.attribute
		"FreeText"					| TestData.contexts.FreeText	| TestData.contexts.FreeText.attribute
		"NumericValue"				| TestData.contexts.Numeric		| TestData.contexts.Numeric.attribute
		"ExOntologyIntegtegrated"	| TestData.contexts.ExtOntology	| TestData.contexts.ExtOntology.attribute
		//		"ExOntologyNoIntegtegrated"	| TestData.contexts.ExtOntology	| TestData.contexts.ExtOntology.attribute
	}

}
