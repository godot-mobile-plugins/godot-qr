//
// © 2026-present https://github.com/cengiz-pz
//

import Foundation
import CoreImage
import UIKit

@objc public class QR: NSObject {

	/// Generates a QR code and returns it as an ImageInfo object.
	/// Returns nil if generation fails.
	@objc public func generateQR(uri: String, size: Int, foreground: Int, background: Int) -> ImageInfo? {
		let data = uri.data(using: .utf8)

		// Generate QR CIImage
		guard let filter = CIFilter(name: "CIQRCodeGenerator") else { return nil }
		filter.setValue(data, forKey: "inputMessage")
		filter.setValue("M", forKey: "inputCorrectionLevel")

		guard let output = filter.outputImage else { return nil }

		// Colorize (False Color Filter)
		let fgColor = extractColor(from: foreground)
		let bgColor = extractColor(from: background)

		guard let colorFilter = CIFilter(name: "CIFalseColor") else { return nil }
		colorFilter.setValue(output, forKey: "inputImage")
		colorFilter.setValue(fgColor, forKey: "inputColor0")
		colorFilter.setValue(bgColor, forKey: "inputColor1")

		guard let colorOutput = colorFilter.outputImage else { return nil }

		// Scale to desired size
		// CIImage is coordinate-less until rendered, but has an extent.
		let scaleX = CGFloat(size) / colorOutput.extent.width
		let scaleY = CGFloat(size) / colorOutput.extent.height
		let transformedImage = colorOutput.transformed(by: CGAffineTransform(scaleX: scaleX, y: scaleY))

		// Render to Bitmap (RGBA8)
		let context = CIContext(options: [
			.workingColorSpace: CGColorSpace(name: CGColorSpace.sRGB)!,
			.outputColorSpace: CGColorSpace(name: CGColorSpace.sRGB)!,
			.useSoftwareRenderer: false
		])
		guard let cgImage = context.createCGImage(transformedImage, from: transformedImage.extent) else { return nil }

		let width = Int(cgImage.width)
		let height = Int(cgImage.height)
		let bytesPerPixel = 4
		let bytesPerRow = bytesPerPixel * width
		let rawData = UnsafeMutablePointer<UInt8>.allocate(capacity: height * bytesPerRow)

		let colorSpace = CGColorSpaceCreateDeviceRGB()
		guard let bitmapContext = CGContext(
			data: rawData,
			width: width,
			height: height,
			bitsPerComponent: 8,
			bytesPerRow: bytesPerRow,
			space: colorSpace,
			bitmapInfo:
				CGImageAlphaInfo.premultipliedLast.rawValue |
				CGBitmapInfo.byteOrder32Big.rawValue
		) else {
			rawData.deallocate()
			return nil
		}

		bitmapContext.draw(cgImage, in: CGRect(x: 0, y: 0, width: CGFloat(width), height: CGFloat(height)))

		let dataBuffer = Data(bytes: rawData, count: height * bytesPerRow)
		rawData.deallocate()

		// Format 5 corresponds to Image.FORMAT_RGBA8 in Godot 4
		return ImageInfo(buffer: dataBuffer, width: width, height: height, format: 5, hasMipmaps: false)
	}

	@objc public func scanQR(_ imageInfo: ImageInfo) -> ScanResult {
		let buffer = imageInfo.buffer
		let width = imageInfo.width
		let height = imageInfo.height

		// Basic input validation
		if buffer.isEmpty || width <= 0 || height <= 0 {
			return ScanResult(code: .INVALID_IMAGE, uri: "", description: "Invalid Image Data")
		}

		// Create CIImage from raw data in Godot's RGBA8 format (Format 5)
		let size = CGSize(width: width, height: height)
		let ciImage = CIImage(
			bitmapData: buffer,
			bytesPerRow: width * 4,
			size: size,
			format: .RGBA8,
			colorSpace: CGColorSpaceCreateDeviceRGB()
		)

		if ciImage.extent.isEmpty || ciImage.extent.width <= 0 || ciImage.extent.height <= 0 {
			return ScanResult(code: .INVALID_IMAGE, uri: "", description: "Invalid image dimensions after creating CIImage")
		}

		// Detect
		let detector = CIDetector(ofType: CIDetectorTypeQRCode, context: nil, options: [CIDetectorAccuracy: CIDetectorAccuracyHigh])
		let features = detector?.features(in: ciImage)

		if let qrFeature = features?.first as? CIQRCodeFeature, let message = qrFeature.messageString {
			return ScanResult(code: .SUCCESS, uri: message, description: "QR Code Detected")
		}

		return ScanResult(code: .NO_CODE_DETECTED, uri: "", description: "No QR code detected")
	}

	// ARGB --> CIColor extraction
	private func extractColor(from intColor: Int) -> CIColor {
		// Godot Color.to_argb32() = 0xAARRGGBB
		let a = CGFloat((intColor >> 24) & 0xFF) / 255.0
		let r = CGFloat((intColor >> 16) & 0xFF) / 255.0
		let g = CGFloat((intColor >> 8) & 0xFF) / 255.0
		let b = CGFloat(intColor & 0xFF) / 255.0

		let srgb = CGColorSpace(name: CGColorSpace.sRGB)!

		let cgColor = CGColor(
			colorSpace: srgb,
			components: [r, g, b, a]
		)!

		return CIColor(cgColor: cgColor)
	}
}
