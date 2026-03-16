//
// © 2026-present https://github.com/cengiz-pz
//

package org.godotengine.plugin.qr;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import org.godotengine.godot.Godot;
import org.godotengine.godot.Dictionary;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import org.godotengine.plugin.qr.model.ImageInfo;
import org.godotengine.plugin.qr.model.ScanError;
import static org.godotengine.plugin.qr.model.ScanError.Code;


public class QRPlugin extends GodotPlugin {
	public static final String CLASS_NAME = QRPlugin.class.getSimpleName();
	static final String LOG_TAG = "godot::" + CLASS_NAME;


	static final String QR_DETECTED_SIGNAL = "qr_detected";
	static final String QR_SCAN_FAILED_SIGNAL = "qr_scan_failed";

	public QRPlugin(Godot godot) {
		super(godot);
	}

	@Override
	public String getPluginName() {
		return CLASS_NAME;
	}

	@Override
	public Set<SignalInfo> getPluginSignals() {
		Set<SignalInfo> signals = new HashSet<>();
		signals.add(new SignalInfo(QR_DETECTED_SIGNAL, String.class));
		signals.add(new SignalInfo(QR_SCAN_FAILED_SIGNAL, Dictionary.class));
		return signals;
	}

	@Override
	public View onMainCreate(Activity activity) {
		// Godot activity is ready

		return super.onMainCreate(activity);
	}

	@Override
	public void onGodotSetupCompleted() {
		super.onGodotSetupCompleted();

		// Godot is ready
	}

	@UsedByGodot
	public Dictionary generate_qr(String aUri, int aSize, int aForeground, int aBackground) {
		Log.d(LOG_TAG, "generate_qr() invoked with URI: " + aUri);

		ImageInfo result = new ImageInfo();

		try {
			// Force UTF-8 so MLKit (and all scanners) decode non-ASCII characters correctly.
			// Without this hint ZXing defaults to ISO-8859-1, which produces byte sequences
			// that MLKit's getRawValue() cannot decode as UTF-8 and therefore returns null.
			Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
			hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

			// Create the BitMatrix for the QR Code
			BitMatrix bitMatrix = new MultiFormatWriter().encode(
					aUri,
					BarcodeFormat.QR_CODE,
					aSize,
					aSize,
					hints
			);

			int width = bitMatrix.getWidth();
			int height = bitMatrix.getHeight();

			// Prepare the pixel buffer (RGBA8888) -- 4 bytes per pixel (Red, Green, Blue, Alpha)
			byte[] pixels = new byte[width * height * 4];
			int offset = 0;

			// Extract ARGB components from the int, which is packed as ARGB
			byte fgAlpha = (byte) Color.alpha(aForeground);
			byte fgRed = (byte) Color.red(aForeground);
			byte fgGreen = (byte) Color.green(aForeground);
			byte fgBlue = (byte) Color.blue(aForeground);

			byte bgAlpha = (byte) Color.alpha(aBackground);
			byte bgRed = (byte) Color.red(aBackground);
			byte bgGreen = (byte) Color.green(aBackground);
			byte bgBlue = (byte) Color.blue(aBackground);

			// Iterate through the matrix and set pixel colors
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					// Store as RGBA for Godot
					if (bitMatrix.get(x, y)) {
						pixels[offset] = fgRed;
						pixels[offset + 1] = fgGreen;
						pixels[offset + 2] = fgBlue;
						pixels[offset + 3] = fgAlpha;
					} else {
						pixels[offset] = bgRed;
						pixels[offset + 1] = bgGreen;
						pixels[offset + 2] = bgBlue;
						pixels[offset + 3] = bgAlpha;
					}
					offset += 4;
				}
			}

			result.setWidth(width);
			result.setHeight(height);
			result.setBuffer(pixels);
		} catch (Exception e) {
			Log.e(LOG_TAG, "QR Generation failed", e);
		}

		return result.getRawData();
	}

	@UsedByGodot
	public void scan_qr(Dictionary imageDict) {
		Log.d(LOG_TAG, "scan_qr() invoked");

		ImageInfo imageInfo = new ImageInfo(imageDict);
		byte[] buffer = imageInfo.getBuffer();
		int width = imageInfo.getWidth();
		int height = imageInfo.getHeight();

		if (buffer == null || width <= 0 || height <= 0) {
			emitSignal(QR_SCAN_FAILED_SIGNAL, new ScanError(Code.INVALID_IMAGE,
					"Invalid image data").buildRawData());
			return;
		}

		try {
			// Godot Image.get_data() returns raw bytes. For scan_qr, we expect FORMAT_RGBA8.
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(buffer));

			InputImage image = InputImage.fromBitmap(bitmap, 0);

			BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
					.setBarcodeFormats(Barcode.FORMAT_QR_CODE)
					.build();

			BarcodeScanner scanner = BarcodeScanning.getClient(options);

			scanner.process(image)
					.addOnSuccessListener(barcodes -> {
						if (barcodes.isEmpty()) {
							emitSignal(QR_SCAN_FAILED_SIGNAL, new ScanError(Code.NO_CODE_DETECTED,
									"No QR code detected").buildRawData());
						} else {
							// getRawValue() returns null when MLKit cannot decode the raw bytes as
							// UTF-8 (e.g. a QR generated with ISO-8859-1 encoding and no ECI marker).
							// Fall back to getDisplayValue(), and emit a failure if both are null.
							String rawValue = barcodes.get(0).getRawValue();
							if (rawValue == null) {
								rawValue = barcodes.get(0).getDisplayValue();
							}
							if (rawValue == null) {
								emitSignal(QR_SCAN_FAILED_SIGNAL, new ScanError(Code.SCANNER_FAILURE,
										"QR code detected but value could not be decoded").buildRawData());
							} else {
								emitSignal(QR_DETECTED_SIGNAL, rawValue);
							}
						}
					})
					.addOnFailureListener(e -> {
						Log.e(LOG_TAG, "Scan failed", e);
						emitSignal(QR_SCAN_FAILED_SIGNAL, new ScanError(Code.SCANNER_FAILURE,
								e.getMessage()).buildRawData());
					});
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error processing image", e);
			emitSignal(QR_SCAN_FAILED_SIGNAL, new ScanError(Code.INTERNAL_ERROR, e.getMessage()).buildRawData());
		}
	}

	@Override
	public void onMainDestroy() {
		// Plugin cleanup
	}
}
