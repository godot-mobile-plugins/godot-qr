//
// © 2026-present https://github.com/cengiz-pz
//

import Foundation
import CoreImage
@testable import qr_plugin

// MARK: - Constants

enum Fixture {
	// A short, well-known URI used across generation and round-trip scan tests.
	static let sampleURI = "https://godotengine.org"

	// ARGB32 colour values matching Godot's Color.to_argb32() layout (0xAARRGGBB).
	static let black: Int = 0xFF_00_00_00
	static let white: Int = 0xFF_FF_FF_FF
	static let red:   Int = 0xFF_FF_00_00

	// A common square side used in generation tests.
	static let defaultSize = 256
	static let smallSize   = 64
	static let largeSize   = 512
}

// MARK: - Image builders

enum ImageFactory {

	/// Returns a raw RGBA8 `Data` blob (4 bytes per pixel) filled with a solid colour.
	/// Suitable for populating an `ImageInfo` whose buffer is fed to `scanQR`.
	///
	/// - Parameters:
	///   - width:  Image width in pixels.
	///   - height: Image height in pixels.
	///   - r/g/b/a: 0-255 channel values.
	static func solidColorRGBA8(
		width: Int,
		height: Int,
		r: UInt8, g: UInt8, b: UInt8, a: UInt8 = 0xFF
	) -> Data {
		let pixelCount = width * height
		var bytes = [UInt8](repeating: 0, count: pixelCount * 4)
		for i in 0 ..< pixelCount {
			bytes[i * 4 + 0] = r
			bytes[i * 4 + 1] = g
			bytes[i * 4 + 2] = b
			bytes[i * 4 + 3] = a
		}
		return Data(bytes)
	}

	/// Returns a zero-length `Data` (simulates a completely empty/missing buffer).
	static var emptyBuffer: Data { Data() }

	/// Builds an `ImageInfo` wrapping a solid white image — no QR code present.
	static func whitePlaceholder(width: Int = 128, height: Int = 128) -> ImageInfo {
		let buffer = solidColorRGBA8(width: width, height: height,
									r: 0xFF, g: 0xFF, b: 0xFF)
		// Format 5 = Image.FORMAT_RGBA8 in Godot 4
		return ImageInfo(buffer: buffer, width: width, height: height,
						format: 5, hasMipmaps: false)
	}

	/// Generates a real QR code `ImageInfo` via the production path.
	/// Returns `nil` only when `QRKit.generateQR` itself fails — which in turn
	/// will cause the calling test to fail via `XCTUnwrap`.
	static func generatedQR(
		uri: String = Fixture.sampleURI,
		size: Int = Fixture.defaultSize,
		foreground: Int = Fixture.black,
		background: Int = Fixture.white
	) -> ImageInfo? {
		QRKit().generateQR(uri: uri, size: size,
						foreground: foreground, background: background)
	}
}
