import dataexport.registration.MediaTypesDTO
import dataexport.experiment.ResultExportService
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

// Place your Spring DSL code here
beans = {
    final MediaTypesDTO mediaTypesDTO = new MediaTypesDTO()
    mediaTypesDTO.elementMediaType = grailsApplication.config.bard.data.export.dictionary.element.xml
    mediaTypesDTO.stageMediaType = grailsApplication.config.bard.data.export.dictionary.stage.xml
    mediaTypesDTO.resultTypeMediaType = grailsApplication.config.bard.data.export.dictionary.resultType.xml
    mediaTypesDTO.assayMediaType = grailsApplication.config.bard.data.export.assay.xml
    mediaTypesDTO.assayDocMediaType = grailsApplication.config.bard.data.export.assay.doc.xml
    mediaTypesDTO.dictionaryMediaType = grailsApplication.config.bard.data.export.dictionary.xml
    mediaTypesDTO.assaysMediaType = grailsApplication.config.bard.data.export.assays.xml
    mediaTypesDTO.projectMediaType = grailsApplication.config.bard.data.export.project.xml
    mediaTypesDTO.projectsMediaType = grailsApplication.config.bard.data.export.projects.xml
    mediaTypesDTO.experimentsMediaType = grailsApplication.config.bard.data.export.experiments.xml
    mediaTypesDTO.experimentMediaType = grailsApplication.config.bard.data.export.experiment.xml
    mediaTypesDTO.resultsMediaType = grailsApplication.config.bard.data.export.results.xml
    mediaTypesDTO.resultMediaType = grailsApplication.config.bard.data.export.result.xml

    final int maxExperimentsRecordsPerPage = grailsApplication.config.bard.experiments.max.per.page

    resultExportService(dataexport.experiment.ResultExportService) {
        maxResultsRecordsPerPage = maxExperimentsRecordsPerPage
        mediaTypes = mediaTypesDTO
        grailsLinkGenerator = ref('grailsLinkGenerator')
    }

    experimentExportService(dataexport.experiment.ExperimentExportService) {
        numberRecordsPerPage = maxExperimentsRecordsPerPage
        mediaTypeDTO = mediaTypesDTO
        grailsLinkGenerator = ref('grailsLinkGenerator')
        resultExportService= ref('resultExportService')
    }
    //inject element mime type here
    dictionaryExportHelperService(dataexport.dictionary.DictionaryExportHelperService, mediaTypesDTO) {
        grailsLinkGenerator = ref('grailsLinkGenerator')
    }

    rootService(dataexport.util.RootService, mediaTypesDTO) {
        grailsLinkGenerator = ref('grailsLinkGenerator')
    }


    assayExportHelperService(dataexport.registration.AssayExportHelperService, mediaTypesDTO) {
        grailsLinkGenerator = ref('grailsLinkGenerator')
    }

    projectExportService(dataexport.experiment.ProjectExportService, mediaTypesDTO) {
        grailsLinkGenerator = ref('grailsLinkGenerator')
    }
}
