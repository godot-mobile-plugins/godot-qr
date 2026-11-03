#
# © 2026-present https://github.com/cengiz-pz
#

class_name ImageInfo extends RefCounted

const BUFFER_PROPERTY := &"buffer"
const WIDTH_PROPERTY := &"width"
const HEIGHT_PROPERTY := &"height"
const FORMAT_PROPERTY := &"format"
const HAS_MIPMAPS_PROPERTY := &"has_mipmaps"

var _data: Dictionary


func _init(a_data: Dictionary):
	_data = a_data


static func create_from_image(a_image: Image) -> ImageInfo:
	return ImageInfo.new(
		{
			BUFFER_PROPERTY: a_image.get_data(),
			WIDTH_PROPERTY: a_image.get_width(),
			HEIGHT_PROPERTY: a_image.get_height(),
			FORMAT_PROPERTY: a_image.get_format(),
			HAS_MIPMAPS_PROPERTY: a_image.has_mipmaps()
		}
	)


func to_image() -> Image:
	if not is_valid():
		push_error("ImageInfo: Cannot create image, missing required data.")
		return null
		
	return Image.create_from_data(
		get_width(),
		get_height(),
		has_mipmaps(),
		get_format(),
		get_buffer()
	)


func get_buffer() -> PackedByteArray:
	return _data[BUFFER_PROPERTY] if _data.has(BUFFER_PROPERTY) else []


func get_width() -> int:
	return _data[WIDTH_PROPERTY] if _data.has(WIDTH_PROPERTY) else 0


func get_height() -> int:
	return _data[HEIGHT_PROPERTY] if _data.has(HEIGHT_PROPERTY) else 0


func get_format() -> Image.Format:
	return _data[FORMAT_PROPERTY] if _data.has(FORMAT_PROPERTY) else Image.Format.FORMAT_RGBA8


func has_mipmaps() -> bool:
	return _data[HAS_MIPMAPS_PROPERTY] if _data.has(HAS_MIPMAPS_PROPERTY) else false


func is_valid() -> bool:
	return _data.has(WIDTH_PROPERTY) and _data.has(HEIGHT_PROPERTY) and _data.has(BUFFER_PROPERTY)


func get_raw_data() -> Dictionary:
	return _data
