package base

import java.sql.Date;

import geb.spock.GebReportingSpec
import grails.util.Holders
import pages.HomePage
import pages.LoginPage
import spock.lang.Shared
/**
 * Created by GGTS.
 * User: mrafique
 * Date Created: 13/02/06
 * Date Updated: 13/02/13
 */
abstract class BardFunctionalSpec extends GebReportingSpec {
	@Shared protected Map<String, Map> usernameUserPropsMap = [:]
	def setupSpec() {
//		RemoteControl remote = new RemoteControl()
//		 String baseUrl = remote { ctx.grailsApplication.config.grails.serverURL }

		// def mockUsers = remote { ctx.grailsApplication.config.CbipCrowd.mockUsers }
//		def mockUsers = remote { ctx.grailsApplication.config.mockUsers }
		def mockUsers = Holders.config.mockUsers
		//Map userProps =[:]
		mockUsers.each {user ->
			Map userProps = user.value
			usernameUserPropsMap.put(userProps.username, userProps)
		}
		driver.manage().window().maximize()
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