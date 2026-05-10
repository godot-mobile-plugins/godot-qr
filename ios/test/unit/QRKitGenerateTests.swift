//
// © 2026-present https://github.com/cengiz-pz
//

import XCTest
@testable import qr_plugin

final class QRKitGenerateTests: XCTestCase {

	private var sut: QRKit!

	override func setUp() {
		super.setUp()
		sut = QRKit()
	}

	override func tearDown() {
		sut = nil
		super.tearDown()
	}

	// MARK: - Return value

	func test_generateQR_returnsNonNil_forValidURI() throws {
		let result = sut.generateQR(uri: Fixture.sampleURI,
									size: Fixture.defaultSize,
									foreground: Fixture.black,
									background: Fixture.white)
		XCTAssertNotNil(result, "generateQR should return a non-nil ImageInfo for a valid URI")
	}

	func test_generateQR_returnsNonNil_forShortString() throws {
		let result = sut.generateQR(uri: "A", size: Fixture.smallSize,
									foreground: Fixture.black, background: Fixture.white)
		XCTAssertNotNil(result)
	}

	func test_generateQR_returnsNonNil_forEmptyString() {
		// An empty input message is valid for CIQRCodeGenerator;
		// the plugin must not crash or return nil.
		let result = sut.generateQR(uri: "", size: Fixture.smallSize,
									foreground: Fixture.black, background: Fixture.white)
		XCTAssertNotNil(result)
	}

	// MARK: - Image dimensions

	func test_generateQR_imageWidth_equalsRequestedSize() throws {
		let size = Fixture.defaultSize
		let result = try XCTUnwrap(sut.generateQR(uri: Fixture.sampleURI, size: size,
												foreground: Fixture.black,
												background: Fixture.white))
		XCTAssertEqual(result.width, size,
					"Generated image width should match the requested size")
	}

	func test_generateQR_imageHeight_equalsRequestedSize() throws {
		let size = Fixture.defaultSize
		let result = try XCTUnwrap(sut.generateQR(uri: Fixture.sampleURI, size: size,
												foreground: Fixture.black,
												background: Fixture.white))
		XCTAssertEqual(result.height, size,
					"Generated image height should match the requested size")
	}

	func test_generateQR_imageIsSquare() throws {
		let result = try XCTUnwrap(sut.generateQR(uri: Fixture.sampleURI,
												size: Fixture.defaultSize,
												foreground: Fixture.black,
												background: Fixture.white))
		XCTAssertEqual(result.width, result.height, "QR image should always be square")
	}

	func test_generateQR_smallSize_hasCorrectDimensions() throws {
		let size = Fixture.smallSize
		let result = try XCTUnwrap(sut.generateQR(uri: Fixture.sampleURI, size: size,
												foreground: Fixture.black,
												background: Fixture.white))
		XCTAssertEqual(result.width, size)
		XCTAssertEqual(result.height, size)
	}

	func test_generateQR_largeSize_hasCorrectDimensions() throws {
		let size = Fixture.largeSize
		let result = try XCTUnwrap(sut.generateQR(uri: Fixture.sampleURI, size: size,
												foreground: Fixture.black,
												background: Fixture.white))
		XCTAssertEqual(result.width, size)
		XCTAssertEqual(result.height, size)
	}

	// MARK: - Image format

	func test_generateQR_formatIsRGBA8() throws {
		// Format 5 == Image.FORMAT_RGBA8 in Godot 4. This value is consumed by the
		// Obj-C wrapper and must not drift.
		let result = try XCTUnwrap(sut.generateQR(uri: Fixture.sampleURI,
												size: Fixture.defaultSize,
												foreground: Fixture.black,
												background: Fixture.white))
		XCTAssertEqual(result.format, 5, "Generated image format must be Godot FORMAT_RGBA8 (5)")
	}

	func test_generateQR_hasMipmapsFalse() throws {
		let result = try XCTUnwrap(sut.generateQR(uri: Fixture.sampleURI,
												size: Fixture.defaultSize,
												foreground: Fixture.black,
												background: Fixture.white))
		XCTAssertFalse(result.hasMipmaps, "QR images should not carry mipmap data")
	}

	// MARK: - Buffer integrity

	func test_generateQR_bufferIsNotEmpty() throws {
		let result = try XCTUnwrap(sut.generateQR(uri: Fixture.sampleURI,
												size: Fixture.defaultSize,
												foreground: Fixture.black,
												background: Fixture.white))
		XCTAssertFalse(result.buffer.isEmpty)
	}

	func test_generateQR_bufferSizeMatchesRGBA8Layout() throws {
		let size = Fixture.defaultSize
		let result = try XCTUnwrap(sut.generateQR(uri: Fixture.sampleURI, size: size,
												foreground: Fixture.black,
												background: Fixture.white))
		let expectedBytes = size * size * 4   // width × height × 4 bytes (RGBA8)
		XCTAssertEqual(result.buffer.count, expectedBytes,
					"Buffer size must equal width × height × 4 for RGBA8")
	}

	// MARK: - Colour influence

	func test_generateQR_differentColours_produceDifferentBuffers() throws {
		// Changing the foreground colour must produce visibly different pixel data.
		let blackOnWhite = try XCTUnwrap(
			sut.generateQR(uri: Fixture.sampleURI, size: Fixture.smallSize,
						foreground: Fixture.black, background: Fixture.white))

		let redOnWhite = try XCTUnwrap(
			sut.generateQR(uri: Fixture.sampleURI, size: Fixture.smallSize,
						foreground: Fixture.red, background: Fixture.white))

		XCTAssertNotEqual(blackOnWhite.buffer, redOnWhite.buffer,
						"Different foreground colours should produce different pixel buffers")
	}

	// MARK: - Determinism

	func test_generateQR_sameInputs_produceSameBufferLength() throws {
		// Two calls with identical parameters must yield buffers of the same byte count.
		// (Exact equality isn't guaranteed if CIContext introduces non-determinism,
		//  but dimensions and byte count must always be identical.)
		let first = try XCTUnwrap(sut.generateQR(uri: Fixture.sampleURI,
												size: Fixture.defaultSize,
												foreground: Fixture.black,
												background: Fixture.white))
		let second = try XCTUnwrap(sut.generateQR(uri: Fixture.sampleURI,
												size: Fixture.defaultSize,
												foreground: Fixture.black,
												background: Fixture.white))

		XCTAssertEqual(first.buffer.count, second.buffer.count)
		XCTAssertEqual(first.width,  second.width)
		XCTAssertEqual(first.height, second.height)
	}
}
