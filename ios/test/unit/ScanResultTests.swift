//
// © 2026-present https://github.com/cengiz-pz
//

import XCTest
@testable import qr_plugin

final class ScanResultTests: XCTestCase {

	// MARK: - Code enum raw values
	// These raw values are serialised into the Godot Dictionary that the plugin
	// emits via the qr_scan_failed signal. Any change breaks GDScript consumers.

	func test_code_success_rawValue() {
		XCTAssertEqual(Code.success.rawValue, 0)
	}

	func test_code_invalidImage_rawValue() {
		XCTAssertEqual(Code.invalidImage.rawValue, 1)
	}

	func test_code_noCodeDetected_rawValue() {
		XCTAssertEqual(Code.noCodeDetected.rawValue, 2)
	}

	func test_code_scannerFailure_rawValue() {
		XCTAssertEqual(Code.scannerFailure.rawValue, 3)
	}

	func test_code_internalError_rawValue() {
		XCTAssertEqual(Code.internalError.rawValue, 4)
	}

	// MARK: - Property storage

	func test_init_storesCode() {
		let sut = ScanResult(code: .success, uri: "https://example.com", description: "ok")
		XCTAssertEqual(sut.code, .success)
	}

	func test_init_storesURI() {
		let uri = "https://godotengine.org"
		let sut = ScanResult(code: .success, uri: uri, description: "ok")
		XCTAssertEqual(sut.uri, uri)
	}

	func test_init_storesDescription() {
		let desc = "QR Code Detected"
		let sut = ScanResult(code: .success, uri: "", description: desc)
		XCTAssertEqual(sut.resultDescription, desc)
	}

	func test_emptyURIForFailureCode() {
		let sut = ScanResult(code: .noCodeDetected, uri: "", description: "No QR code detected")
		XCTAssertTrue(sut.uri.isEmpty)
	}

	// MARK: - Type identity

	func test_isNSObject_forObjCInterop() {
		let sut = ScanResult(code: .success, uri: "", description: "")
		XCTAssertTrue(sut is NSObject)
	}
}
