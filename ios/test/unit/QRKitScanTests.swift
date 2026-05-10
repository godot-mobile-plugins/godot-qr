//
// © 2026-present https://github.com/cengiz-pz
//

import XCTest
@testable import qr_plugin

final class QRKitScanTests: XCTestCase {

	private var sut: QRKit!

	override func setUp() {
		super.setUp()
		sut = QRKit()
	}

	override func tearDown() {
		sut = nil
		super.tearDown()
	}

	// MARK: - Invalid input rejection

	func test_scanQR_emptyBuffer_returnsInvalidImage() {
		let imageInfo = ImageInfo(buffer: ImageFactory.emptyBuffer,
								width: 128, height: 128, format: 5, hasMipmaps: false)
		let result = sut.scanQR(imageInfo)
		XCTAssertEqual(result.code, .invalidImage,
					"An empty buffer must be rejected with .invalidImage")
	}

	func test_scanQR_zeroWidth_returnsInvalidImage() {
		let buffer = ImageFactory.solidColorRGBA8(width: 128, height: 128,
												r: 0xFF, g: 0xFF, b: 0xFF)
		let imageInfo = ImageInfo(buffer: buffer, width: 0, height: 128,
								format: 5, hasMipmaps: false)
		let result = sut.scanQR(imageInfo)
		XCTAssertEqual(result.code, .invalidImage)
	}

	func test_scanQR_zeroHeight_returnsInvalidImage() {
		let buffer = ImageFactory.solidColorRGBA8(width: 128, height: 128,
												r: 0xFF, g: 0xFF, b: 0xFF)
		let imageInfo = ImageInfo(buffer: buffer, width: 128, height: 0,
								format: 5, hasMipmaps: false)
		let result = sut.scanQR(imageInfo)
		XCTAssertEqual(result.code, .invalidImage)
	}

	func test_scanQR_negativeWidth_returnsInvalidImage() {
		let buffer = ImageFactory.solidColorRGBA8(width: 1, height: 1,
												r: 0x00, g: 0x00, b: 0x00)
		let imageInfo = ImageInfo(buffer: buffer, width: -1, height: 1,
								format: 5, hasMipmaps: false)
		let result = sut.scanQR(imageInfo)
		XCTAssertEqual(result.code, .invalidImage)
	}

	func test_scanQR_negativeHeight_returnsInvalidImage() {
		let buffer = ImageFactory.solidColorRGBA8(width: 1, height: 1,
												r: 0x00, g: 0x00, b: 0x00)
		let imageInfo = ImageInfo(buffer: buffer, width: 1, height: -1,
								format: 5, hasMipmaps: false)
		let result = sut.scanQR(imageInfo)
		XCTAssertEqual(result.code, .invalidImage)
	}

	func test_scanQR_invalidImage_descriptionIsNotEmpty() {
		let imageInfo = ImageInfo(buffer: ImageFactory.emptyBuffer,
								width: 0, height: 0, format: 5, hasMipmaps: false)
		let result = sut.scanQR(imageInfo)
		XCTAssertFalse(result.resultDescription.isEmpty,
					"A failed scan must always populate a human-readable description")
	}

	func test_scanQR_invalidImage_uriIsEmpty() {
		let imageInfo = ImageInfo(buffer: ImageFactory.emptyBuffer,
								width: 0, height: 0, format: 5, hasMipmaps: false)
		let result = sut.scanQR(imageInfo)
		XCTAssertTrue(result.uri.isEmpty,
					"URI must be empty when no QR code was decoded")
	}

	// MARK: - No QR code present

	func test_scanQR_solidWhiteImage_returnsNoCodeDetected() {
		// A plain white image contains no QR code; the detector should return .noCodeDetected,
		// not crash or return an unexpected code.
		let placeholder = ImageFactory.whitePlaceholder()
		let result = sut.scanQR(placeholder)

		// The detector may also report .invalidImage on very small images; accept both
		// non-success outcomes as valid "nothing found" states.
		let acceptableCodes: [Code] = [.noCodeDetected, .invalidImage]
		XCTAssertTrue(acceptableCodes.contains(result.code),
					"A blank image must not produce a success result. Got: \(result.code)")
	}

	func test_scanQR_solidColorImage_returnsNoQRCode() {
		// Solid red — definitely not a valid QR pattern.
		let buffer = ImageFactory.solidColorRGBA8(width: 256, height: 256,
												r: 0xFF, g: 0x00, b: 0x00)
		let imageInfo = ImageInfo(buffer: buffer, width: 256, height: 256,
								format: 5, hasMipmaps: false)
		let result = sut.scanQR(imageInfo)

		let acceptableCodes: [Code] = [.noCodeDetected, .invalidImage]
		XCTAssertTrue(acceptableCodes.contains(result.code))
	}

	// MARK: - Round-trip (generate → scan)

	func test_roundTrip_generatedQR_isDetectedWithCorrectURI() throws {
		// Generate a QR code through the production code path, then feed it straight
		// back into the scanner.  This is the most realistic integration test available
		// without a physical device or camera.
		let uri = Fixture.sampleURI

		let generated = try XCTUnwrap(
			ImageFactory.generatedQR(uri: uri),
			"QRKit.generateQR returned nil for '\(uri)'"
		)

		let result = sut.scanQR(generated)

		XCTAssertEqual(result.code, .success,
					"Round-trip scan must succeed. Got code \(result.code): \(result.resultDescription)")
		XCTAssertEqual(result.uri, uri,
					"Round-trip scan must return the original URI")
	}

	func test_roundTrip_numericURI_isDetected() throws {
		let uri = "12345"
		let generated = try XCTUnwrap(ImageFactory.generatedQR(uri: uri))
		let result = sut.scanQR(generated)

		XCTAssertEqual(result.code, .success)
		XCTAssertEqual(result.uri, uri)
	}

	func test_roundTrip_longURI_isDetected() throws {
		// QR codes support up to ~2953 bytes in binary mode; stay well inside that.
		let uri = "https://godotengine.org/asset-library/asset/12345?source=qrplugin&version=1.0.0"
		let generated = try XCTUnwrap(ImageFactory.generatedQR(uri: uri, size: Fixture.largeSize))
		let result = sut.scanQR(generated)

		XCTAssertEqual(result.code, .success)
		XCTAssertEqual(result.uri, uri)
	}

	func test_roundTrip_uriWithSpecialCharacters_isDetected() throws {
		let uri = "https://example.com/path?q=hello+world&lang=en"
		let generated = try XCTUnwrap(ImageFactory.generatedQR(uri: uri))
		let result = sut.scanQR(generated)

		XCTAssertEqual(result.code, .success)
		XCTAssertEqual(result.uri, uri)
	}

	func test_roundTrip_smallGeneratedSize_isStillDetectable() throws {
		// Use a reasonable minimum that CIDetector can resolve.
		let uri = Fixture.sampleURI
		let generated = try XCTUnwrap(
			ImageFactory.generatedQR(uri: uri, size: Fixture.smallSize))
		let result = sut.scanQR(generated)

		XCTAssertEqual(result.code, .success,
					"Even a \(Fixture.smallSize)px QR code should be detectable")
		XCTAssertEqual(result.uri, uri)
	}

	// MARK: - Success result properties

	func test_scanQR_success_uriIsPopulated() throws {
		let uri = Fixture.sampleURI
		let generated = try XCTUnwrap(ImageFactory.generatedQR(uri: uri))
		let result = sut.scanQR(generated)

		XCTAssertFalse(result.uri.isEmpty,
					"URI must not be empty on a successful scan")
	}

	func test_scanQR_success_descriptionIsNotEmpty() throws {
		let generated = try XCTUnwrap(ImageFactory.generatedQR(uri: Fixture.sampleURI))
		let result = sut.scanQR(generated)

		XCTAssertFalse(result.resultDescription.isEmpty,
					"resultDescription must always be set on a successful scan")
	}
}
