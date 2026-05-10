//
// © 2026-present https://github.com/cengiz-pz
//

import XCTest
@testable import qr_plugin

final class ImageInfoTests: XCTestCase {

	// MARK: - Property storage

	func test_init_storesBuffer() {
		let buffer = Data([0xDE, 0xAD, 0xBE, 0xEF])
		let sut = ImageInfo(buffer: buffer, width: 1, height: 1, format: 5, hasMipmaps: false)
		XCTAssertEqual(sut.buffer, buffer)
	}

	func test_init_storesWidth() {
		let sut = ImageInfo(buffer: Data(), width: 128, height: 64, format: 5, hasMipmaps: false)
		XCTAssertEqual(sut.width, 128)
	}

	func test_init_storesHeight() {
		let sut = ImageInfo(buffer: Data(), width: 128, height: 64, format: 5, hasMipmaps: false)
		XCTAssertEqual(sut.height, 64)
	}

	func test_init_storesFormat() {
		let sut = ImageInfo(buffer: Data(), width: 1, height: 1, format: 5, hasMipmaps: false)
		XCTAssertEqual(sut.format, 5)
	}

	func test_init_storesHasMipmapsFalse() {
		let sut = ImageInfo(buffer: Data(), width: 1, height: 1, format: 5, hasMipmaps: false)
		XCTAssertFalse(sut.hasMipmaps)
	}

	func test_init_storesHasMipmapsTrue() {
		let sut = ImageInfo(buffer: Data(), width: 1, height: 1, format: 5, hasMipmaps: true)
		XCTAssertTrue(sut.hasMipmaps)
	}

	// MARK: - Buffer size consistency

	func test_bufferSizeMatchesExpectedRGBA8Layout() {
		// A 4×4 RGBA8 image → 4 * 4 * 4 = 64 bytes
		let width  = 4
		let height = 4
		let expectedBytes = width * height * 4
		let buffer = Data(repeating: 0xFF, count: expectedBytes)

		let sut = ImageInfo(buffer: buffer, width: width, height: height,
							format: 5, hasMipmaps: false)
		XCTAssertEqual(sut.buffer.count, expectedBytes)
	}

	func test_emptyBufferIsAccepted() {
		// The model is a plain data container; it must not crash on empty input.
		let sut = ImageInfo(buffer: Data(), width: 0, height: 0, format: 5, hasMipmaps: false)
		XCTAssertTrue(sut.buffer.isEmpty)
	}

	// MARK: - Type identity

	func test_isNSObject_forObjCInterop() {
		// ImageInfo must be an NSObject subclass so it can be referenced from ObjC wrappers.
		let sut = ImageInfo(buffer: Data(), width: 1, height: 1, format: 5, hasMipmaps: false)
		XCTAssertTrue(sut is NSObject)
	}
}
