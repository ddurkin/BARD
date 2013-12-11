package bard.db.dictionary

import bard.db.command.BardCommand
import bard.db.enums.AddChildMethod
import bard.util.BardCacheUtilsService
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import grails.validation.Validateable
import grails.validation.ValidationErrors
import groovy.transform.InheritConstructors

import javax.servlet.http.HttpServletResponse

class ElementController {

    private static final String errorMessageKey = "errorMessageKey"
    ElementService elementService
    BuildElementPathsService buildElementPathsService
    BardCacheUtilsService bardCacheUtilsService
    ModifyElementAndHierarchyService modifyElementAndHierarchyService

    def index() {
        redirect(uri: '/')
    }

    def showTopLevelHierarchyHelp() {
        render(view: 'showTopLevelHierarchyHelp')
    }

    def list() {
        Map parameterMap = generatePaths()

        if (!parameterMap.containsKey(errorMessageKey)) {
            return parameterMap
        } else {
            render(parameterMap.get(errorMessageKey))
        }
    }

    @Secured(['isAuthenticated()'])
    def select() {}

    @Secured(['isAuthenticated()'])
    def listAjax() {
        Map parameterMap = generatePaths()

        if (!parameterMap.containsKey(errorMessageKey)) {
            Map map = [results: elementService.convertPathsToSelectWidgetStructures(parameterMap.list)]
            render map as JSON
        } else {
            render(parameterMap.get(errorMessageKey))
        }
    }

    @Secured(['isAuthenticated()'])
    def getChildrenAsJson(long elementId, boolean doNotShowRetired, String expectedValueType) {
        List elementHierarchyTree = elementService.getChildNodes(elementId, doNotShowRetired, expectedValueType)
        JSON elementHierarchyAsJsonTree = new JSON(elementHierarchyTree)
        render elementHierarchyAsJsonTree
    }

    @Secured(['isAuthenticated()'])
    def buildTopLevelHierarchyTree(boolean doNotShowRetired, String treeRoot, String expectedValueType) {
        List elementHierarchyTree = elementService.createElementHierarchyTree(doNotShowRetired, treeRoot, expectedValueType)
        JSON elementHierarchyAsJsonTree = new JSON(elementHierarchyTree)
        render elementHierarchyAsJsonTree
    }

    /**
     * Handles adding a new term element to the dictionary.
     * First step is to choose the new term's parent ID
     * Second step is to add content and create the new term.
     *
     * @return
     */
    @Secured(['isAuthenticated()'])
    def selectParent() {
        flash.message = ''
        Element parentElement = Element.findById(params.attributeElementId)
        render(view: 'selectParent', model: [termCommand: new TermCommand(parentElementId: parentElement.id, parentLabel: parentElement?.label, parentDescription: parentElement?.description)])
    }

    @Secured(['isAuthenticated()'])
    def addTerm() {
        flash.message = ''
        Element parentElement = Element.findById(params.attributeElementId)
        if (!parentElement || parentElement?.addChildMethod != AddChildMethod.DIRECT) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Could not find Experiment ${params.attributeElementId} or this experiment could not be used as a parent-element for a new proposed term-element")
            return
        }
        render(view: 'addTerm', model: [termCommand: new TermCommand(parentElementId: parentElement.id, parentLabel: parentElement?.label, parentDescription: parentElement?.description)])
    }

    @Secured(['isAuthenticated()'])
    def saveTerm(TermCommand termCommand) {
        Element currentElement = null
        flash.message = ''
        if (termCommand.validate()) {

            if (!termCommand.hasErrors()) {
                //remove duplicate white spaces, then trim the string
                //we probably should also consider removing some other characters
                termCommand.label = termCommand.label.replaceAll("\\s+", " ").trim()
                currentElement =
                    this.elementService.addNewTerm(termCommand)
                if (currentElement.hasErrors()) {
                    termCommand.currentElement = currentElement
                    termCommand.transferErrorsFromCurrentElement()
                } else {
                    termCommand.success = true
                    flash.message = "Proposed term ${currentElement?.label} has been saved"
                    bardCacheUtilsService.refreshDueToNewDictionaryEntry()
                }
            }
        }
        if (request.getHeader('X-Requested-With') == 'XMLHttpRequest') {  //if ajax then render template
            render(template: 'addForm', model: [termCommand: termCommand, currentElement: currentElement])
            return
        }
        render(view: 'addTerm', model: [termCommand: termCommand, currentElement: currentElement])
    }

    @Secured(["hasRole('ROLE_CURATOR')"])
    def edit() {
        Map parameterMap = generatePaths()

        if (!parameterMap.containsKey(errorMessageKey)) {
            return parameterMap
        } else {
            render(parameterMap.get(errorMessageKey))
        }
    }

    private Map generatePaths() {
        Map result = null
        try {
            ElementAndFullPathListAndMaxPathLength elementAndFullPathListAndMaxPathLength = buildElementPathsService.createListSortedByString(buildElementPathsService.buildAll())
            result = [list: elementAndFullPathListAndMaxPathLength.elementAndFullPathList,
                    maxPathLength: elementAndFullPathListAndMaxPathLength.maxPathLength]
        } catch (BuildElementPathsServiceLoopInPathException e) {
            result = [errorMessageKey: """A loop was found in one of the paths based on ElementHierarchy (for the relationship ${buildElementPathsService.relationshipType}).<br/>
        The path starting before the loop was detected is:  ${e.elementAndFullPath.toString()}<br/>
        Path element hierarchies: ${e.elementAndFullPath.path}<br/>
        The id of the element hierarchy where the loop was detected is:  ${e.nextTopElementHierarchy.id}"""]
        }

        return result
    }

    @Secured(["hasRole('ROLE_CURATOR')"])
    def update(ElementEditCommand elementEditCommand) {
        String errorMessage = null

        elementEditCommand.newPathString = elementEditCommand.newPathString?.trim()

        if (elementEditCommand.newPathString) {
            try {
                NewElementAndPath newElementAndPath = findElementsFromPathString(elementEditCommand.newPathString,
                        buildElementPathsService.pathDelimeter)

                newElementAndPath.elementHierarchyList = findElementHierarchyFromIds(elementEditCommand.elementHierarchyIdList)
                newElementAndPath.element = findElementFromId(elementEditCommand.elementId)

                modifyElementAndHierarchyService.modify(newElementAndPath)

            } catch (UnrecognizedElementLabelException e) {
                errorMessage = "This internal section of the path did not contain a recognizable element:  ${e.unrecognizedText}<br/>Whole path: ${elementEditCommand.newPathString}<br/>To edit an element label, edit it when it is the at the end of the path"
            } catch (EmptyPathSectionException e) {
                errorMessage = "The section of the path with this index (0-based) did not contain any text:  ${e.sectionIndex}<br/>Whole path:  ${elementEditCommand.newPathString}"
            } catch (EmptyPathException e) {
                errorMessage = "The path is empty of any element labels"
            } catch (UnrecognizedElementHierachyIdException e) {
                errorMessage = "internal data was corrupted - ElementHierarchy ID notrecognized - seek help"
            } catch (UnrecognizedElementIdException e) {
                errorMessage = "internal data was corrupted - Element ID not recognized - seek help"
            } catch (NewElementLabelMatchesExistingElementLabelException e) {
                errorMessage = "The new label provided for this element matches an existing element ID=${e.matchingElement.id} ${e.matchingElement.label}"
            } catch (InvalidElementPathStringException e) {
                errorMessage = "The path entered is invalid:  it must beging and end with the path delimeter ${buildElementPathsService.pathDelimeter}"
            } catch (ModifyElementAndHierarchyLoopInPathException e) {
                StringBuilder idBuilder = new StringBuilder().append(buildElementPathsService.pathDelimeter)
                StringBuilder labelBuilder = new StringBuilder(buildElementPathsService.pathDelimeter)
                for (Element element : e.pathWithLoop) {
                    idBuilder.append(element.id).append(buildElementPathsService.pathDelimeter)
                    labelBuilder.append(element.label).append(buildElementPathsService.pathDelimeter)
                }
                errorMessage = """A loop was found in the proposed path:<br>
original element id:  ${elementEditCommand.elementId}<br/>
entered text:  ${elementEditCommand.newPathString}<br/>
detected loop laels:  ${labelBuilder.toString()}<br/>
detected loop id's:${idBuilder.toString()}<br/>"""
            }
        } else {
            errorMessage = "There was no text passed in to parse to edit the element and element hierarchy"
        }

        if (null == errorMessage) {
            redirect(action: 'edit')
        } else {
            render(errorMessage)
        }
    }


    static Element findElementFromId(Long id) {
        Element result = Element.findById(id)

        if (!result) {
            throw new UnrecognizedElementIdException()
        }

        return result
    }

    static List<ElementHierarchy> findElementHierarchyFromIds(List<Long> elementHierarchyIdList) throws
            UnrecognizedElementHierachyIdException {

        List<ElementHierarchy> result = new ArrayList<ElementHierarchy>(elementHierarchyIdList.size())

        for (Long id : elementHierarchyIdList) {
            ElementHierarchy elementHierarchy = ElementHierarchy.findById(id)
            if (elementHierarchy) {
                result.add(elementHierarchy)
            } else {
                throw new UnrecognizedElementHierachyIdException()
            }
        }

        return result
    }


    static NewElementAndPath findElementsFromPathString(String pathString, String pathDelimeter) throws
            UnrecognizedElementLabelException, EmptyPathSectionException, InvalidElementPathStringException {

        List<String> tokenList = ElementAndFullPath.splitPathIntoTokens(pathString, pathDelimeter)
        if (tokenList.size() == 0) {
            throw new EmptyPathException()
        }

        NewElementAndPath result = new NewElementAndPath()

        for (int tokenIndex = 0; tokenIndex < tokenList.size() - 1; tokenIndex++) {
            String token = tokenList.get(tokenIndex)

            if (token != "") {
                Element foundElement = Element.findByLabel(token)
                if (foundElement) {
                    result.newPathElementList.add(foundElement)
                } else {
                    throw new UnrecognizedElementLabelException(unrecognizedText: token)
                }
            } else {
                throw new EmptyPathSectionException(sectionIndex: tokenIndex)
            }
        }

        result.newElementLabel = tokenList.get(tokenList.size() - 1)

        return result
    }
}
@InheritConstructors
@Validateable
class TermCommand extends BardCommand {
    BuildElementPathsService buildElementPathsService
    Long parentElementId
    String parentLabel
    String parentDescription
    String label
    String description
    String abbreviation
    String synonyms
    String curationNotes
    String relationship = "subClassOf"
    Element currentElement
    Boolean success = false

    /**
     * Copy errors from the current element into the Command object for display
     */
    void transferErrorsFromCurrentElement() {
        this.addToErrors(new ValidationErrors(currentElement))
    }

    static def validateLabelUniqueness(String label, TermCommand termCommand) {
        if (label) {
            Element currentElement = Element.findByLabel(label.replaceAll("\\s+", " ").trim())
            if (currentElement) {
                String path = termCommand.buildElementPathsService.buildSinglePath(currentElement)
                return ['unique', label, path]
            }
        }
    }

    static def validateParentLabelMustExist(String parentLabel, TermCommand termCommand) {
        if (parentLabel) {
            //this value must exist
            if (!Element.findByLabel(parentLabel.replaceAll("\\s+", " ").trim())) {
                return 'mustexist'
            }
        }
    }

    static constraints = {
        parentLabel(blank: false, nullable: false, maxSize: Element.LABEL_MAX_SIZE, validator: { value, command ->
            validateParentLabelMustExist(value, command)
        })
        label(blank: false, nullable: false, maxSize: Element.LABEL_MAX_SIZE, validator: { value, command ->
            validateLabelUniqueness(value, command)
        })
        parentDescription(blank: true, nullable: true, maxSize: Element.DESCRIPTION_MAX_SIZE)
        description(blank: false, nullable: false, maxSize: Element.DESCRIPTION_MAX_SIZE)
        curationNotes(blank: false, nullable: false, maxSize: Element.DESCRIPTION_MAX_SIZE)
        abbreviation(blank: true, nullable: true, maxSize: Element.ABBREVIATION_MAX_SIZE)
        synonyms(blank: true, nullable: true, maxSize: Element.SYNONYMS_MAX_SIZE)
    }
}

class NewElementAndPath {
    List<Element> newPathElementList

    String newElementLabel

    List<ElementHierarchy> elementHierarchyList

    Element element

    NewElementAndPath() {
        newPathElementList = new LinkedList<Element>()
    }
}

class ElementEditCommand {
    List<Long> elementHierarchyIdList

    Long elementId

    String newPathString

    ElementEditCommand() {
        elementHierarchyIdList = new LinkedList<Long>()
    }
}


class UnrecognizedElementLabelException extends Exception {
    String unrecognizedText
}

class EmptyPathSectionException extends Exception {
    int sectionIndex
}

class EmptyPathException extends Exception {}

class UnrecognizedElementHierachyIdException extends Exception {}

class UnrecognizedElementIdException extends Exception {}