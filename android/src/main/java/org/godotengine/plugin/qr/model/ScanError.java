//
// © 2026-present https://github.com/cengiz-pz
//

package org.godotengine.plugin.qr.model;

import android.util.Log;

import org.godotengine.godot.Dictionary;


public class ScanError {
	public enum Code {
		NONE,
		INVALID_IMAGE,
		NO_CODE_DETECTED,
		SCANNER_FAILURE,
		INTERNAL_ERROR
	}

	private static final String CLASS_NAME = ScanError.class.getSimpleName();
	private static final String LOG_TAG = "godot::" + CLASS_NAME;

	private static final String DATA_CODE_PROPERTY = "code";
	private static final String DATA_DESCRIPTION_PROPERTY = "description";

	private Code code;
	private String description;

	public ScanError(Code code, String description) {
		this.code = code;
		this.description = description;
	}

	public Dictionary buildRawData() {
		Dictionary dict = new Dictionary();

		dict.put(DATA_CODE_PROPERTY, code);
		dict.put(DATA_DESCRIPTION_PROPERTY, description);

		return dict;
	}
}
