//
// © 2026-present https://github.com/cengiz-pz
//

package org.godotengine.plugin.qr.model;

import org.godotengine.godot.Dictionary;
import org.godotengine.plugin.qr.fixtures.ImageInfoFixtures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DisplayName("ImageInfo")
class ImageInfoTest {

	// -------------------------------------------------------------------------
	//  Default constructor
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("default constructor")
	class DefaultConstructor {

		private ImageInfo imageInfo;

		@BeforeEach
		void setUp() {
			imageInfo = new ImageInfo();
		}

		@Test
		@DisplayName("getBuffer returns null when no buffer has been set")
		void getBuffer_returnsNull() {
			assertNull(imageInfo.getBuffer());
		}

		@Test
		@DisplayName("getWidth returns -1 when no width has been set")
		void getWidth_returnsNegativeOne() {
			assertEquals(-1, imageInfo.getWidth());
		}

		@Test
		@DisplayName("getHeight returns -1 when no height has been set")
		void getHeight_returnsNegativeOne() {
			assertEquals(-1, imageInfo.getHeight());
		}

		@Test
		@DisplayName("getFormat returns 3 (FORMAT_RGBA8) as default")
		void getFormat_returnsDefaultFormatRGBA8() {
			assertEquals(ImageInfoFixtures.FORMAT_RGBA8, imageInfo.getFormat());
		}

		@Test
		@DisplayName("hasMipmaps returns false by default")
		void hasMipmaps_returnsFalse() {
			assertFalse(imageInfo.hasMipmaps());
		}

		@Test
		@DisplayName("getRawData returns a non-null Dictionary")
		void getRawData_returnsNonNullDictionary() {
			assertNotNull(imageInfo.getRawData());
		}

		@Test
		@DisplayName("getRawData returns an empty Dictionary")
		void getRawData_returnsEmptyDictionary() {
			assertTrue(imageInfo.getRawData().isEmpty());
		}
	}


	// -------------------------------------------------------------------------
	//  Dictionary constructor
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Dictionary constructor")
	class DictionaryConstructor {

		@Test
		@DisplayName("wraps a fully-populated dictionary without copying it")
		void wrapsSuppliedDictionary() {
			Dictionary dict = ImageInfoFixtures.validImageDict(64, 64);
			ImageInfo info = new ImageInfo(dict);
			assertSame(dict, info.getRawData(),
					"getRawData() should return the exact same Dictionary instance");
		}

		@Test
		@DisplayName("reads width stored as Long from Godot")
		void readsWidthFromDictionary() {
			Dictionary dict = ImageInfoFixtures.validImageDict(128, 64);
			ImageInfo info = new ImageInfo(dict);
			assertEquals(128, info.getWidth());
		}

		@Test
		@DisplayName("reads height stored as Long from Godot")
		void readsHeightFromDictionary() {
			Dictionary dict = ImageInfoFixtures.validImageDict(128, 64);
			ImageInfo info = new ImageInfo(dict);
			assertEquals(64, info.getHeight());
		}

		@Test
		@DisplayName("reads buffer byte array from dictionary")
		void readsBufferFromDictionary() {
			byte[] buffer = ImageInfoFixtures.createSolidRgbaBuffer(32, 32, 0xFF, 0, 0, 0xFF);
			Dictionary dict = ImageInfoFixtures.validImageDictWithBuffer(32, 32, buffer);
			ImageInfo info = new ImageInfo(dict);
			assertArrayEquals(buffer, info.getBuffer());
		}

		@Test
		@DisplayName("reads format from dictionary")
		void readsFormatFromDictionary() {
			Dictionary dict = ImageInfoFixtures.validImageDict(64, 64);
			dict.put("format", 5L); // e.g. FORMAT_RGB8
			ImageInfo info = new ImageInfo(dict);
			assertEquals(5, info.getFormat());
		}

		@Test
		@DisplayName("reads has_mipmaps flag from dictionary")
		void readsMipmapsFlagFromDictionary() {
			Dictionary dict = ImageInfoFixtures.validImageDict(64, 64);
			dict.put("has_mipmaps", true);
			ImageInfo info = new ImageInfo(dict);
			assertTrue(info.hasMipmaps());
		}

		@Test
		@DisplayName("returns -1 for width when key is absent")
		void getWidth_returnsMinus1WhenKeyAbsent() {
			ImageInfo info = new ImageInfo(ImageInfoFixtures.emptyImageDict());
			assertEquals(-1, info.getWidth());
		}

		@Test
		@DisplayName("returns -1 for height when key is absent")
		void getHeight_returnsMinus1WhenKeyAbsent() {
			ImageInfo info = new ImageInfo(ImageInfoFixtures.emptyImageDict());
			assertEquals(-1, info.getHeight());
		}

		@Test
		@DisplayName("returns null for buffer when key is absent")
		void getBuffer_returnsNullWhenKeyAbsent() {
			ImageInfo info = new ImageInfo(ImageInfoFixtures.emptyImageDict());
			assertNull(info.getBuffer());
		}

		@Test
		@DisplayName("returns FORMAT_RGBA8 (3) for format when key is absent")
		void getFormat_returnsDefaultWhenKeyAbsent() {
			ImageInfo info = new ImageInfo(ImageInfoFixtures.emptyImageDict());
			assertEquals(ImageInfoFixtures.FORMAT_RGBA8, info.getFormat());
		}

		@Test
		@DisplayName("returns false for has_mipmaps when key is absent")
		void hasMipmaps_returnsFalseWhenKeyAbsent() {
			ImageInfo info = new ImageInfo(ImageInfoFixtures.emptyImageDict());
			assertFalse(info.hasMipmaps());
		}
	}


	// -------------------------------------------------------------------------
	//  Setters
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("setters")
	class Setters {

		private ImageInfo imageInfo;

		@BeforeEach
		void setUp() {
			imageInfo = new ImageInfo();
		}

		@Test
		@DisplayName("setWidth stores the value so getWidth returns it")
		void setWidth_roundtrips() {
			imageInfo.setWidth(512);
			assertEquals(512, imageInfo.getWidth());
		}

		@Test
		@DisplayName("setHeight stores the value so getHeight returns it")
		void setHeight_roundtrips() {
			imageInfo.setHeight(256);
			assertEquals(256, imageInfo.getHeight());
		}

		@Test
		@DisplayName("setBuffer stores the array so getBuffer returns it")
		void setBuffer_roundtrips() {
			byte[] buffer = ImageInfoFixtures.createSolidRgbaBuffer(8, 0, 128, 255);
			imageInfo.setBuffer(buffer);
			assertArrayEquals(buffer, imageInfo.getBuffer());
		}

		@Test
		@DisplayName("setFormat stores the value so getFormat returns it")
		void setFormat_roundtrips() {
			imageInfo.setFormat(5);
			assertEquals(5, imageInfo.getFormat());
		}

		@Test
		@DisplayName("setHasMipmaps(true) makes hasMipmaps() return true")
		void setHasMipmaps_true_roundtrips() {
			imageInfo.setHasMipmaps(true);
			assertTrue(imageInfo.hasMipmaps());
		}

		@Test
		@DisplayName("setHasMipmaps(false) makes hasMipmaps() return false")
		void setHasMipmaps_false_roundtrips() {
			imageInfo.setHasMipmaps(true);   // set to true first
			imageInfo.setHasMipmaps(false);  // then reset
			assertFalse(imageInfo.hasMipmaps());
		}

		@Test
		@DisplayName("setWidth stores value as Long in the underlying Dictionary")
		void setWidth_storesAsLongInDictionary() {
			imageInfo.setWidth(100);
			Object raw = imageInfo.getRawData().get("width");
			assertInstanceOf(Long.class, raw);
			assertEquals(100L, raw);
		}

		@Test
		@DisplayName("setHeight stores value as Long in the underlying Dictionary")
		void setHeight_storesAsLongInDictionary() {
			imageInfo.setHeight(200);
			Object raw = imageInfo.getRawData().get("height");
			assertInstanceOf(Long.class, raw);
			assertEquals(200L, raw);
		}

		@ParameterizedTest(name = "width = {0}")
		@ValueSource(ints = {1, 64, 256, 512, 1024, Integer.MAX_VALUE})
		@DisplayName("setWidth handles various widths correctly")
		void setWidth_variousSizes(int width) {
			imageInfo.setWidth(width);
			assertEquals(width, imageInfo.getWidth());
		}

		@ParameterizedTest(name = "width={0}, height={1}")
		@CsvSource({
				"64,  64",
				"256, 128",
				"128, 256",
				"512, 512",
				"1,   1"
		})
		@DisplayName("setWidth and setHeight are independent")
		void setWidthAndHeight_independent(int width, int height) {
			imageInfo.setWidth(width);
			imageInfo.setHeight(height);
			assertEquals(width,  imageInfo.getWidth());
			assertEquals(height, imageInfo.getHeight());
		}
	}


	// -------------------------------------------------------------------------
	//  Buffer content integrity
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("buffer content")
	class BufferContent {

		@Test
		@DisplayName("buffer length matches width × height × 4 for RGBA8")
		void bufferLength_matchesPixelCount() {
			int width = 32;
			int height = 32;
			ImageInfo info = ImageInfoFixtures.validImageInfo(width, height);
			assertEquals(width * height * 4, info.getBuffer().length);
		}

		@Test
		@DisplayName("a zero-filled buffer is accepted and returned unchanged")
		void zeroBuffer_isStoredAndRetrieved() {
			byte[] zeros = ImageInfoFixtures.createZeroBuffer(16, 16);
			ImageInfo info = new ImageInfo();
			info.setBuffer(zeros);
			assertArrayEquals(zeros, info.getBuffer());
		}

		@Test
		@DisplayName("setBuffer accepts a single-pixel (4-byte) buffer")
		void setBuffer_singlePixel() {
			byte[] pixel = {(byte) 0xFF, 0x00, 0x00, (byte) 0xFF}; // opaque red in RGBA
			ImageInfo info = new ImageInfo();
			info.setWidth(1);
			info.setHeight(1);
			info.setBuffer(pixel);
			assertArrayEquals(pixel, info.getBuffer());
		}
	}


	// -------------------------------------------------------------------------
	//  getRawData / round-trip
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("getRawData round-trip")
	class RawDataRoundTrip {

		@Test
		@DisplayName("getRawData contains all set fields")
		void getRawData_containsAllFields() {
			ImageInfo info = ImageInfoFixtures.validImageInfo(64, 64);
			Dictionary raw = info.getRawData();
			assertTrue(raw.containsKey("width"));
			assertTrue(raw.containsKey("height"));
			assertTrue(raw.containsKey("buffer"));
		}

		@Test
		@DisplayName("ImageInfo built from getRawData reproduces identical values")
		void roundTrip_viaRawData() {
			ImageInfo original = ImageInfoFixtures.validImageInfo(128, 128);
			Dictionary raw = original.getRawData();

			ImageInfo reconstructed = new ImageInfo(raw);

			assertEquals(original.getWidth(),   reconstructed.getWidth());
			assertEquals(original.getHeight(),  reconstructed.getHeight());
			assertEquals(original.getFormat(),  reconstructed.getFormat());
			assertEquals(original.hasMipmaps(), reconstructed.hasMipmaps());
			assertArrayEquals(original.getBuffer(), reconstructed.getBuffer());
		}
	}
}
