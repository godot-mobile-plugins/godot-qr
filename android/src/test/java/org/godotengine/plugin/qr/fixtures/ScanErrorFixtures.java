//
// © 2026-present https://github.com/cengiz-pz
//

package org.godotengine.plugin.qr.fixtures;

import org.godotengine.godot.Dictionary;
import org.godotengine.plugin.qr.model.ScanError;
import org.godotengine.plugin.qr.model.ScanError.Code;


/**
 * Factory methods that produce {@link ScanError} instances and the raw
 * {@link Dictionary} objects they emit, for use across all tests.
 */
public final class ScanErrorFixtures {

	// -- Canonical description strings used by QRPlugin -----------------------

	public static final String DESC_INVALID_IMAGE    = "Invalid image data";
	public static final String DESC_NO_CODE          = "No QR code detected";
	public static final String DESC_SCANNER_FAILURE  = "QR code detected but value could not be decoded";
	public static final String DESC_GENERIC_FAILURE  = "Scanner process failed";
	public static final String DESC_INTERNAL_ERROR   = "Unexpected internal error";

	// -- Dictionary key constants (mirrors ScanError private constants) --------

	public static final String KEY_CODE        = "code";
	public static final String KEY_DESCRIPTION = "description";

	private ScanErrorFixtures() { /* utility class */ }


	// -------------------------------------------------------------------------
	//  ScanError builders
	// -------------------------------------------------------------------------

	public static ScanError invalidImage() {
		return new ScanError(Code.INVALID_IMAGE, DESC_INVALID_IMAGE);
	}

	public static ScanError noCodeDetected() {
		return new ScanError(Code.NO_CODE_DETECTED, DESC_NO_CODE);
	}

	public static ScanError scannerFailure() {
		return new ScanError(Code.SCANNER_FAILURE, DESC_SCANNER_FAILURE);
	}

	public static ScanError scannerFailure(String detail) {
		return new ScanError(Code.SCANNER_FAILURE, detail);
	}

	public static ScanError internalError() {
		return new ScanError(Code.INTERNAL_ERROR, DESC_INTERNAL_ERROR);
	}

	public static ScanError internalError(String detail) {
		return new ScanError(Code.INTERNAL_ERROR, detail);
	}

	public static ScanError withCode(Code code, String description) {
		return new ScanError(code, description);
	}


	// -------------------------------------------------------------------------
	//  Raw Dictionary builders (what emitSignal receives)
	// -------------------------------------------------------------------------

	public static Dictionary invalidImageDict() {
		return invalidImage().buildRawData();
	}

	public static Dictionary noCodeDetectedDict() {
		return noCodeDetected().buildRawData();
	}

	public static Dictionary scannerFailureDict() {
		return scannerFailure().buildRawData();
	}

	public static Dictionary internalErrorDict() {
		return internalError().buildRawData();
	}
}
