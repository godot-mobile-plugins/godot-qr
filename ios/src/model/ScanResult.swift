//
// © 2026-present https://github.com/cengiz-pz
//

import Foundation

@objc enum Code: Int {
	case SUCCESS = 0
	case INVALID_IMAGE = 1
	case NO_CODE_DETECTED = 2
	case SCANNER_FAILURE = 3
	case INTERNAL_ERROR = 4
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
