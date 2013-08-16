package molspreadsheet

import bard.core.rest.spring.CompoundRestService
import bard.core.rest.spring.SunburstCacheService
import bard.core.rest.spring.compounds.CompoundSummary
import bard.core.rest.spring.compounds.TargetClassInfo
import grails.converters.JSON
import grails.plugin.spock.IntegrationSpec
import bard.core.rest.spring.util.RingNode
import groovy.json.JsonBuilder
import spock.lang.IgnoreRest

/**
 * Created with IntelliJ IDEA.
 * User: balexand
 * Date: 4/2/13
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */
class RingNodeIntegrationSpec  extends IntegrationSpec {
    SunburstCacheService sunburstCacheService
    RingManagerService ringManagerService
    CompoundRestService compoundRestService

    void "test generateAccessionIdentifiers"(){
        given:
        String ncgcBaseURL = applicationContext.getBean("grailsApplication").config.ncgc.server.root.url
        def result = this.compoundRestService.getForObject("${ncgcBaseURL}/biology?top=10", String.class)
        def resultJSON = JSON.parse(result)
        final List<Long> bids = [(resultJSON.collection[0] - '/biology/').toLong()]

        when:
        List<String> accessionIdentifiers  = ringManagerService.generateAccessionIdentifiers (bids, "hits", [:])

        then:
        accessionIdentifiers.size()  > 0
    }




    void "test convertCompoundIntoSunburst for CompoundSummaryCategorizer"() {
        when:
        LinkedHashMap<String,Object>  ringNodeAndCrossLinks   = ringManagerService.convertCompoundIntoSunburstById (2382353L , true, true )
        CompoundSummaryCategorizer compoundSummaryCategorizer  =  ringNodeAndCrossLinks ["CompoundSummaryCategorizer"]

        then:
        compoundSummaryCategorizer.toString().size() > 0
    }





    void "test convertBiologyIdsToAscensionNumbers"(){
        given:
        LinkedHashMap activeInactiveDataPriorToConversion = [:]
        LinkedHashMap activeInactiveDataAfterConversion
        String ncgcBaseURL = applicationContext.getBean("grailsApplication").config.ncgc.server.root.url
        def result = this.compoundRestService.getForObject("${ncgcBaseURL}/biology/types/protein?top=10", String.class)
        def resultJSON = JSON.parse(result)
        activeInactiveDataPriorToConversion["hits"] = [(resultJSON[0] - '/biology/').toLong()]
        activeInactiveDataPriorToConversion["misses"] = [(resultJSON[1] - '/biology/').toLong()]

        when:
        try {
            activeInactiveDataAfterConversion = ringManagerService.convertBiologyIdsToAscensionNumbers (activeInactiveDataPriorToConversion,[:])
        } catch (Exception e)  {
            e.printStackTrace()
        }


        then:
        activeInactiveDataAfterConversion["hits"].size ()   > 0
        activeInactiveDataAfterConversion["misses"].size ()   > 0
    }


    void "test getLinkedAnnotationData"(){
        given:
        final List<Long> aids = [25, 26, 27]

        when:
        LinkedHashMap<Long, LinkedHashMap <String,List<String>>> accumulatedAnnotationInformation = ringManagerService.getLinkedAnnotationData (aids)

        then:
        accumulatedAnnotationInformation
        accumulatedAnnotationInformation.keySet().size()==3
        accumulatedAnnotationInformation[25L]
        accumulatedAnnotationInformation[25L]["assay type".replaceAll(/\s/,"_")]
        accumulatedAnnotationInformation[25L]["assay format".replaceAll(/\s/,"_")]
        (!accumulatedAnnotationInformation[25L]["GO biological process term".replaceAll(/\s/,"_")])
        accumulatedAnnotationInformation[26L]["GO biological process term".replaceAll(/\s/,"_")]
        accumulatedAnnotationInformation[26L].keySet().size()==3
        accumulatedAnnotationInformation[27L].keySet().size()==3
    }



    void "test combineLinkedAnnotationDataWithTargetInformation"(){
        given:
        final List<Long> aids = [26]

        when:
        LinkedHashMap<Long, LinkedHashMap <String,List<String>>> accumulatedAnnotationInformation = ringManagerService.getLinkedAnnotationData (aids)
        ringManagerService.combineLinkedAnnotationDataWithTargetInformation ( accumulatedAnnotationInformation, null )

        then:
        accumulatedAnnotationInformation.getClass().name
    }

    void "test convertDataIntoJson"(){
        given:
        final List<Long> aids = [25,26,27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 32, 43, 44, 45]

        when:
        LinkedHashMap<Long, LinkedHashMap <String,List<String>>> accumulatedAnnotationInformation = ringManagerService.getLinkedAnnotationData (aids)
        ArrayList<Map> superSimpleList = []
        for (Long aid in accumulatedAnnotationInformation.keySet())   {
            LinkedHashMap <String,List<String>> value =  accumulatedAnnotationInformation [aid]
            LinkedHashMap <String,String> elementsOfAnAssay = [:]
            for (String elementKey in value.keySet()) {
                List<String> elementValue = value [elementKey]
                if  (elementValue.size()==0) {
                    elementsOfAnAssay [elementKey]  = 'none'
                }  else {
                    elementsOfAnAssay [elementKey]  = elementValue [0]
                }
            }
            Map simplifiedAnnotationInformation = [:]
            simplifiedAnnotationInformation ['assayId'] = aid.toString()
            simplifiedAnnotationInformation ['data'] = elementsOfAnAssay
            superSimpleList << simplifiedAnnotationInformation
        }

        String jsonString = ringManagerService.convertDataIntoJson ( accumulatedAnnotationInformation )
        JsonBuilder jsonBuilder = new  JsonBuilder( superSimpleList as List )
        String superSimpleJsonString = jsonBuilder.toPrettyString()

        then:
        jsonString
        superSimpleJsonString

    }







    void "test sunburst machinery with the target that has no Panther classes"(){
        given:
        final List<String> targets = ["P03230","Q92698","Q9Y6A5","P69722","P69721","P69723","P10520"]
        LinkedHashMap<String, Integer> accumulatedTargets = ringManagerService.accumulateAccessionNumbers( targets )

        when:
        List<List<TargetClassInfo>> accumulatedMaps = []
        accumulatedTargets.each{k,v->
            List<String> hierarchyDescription = sunburstCacheService.getTargetClassInfo(k)
            if (hierarchyDescription != null){
                accumulatedMaps<<sunburstCacheService.getTargetClassInfo(k)
            }
        }

        then:
        accumulatedMaps.size() == targets.size()
        accumulatedMaps.flatten().size() == 0
    }


    void "test sunburst machinery with targets that all have associated Panther classes"(){
        given:
        final List<String> targets = ["Q3TKT4","P49615","Q9H3R0","P37840","P21399",
                "P55789","Q99814","P27540","Q16665","P38532","O75030","Q14145","Q16236","Q04206","Q61009"]
        LinkedHashMap<String, Integer> accumulatedTargets = ringManagerService.accumulateAccessionNumbers( targets )

        when:
        List<List<TargetClassInfo>> accumulatedMaps = []
        accumulatedTargets.each{ k,v ->
            List<String> hierarchyDescription = sunburstCacheService.getTargetClassInfo(k)
            if (hierarchyDescription != null){
                accumulatedMaps<<sunburstCacheService.getTargetClassInfo(k)
            }
        }

        then:
        accumulatedMaps.size() == targets.size()
        accumulatedMaps.get(0).size() > 0
        accumulatedMaps.flatten().size() > 0
    }




    void "test building a tree from a few targets withou actives/inactives to consider"(){
        given:
        final List<String> targets = ["Q14145","Q16236","Q61009"]
        LinkedHashMap<String, Integer> accumulatedTargets = ringManagerService.accumulateAccessionNumbers( targets )

        when:
        List<List<TargetClassInfo>> accumulatedMaps = []
        accumulatedTargets.each{ k,v ->
            List<String> hierarchyDescription = sunburstCacheService.getTargetClassInfo(k)
            if (hierarchyDescription != null){
                accumulatedMaps<<sunburstCacheService.getTargetClassInfo(k)
            }
        }
        RingNode root = ringManagerService.ringNodeFactory( accumulatedMaps.flatten() as List<TargetClassInfo> )
        then:
        root.toString().contains("nucleic acid binding")
        root.toString().find(/nucleic acid[^\n]+/).find(/size\":\d/).find(/\d/) == '2'
        root.toString().find(/hydrolase[^\n]+/).find(/children/) == 'children'
        root.toString().find(/receptor[^\n]+/).find(/size\":\d/).find(/\d/) == '1'
    }





    void "test writeRingTree"(){
        given:
        final List<String> targets = ["Q14145","Q16236","Q61009"]
        LinkedHashMap<String, Integer> accumulatedTargets = ringManagerService.accumulateAccessionNumbers( targets )

        when:
        List<List<TargetClassInfo>> accumulatedMaps = []
        accumulatedTargets.each{ k,v ->
            List<String> hierarchyDescription = sunburstCacheService.getTargetClassInfo(k)
            if (hierarchyDescription != null){
                accumulatedMaps<<sunburstCacheService.getTargetClassInfo(k)
            }
        }
        RingNode root = ringManagerService.ringNodeFactory( accumulatedMaps.flatten() as List<TargetClassInfo> )
        String noTextTree = ringManagerService.writeRingTree(root, false)
        String fullTextTree = ringManagerService.writeRingTree(root, true)

        then:
        fullTextTree.length() > noTextTree.length()

        !(noTextTree =~ /nucleic acid binding/)
        fullTextTree =~ /nucleic acid binding/

        noTextTree =~ /children/
        fullTextTree =~ /children/

    }




    void "test colorMapping method"(){
        given:
        String minimumValue = "0.0"
        String maximumValue = "1.0"

        when:
        String colorMapping = ringManagerService.colorMappingOnPage(  minimumValue,  maximumValue)

        then:
        colorMapping =~ /continuousColorScale/
        colorMapping =~ /domain/
        colorMapping =~ /interpolate/
        colorMapping =~ /range/
    }





    void "test placeSunburstOnPage method"(){
        when:
        String sunburstOnPage = ringManagerService.placeSunburstOnPage(1024,768,2382353L)

        then:
        sunburstOnPage =~ /sunburstdiv/
        sunburstOnPage =~ /createASunburst/
        sunburstOnPage =~ /1024/
        sunburstOnPage =~ /768/
        sunburstOnPage =~ /2382353/
    }




    void "test building a tree from a few targets but with actives/inactives to consider"(){
        given:
        final List<String> targets = ["Q14145","Q16236","Q61009"]
        LinkedHashMap<String, Integer> accumulatedTargets = ringManagerService.accumulateAccessionNumbers( targets )

        when:
        List<List<TargetClassInfo>> accumulatedMaps = []
        accumulatedTargets.each{ k,v ->
            List<String> hierarchyDescription = sunburstCacheService.getTargetClassInfo(k)
            if (hierarchyDescription != null){
                accumulatedMaps<<sunburstCacheService.getTargetClassInfo(k)
            }
        }
        RingNode root = ringManagerService.ringNodeFactory( accumulatedMaps.flatten() as List<TargetClassInfo>,
                ['hits':["Q14145","Q16236"],'misses':["Q61009"]])
        then:
        root.toString().contains("nucleic acid binding")
        root.toString().find(/nucleic acid[^\n]+/).find(/size\":\d/).find(/\d/) == '2'
        root.toString().find(/hydrolase[^\n]+/).find(/children/) == 'children'
        root.toString().find(/receptor[^\n]+/).find(/size\":\d/).find(/\d/) == '1'
    }





    /**
     * As of version 17 of the NCGC API we have real data
     */
    void "test working with Current compound summary information"(){
        given:
        CompoundSummary compoundSummary = compoundRestService.getSummaryForCompound(2382353L)

        when:
        LinkedHashMap activeInactiveData = ringManagerService.retrieveActiveInactiveDataFromCompound(compoundSummary)

        then:
        activeInactiveData ["hits"].size ()    > 0
        activeInactiveData ["misses"].size ()   > 0
    }

    /**
     * For now use dummy routine to pull back real data from v12 of the API
     */
    void "test class data exists for this compound where the answer is false"(){
        given:
        CompoundSummary compoundSummary = compoundRestService.getSummaryForCompound(2382353L)

        when:
        Boolean classDataExists = ringManagerService.classDataExistsForThisCompound(compoundSummary)

        then:
        classDataExists == true
    }



    void "test convertCompoundIntoSunburst"() {
        when:
        LinkedHashMap<String,Object>  ringNodeAndCrossLinks   = ringManagerService.convertCompoundIntoSunburstById (2382353L , includeHits, includeNonHits )
        RingNode ringNode  =  ringNodeAndCrossLinks ["RingNode"]

        then:
        ringNode.toString().size() > 0

        where:
        includeHits     |    includeNonHits
        true            |       false
        false           |       true
        true            |       true
        false           |       false

    }



}