package molspreadsheet

import org.apache.log4j.Level
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: balexand
 * Date: 6/10/13
 * Time: 2:03 PM
 * To change this template use File | Settings | File Templates.
 */
class CompoundSummaryCategorizer {
    final LinkedHashMap<Long, SingleEidSummary> totalContents = [:]
    final LinkedHashMap<String, Integer> assayFormatMap = [:]
    final LinkedHashMap<String, Integer> assayTypeMap = [:]
    final LinkedHashMap<String, Integer> biologicalProcessMap = [:]
    final LinkedHashMap<String, Integer> proteinTargetMap = [:]


    static Logger log
    static {
        this.log = Logger.getLogger(RingManagerService.class)
        log.setLevel(Level.ERROR)
    }



    public CompoundSummaryCategorizer() {

    }




    /***
     * store a key in a map with a unique value as an index
     * @param arbitraryMap
     * @param arbitraryKey
     * @return the unique index we generated
     */
    int deriveArbitraryIndex (LinkedHashMap<String, Integer> arbitraryMap,String arbitraryKey) {
        int returnValue = 0
        if (arbitraryMap.size() == 0) {
            arbitraryMap[arbitraryKey]  =  returnValue
        } else {
            if (arbitraryMap.containsKey(arbitraryKey)) {
                returnValue =  arbitraryMap [arbitraryKey]
            }  else {
                returnValue = arbitraryMap.max {it.value}.value  + 1
                arbitraryMap [arbitraryKey] =  returnValue
            }
        }
        return returnValue
    }


    int deriveAssayFormatIndex (String assayFormat) {
        deriveArbitraryIndex (assayFormatMap,assayFormat)
    }


    int deriveAssayTypeIndex(String assayType) {
        deriveArbitraryIndex (assayTypeMap,assayType)
    }


    int deriveBiologicalProcessIndex (String biologicalProcess) {
        deriveArbitraryIndex (biologicalProcessMap,biologicalProcess)
    }


    int deriveProteinTargetIndex (String proteinTargets) {
        deriveArbitraryIndex (proteinTargetMap,proteinTargets)
    }

    public addNewRecord (long eid, String assayFormat, String assayType, List<Integer> unconvertedBiologyObjects ) {
        if (totalContents.keySet().contains(eid)) {
            log.warn("Error aggregating experiment data. Repeated experiment ID =: '${eid}'")
        }  else {
            totalContents[eid] = new SingleEidSummary( eid,  assayFormat,  assayType, unconvertedBiologyObjects)
        }

    }
    public addNewRecord (long eid, String assayFormat, String assayType ) {
        if (totalContents.keySet().contains(eid)) {
            log.warn("Error aggregating experiment data. Repeated experiment ID =: '${eid}'")
        }  else {
            totalContents[eid] = new SingleEidSummary( eid,  assayFormat,  assayType )
        }

    }


    public combineInNewProteinTargetValue (long eid, String proteinTarget ) {
        if (!totalContents.keySet().contains(eid)) {
            log.warn("Error expected experiment ID =: '${eid}'")
        }  else {
            SingleEidSummary singleEidSummary = totalContents[eid]
            int proteinTargetIndex = deriveProteinTargetIndex (proteinTarget)
            if (!singleEidSummary.proteinTargetsIndexList.contains(proteinTargetIndex))
                singleEidSummary.proteinTargetsIndexList <<  proteinTargetIndex
        }

    }


    public combineInNewBiologicalProcessValue (long eid, String biologicalProcess ) {
        if (!totalContents.keySet().contains(eid)) {
            log.warn("Error expected experiment ID =:  '${eid}'")
        }  else {
            SingleEidSummary singleEidSummary = totalContents[eid]
            int biologicalProcessIndex = deriveBiologicalProcessIndex (biologicalProcess)
            if (!singleEidSummary.biologicalProcessIndexList.contains(biologicalProcessIndex))
                singleEidSummary.biologicalProcessIndexList <<  biologicalProcessIndex
        }

    }




    public updateOutcome (long eid, int outcome, List <String> unconvertedBiologyHitIdList,List <String> unconvertedBiologyMissIdList) {
        if (!totalContents.keySet().contains(eid)) {
            log.warn("Programming error. Expected to find experiment ID = '${eid}' already stored.")
        }  else {
            SingleEidSummary singleEidSummary = totalContents[eid]
            singleEidSummary.setOutcome(outcome)
            // always one or the other -- is that right?
            for (String singleUnconvertedBiologyHitId in unconvertedBiologyHitIdList){
                singleEidSummary.getUnconvertedBiologyObjects() << Long.parseLong(singleUnconvertedBiologyHitId)
            }
            for (String singleUnconvertedBiologyMissId in unconvertedBiologyMissIdList){
                singleEidSummary.getUnconvertedBiologyObjects() << Long.parseLong(singleUnconvertedBiologyMissId)
            }

        }

    }


    public


    class SingleEidSummary {
        long eid = 0
        int assayFormatIndex = 0
        int assayTypeIndex = 0
        List<Integer> biologicalProcessIndexList = []
        List<Integer>  proteinTargetsIndexList = []
        List<Long> unconvertedBiologyObjects = []
        int outcome = 0


        public SingleEidSummary(long eid, String assayFormat, String assayType, List<Integer> unconvertedBiologyObjects) {
            this.eid =  eid
            this.assayFormatIndex = deriveAssayFormatIndex (assayFormat)
            this.assayTypeIndex = deriveAssayTypeIndex (assayType)
            this.unconvertedBiologyObjects = unconvertedBiologyObjects
        }


        public SingleEidSummary(long eid, String assayFormat, String assayType) {
            this.eid =  eid
            this.assayFormatIndex = deriveAssayFormatIndex (assayFormat)
            this.assayTypeIndex = deriveAssayTypeIndex (assayType)
        }

        public List<Long>  retrieveBiologyObjects (){
            unconvertedBiologyObjects
        }


    }

}
