package bard.core.rest.spring

/**
 * Created with IntelliJ IDEA.
 * User: balexand
 * Date: 5/16/13
 * Time: 10:15 AM
 * To change this template use File | Settings | File Templates.
 */

import bard.core.rest.helper.RESTTestHelper
import bard.core.rest.spring.biology.BiologyEntity
import grails.converters.JSON
import grails.plugin.spock.IntegrationSpec
import spock.lang.Shared
import spock.lang.Unroll

/**
 * Tests for ProjectRestService
 */
@Mixin(RESTTestHelper)
@Unroll
class BiologyRestServiceIntegrationSpec extends IntegrationSpec {

    BiologyRestService biologyRestService


    void "test biologyRestService "() {
        given:
        String ncgcBaseURL = applicationContext.getBean("grailsApplication").config.ncgc.server.root.url
        def result = this.biologyRestService.getForObject("${ncgcBaseURL}/biology?top=10", String.class)
        def resultJSON = JSON.parse(result)
        List<Long> TEST_BIDS = [(resultJSON.collection[0] - '/biology/').toLong(), (resultJSON.collection[1] - '/biology/').toLong()]

        when: "generate activities directly via post"
        final List <BiologyEntity> biologyEntityList = biologyRestService.convertBiologyId(TEST_BIDS)

        then:
        assert biologyEntityList != null


    }
}