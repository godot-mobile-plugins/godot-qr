#
# © 2026-present https://github.com/cengiz-pz
#

@tool
extends Node

@export_color_no_alpha var foreground_color: Color = Color.BLACK
@export_color_no_alpha var background_color: Color = Color.WHITE

@onready var qr_node: QR = $QR
@onready var camera_node: NativeCamera = $NativeCamera
@onready var start_button: Button = $CanvasLayer/MainContainer/VBoxContainer/TabContainer/Scan/ButtonsVBC/StartButton
@onready var stop_button: Button = $CanvasLayer/MainContainer/VBoxContainer/TabContainer/Scan/ButtonsVBC/StopButton
@onready var uri_label: Label = $CanvasLayer/MainContainer/VBoxContainer/TabContainer/Scan/ButtonsVBC/UriLabel
@onready var scan_texture_rect: TextureRect = $CanvasLayer/MainContainer/VBoxContainer/TabContainer/Scan/ScanTextureRect
@onready var uri_text_edit: TextEdit = $CanvasLayer/MainContainer/VBoxContainer/TabContainer/Generate/ButtonsVBC/UriTextEdit
@onready var background_rect: ColorRect = $CanvasLayer/MainContainer/VBoxContainer/TabContainer/Generate/ButtonsVBC/ColorHBC/BgColorRect
@onready var foreground_rect: ColorRect = $CanvasLayer/MainContainer/VBoxContainer/TabContainer/Generate/ButtonsVBC/ColorHBC/FgColorRect
@onready var generate_button: Button = $CanvasLayer/MainContainer/VBoxContainer/TabContainer/Generate/ButtonsVBC/GenerateButton
@onready var generate_texture_rect: TextureRect = $CanvasLayer/MainContainer/VBoxContainer/TabContainer/Generate/GenerateTextureRect
@onready var _label: RichTextLabel = $CanvasLayer/MainContainer/VBoxContainer/RichTextLabel as RichTextLabel
@onready var _android_texture_rect: TextureRect = $CanvasLayer/MainContainer/VBoxContainer/TextureHBC/AndroidTextureRect as TextureRect
@onready var _ios_texture_rect: TextureRect = $CanvasLayer/MainContainer/VBoxContainer/TextureHBC/iOSTextureRect as TextureRect

var _camera: CameraInfo
var _scan_texture: ImageTexture = null
var _generate_texture: ImageTexture = null

var _active_texture_rect: TextureRect


func _ready() -> void:
	if OS.has_feature("ios"):
		_android_texture_rect.hide()
		_active_texture_rect = _ios_texture_rect
	else:
		_ios_texture_rect.hide()
		_active_texture_rect = _android_texture_rect

	foreground_rect.color = foreground_color
	background_rect.color = background_color

	if not Engine.is_editor_hint():
		if camera_node.has_camera_permission():
			_get_cameras()
		else:
			camera_node.request_camera_permission()


func _on_start_button_pressed() -> void:
	if _camera:
		camera_node.start(camera_node.create_feed_request()
			.set_camera_id(_camera.get_camera_id())
			.set_rotation(90))
		start_button.disabled = true
		stop_button.disabled = false
		_print_to_screen("Camera started [id: %s]" % [_camera.get_camera_id()])
	else:
		_print_to_screen("No camera available", true)


func _on_stop_button_pressed() -> void:
	camera_node.stop()
	stop_button.disabled = true
	start_button.disabled = false
	# Clear the texture visually so we know it stopped
	scan_texture_rect.texture = null
	_scan_texture = null
	_print_to_screen("Camera stopped")


func _get_cameras() -> void:
	var __cameras: Array[CameraInfo] = camera_node.get_all_cameras()
	if __cameras.is_empty():
		_print_to_screen("No camera found!", true)
	else:
		_camera = __cameras[0]
		start_button.disabled = false


func _update_scan_texture(image: Image) -> void:
	if _scan_texture == null:
		_scan_texture = ImageTexture.create_from_image(image)
		scan_texture_rect.texture = _scan_texture
	else:
		_scan_texture.update(image)


func _on_generate_button_pressed() -> void:
	if uri_text_edit.text.is_empty():
		_print_to_screen("URI is empty", true)
		return

	var __image := qr_node.generate_qr_image(
		uri_text_edit.text,
		256,
		foreground_color,
		background_color
	)

	_update_generate_texture(__image)


func _update_generate_texture(image: Image) -> void:
	if _generate_texture == null:
		_generate_texture = ImageTexture.create_from_image(image)
		generate_texture_rect.texture = _generate_texture
	else:
		_generate_texture.update(image)


func _on_qr_qr_detected(data: String) -> void:
	uri_label.text = data
	_print_to_screen("QR detected: '%s'" % data)


func _on_qr_qr_scan_failed(error: ScanError) -> void:
	_print_to_screen("QR scan failed: '%s'" % error.get_description())


func _on_native_camera_frame_available(a_info: FrameInfo) -> void:
	var image: Image = a_info.get_image()
	if image.is_empty():
		_print_to_screen("Received empty image!", true)
		return

	_print_to_screen("Scanning received frame")
	qr_node.scan_qr_image(image)

	# Force execution on main thread
	call_deferred("_update_scan_texture", image)


func _on_native_camera_camera_permission_granted() -> void:
	_print_to_screen("Camera permission granted")
	_get_cameras()


func _on_native_camera_camera_permission_denied() -> void:
	_print_to_screen("Camera permission denied")


func _print_to_screen(a_message: String, a_is_error: bool = false) -> void:
	if a_is_error:
		_label.push_color(Color.CRIMSON)

	_label.add_text("%s\n\n" % a_message)

	if a_is_error:
		_label.pop()
		printerr("Demo app:: " + a_message)
	else:
		print("Demo app:: " + a_message)

	_label.scroll_to_line(_label.get_line_count() - 1)
