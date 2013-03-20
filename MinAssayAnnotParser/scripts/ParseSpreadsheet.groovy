import bard.db.registration.AttributeType
import bard.dm.minimumassayannotation.ContextGroup
import bard.dm.minimumassayannotation.AssayContextGroupsBuilder
import bard.dm.minimumassayannotation.AttributeNameMappingBuilder
import bard.dm.minimumassayannotation.ParseAndBuildAttributeGroups
import bard.dm.minimumassayannotation.AttributesContentsCleaner
import bard.dm.minimumassayannotation.validateCreatePersist.AttributeContentAgainstElementTableValidator
import bard.dm.minimumassayannotation.validateCreatePersist.AssayContextsValidatorCreatorAndPersistor
import bard.dm.Log
import org.apache.log4j.Level
import bard.dm.minimumassayannotation.ContextLoadResultsWriter
import bard.dm.minimumassayannotation.CouldNotReadExcelFileException
import bard.dm.minimumassayannotation.AssayDto
import bard.dm.minimumassayannotation.AssayLoadResultsWriter
import maas.MustLoadAid
import maas.ExcelHandler
import maas.FileHashMap


final String baseModifiedBy = "xiaorong"
final String baseOutputPath = "data/maas/maasDataset1/output/"
Log.initializeLogger("${baseOutputPath}${baseModifiedBy}.log")
Log.logger.setLevel(Level.INFO)

final Date startDate = new Date()
Log.logger.info("Start load of minimum assay annotation spreadsheets ${startDate}")

final Integer START_ROW = 2 //0-based

List<File> inputFileList = new LinkedList<File>()

List<String> inputDirPathArray = ["data/maas/maasDataset1"]
ExcelHandler.constructInputFileList(inputDirPathArray, inputFileList)

Log.logger.info("loading ${inputFileList.size()} files found in ${inputDirPathArray.size()} directories")

println("build mapping of columns to attributes and values")
List<ContextGroup> spreadsheetAssayContextGroups = (new AssayContextGroupsBuilder()).build()

final String contextLoadResultFilePath = "${baseOutputPath}minAssayAnnotParseResults.csv"
final ContextLoadResultsWriter loadResultsWriter = new ContextLoadResultsWriter(contextLoadResultFilePath)
final AssayLoadResultsWriter assayLoadResultsWriter = new AssayLoadResultsWriter("${baseOutputPath}fileAndAssayStatsForMinAssayAnnotParse.csv")
final FileHashMap fileHashMap = new FileHashMap("${baseOutputPath}hash_file.csv")

final ParseAndBuildAttributeGroups parseAndBuildAttributeGroups = new ParseAndBuildAttributeGroups(loadResultsWriter, 130)
final Map attributeNameMapping = (new AttributeNameMappingBuilder()).build()
final AttributesContentsCleaner attributesContentsCleaner = new AttributesContentsCleaner(attributeNameMapping)
final AttributeContentAgainstElementTableValidator attributeContentAgainstElementTableValidator = new AttributeContentAgainstElementTableValidator(loadResultsWriter)
final AssayContextsValidatorCreatorAndPersistor assayContextsValidatorCreatorAndPersistor = new AssayContextsValidatorCreatorAndPersistor(baseModifiedBy, loadResultsWriter, false)

def mustLoadedAids = MustLoadAid.mustLoadedAids('data/maas/maasDataset1/aids_dataset_1.csv')

try {
    for (File inputFile : inputFileList) {
        final int fileHash = fileHashMap.addFile(inputFile)
        Log.logger.info("${new Date()} processing file ${inputFile.absolutePath} hashCode: $fileHash")

        try {
            List<AssayDto> assayDtoList = parseAndBuildAttributeGroups.build(inputFile, START_ROW, [spreadsheetAssayContextGroups])

            println("Clean loaded attributes")
            attributesContentsCleaner.clean(assayDtoList)

            for (AssayDto assayDto : assayDtoList) {
                if (assayDto.aid && mustLoadedAids.contains(assayDto.aid)) {
                    println("validate loaded attributes")
                    attributeContentAgainstElementTableValidator.removeInvalid(assayDto.assayContextDTOList, attributeNameMapping)

                    if (assayDto.assayContextDTOList.size() > 0) {
                       // final String currentModifiedBy = "${baseModifiedBy}_FH$fileHash"
                        final String currentModifiedBy = "${baseModifiedBy}_${inputFile.name}"
                        if (currentModifiedBy.length() >= 40){
                            currentModifiedBy = currentModifiedBy.substring(0,40)
                        }
                        assayContextsValidatorCreatorAndPersistor.modifiedBy = currentModifiedBy
                        if (assayContextsValidatorCreatorAndPersistor.createAndPersist(assayDto.assayContextDTOList)) {
                            assayLoadResultsWriter.write(assayDto, AssayLoadResultsWriter.LoadResultType.success,
                                    "successfully loaded assay context")
                        } else {
                            assayLoadResultsWriter.write(assayDto, AssayLoadResultsWriter.LoadResultType.fail,
                                    "failed to load assay contexts loaded assay context")
                        }
                    } else {
                        assayLoadResultsWriter.write(assayDto, AssayLoadResultsWriter.LoadResultType.nothingToLoad, null)
                    }
                } else {
                    assayLoadResultsWriter.write(assayDto, AssayLoadResultsWriter.LoadResultType.fail,
                            "aid found in cell not a number or not in must load list")
                }
            }
        } catch (CouldNotReadExcelFileException e) {
            final String message = "could not read excel file: ${inputFile.absolutePath} ${e.message}"
            Log.logger.error(message)
            loadResultsWriter.write(null, null, ContextLoadResultsWriter.LoadResultType.fail, null, 0, message)
        }
    }
} catch (Exception e) {
    e.printStackTrace()
    Log.logger.error(e.message)
} finally {
    loadResultsWriter.close()
    assayLoadResultsWriter.close()
    fileHashMap.close()

    final Date endDate = new Date()
    final double durationMin = (endDate.time - startDate.time) / 60000.0
    Log.logger.info("finished at ${endDate}   duration[min]: ${durationMin}")
    Log.close()
}

class FileHashMap {
    private final Map<File, Integer> fileHashCodeMap

    private final BufferedWriter writer

    FileHashMap(String outputFilename) {
        fileHashCodeMap = new HashMap<File, Integer>()

        writer = new BufferedWriter(new FileWriter(outputFilename))
        writer.writeLine("hash_code,file_path")
    }

    int addFile(File file) {
        final int hashCode = file.absolutePath.hashCode()

        if (!fileHashCodeMap.containsKey(file)) {
            fileHashCodeMap.put(file, hashCode)
            writer.writeLine("$hashCode,${file.absolutePath}")
        }

        return hashCode
    }

    void close() {
        writer.close()
    }
}
