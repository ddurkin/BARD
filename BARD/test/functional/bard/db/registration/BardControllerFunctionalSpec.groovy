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

package bard.db.registration

import grails.plugin.remotecontrol.RemoteControl
import grails.util.BuildSettingsHolder
import org.apache.commons.lang.StringUtils
import spock.lang.Specification
import spock.lang.Unroll
import wslite.http.auth.HTTPBasicAuthorization
import wslite.rest.RESTClient

/**
 * Created with IntelliJ IDEA.
 * User: jasiedu
 * Date: 7/2/13
 * Time: 9:16 PM
 * To change this template use File | Settings | File Templates.
 *
 *  TEAM_A_1 and TEAM_A_2 belong to the same group
 *
 *  TEAM_B_1 belong to a different group
 *
 *  TEAM_A_1 also has ROLE_CURATOR
 *
 *
 *
 */
@Unroll
abstract class BardControllerFunctionalSpec extends Specification {


    static RemoteControl remote = new RemoteControl()

    static final String TEAM_A_1_USERNAME = remote { ctx.grailsApplication.config.CbipCrowd.mockUsers.teamA_1.username }
    static final String TEAM_A_1_PASSWORD = remote { ctx.grailsApplication.config.CbipCrowd.mockUsers.teamA_1.password }

    static final String TEAM_A_2_USERNAME = remote { ctx.grailsApplication.config.CbipCrowd.mockUsers.teamA_2.username }
    static final String TEAM_A_2_PASSWORD = remote { ctx.grailsApplication.config.CbipCrowd.mockUsers.teamA_2.password }

    static final String TEAM_B_1_USERNAME = remote { ctx.grailsApplication.config.CbipCrowd.mockUsers.teamB_1.username }
    static final String TEAM_B_1_PASSWORD = remote { ctx.grailsApplication.config.CbipCrowd.mockUsers.teamB_1.password }


    static final String ADMIN_USERNAME = remote { ctx.grailsApplication.config.CbipCrowd.mockUsers.integrationTestUser.username }
    static final String ADMIN_PASSWORD = remote { ctx.grailsApplication.config.CbipCrowd.mockUsers.integrationTestUser.password }
    static final String ADMIN_EMAIL = remote { ctx.grailsApplication.config.CbipCrowd.mockUsers.integrationTestUser.email }
    static final String ADMIN_ROLE = 'ROLE_BARD_ADMINISTRATOR'


    static final String CURATOR_USERNAME = remote { ctx.grailsApplication.config.CbipCrowd.mockUsers.curator.username }
    static final String CURATOR_PASSWORD = remote { ctx.grailsApplication.config.CbipCrowd.mockUsers.curator.password }


    static final String dburl = remote { ctx.grailsApplication.config.dataSource.url }


    static final String driverClassName = remote { ctx.grailsApplication.config.dataSource.driverClassName }

    static final String dbusername = remote { ctx.grailsApplication.config.dataSource.username }

    static final String dbpassword = remote { ctx.grailsApplication.config.dataSource.password }

    static String getBaseUrl() {
        return BuildSettingsHolder.settings?.functionalTestBaseUrl
    }

    static RESTClient getRestClient(String baseUrl, String action, String team, String teamPassword) {

        String url = baseUrl + action
        if (!action) {
            url = StringUtils.removeEnd(baseUrl, "/")
        }
        RESTClient client = new RESTClient(url)
        if (team && teamPassword) {
            client.authorization = new HTTPBasicAuthorization(team, teamPassword)
        }
        client.httpClient.sslTrustAllCerts = true
        client.httpClient.followRedirects = false
        return client
    }
}
