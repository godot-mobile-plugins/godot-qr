//
// © 2026-present https://github.com/cengiz-pz
//

import Foundation

@objc enum Code: Int {
	case success = 0
	case invalidImage = 1
	case noCodeDetected = 2
	case scannerFailure = 3
	case internalError = 4
}

@objc public final class ScanResult: NSObject {
	@objc let code: Code
	@objc let uri: String
	@objc let resultDescription: String

	init(code: Code, uri: String, description: String) {
		self.code = code
		self.uri = uri
		self.resultDescription = description
		super.init()
	}
}
