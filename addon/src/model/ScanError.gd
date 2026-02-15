#
# © 2026-present https://github.com/cengiz-pz
#

class_name ScanError extends RefCounted

enum Code {
	NONE,
	INVALID_IMAGE,
	NO_CODE_DETECTED,
	SCANNER_FAILURE,
	INTERNAL_ERROR
}

const DATA_CODE_PROPERTY: String = "code"
const DATA_DESCRIPTION_PROPERTY: String = "description"

var _data: Dictionary


func _init(a_data: Dictionary):
	_data = a_data


func get_code() -> Code:
	return _data[DATA_CODE_PROPERTY]


func get_description() -> String:
	return _data[DATA_DESCRIPTION_PROPERTY]
