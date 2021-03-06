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

package adf

import org.openrdf.rio.RDFParser
import org.openrdf.rio.Rio
import org.openrdf.rio.RDFFormat

import org.openrdf.model.Model
import org.openrdf.rio.helpers.StatementCollector
import org.openrdf.model.vocabulary.RDFS
import org.openrdf.model.Resource

import bard.db.model.AbstractContextItem
import org.openrdf.model.Value
import bard.db.dictionary.Element
import org.openrdf.model.URI
import bard.db.registration.AssayContextItem
import bard.db.enums.ValueType
import org.openrdf.model.Literal
import bard.db.model.AbstractContext
import bard.db.registration.AssayContext
import bard.db.enums.ContextType
import org.openrdf.model.impl.TreeModel
import bard.db.registration.Assay
import bard.db.registration.AttributeType
import bard.db.people.Role
import bard.db.model.AbstractDocument
import bard.db.registration.AssayDocument
import bard.db.experiment.Experiment
import bard.db.experiment.ExperimentContext
import bard.db.project.ProjectContext
import bard.db.experiment.ExperimentContextItem
import bard.db.project.ProjectContextItem
import bard.db.experiment.ExperimentDocument
import bard.db.project.ProjectDocument
import bard.db.project.Project
import bard.db.enums.DocumentType
import bard.db.registration.ExternalReference
import bard.db.registration.ExternalSystem

class RdfToBardMetadataService extends AbstractService{
    Model createModel(String fileName) throws Exception {
        RDFParser rdfParser = Rio.createParser(RDFFormat.N3);
        Model m = new TreeModel();
        rdfParser.setRDFHandler(new StatementCollector(m));

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        rdfParser.parse(reader, "");
        print "model size: ${m.size()}";
        return m
    }

    List<Assay> handleAssay(Model m) {
        List<Assay> assays = []
        for (Resource r : m.filter(null, RDFS.CLASS, assayClass).subjects()){
            String assayName = m.filter(r, RDFS.LABEL, null).objectString()
            Assay assay = new Assay(assayName: assayName)
            List<AbstractContext> contexts = handleContext(m, r, assayClass)
            contexts.each{
                assay.addToAssayContexts(it)
            }
            assay.assayVersion = '1'
            assay.ownerRole = Role.findAll().get(0)
            assay.dateCreated = new Date()
            List<AbstractDocument> documents = handleDocument(m, r, assayClass)
            documents.each{
                assay.addToAssayDocuments(it)
            }
            assay.save(failOnError: true)
            assays.add(assay)
        }
        return assays
    }

    List<Experiment> handleExperiment(Model m, Assay assay) {
        List<Experiment> experiments = []
        for (Resource r : m.filter(null, RDFS.CLASS, experimentClass).subjects()){
            String experimentDescription = m.filter(r, RDFS.COMMENT, null).objectString()
            String experimentName = m.filter(r, RDFS.LABEL, null).objectString()
            Experiment experiment = new Experiment(description: experimentDescription, experimentName: experimentName)
            List<ExternalReference> references = handleReference(m, r, experimentClass)
            references.each{
                experiment.addToExternalReferences(it)
            }
            List<AbstractContext> contexts = handleContext(m, r, experimentClass)
            contexts.each{
                experiment.addToExperimentContexts(it)
            }
            List<AbstractDocument> documents = handleDocument(m, r, experimentClass)
            documents.each{
                experiment.addToExperimentDocuments(it)
            }

            experiment.assay = assay
            experiment.ownerRole = Role.findAll().get(0)
            experiment.save(failOnError: true)
            experiments.add(experiment)
        }
        return experiments
    }

    List<Project> handleProject(Model m) {
        List<Project> projects = []
        for (Resource r : m.filter(null, RDFS.CLASS, projectClass).subjects()){
            String description = m.filter(r, RDFS.COMMENT, null).objectString()
            String name = m.filter(r, RDFS.LABEL, null).objectString()
            Project project = new Project(description: description, name: name)
            List<ExternalReference> references = handleReference(m, r, projectClass)
            references.each{
                project.addToExternalReferences(it)
            }
            List<AbstractContext> contexts = handleContext(m, r, projectClass)
            contexts.each{
                project.addToContexts(it)
            }
            List<AbstractDocument> documents = handleDocument(m, r, projectClass)
            documents.each{
                project.addToDocuments(it)
            }

            project.ownerRole = Role.findAll().get(0)
            project.dateCreated = new Date()
            project.save(failOnError: true)
            projects.add(project)
        }
        return projects
    }

    List<AbstractDocument> handleDocument(Model m, Resource resource, URI classType) {
        List<AbstractDocument> documents = []
        for (Resource r : m.filter(resource, hasDocument, null).objects()){
            String documentLabel = m.filter(r, RDFS.LABEL, null).objectString()
            AbstractDocument document = createDocument(classType)
            document.documentName = documentLabel
            String documentContent = m.filter(r, hasContent, null).objectString()
            document.documentContent = documentContent
            String documentType = m.filter(r, hasDocumentType, null).objectString()
            document.documentType = DocumentType.byId(documentType)
            documents.add(document)
        }
        return documents
    }

    List<ExternalReference> handleReference(Model m, Resource resource, URI classType) {
        List<ExternalReference> references = []
        for (Resource r : m.filter(resource, identifiedBy, null).objects()){
            URI externalSystemURI = m.filter(r, hasExternalSystem, null).objectURI()
            String externalRef = m.filter(r, hasExternalRef, null).objectString()
            ExternalSystem es = ExternalSystem.findBySystemUrl(externalSystemURI.toString())
            assert es : "There is no external system found for URL ${externalSystemURI}"
            ExternalReference er = new ExternalReference(externalSystem: es, extAssayRef: externalRef)
            references.add(er)
        }
        return references
    }

    List<AbstractContext> handleContext(Model m, Resource resource, URI classType) {
        List<AbstractContext> contexts = []
        for (Resource r : m.filter(resource, hasContext, null).objects()){
            String contextLabel = m.filter(r, RDFS.LABEL, null).objectString()
            AbstractContext context = createContext(classType)
            context.contextName = contextLabel
            Value contextType = m.filter(r, hasContextType, null).objectValue()
            if (contextType instanceof URI) {
                assert contextType.namespace == adfOntology
                assert contextType.localName
                context.contextType = ContextType.byId(contextType.localName)
            }
            List<AbstractContextItem> items = handleContextItem(m, r, classType)
            items.each{AbstractContextItem item ->
                item.context = context
                context.addContextItem(item)
            }
            contexts.add(context)
        }
        return contexts
    }

    List<AbstractContextItem> handleContextItem (Model m, Resource context, URI classType) {
        List<AbstractContextItem> items = []
        for (Resource contextItem : m.filter(context, hasItem, null).objects()) {  // a set of contextItem
            Value value = m.filter(contextItem, hasAttribute, null).objectValue()
            assert value || value instanceof URI          //given a context item, it must have attribute and it must be a URI
            Element attribute = findElementForAttribute((URI) value)
            assert attribute
            AbstractContextItem item = createContextItem(classType)
            item.attributeElement = attribute
            if (item instanceof AssayContextItem) {
                String attributeType = m.filter(contextItem, hasAttributeType, null).objectString()
                ((AssayContextItem) item).attributeType = Enum.valueOf(AttributeType.class, attributeType)
            }

            String valueType = m.filter(contextItem, hasValueType, null).objectString()
            item.valueType = ValueType.byId(valueType)

            String valueDisplay =  m.filter(contextItem, hasValueDisplay, null).objectString()
            item.valueDisplay = valueDisplay

            Resource objectResource = m.filter(contextItem, hasValue, null).objectResource()
            if (objectResource instanceof  URI) {
                Element valueElement = findElementForValue(value)
                item.valueType = ValueType.ELEMENT
                item.valueElement = valueElement
            }

            String external_value_Id = m.filter(contextItem, hasExternalOntoId, null).objectString()
            if (external_value_Id)
                item.extValueId = external_value_Id

            String qualifier = m.filter(contextItem, hasQualifier, null).objectString()
            String numericValue = m.filter(contextItem, hasNumericValue, null).objectString()

            if (qualifier) {
                assert numericValue
                item.qualifier = qualifier
                item.valueNum = Float.valueOf(numericValue)
            }

            String max = m.filter(contextItem, hasMaxValue, null).objectString()
            String min = m.filter(contextItem, hasMinValue, null).objectString()

            if (max && min) {
                item.valueMax = Float.valueOf(max)
                item.valueMin = Float.valueOf(min)
            }

            items.add(item)
        }
        return items
    }

    AbstractContext createContext(URI classType) {
        if (classType == assayClass)
            return new AssayContext()
        else if (classType == experimentClass)
            return new ExperimentContext()
        else if (classType == projectClass)
            return new ProjectContext()
        return null
    }

    AbstractContextItem createContextItem(URI classType){
        if (classType == assayClass)
            return new AssayContextItem()
        else if (classType == experimentClass)
            return new ExperimentContextItem()
        else if (classType == projectClass)
            return new ProjectContextItem()
        return null
    }

    AbstractDocument createDocument(URI classType) {
        if (classType == assayClass)
            return new AssayDocument()
        else if (classType == experimentClass)
            return new ExperimentDocument()
        else if (classType == projectClass)
            return new ProjectDocument()
        return null
    }

    Element findElementForAttribute(URI uri) {
        Element element = Element.findByBardURI(uri.toString())
        if (!element) {
            assert uri.namespace == internalBardOntology
            element = Element.findById(uri.localName)
        }
        return element
    }


    Element findElementForValue(URI uri){
        Element element = Element.findByBardURI(uri.toString())
        if (!element) {
            element = Element.findById(uri.localName)
        }
        return element
    }
}
