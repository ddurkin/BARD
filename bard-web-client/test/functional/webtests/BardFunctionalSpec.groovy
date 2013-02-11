package webtests

import geb.spock.GebReportingSpec
import grails.plugin.remotecontrol.RemoteControl
import spock.lang.Shared
import webtests.pages.HomePage
import webtests.pages.LoginPage

/**
 * Created by IntelliJ IDEA.
 * User: jlev
 * Date: 10/21/11
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
abstract class BardFunctionalSpec extends GebReportingSpec {
    @Shared protected Map<String, Map> usernameUserPropsMap = [:]

    void setupSpec() {
        RemoteControl remote = new RemoteControl()
      //  String baseUrl = remote { ctx.grailsApplication.config.grails.serverURL }

        def mockUsers = remote { ctx.grailsApplication.config.CbipCrowd.mockUsers }
        //Map userProps =[:]
        mockUsers.each {user ->
            Map userProps = user.value
            usernameUserPropsMap.put(userProps.username, userProps)
        }
     }

    HomePage logInWithRole(String role) {
        Map.Entry<String, Map> userInfoMap = usernameUserPropsMap.find { k, v ->
            v.roles.contains(role)
        }

        return logInAsUser(userInfoMap.key)
    }

    HomePage logInAsUser(String username) {
        to LoginPage
        return logIn(username, usernameUserPropsMap.get(username).password)
    }

    HomePage logInSomeUser() {
        String username = getCredentialsForTest().username
        return logInAsUser(username)
    }

    Map<String, String> getCredentialsForTest() {
        return usernameUserPropsMap.find{it}.value    // returns the first entry
    }
}