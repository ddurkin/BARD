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

package bard.validation.ext

import edu.scripps.fl.entrez.EUtilsWeb
import grails.plugin.spock.IntegrationSpec
import spock.lang.Ignore
import spock.lang.Unroll
import uk.ac.ebi.kraken.uuw.services.remoting.RemoteDataAccessException

/**
 * Created with IntelliJ IDEA.
 * User: ddurkin
 * Date: 3/1/13
 * Time: 5:47 PM
 * To change this template use File | Settings | File Templates.
 */
@Unroll
class ExternalOntologyIntegrationSpec extends IntegrationSpec {

    String NCBI_EMAIL = ExternalOntologyNCBI.NCBI_EMAIL
    String NCBI_TOOL = ExternalOntologyNCBI.NCBI_TOOL

    ExternalOntologyFactory externalOntologyFactory

    void "test valid urls externalOntologyFactory.getExternalOntologyAPI() for #externalUrl"() {
        when:
        println("url: $externalUrl")
        Properties props = new Properties([(NCBI_TOOL): 'bard', (NCBI_EMAIL): 'test@test.com'])
        ExternalOntologyAPI externalOntologyAPI = externalOntologyFactory.getExternalOntologyAPI(externalUrl, props)

        then:
        (externalOntologyAPI != null) == notNull


        where:
        externalUrl                                                                                    | notNull
        "http://amigo.geneontology.org/cgi-bin/amigo/gp-details.cgi?gp=FB:FBgn"                        | true
        "http://amigo.geneontology.org/cgi-bin/amigo/term_details?term="                               | true
        "http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?sid="                                     | true
        "http://omim.org/entry/"                                                                       | true
        "http://www.ncbi.nlm.nih.gov/biosystems/"                                                      | true
        "http://www.ncbi.nlm.nih.gov/gene/"                                                            | true
        "http://www.ncbi.nlm.nih.gov/mesh/"                                                            | true
        "http://www.ncbi.nlm.nih.gov/nuccore/"                                                         | true
        "http://www.ncbi.nlm.nih.gov/omim/"                                                            | true
        "http://www.ncbi.nlm.nih.gov/protein/"                                                         | true
        "http://www.ncbi.nlm.nih.gov/pubmed/"                                                          | true
        "http://www.ncbi.nlm.nih.gov/structure/?term="                                                 | true
        "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id="                                  | true
        "http://www.uniprot.org/uniprot/"                                                              | true
        "http://www.atcc.org/ATCCAdvancedCatalogSearch/ProductDetails/tabid/452/Default.aspx?ATCCNum=" | true

        "http://cas.org/"                                                                              | false
        "https://mli.nih.gov/mli/?dl_id="                                                              | false
        "http://regid.org/find"                                                                        | false

        "http://someUnknown.com"                                                                       | false
    }

    void "test invalid urls new ConfigurableExternalOntologyFactory().getExternalOntologyAPI for #externalUrl"() {
        when:
        println("url: $externalUrl")
        Properties props = new Properties([(NCBI_TOOL): 'bard', (NCBI_EMAIL): 'test@test.com'])
        externalOntologyFactory.getExternalOntologyAPI(externalUrl, props)

        then:
        ExternalOntologyException e = thrown()
        e.message == message

        where:
        externalUrl                                 | message
        "http://www.ncbi.nlm.nih.gov/gquery/?term=" | 'Unknown NCBI database gquery'
    }

    void "test successful externalOntologyAPI.findById for url: #externalUrl externalValueId: #externalValueId"() {
        when:
        println("url: $externalUrl externalValueId: $externalValueId")
        Properties props = new Properties([(NCBI_TOOL): 'bard', (NCBI_EMAIL): 'test@test.com'])
        ExternalOntologyAPI extOntology = externalOntologyFactory.getExternalOntologyAPI(externalUrl, props)
        ExternalItem externalItem = extOntology.findById(externalValueId)

        then:
        println(externalItem)
        println(externalItem.display)
        println(externalItem.dump())
        externalItem.id == externalValueId
        externalItem.display

        where:
        externalUrl                                                      | externalValueId
        "http://www.ncbi.nlm.nih.gov/gene/"                              | "9986"
        "http://www.ncbi.nlm.nih.gov/protein/"                           | "9966877"
        "http://www.ncbi.nlm.nih.gov/mesh/"                              | "68020170"
        "http://omim.org/entry/"                                         | "602706"
        "http://www.ncbi.nlm.nih.gov/omim/"                              | "602706"
        "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id="    | "9986"
        "http://www.ncbi.nlm.nih.gov/nuccore/"                           | "91199539"
        "http://amigo.geneontology.org/cgi-bin/amigo/term_details?term=" | "GO:1901112"
        "http://www.uniprot.org/uniprot/"                                | "Q9Y6Q9"
    }

    void "test exception externalOntologyAPI.findById for url: #externalUrl externalValueId: '#externalValueId'"() {
        when:
        println("url: $externalUrl externalValueId: $externalValueId")
        Properties props = new Properties([(NCBI_TOOL): 'bard', (NCBI_EMAIL): 'test@test.com'])
        ExternalOntologyAPI extOntology = externalOntologyFactory.getExternalOntologyAPI(externalUrl, props)
        ExternalItem externalItem = extOntology.findById(externalValueId)

        then:
        notThrown()

        where:
        externalUrl                                                      | externalValueId | expectedException
        "http://www.ncbi.nlm.nih.gov/gene/"                              | null            | ExternalOntologyException
        "http://www.ncbi.nlm.nih.gov/gene/"                              | " "             | IndexOutOfBoundsException
        "http://www.ncbi.nlm.nih.gov/gene/"                              | ""              | ExternalOntologyException
        "http://amigo.geneontology.org/cgi-bin/amigo/term_details?term=" | null            | ExternalOntologyException
        "http://www.uniprot.org/uniprot/"                                | null            | ExternalOntologyException
        "http://www.uniprot.org/uniprot/"                                | " "             | RemoteDataAccessException
        "http://www.uniprot.org/uniprot/"                                | ""              | RemoteDataAccessException
    }

    void "test successful externalOntologyAPI.findMatching for url: #externalUrl term: #term"() {
        when:
        println("url: $externalUrl term: $term")
        Properties props = new Properties([(NCBI_TOOL): 'bard', (NCBI_EMAIL): 'test@test.com'])
        ExternalOntologyAPI extOntology = externalOntologyFactory.getExternalOntologyAPI(externalUrl, props)
        List<ExternalItem> externalItems = extOntology.findMatching(term)

        then:
        println("url: $externalUrl term: $term")
        println("count: ${externalItems.size()}")
        for (ExternalItem externalItem in externalItems) {
            println(externalItem.id)
            println(externalItem.display)
            println(externalItem.dump())
        }
        println("*******************************")
        externalItems

        where:
        externalUrl                                                      | term
        "http://omim.org/entry/"                                         | "PROTEASOME 26S SUBUNIT, ATPase, 1;"
        "http://www.ncbi.nlm.nih.gov/biosystems/"                        | "9986"
        "http://www.ncbi.nlm.nih.gov/gene/"                              | "9986"
        "http://www.ncbi.nlm.nih.gov/mesh/"                              | "68020170"
        "http://www.ncbi.nlm.nih.gov/protein/"                           | "9966877"
        "http://www.ncbi.nlm.nih.gov/omim/"                              | "PROTEASOME 26S SUBUNIT, ATPase, 1;"
        "http://www.ncbi.nlm.nih.gov/pubmed/"                            | "Distinct 3-O-Sulfated Heparan Sulfate"
        "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id="    | "Cellana capensis"
        "http://www.ncbi.nlm.nih.gov/nuccore/"                           | "91199539"
        "http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid="       | "9562061"
        "http://www.uniprot.org/uniprot/"                                | "Q9Y6Q9"
        "http://amigo.geneontology.org/cgi-bin/amigo/term_details?term=" | "apoptotic process"
    }

    void "test exception externalOntologyAPI.findMatching for url: #externalUrl term: '#term'"() {
        when:
        println("url: $externalUrl term: $term")
        Properties props = new Properties([(NCBI_TOOL): 'bard', (NCBI_EMAIL): 'test@test.com'])
        ExternalOntologyAPI extOntology = externalOntologyFactory.getExternalOntologyAPI(externalUrl, props)
        List<ExternalItem> externalItems = extOntology.findMatching(term)

        then:
        notThrown()

        where:
        externalUrl                                                      | term | expectedException
        "http://www.ncbi.nlm.nih.gov/gene/"                              | null | NullPointerException
        "http://www.ncbi.nlm.nih.gov/gene/"                              | " "  | ExternalOntologyException
        "http://www.ncbi.nlm.nih.gov/gene/"                              | ""   | ExternalOntologyException
        "http://amigo.geneontology.org/cgi-bin/amigo/term_details?term=" | null | ExternalOntologyException
        "http://www.uniprot.org/uniprot/"                                | null | RemoteDataAccessException
        "http://www.uniprot.org/uniprot/"                                | " "  | RemoteDataAccessException
        "http://www.uniprot.org/uniprot/"                                | ""   | RemoteDataAccessException
    }
	@Ignore
    void "test show NCBI Databases"() {
        when:
        EUtilsWeb web = new EUtilsWeb("BARD-CAP", "anonymous@bard.nih.gov");
        Set<String> databases = web.getDatabases()
        databases.each { println(it) }

        then:
        databases
    }
}
