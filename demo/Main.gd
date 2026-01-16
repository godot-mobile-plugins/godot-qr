#
# © 2026-present https://github.com/cengiz-pz
#

extends Node

@onready var plugin_template_node: PluginTemplate = $PluginTemplate
@onready var get_plugin_template_button: Button = $CanvasLayer/MainContainer/VBoxContainer/GetStateButton
@onready var _label: RichTextLabel = $CanvasLayer/MainContainer/VBoxContainer/RichTextLabel as RichTextLabel
@onready var _android_texture_rect: TextureRect = $CanvasLayer/MainContainer/VBoxContainer/TextureHBoxContainer/AndroidTextureRect as TextureRect
@onready var _ios_texture_rect: TextureRect = $CanvasLayer/MainContainer/VBoxContainer/TextureHBoxContainer/iOSTextureRect as TextureRect

var _active_texture_rect: TextureRect


func _ready() -> void:
	if OS.has_feature("ios"):
		_android_texture_rect.hide()
		_active_texture_rect = _ios_texture_rect
	else:
		_ios_texture_rect.hide()
		_active_texture_rect = _android_texture_rect


func _on_get_button_pressed() -> void:
	_print_to_screen("Get button pressed")


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
