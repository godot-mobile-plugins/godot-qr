#
# © 2026-present https://github.com/cengiz-pz
#

@tool
@icon("icon.png")
class_name PluginTemplate extends Node

signal template_ready(a_dict: Dictionary)

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
			PluginTemplate.log_error("%s singleton not found on this platform!" % PLUGIN_SINGLETON_NAME)


func _connect_signals() -> void:
	_plugin_singleton.connect("template_ready", _on_template_ready)


func get_plugin_template() -> Array:
	var __result: Array = []

	if _plugin_singleton:
		__result = _plugin_singleton.get_plugin_template()
	else:
		PluginTemplate.log_error("%s plugin not initialized" % PLUGIN_SINGLETON_NAME)

	return __result


func _on_template_ready(a_dict: Dictionary) -> void:
	template_ready.emit(a_dict)


static func log_error(a_description: String) -> void:
	push_error("%s: %s" % [PLUGIN_SINGLETON_NAME, a_description])


static func log_warn(a_description: String) -> void:
	push_warning("%s: %s" % [PLUGIN_SINGLETON_NAME, a_description])


static func log_info(a_description: String) -> void:
	print_rich("[color=lime]%s: INFO: %s[/color]" % [PLUGIN_SINGLETON_NAME, a_description])
