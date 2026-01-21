#
# © 2026-present https://github.com/cengiz-pz
#

class_name ImageInfo extends RefCounted

const DATA_BUFFER_PROPERTY: String = "buffer"
const DATA_WIDTH_PROPERTY: String = "width"
const DATA_HEIGHT_PROPERTY: String = "height"
const DATA_FORMAT_PROPERTY: String = "format"
const DATA_HAS_MIPMAPS_PROPERTY: String = "has_mipmaps"

var _data: Dictionary


func _init(a_data: Dictionary):
	_data = a_data


static func create_from_image(a_image: Image) -> ImageInfo:
	return ImageInfo.new(
		{
			DATA_BUFFER_PROPERTY: a_image.get_data(),
			DATA_WIDTH_PROPERTY: a_image.get_width(),
			DATA_HEIGHT_PROPERTY: a_image.get_height(),
			DATA_FORMAT_PROPERTY: a_image.get_format(),
			DATA_HAS_MIPMAPS_PROPERTY: a_image.has_mipmaps()
		}
	)


func get_buffer() -> PackedByteArray:
	return _data[DATA_BUFFER_PROPERTY] if _data.has(DATA_BUFFER_PROPERTY) else []


func get_width() -> int:
	return _data[DATA_WIDTH_PROPERTY] if _data.has(DATA_WIDTH_PROPERTY) else 0


func get_height() -> int:
	return _data[DATA_HEIGHT_PROPERTY] if _data.has(DATA_HEIGHT_PROPERTY) else 0


func get_format() -> Image.Format:
	return _data[DATA_FORMAT_PROPERTY] if _data.has(DATA_FORMAT_PROPERTY) else Image.Format.FORMAT_RGBA8


func has_mipmaps() -> bool:
	return _data[DATA_HAS_MIPMAPS_PROPERTY] if _data.has(DATA_HAS_MIPMAPS_PROPERTY) else false


func is_valid() -> bool:
	return _data.has(DATA_WIDTH_PROPERTY) \
			and _data.has(DATA_HEIGHT_PROPERTY) \
			and _data.has(DATA_BUFFER_PROPERTY)


func get_raw_data() -> Dictionary:
	return _data
