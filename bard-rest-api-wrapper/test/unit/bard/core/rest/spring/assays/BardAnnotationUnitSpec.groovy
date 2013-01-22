package bard.core.rest.spring.assays

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class BardAnnotationUnitSpec extends Specification {
    @Shared
    ObjectMapper objectMapper = new ObjectMapper()

    public static final String BARD_ANNOTATION = '''
{
   "contexts":[
      {
         "id":16715,
         "name":"species name",
         "comps":[
            {
               "entityId":null,
               "entity":"assay",
               "source":"cap-context",
               "id":16715,
               "display":"Homo sapiens",
               "contextRef":"species name",
               "key":"species name",
               "value":null,
               "extValueId":"9606",
               "url":"http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=9606",
               "displayOrder":0,
               "related":null
            }
         ]
      }
   ],
   "measures":[
      {
         "id":2761,
         "name":null,
         "comps":[
            {
               "entityId":null,
               "entity":"assay",
               "source":"cap-measure",
               "id":2761,
               "display":"Hill coefficient",
               "contextRef":null,
               "key":null,
               "value":"Hill coefficient",
               "extValueId":null,
               "url":null,
               "displayOrder":0,
               "related":""
            }
         ]
      }
   ],
   "docs":[
      {
         "id":28391,
         "name":"External URL",
         "comps":[
            {
               "entityId":null,
               "entity":"assay",
               "source":"cap-doc",
               "id":28391,
               "display":"Assay Link",
               "contextRef":"External URL",
               "key":"doc",
               "value":"http://ncgc.nih.gov/db/?aid=614",
               "extValueId":"http://ncgc.nih.gov/db/?aid=614",
               "url":null,
               "displayOrder":0,
               "related":null
            }
         ]
      }
   ]
}
    '''

    void "test serialization to AssayAnnotation"() {
        when:
        final BardAnnotation bardAnnotation = objectMapper.readValue(BARD_ANNOTATION, BardAnnotation.class)
        then:
        assert bardAnnotation.measures
        assert bardAnnotation.measures.size() == 1
        assert bardAnnotation.docs
        assert bardAnnotation.docs.size() == 1
        assert bardAnnotation.contexts
        assert bardAnnotation.contexts.size()==1


    }


}
