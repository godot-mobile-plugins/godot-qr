#
# © 2026-present https://github.com/cengiz-pz
#

@tool
@icon("icon.png")
class_name QR extends Node

signal qr_detected(data: String)
signal qr_scan_failed(error: ScanError)

const PLUGIN_SINGLETON_NAME: String = "@pluginName@"

var _plugin_singleton: Object


func _ready() -> void:
	_update_plugin()


func _notification(a_what: int) -> void:
	if a_what == NOTIFICATION_APPLICATION_RESUMED:
		_update_plugin()


func _update_plugin() -> void:
	if _plugin_singleton == null:
		if Engine.has_singleton(PLUGIN_SINGLETON_NAME):
			_plugin_singleton = Engine.get_singleton(PLUGIN_SINGLETON_NAME)
			_connect_signals()
		elif not Engine.is_editor_hint():
			QR.log_error("%s singleton not found on this platform!" % PLUGIN_SINGLETON_NAME)


func _connect_signals() -> void:
	_plugin_singleton.connect("qr_detected", _on_qr_detected)
	_plugin_singleton.connect("qr_scan_failed", _on_qr_scan_failed)


func generate_qr_image(
	a_uri: String,
	a_size: int = 512,
	a_foreground: Color = Color.BLACK,
	a_background: Color = Color.WHITE
) -> Image:

	var __img: Image

	if _plugin_singleton:
		# Convert Godot Colors (floats) to ARGB 32-bit integers
		var fg_argb: int = a_foreground.to_argb32()
		var bg_argb: int = a_background.to_argb32()

		var result: Dictionary = _plugin_singleton.generate_qr(a_uri, a_size, fg_argb, bg_argb)
		var __image_info = ImageInfo.new(result)
		if __image_info.is_valid():
			__img = Image.create_from_data(
				__image_info.get_width(),
				__image_info.get_height(),
				false,
				Image.FORMAT_RGBA8,
				__image_info.get_buffer()
			)
		else:
			QR.log_error("Failed to generate QR image: Invalid data returned from plugin.")
	else:
		QR.log_error("%s plugin not initialized" % PLUGIN_SINGLETON_NAME)

	return __img


func generate_qr_texture(
	a_uri: String,
	a_size: int = 512,
	a_foreground: Color = Color.BLACK,
	a_background: Color = Color.WHITE
) -> ImageTexture:

	var img := generate_qr_image(
		a_uri,
		a_size,
		a_foreground,
		a_background
	)

	return ImageTexture.create_from_image(img)


func scan_qr_image(a_image: Image) -> void:
	if _plugin_singleton:
		# Ensure image is in RGBA8 format as expected by the Android Bitmap logic
		var scan_image := a_image
		if scan_image.get_format() != Image.FORMAT_RGBA8:
			scan_image = Image.new()
			scan_image.copy_from(a_image)
			scan_image.convert(Image.FORMAT_RGBA8)

		# Pass the raw dictionary data to the plugin
		_plugin_singleton.scan_qr(ImageInfo.create_from_image(scan_image).get_raw_data())
	else:
		log_error("%s plugin not initialized" % PLUGIN_SINGLETON_NAME)


func _on_qr_detected(a_dict: String) -> void:
	qr_detected.emit(a_dict)


func _on_qr_scan_failed(a_dict: Dictionary) -> void:
	qr_scan_failed.emit(ScanError.new(a_dict))


static func log_error(a_description: String) -> void:
	push_error("%s: %s" % [PLUGIN_SINGLETON_NAME, a_description])


static func log_warn(a_description: String) -> void:
	push_warning("%s: %s" % [PLUGIN_SINGLETON_NAME, a_description])


static func log_info(a_description: String) -> void:
	print_rich("[color=lime]%s: INFO: %s[/color]" % [PLUGIN_SINGLETON_NAME, a_description])
