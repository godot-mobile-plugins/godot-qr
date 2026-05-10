//
// © 2026-present https://github.com/cengiz-pz
//

package org.godotengine.plugin.qr.fixtures;

import org.godotengine.godot.Dictionary;
import org.godotengine.plugin.qr.model.ImageInfo;


/**
 * Factory methods that produce ready-to-use {@link ImageInfo} instances and raw
 * {@link Dictionary} objects for use across all tests.
 *
 * <p>Convention: methods prefixed with {@code valid} build well-formed objects;
 * methods prefixed with {@code invalid} / {@code empty} build intentionally
 * broken objects used to verify error-handling paths.</p>
 */
public final class ImageInfoFixtures {

	// -- Common image dimensions -----------------------------------------------

	public static final int SMALL_SIZE  = 64;
	public static final int MEDIUM_SIZE = 256;
	public static final int LARGE_SIZE  = 512;

	// -- Common ARGB colours (packed int, same layout as Android Color) --------

	/** Opaque black  – typical QR foreground. */
	public static final int COLOR_BLACK = 0xFF000000;
	/** Opaque white  – typical QR background. */
	public static final int COLOR_WHITE = 0xFFFFFFFF;
	/** Opaque red. */
	public static final int COLOR_RED   = 0xFFFF0000;
	/** 50 % transparent black. */
	public static final int COLOR_HALF_TRANSPARENT_BLACK = 0x80000000;

	// -- Well-known URIs / payloads --------------------------------------------

	public static final String URI_SIMPLE    = "https://example.com";
	public static final String URI_UNICODE   = "https://example.com/ünïcödé?q=テスト";
	public static final String URI_LONG      =
			"https://example.com/very/long/path?param1=value1&param2=value2&param3=value3";

	// -- Godot Image.Format constant for FORMAT_RGBA8 --------------------------
	public static final int FORMAT_RGBA8 = 3;

	private ImageInfoFixtures() { /* utility class */ }


	// -------------------------------------------------------------------------
	//  Buffer helpers
	// -------------------------------------------------------------------------

	/**
	 * Creates a fully-opaque, single-colour RGBA8 byte buffer for a square
	 * image of {@code size × size} pixels.
	 *
	 * @param size  width (= height) in pixels
	 * @param r     red   channel [0-255]
	 * @param g     green channel [0-255]
	 * @param b     blue  channel [0-255]
	 */
	public static byte[] createSolidRgbaBuffer(int size, int r, int g, int b) {
		return createSolidRgbaBuffer(size, size, r, g, b, 0xFF);
	}

	/**
	 * Creates an RGBA8 byte buffer for a {@code width × height} image where
	 * every pixel has the supplied RGBA values.
	 */
	public static byte[] createSolidRgbaBuffer(int width, int height,
												int r, int g, int b, int a) {
		byte[] buffer = new byte[width * height * 4];
		for (int i = 0; i < buffer.length; i += 4) {
			buffer[i]     = (byte) r;
			buffer[i + 1] = (byte) g;
			buffer[i + 2] = (byte) b;
			buffer[i + 3] = (byte) a;
		}
		return buffer;
	}

	/** Returns a zero-filled (transparent black) RGBA8 buffer. */
	public static byte[] createZeroBuffer(int width, int height) {
		return new byte[width * height * 4];
	}


	// -------------------------------------------------------------------------
	//  Dictionary (raw data) builders
	// -------------------------------------------------------------------------

	/**
	 * Builds a fully-populated, valid image {@link Dictionary} as Godot would
	 * produce from {@code Image.get_data()}.
	 */
	public static Dictionary validImageDict(int width, int height) {
		Dictionary dict = new Dictionary();
		dict.put("buffer",      createSolidRgbaBuffer(width, height, 0, 0, 0, 0xFF));
		dict.put("width",       (long) width);
		dict.put("height",      (long) height);
		dict.put("format",      (long) FORMAT_RGBA8);
		dict.put("has_mipmaps", false);
		return dict;
	}

	/** Convenience overload using the default {@link #MEDIUM_SIZE}. */
	public static Dictionary validImageDict() {
		return validImageDict(MEDIUM_SIZE, MEDIUM_SIZE);
	}

	/** Valid dictionary whose buffer is explicitly the supplied bytes. */
	public static Dictionary validImageDictWithBuffer(int width, int height, byte[] buffer) {
		Dictionary dict = validImageDict(width, height);
		dict.put("buffer", buffer);
		return dict;
	}

	/** Dictionary that is completely empty – simulates a missing / null image. */
	public static Dictionary emptyImageDict() {
		return new Dictionary();
	}

	/** Dictionary that contains width/height but no buffer. */
	public static Dictionary imageDictMissingBuffer(int width, int height) {
		Dictionary dict = new Dictionary();
		dict.put("width",  (long) width);
		dict.put("height", (long) height);
		return dict;
	}

	/** Dictionary whose width is zero – invalid even if buffer is present. */
	public static Dictionary imageDictZeroWidth(int height) {
		Dictionary dict = validImageDict(MEDIUM_SIZE, height);
		dict.put("width", 0L);
		return dict;
	}

	/** Dictionary whose height is zero – invalid even if buffer is present. */
	public static Dictionary imageDictZeroHeight(int width) {
		Dictionary dict = validImageDict(width, MEDIUM_SIZE);
		dict.put("height", 0L);
		return dict;
	}


	// -------------------------------------------------------------------------
	//  ImageInfo object builders
	// -------------------------------------------------------------------------

	/** Returns a new {@link ImageInfo} with all fields populated. */
	public static ImageInfo validImageInfo(int width, int height) {
		ImageInfo info = new ImageInfo();
		info.setWidth(width);
		info.setHeight(height);
		info.setBuffer(createSolidRgbaBuffer(width, height, 0, 0, 0, 0xFF));
		info.setFormat(FORMAT_RGBA8);
		info.setHasMipmaps(false);
		return info;
	}

	/** Convenience overload using the default {@link #MEDIUM_SIZE}. */
	public static ImageInfo validImageInfo() {
		return validImageInfo(MEDIUM_SIZE, MEDIUM_SIZE);
	}

	/** Returns an {@link ImageInfo} reconstructed from a raw dictionary. */
	public static ImageInfo imageInfoFromDict(Dictionary dict) {
		return new ImageInfo(dict);
	}
}
