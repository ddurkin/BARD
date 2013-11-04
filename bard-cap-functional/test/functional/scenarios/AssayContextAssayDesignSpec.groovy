package scenarios

import pages.CapSearchPage
import pages.HomePage
import pages.ViewAssayDefinitionPage

import common.Constants.NavigateTo
import common.Constants.SearchBy

class AssayContextAssayDesignSpec extends AssayBaseContextSpec{
	@Override
	def setup() {
		section = "assay-design"
		cardGroup = "cardHolderAssayDesign"
		editContextGroup = "Assay-Design"
		dbContextGroup = "Assay Design"

		logInSomeUser()

		when: "User is at Home page, Navigating to Search Assay By Id page"
		at HomePage
		navigateTo(NavigateTo.ASSAY_BY_ID)

		then: "User is at Search Assay by Id page"
		at CapSearchPage

		when: "User is trying to search some Assay"
		at CapSearchPage
		capSearchBy(SearchBy.ASSAY_ID, testData.AssayId)

		then: "User is at View Assay Definition page"
		at ViewAssayDefinitionPage
	}

}