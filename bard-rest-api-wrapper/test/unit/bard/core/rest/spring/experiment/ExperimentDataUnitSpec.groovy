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

package bard.core.rest.spring.experiment

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import bard.core.rest.spring.experiment.ExperimentData
import bard.core.rest.spring.experiment.Activity
import bard.core.rest.spring.experiment.Readout

@Unroll
class ExperimentDataUnitSpec extends Specification {
    @Shared
    ObjectMapper objectMapper = new ObjectMapper()

    final String EXPERIMENT_DATA_JSON = '''


    {
       "collection":
       [
           {
               "exptDataId": "197.859140",
               "eid": 519,
               "cid": 5389251,
               "sid": 859140,
               "bardExptId": 197,
               "runset": "default",
               "outcome": 2,
               "score": 78,
               "classification": 0,
               "potency": null,
               "readouts":
               [
                   {
                       "name": "INHIBITION",
                       "s0": 3.57,
                       "sInf": 100,
                       "hill": 1.35,
                       "ac50": 0.0000033999999999999996,
                       "cr":
                       [
                           [
                               1.02e-8,
                               11.67
                           ],
                           [
                               3.05e-8,
                               4.38
                           ],
                           [
                               9.15e-8,
                               3.57
                           ],
                           [
                               2.74e-7,
                               4.13
                           ],
                           [
                               8.230000000000001e-7,
                               12.16
                           ],
                           [
                               0.00000247,
                               39.47
                           ],
                           [
                               0.00000741,
                               72.93
                           ],
                           [
                               0.000022199999999999998,
                               93.92
                           ],
                           [
                               0.0000667,
                               100
                           ]
                       ],
                       "npoint": 9,
                       "concUnit": "m",
                       "responseUnit": null
                   }
               ],
               "resourcePath": "/exptdata/197.859140"
           },
           {
               "exptDataId": "197.863212",
               "eid": 519,
               "cid": 5389834,
               "sid": 863212,
               "bardExptId": 197,
               "runset": "default",
               "outcome": 2,
               "score": 67,
               "classification": 0,
               "potency": null,
               "readouts":
               [
                   {
                       "name": "INHIBITION",
                       "s0": 3.92,
                       "sInf": 94.26,
                       "hill": 0.93,
                       "ac50": 0.0000049999999999999996,
                       "cr":
                       [
                           [
                               1.02e-8,
                               20.22
                           ],
                           [
                               3.05e-8,
                               14.03
                           ],
                           [
                               9.15e-8,
                               12.22
                           ],
                           [
                               2.74e-7,
                               3.92
                           ],
                           [
                               8.230000000000001e-7,
                               11.83
                           ],
                           [
                               0.00000247,
                               34.33
                           ],
                           [
                               0.00000741,
                               57.48
                           ],
                           [
                               0.000022199999999999998,
                               80.79
                           ],
                           [
                               0.0000667,
                               94.26
                           ]
                       ],
                       "npoint": 9,
                       "concUnit": "m",
                       "responseUnit": null
                   }
               ],
               "resourcePath": "/exptdata/197.863212"
           }
       ],
       "link": null
    }
'''

    void setup() {

    }

    void tearDown() {
        // Tear down logic here
    }

    void "test experiment data"() {
        when:
        ExperimentData experimentData = objectMapper.readValue(EXPERIMENT_DATA_JSON, ExperimentData.class)
        then:
        assert experimentData
        assert !experimentData.link
        assert experimentData.activities
        Activity activity = experimentData.activities.get(0)

        assert activity
        assert activity.exptDataId == "197.859140"
        assert activity.eid == 519
        assert activity.cid == 5389251
        assert activity.sid==859140
        assert activity.bardExptId== 197
        assert activity.runset=="default"
        assert activity.outcome== 2
        assert activity.score== 78
        assert activity.classification==0
        assert activity.potency== null
        assert activity.resourcePath=="/exptdata/197.859140"

        assert activity.readouts
        Readout readOut = activity.readouts.get(0)
        assert readOut
        assert !readOut.responseUnit
        assert readOut.concentrationUnits
        assert readOut.numberOfPoints == 9
        assert readOut.name == "INHIBITION"
        assert readOut.s0 == 3.57
        assert readOut.sInf == 100
        assert readOut.coef == 1.35
        assert readOut.slope == 0.0000033999999999999996
        assert readOut.getCr().size() == 9
        assert readOut.getConcAsList().size() == 9
        assert readOut.getResponseAsList().size() == 9

    }

}

