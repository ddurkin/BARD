package scenarios

import pages.HomePage

/**
 * @author Muhammad.Rafique
 * Date Created: 2013/02/07
 */
class AssayContextAssayProtocolSpec extends AssayBaseContextSpec{
	@Override
	def setup() {
		section = "assay-protocol-header"
//		cardGroup = "cardHolderAssayProtocol"
		editContextGroup = "Assay-Protocol"
		dbContextType = "Assay Protocol"
		
		logInSomeUser()

	}

}