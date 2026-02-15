//
// © 2026-present https://github.com/cengiz-pz
//

import Foundation

@objc public final class ImageInfo: NSObject {
	@objc let buffer: Data
	@objc let width: Int
	@objc let height: Int
	@objc let format: Int
	@objc let hasMipmaps: Bool

	@objc init(buffer: Data, width: Int, height: Int, format: Int, hasMipmaps: Bool) {
		self.buffer = buffer
		self.width = width
		self.height = height
		self.format = format
		self.hasMipmaps = hasMipmaps
		super.init()
	}
}
