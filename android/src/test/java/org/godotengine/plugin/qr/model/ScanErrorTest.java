//
// © 2026-present https://github.com/cengiz-pz
//

package org.godotengine.plugin.qr.model;

import org.godotengine.godot.Dictionary;
import org.godotengine.plugin.qr.fixtures.ScanErrorFixtures;
import org.godotengine.plugin.qr.model.ScanError.Code;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DisplayName("ScanError")
class ScanErrorTest {

	// -------------------------------------------------------------------------
	//  Code enum
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Code enum")
	class CodeEnum {

		@Test
		@DisplayName("NONE is the first value (ordinal 0)")
		void none_isOrdinalZero() {
			assertEquals(0, Code.NONE.ordinal());
		}

		@Test
		@DisplayName("all expected codes are present")
		void allExpectedCodesPresent() {
			Code[] codes = Code.values();
			assertEquals(5, codes.length,
					"Expected exactly 5 codes: NONE, INVALID_IMAGE, NO_CODE_DETECTED, " +
					"SCANNER_FAILURE, INTERNAL_ERROR");
		}

		@ParameterizedTest(name = "Code.{0} exists")
		@EnumSource(Code.class)
		@DisplayName("every Code can be used to construct a ScanError")
		void everyCode_canConstructScanError(Code code) {
			ScanError error = new ScanError(code, "test description");
			assertNotNull(error.buildRawData());
		}
	}


	// -------------------------------------------------------------------------
	//  buildRawData structure
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("buildRawData()")
	class BuildRawData {

		@Test
		@DisplayName("returns a non-null Dictionary")
		void returnsNonNull() {
			assertNotNull(ScanErrorFixtures.invalidImage().buildRawData());
		}

		@Test
		@DisplayName("dictionary contains the 'code' key")
		void containsCodeKey() {
			Dictionary dict = ScanErrorFixtures.invalidImage().buildRawData();
			assertTrue(dict.containsKey("code"),
					"Dictionary must have key 'code'");
		}

		@Test
		@DisplayName("dictionary contains the 'description' key")
		void containsDescriptionKey() {
			Dictionary dict = ScanErrorFixtures.invalidImage().buildRawData();
			assertTrue(dict.containsKey("description"),
					"Dictionary must have key 'description'");
		}

		@Test
		@DisplayName("dictionary has exactly two entries")
		void hasTwoEntries() {
			Dictionary dict = ScanErrorFixtures.internalError().buildRawData();
			assertEquals(2, dict.size());
		}

		@Test
		@DisplayName("'code' value is the exact Code enum instance")
		void codeValue_isEnumInstance() {
			Dictionary dict = new ScanError(Code.NO_CODE_DETECTED, "none").buildRawData();
			assertSame(Code.NO_CODE_DETECTED, dict.get("code"));
		}

		@Test
		@DisplayName("'description' value matches the string passed to constructor")
		void descriptionValue_matchesConstructorArg() {
			String desc = "Custom description text";
			Dictionary dict = new ScanError(Code.SCANNER_FAILURE, desc).buildRawData();
			assertEquals(desc, dict.get("description"));
		}

		@Test
		@DisplayName("description can be null (graceful handling of null messages)")
		void description_canBeNull() {
			Dictionary dict = new ScanError(Code.INTERNAL_ERROR, null).buildRawData();
			assertNull(dict.get("description"));
		}

		@Test
		@DisplayName("description can be an empty string")
		void description_canBeEmpty() {
			Dictionary dict = new ScanError(Code.SCANNER_FAILURE, "").buildRawData();
			assertEquals("", dict.get("description"));
		}

		@Test
		@DisplayName("each call to buildRawData returns a NEW Dictionary instance")
		void buildRawData_returnsNewInstance() {
			ScanError error = ScanErrorFixtures.noCodeDetected();
			Dictionary first  = error.buildRawData();
			Dictionary second = error.buildRawData();
			assertNotSame(first, second,
					"buildRawData() must not cache/return the same Dictionary reference");
		}

		@Test
		@DisplayName("successive calls produce equal Dictionaries")
		void buildRawData_idempotent() {
			ScanError error = ScanErrorFixtures.noCodeDetected();
			Dictionary first  = error.buildRawData();
			Dictionary second = error.buildRawData();
			assertEquals(first.get("code"),        second.get("code"));
			assertEquals(first.get("description"), second.get("description"));
		}
	}


	// -------------------------------------------------------------------------
	//  Per-code contract checks
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("per-code contracts")
	class PerCodeContracts {

		static Stream<Arguments> codeAndDescription() {
			return Stream.of(
				Arguments.of(Code.NONE,              "No error"),
				Arguments.of(Code.INVALID_IMAGE,     ScanErrorFixtures.DESC_INVALID_IMAGE),
				Arguments.of(Code.NO_CODE_DETECTED,  ScanErrorFixtures.DESC_NO_CODE),
				Arguments.of(Code.SCANNER_FAILURE,   ScanErrorFixtures.DESC_SCANNER_FAILURE),
				Arguments.of(Code.INTERNAL_ERROR,    ScanErrorFixtures.DESC_INTERNAL_ERROR)
			);
		}

		@ParameterizedTest(name = "{0} → description stored correctly")
		@MethodSource("codeAndDescription")
		@DisplayName("code and description are stored correctly for every Code value")
		void codeAndDescription_storedCorrectly(Code code, String description) {
			Dictionary dict = new ScanError(code, description).buildRawData();
			assertAll(
					() -> assertSame(code, dict.get("code"),
							"code mismatch for " + code),
					() -> assertEquals(description, dict.get("description"),
							"description mismatch for " + code)
			);
		}
	}


	// -------------------------------------------------------------------------
	//  Fixture sanity – ensures ScanErrorFixtures are self-consistent
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("ScanErrorFixtures sanity")
	class FixtureSanity {

		@Test
		@DisplayName("invalidImage() fixture builds INVALID_IMAGE code")
		void invalidImageFixture_hasCorrectCode() {
			Dictionary dict = ScanErrorFixtures.invalidImageDict();
			assertSame(Code.INVALID_IMAGE, dict.get("code"));
		}

		@Test
		@DisplayName("noCodeDetected() fixture builds NO_CODE_DETECTED code")
		void noCodeDetectedFixture_hasCorrectCode() {
			Dictionary dict = ScanErrorFixtures.noCodeDetectedDict();
			assertSame(Code.NO_CODE_DETECTED, dict.get("code"));
		}

		@Test
		@DisplayName("scannerFailure() fixture builds SCANNER_FAILURE code")
		void scannerFailureFixture_hasCorrectCode() {
			Dictionary dict = ScanErrorFixtures.scannerFailureDict();
			assertSame(Code.SCANNER_FAILURE, dict.get("code"));
		}

		@Test
		@DisplayName("internalError() fixture builds INTERNAL_ERROR code")
		void internalErrorFixture_hasCorrectCode() {
			Dictionary dict = ScanErrorFixtures.internalErrorDict();
			assertSame(Code.INTERNAL_ERROR, dict.get("code"));
		}

		@Test
		@DisplayName("scannerFailure(detail) fixture stores the custom description")
		void scannerFailureWithDetail_storesDescription() {
			String detail = "MLKit process error: timeout";
			Dictionary dict = ScanErrorFixtures.scannerFailure(detail).buildRawData();
			assertEquals(detail, dict.get("description"));
		}

		@Test
		@DisplayName("internalError(detail) fixture stores the custom description")
		void internalErrorWithDetail_storesDescription() {
			String detail = "NullPointerException in processImage";
			Dictionary dict = ScanErrorFixtures.internalError(detail).buildRawData();
			assertEquals(detail, dict.get("description"));
		}
	}
}
