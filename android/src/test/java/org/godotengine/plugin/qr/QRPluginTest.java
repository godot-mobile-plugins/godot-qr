//
// © 2026-present https://github.com/cengiz-pz
//

package org.godotengine.plugin.qr;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.godotengine.godot.Dictionary;
import org.godotengine.godot.Godot;
import org.godotengine.plugin.qr.fixtures.ImageInfoFixtures;
import org.godotengine.plugin.qr.model.ScanError.Code;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Local JVM unit tests for {@link QRPlugin}.
 *
 * <h3>Design decisions</h3>
 *
 * <h4>Why {@code TestableQRPlugin} instead of a Mockito spy?</h4>
 * {@code GodotPlugin.emitSignal(String, Object...)} is {@code protected}.
 * Mockito's {@code spy()} and {@code verify()} operate on the call site from
 * the test class, which is outside the class hierarchy, so the compiler rejects
 * the access even though Mockito would generate a valid subclass at runtime.
 * The solution is a hand-written subclass that <em>overrides</em> the protected
 * method (which is legal for a subclass) and records every invocation in a list
 * that the test can then inspect directly, without any Mockito involvement.
 *
 * <h4>Why raw-type {@code ArgumentCaptor} for {@code OnSuccessListener}?</h4>
 * {@code Task<List<Barcode>>} declares
 * {@code addOnSuccessListener(OnSuccessListener<? super List<Barcode>>)}.
 * A typed {@code ArgumentCaptor<OnSuccessListener<List<Barcode>>>} causes the
 * compiler to fail on inference: {@code captor.capture()} resolves to
 * {@code OnSuccessListener<List<Barcode>>} but the wildcard expects
 * {@code OnSuccessListener<? super List<Barcode>>}.  Using a raw
 * {@code ArgumentCaptor<OnSuccessListener>} avoids the inference altogether;
 * the {@code @SuppressWarnings} keeps the build clean.
 *
 * <h4>Static mocks</h4>
 * Android platform classes ({@link Color}, {@link Bitmap}) and the MLKit
 * factory ({@link BarcodeScanning}) are handled via {@link MockedStatic} so
 * their static methods can be stubbed without Robolectric. Handles are
 * <strong>opened in {@code setUp()} and closed in {@code tearDown()}</strong>
 * to prevent cross-test contamination.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QRPlugin")
class QRPluginTest {

	// ═════════════════════════════════════════════════════════════════════════
	//  TestableQRPlugin — hand-written test double
	// ═════════════════════════════════════════════════════════════════════════

	/**
	 * Subclass of {@link QRPlugin} that overrides the {@code protected}
	 * {@code emitSignal} method so tests can observe emitted signals without
	 * needing Mockito to reach a protected member from outside the hierarchy.
	 */
	static class TestableQRPlugin extends QRPlugin {

		/** Immutable snapshot of one {@code emitSignal} invocation. */
		static final class SignalCall {
			final String   name;
			final Object[] args;

			SignalCall(String name, Object[] args) {
				this.name = name;
				this.args = args;
			}
		}

		/** All signals emitted since construction (or last {@link #clearSignals()}). */
		final List<SignalCall> emittedSignals = new ArrayList<>();

		TestableQRPlugin(Godot godot) {
			super(godot);
		}

		// Override the protected overload used by QRPlugin's production code.
		@Override
		protected void emitSignal(String signalName, Object... args) {
			emittedSignals.add(new SignalCall(signalName, args));
		}

		// -- Convenience accessors used in assertions ----------------------

		void clearSignals() {
			emittedSignals.clear();
		}

		/** Returns the most-recently emitted signal, or {@code null} if none. */
		SignalCall lastSignal() {
			return emittedSignals.isEmpty()
					? null
					: emittedSignals.get(emittedSignals.size() - 1);
		}

		/**
		 * Returns the first argument of the last signal cast to {@link Dictionary}.
		 * Used for {@code qr_scan_failed} payloads.
		 */
		Dictionary lastErrorDict() {
			SignalCall last = lastSignal();
			return (last != null && last.args.length > 0) ? (Dictionary) last.args[0] : null;
		}

		/**
		 * Returns the first argument of the last signal cast to {@link String}.
		 * Used for {@code qr_detected} payloads.
		 */
		String lastStringArg() {
			SignalCall last = lastSignal();
			return (last != null && last.args.length > 0) ? (String) last.args[0] : null;
		}
	}


	// ═════════════════════════════════════════════════════════════════════════
	//  Test infrastructure
	// ═════════════════════════════════════════════════════════════════════════

	@Mock private Godot               mockGodot;
	@Mock private BarcodeScanner      mockScanner;
	@Mock private Bitmap              mockBitmap;
	@Mock private InputImage          mockInputImage;
	@Mock private Task<List<Barcode>> mockTask;

	private TestableQRPlugin plugin;

	// Static-mock handles — opened in setUp(), closed in tearDown()
	private MockedStatic<Color>           colorStatic;
	private MockedStatic<Bitmap>          bitmapStatic;
	private MockedStatic<InputImage>      inputImageStatic;
	private MockedStatic<BarcodeScanning> barcodeScanningStatic;

	@BeforeEach
	void setUp() {
		plugin = new TestableQRPlugin(mockGodot);

		colorStatic           = mockStatic(Color.class);
		bitmapStatic          = mockStatic(Bitmap.class);
		barcodeScanningStatic = mockStatic(BarcodeScanning.class);
		inputImageStatic      = mockStatic(InputImage.class);

		// -- Color stubs (channel decomposition used by generate_qr) ----------
		// Return plausible values; override per-test when pixel content matters.
		colorStatic.when(() -> Color.alpha(anyInt())).thenReturn(0xFF);
		colorStatic.when(() -> Color.red(anyInt())).thenReturn(0x00);
		colorStatic.when(() -> Color.green(anyInt())).thenReturn(0x00);
		colorStatic.when(() -> Color.blue(anyInt())).thenReturn(0x00);

		// -- Bitmap / MLKit stubs (used by scan_qr) ----------------------------
		bitmapStatic
				.when(() -> Bitmap.createBitmap(anyInt(), anyInt(), any(Bitmap.Config.class)))
				.thenReturn(mockBitmap);

		// InputImage.fromBitmap() is a static factory on an Android/Play-Services class.
		// Without this stub it throws on the local JVM, causing scan_qr's outer catch
		// to fire and emit INTERNAL_ERROR before the scanner is ever reached.
		inputImageStatic
				.when(() -> InputImage.fromBitmap(any(Bitmap.class), anyInt()))
				.thenReturn(mockInputImage);

		barcodeScanningStatic
				.when(() -> BarcodeScanning.getClient(any()))
				.thenReturn(mockScanner);

		lenient().when(mockScanner.process(any(InputImage.class))).thenReturn(mockTask);
		lenient().when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);
		lenient().when(mockTask.addOnFailureListener(any())).thenReturn(mockTask);
	}

	@AfterEach
	void tearDown() {
		colorStatic.close();
		bitmapStatic.close();
		inputImageStatic.close();
		barcodeScanningStatic.close();
	}

	// -- Async simulation helpers ----------------------------------------------

	/**
	 * Captures the {@link OnSuccessListener} registered on {@link #mockTask} and
	 * fires it synchronously with {@code barcodes}.
	 *
	 * <p>A raw-type {@code ArgumentCaptor} is used intentionally: the wildcard
	 * bound on {@code Task.addOnSuccessListener} (
	 * {@code OnSuccessListener<? super List<Barcode>>}) causes a compiler error
	 * when a fully-typed captor is combined with {@code captor.capture()} in a
	 * {@code verify()} call.  The raw type sidesteps the inference, and the
	 * {@code @SuppressWarnings} keeps the build clean.</p>
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void triggerSuccess(List<Barcode> barcodes) {
		ArgumentCaptor<OnSuccessListener> cap =
				ArgumentCaptor.forClass(OnSuccessListener.class);
		verify(mockTask).addOnSuccessListener(cap.capture());
		cap.getValue().onSuccess(barcodes);
	}

	/** Captures the {@link OnFailureListener} on {@link #mockTask} and fires it. */
	private void triggerFailure(Exception exception) {
		ArgumentCaptor<OnFailureListener> cap =
				ArgumentCaptor.forClass(OnFailureListener.class);
		verify(mockTask).addOnFailureListener(cap.capture());
		cap.getValue().onFailure(exception);
	}


	// ═════════════════════════════════════════════════════════════════════════
	//  getPluginName
	// ═════════════════════════════════════════════════════════════════════════

	@Test
	@DisplayName("getPluginName() returns the simple class name 'QRPlugin'")
	void getPluginName_returnsQRPlugin() {
		assertEquals("QRPlugin", plugin.getPluginName());
	}


	// ═════════════════════════════════════════════════════════════════════════
	//  getPluginSignals
	// ═════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("getPluginSignals()")
	class GetPluginSignals {

		@Test
		@DisplayName("exposes exactly two signals")
		void exposesTwoSignals() {
			assertEquals(2, plugin.getPluginSignals().size());
		}

		@Test
		@DisplayName("qr_detected signal is registered")
		void qrDetectedSignal_isRegistered() {
			boolean found = plugin.getPluginSignals().stream()
					.anyMatch(s -> QRPlugin.QR_DETECTED_SIGNAL.equals(s.getName()));
			assertTrue(found,
					"Signal '" + QRPlugin.QR_DETECTED_SIGNAL + "' must be registered");
		}

		@Test
		@DisplayName("qr_scan_failed signal is registered")
		void qrScanFailedSignal_isRegistered() {
			boolean found = plugin.getPluginSignals().stream()
					.anyMatch(s -> QRPlugin.QR_SCAN_FAILED_SIGNAL.equals(s.getName()));
			assertTrue(found,
					"Signal '" + QRPlugin.QR_SCAN_FAILED_SIGNAL + "' must be registered");
		}
	}


	// ═════════════════════════════════════════════════════════════════════════
	//  generate_qr
	// ═════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("generate_qr()")
	class GenerateQr {

		@Test
		@DisplayName("returns a non-null Dictionary for a simple URI")
		void returnsNonNull() {
			Dictionary result = plugin.generate_qr(
					ImageInfoFixtures.URI_SIMPLE, ImageInfoFixtures.MEDIUM_SIZE,
					ImageInfoFixtures.COLOR_BLACK, ImageInfoFixtures.COLOR_WHITE);
			assertNotNull(result);
		}

		@Test
		@DisplayName("width in result equals the requested size")
		void result_widthMatchesRequestedSize() {
			Dictionary result = plugin.generate_qr(
					ImageInfoFixtures.URI_SIMPLE, ImageInfoFixtures.MEDIUM_SIZE,
					ImageInfoFixtures.COLOR_BLACK, ImageInfoFixtures.COLOR_WHITE);
			assertEquals((long) ImageInfoFixtures.MEDIUM_SIZE, result.get("width"));
		}

		@Test
		@DisplayName("height in result equals the requested size")
		void result_heightMatchesRequestedSize() {
			Dictionary result = plugin.generate_qr(
					ImageInfoFixtures.URI_SIMPLE, ImageInfoFixtures.MEDIUM_SIZE,
					ImageInfoFixtures.COLOR_BLACK, ImageInfoFixtures.COLOR_WHITE);
			assertEquals((long) ImageInfoFixtures.MEDIUM_SIZE, result.get("height"));
		}

		@Test
		@DisplayName("result contains a non-null buffer")
		void result_bufferIsNotNull() {
			Dictionary result = plugin.generate_qr(
					ImageInfoFixtures.URI_SIMPLE, ImageInfoFixtures.MEDIUM_SIZE,
					ImageInfoFixtures.COLOR_BLACK, ImageInfoFixtures.COLOR_WHITE);
			assertNotNull(result.get("buffer"), "Buffer must not be null");
		}

		@Test
		@DisplayName("buffer length is exactly width × height × 4 (RGBA8)")
		void result_bufferLengthIsCorrect() {
			int size = ImageInfoFixtures.MEDIUM_SIZE;
			Dictionary result = plugin.generate_qr(
					ImageInfoFixtures.URI_SIMPLE, size,
					ImageInfoFixtures.COLOR_BLACK, ImageInfoFixtures.COLOR_WHITE);
			byte[] buffer = (byte[]) result.get("buffer");
			assertNotNull(buffer);
			assertEquals(size * size * 4, buffer.length);
		}

		@ParameterizedTest(name = "size = {0}")
		@ValueSource(ints = {64, 128, 256, 512})
		@DisplayName("produces correctly-sized output for common QR sizes")
		void commonSizes_producesCorrectOutput(int size) {
			Dictionary result = plugin.generate_qr(
					ImageInfoFixtures.URI_SIMPLE, size,
					ImageInfoFixtures.COLOR_BLACK, ImageInfoFixtures.COLOR_WHITE);
			assertAll(
					() -> assertNotNull(result.get("buffer")),
					() -> assertEquals((long) size, result.get("width")),
					() -> assertEquals((long) size, result.get("height"))
			);
		}

		@Test
		@DisplayName("produces output for a URI with Unicode / non-ASCII characters")
		void unicodeUri_producesOutput() {
			Dictionary result = plugin.generate_qr(
					ImageInfoFixtures.URI_UNICODE, ImageInfoFixtures.MEDIUM_SIZE,
					ImageInfoFixtures.COLOR_BLACK, ImageInfoFixtures.COLOR_WHITE);
			assertNotNull(result.get("buffer"));
		}

		@Test
		@DisplayName("produces output for a long URI")
		void longUri_producesOutput() {
			Dictionary result = plugin.generate_qr(
					ImageInfoFixtures.URI_LONG, ImageInfoFixtures.MEDIUM_SIZE,
					ImageInfoFixtures.COLOR_BLACK, ImageInfoFixtures.COLOR_WHITE);
			assertNotNull(result.get("buffer"));
		}

		@Test
		@DisplayName("foreground colour components are extracted via Color.* helpers")
		void foregroundColour_extractedViaColorHelpers() {
			int fgColor = ImageInfoFixtures.COLOR_RED;
			colorStatic.when(() -> Color.alpha(fgColor)).thenReturn(0xFF);
			colorStatic.when(() -> Color.red(fgColor)).thenReturn(0xFF);
			colorStatic.when(() -> Color.green(fgColor)).thenReturn(0x00);
			colorStatic.when(() -> Color.blue(fgColor)).thenReturn(0x00);

			assertDoesNotThrow(() -> plugin.generate_qr(
					ImageInfoFixtures.URI_SIMPLE, ImageInfoFixtures.SMALL_SIZE,
					fgColor, ImageInfoFixtures.COLOR_WHITE));
		}

		@Test
		@DisplayName("returns an empty-buffer Dictionary when ZXing encoding fails")
		void encodingFailure_returnsEmptyResult() {
			// An empty string causes MultiFormatWriter to throw IllegalArgumentException.
			Dictionary result = plugin.generate_qr(
					"", ImageInfoFixtures.MEDIUM_SIZE,
					ImageInfoFixtures.COLOR_BLACK, ImageInfoFixtures.COLOR_WHITE);
			assertNotNull(result, "Result must not be null even on encoding failure");
			assertNull(result.get("buffer"), "Buffer should be null on encoding failure");
		}

		@Test
		@DisplayName("does not emit any signal")
		void doesNotEmitSignal() {
			plugin.generate_qr(
					ImageInfoFixtures.URI_SIMPLE, ImageInfoFixtures.MEDIUM_SIZE,
					ImageInfoFixtures.COLOR_BLACK, ImageInfoFixtures.COLOR_WHITE);
			assertTrue(plugin.emittedSignals.isEmpty(),
					"generate_qr must not emit any signal");
		}
	}


	// ═════════════════════════════════════════════════════════════════════════
	//  scan_qr — input validation  (synchronous; no MLKit paths reached)
	// ═════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("scan_qr() — input validation")
	class ScanQrInputValidation {

		@Test
		@DisplayName("emits qr_scan_failed with INVALID_IMAGE when dictionary is empty")
		void emptyDict_emitsInvalidImage() {
			plugin.scan_qr(ImageInfoFixtures.emptyImageDict());

			assertEquals(QRPlugin.QR_SCAN_FAILED_SIGNAL, plugin.lastSignal().name);
			assertSame(Code.INVALID_IMAGE, plugin.lastErrorDict().get("code"));
		}

		@Test
		@DisplayName("emits qr_scan_failed with INVALID_IMAGE when buffer is missing")
		void missingBuffer_emitsInvalidImage() {
			plugin.scan_qr(ImageInfoFixtures.imageDictMissingBuffer(256, 256));

			assertEquals(QRPlugin.QR_SCAN_FAILED_SIGNAL, plugin.lastSignal().name);
			assertSame(Code.INVALID_IMAGE, plugin.lastErrorDict().get("code"));
		}

		@Test
		@DisplayName("emits qr_scan_failed with INVALID_IMAGE when width is zero")
		void zeroWidth_emitsInvalidImage() {
			plugin.scan_qr(ImageInfoFixtures.imageDictZeroWidth(256));

			assertEquals(QRPlugin.QR_SCAN_FAILED_SIGNAL, plugin.lastSignal().name);
			assertSame(Code.INVALID_IMAGE, plugin.lastErrorDict().get("code"));
		}

		@Test
		@DisplayName("emits qr_scan_failed with INVALID_IMAGE when height is zero")
		void zeroHeight_emitsInvalidImage() {
			plugin.scan_qr(ImageInfoFixtures.imageDictZeroHeight(256));

			assertEquals(QRPlugin.QR_SCAN_FAILED_SIGNAL, plugin.lastSignal().name);
			assertSame(Code.INVALID_IMAGE, plugin.lastErrorDict().get("code"));
		}

		@Test
		@DisplayName("INVALID_IMAGE error description is non-null and non-empty")
		void invalidImage_descriptionIsPresent() {
			plugin.scan_qr(ImageInfoFixtures.emptyImageDict());

			String desc = (String) plugin.lastErrorDict().get("description");
			assertNotNull(desc);
			assertFalse(desc.isBlank());
		}

		@Test
		@DisplayName("does not invoke Bitmap or BarcodeScanning for invalid image")
		void invalidImage_doesNotInvokeBitmapOrScanner() {
			plugin.scan_qr(ImageInfoFixtures.emptyImageDict());

			bitmapStatic.verifyNoInteractions();
			barcodeScanningStatic.verifyNoInteractions();
		}
	}


	// ═════════════════════════════════════════════════════════════════════════
	//  scan_qr — MLKit success paths  (async task simulated synchronously)
	// ═════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("scan_qr() — MLKit success paths")
	class ScanQrMlKitSuccess {

		@Test
		@DisplayName("emits qr_detected with rawValue when a barcode is found")
		void barcodeFound_emitsQrDetected() {
			Barcode mockBarcode = mock(Barcode.class);
			when(mockBarcode.getRawValue()).thenReturn("https://example.com");

			plugin.scan_qr(ImageInfoFixtures.validImageDict());
			triggerSuccess(Collections.singletonList(mockBarcode));

			assertEquals(QRPlugin.QR_DETECTED_SIGNAL, plugin.lastSignal().name);
			assertEquals("https://example.com", plugin.lastStringArg());
		}

		@Test
		@DisplayName("emits qr_detected with displayValue when rawValue is null")
		void rawValueNull_fallsBackToDisplayValue() {
			Barcode mockBarcode = mock(Barcode.class);
			when(mockBarcode.getRawValue()).thenReturn(null);
			when(mockBarcode.getDisplayValue()).thenReturn("https://fallback.example.com");

			plugin.scan_qr(ImageInfoFixtures.validImageDict());
			triggerSuccess(Collections.singletonList(mockBarcode));

			assertEquals(QRPlugin.QR_DETECTED_SIGNAL, plugin.lastSignal().name);
			assertEquals("https://fallback.example.com", plugin.lastStringArg());
		}

		@Test
		@DisplayName("emits qr_scan_failed SCANNER_FAILURE when both rawValue and displayValue are null")
		void bothValuesNull_emitsScannerFailure() {
			Barcode mockBarcode = mock(Barcode.class);
			when(mockBarcode.getRawValue()).thenReturn(null);
			when(mockBarcode.getDisplayValue()).thenReturn(null);

			plugin.scan_qr(ImageInfoFixtures.validImageDict());
			triggerSuccess(Collections.singletonList(mockBarcode));

			assertEquals(QRPlugin.QR_SCAN_FAILED_SIGNAL, plugin.lastSignal().name);
			assertSame(Code.SCANNER_FAILURE, plugin.lastErrorDict().get("code"));
		}

		@Test
		@DisplayName("emits qr_scan_failed NO_CODE_DETECTED when result list is empty")
		void emptyBarcodeList_emitsNoCodeDetected() {
			plugin.scan_qr(ImageInfoFixtures.validImageDict());
			triggerSuccess(Collections.emptyList());

			assertEquals(QRPlugin.QR_SCAN_FAILED_SIGNAL, plugin.lastSignal().name);
			assertSame(Code.NO_CODE_DETECTED, plugin.lastErrorDict().get("code"));
		}

		@Test
		@DisplayName("emits qr_detected with first barcode's value when list has multiple entries")
		void multipleBarcodes_usesFirst() {
			Barcode first  = mock(Barcode.class);
			Barcode second = mock(Barcode.class);
			when(first.getRawValue()).thenReturn("first");
			// second is included in the list to verify only index 0 is used;
			// no stub needed because scan_qr never calls getRawValue() on it.

			plugin.scan_qr(ImageInfoFixtures.validImageDict());
			triggerSuccess(List.of(first, second));

			assertEquals(QRPlugin.QR_DETECTED_SIGNAL, plugin.lastSignal().name);
			assertEquals("first", plugin.lastStringArg());
		}

		@Test
		@DisplayName("emits exactly one signal per scan invocation")
		void scan_emitsExactlyOneSignal() {
			Barcode mockBarcode = mock(Barcode.class);
			when(mockBarcode.getRawValue()).thenReturn("data");

			plugin.scan_qr(ImageInfoFixtures.validImageDict());
			triggerSuccess(Collections.singletonList(mockBarcode));

			assertEquals(1, plugin.emittedSignals.size());
		}
	}


	// ═════════════════════════════════════════════════════════════════════════
	//  scan_qr — MLKit failure paths
	// ═════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("scan_qr() — MLKit failure paths")
	class ScanQrMlKitFailure {

		@Test
		@DisplayName("emits qr_scan_failed SCANNER_FAILURE when MLKit task fails")
		void mlKitTaskFailure_emitsScannerFailure() {
			plugin.scan_qr(ImageInfoFixtures.validImageDict());
			triggerFailure(new RuntimeException("MLKit internal error"));

			assertEquals(QRPlugin.QR_SCAN_FAILED_SIGNAL, plugin.lastSignal().name);
			assertSame(Code.SCANNER_FAILURE, plugin.lastErrorDict().get("code"));
		}

		@Test
		@DisplayName("error description contains the exception message on task failure")
		void taskFailure_descriptionContainsExceptionMessage() {
			String msg = "Process failed: out of memory";

			plugin.scan_qr(ImageInfoFixtures.validImageDict());
			triggerFailure(new RuntimeException(msg));

			assertEquals(msg, plugin.lastErrorDict().get("description"));
		}

		@Test
		@DisplayName("emits qr_scan_failed INTERNAL_ERROR when Bitmap creation throws")
		void bitmapCreationException_emitsInternalError() {
			bitmapStatic
					.when(() -> Bitmap.createBitmap(anyInt(), anyInt(), any(Bitmap.Config.class)))
					.thenThrow(new RuntimeException("Out of memory"));

			plugin.scan_qr(ImageInfoFixtures.validImageDict());

			assertEquals(QRPlugin.QR_SCAN_FAILED_SIGNAL, plugin.lastSignal().name);
			assertSame(Code.INTERNAL_ERROR, plugin.lastErrorDict().get("code"));
		}

		@Test
		@DisplayName("emits qr_scan_failed INTERNAL_ERROR when BarcodeScanning.getClient() throws")
		void getClientException_emitsInternalError() {
			barcodeScanningStatic
					.when(() -> BarcodeScanning.getClient(any()))
					.thenThrow(new RuntimeException("BarcodeScanning unavailable"));

			plugin.scan_qr(ImageInfoFixtures.validImageDict());

			assertEquals(QRPlugin.QR_SCAN_FAILED_SIGNAL, plugin.lastSignal().name);
			assertSame(Code.INTERNAL_ERROR, plugin.lastErrorDict().get("code"));
		}

		@Test
		@DisplayName("task failure emits exactly one signal")
		void taskFailure_emitsExactlyOneSignal() {
			plugin.scan_qr(ImageInfoFixtures.validImageDict());
			triggerFailure(new Exception("fail"));

			assertEquals(1, plugin.emittedSignals.size());
		}
	}


	// ═════════════════════════════════════════════════════════════════════════
	//  scan_qr — Bitmap / MLKit invocation contract
	// ═════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("scan_qr() — Bitmap and scanner invocation")
	class ScanQrInvocationContract {

		@Test
		@DisplayName("creates Bitmap with the correct width, height and ARGB_8888 config")
		void createsBitmapWithCorrectDimensions() {
			int w = 128;
			int h = 128;
			plugin.scan_qr(ImageInfoFixtures.validImageDict(w, h));

			bitmapStatic.verify(() ->
					Bitmap.createBitmap(eq(w), eq(h), eq(Bitmap.Config.ARGB_8888)));
		}

		@Test
		@DisplayName("obtains a BarcodeScanner client from BarcodeScanning.getClient()")
		void obtainsScannerFromFactory() {
			plugin.scan_qr(ImageInfoFixtures.validImageDict());

			barcodeScanningStatic.verify(
					() -> BarcodeScanning.getClient(any()), times(1));
		}

		@Test
		@DisplayName("calls scanner.process() once per scan_qr invocation")
		void callsScannerProcessOnce() {
			plugin.scan_qr(ImageInfoFixtures.validImageDict());

			verify(mockScanner, times(1)).process(any(InputImage.class));
		}
	}


	// ═════════════════════════════════════════════════════════════════════════
	//  generate_qr → scan_qr integration contract
	// ═════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("generate_qr → scan_qr integration contract")
	class GenerateThenScanContract {

		@Test
		@DisplayName("output of generate_qr passes scan_qr input validation")
		void generateQrOutput_passesScanQrInputValidation() {
			Dictionary generated = plugin.generate_qr(
					ImageInfoFixtures.URI_SIMPLE, ImageInfoFixtures.MEDIUM_SIZE,
					ImageInfoFixtures.COLOR_BLACK, ImageInfoFixtures.COLOR_WHITE);

			assertAll("generate_qr output must satisfy scan_qr's guard clause",
					() -> assertNotNull(generated.get("buffer")),
					() -> assertTrue(((Long) generated.get("width"))  > 0),
					() -> assertTrue(((Long) generated.get("height")) > 0)
			);

			plugin.clearSignals();

			// Feed the generated dictionary into scan_qr.
			// If the guard clause fires it emits INVALID_IMAGE *before* touching Bitmap.
			plugin.scan_qr(generated);

			// Bitmap.createBitmap being called proves the image was accepted.
			bitmapStatic.verify(() ->
					Bitmap.createBitmap(anyInt(), anyInt(), any(Bitmap.Config.class)),
					times(1));

			// No INVALID_IMAGE signal must have been emitted.
			plugin.emittedSignals.stream()
					.filter(c -> c.name.equals(QRPlugin.QR_SCAN_FAILED_SIGNAL))
					.map(c -> (Dictionary) c.args[0])
					.forEach(d -> assertNotSame(Code.INVALID_IMAGE, d.get("code"),
							"scan_qr must not reject the output of generate_qr"));
		}
	}
}
