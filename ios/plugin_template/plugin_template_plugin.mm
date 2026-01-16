//
// © 2026-present https://github.com/cengiz-pz
//

#import "plugin_template_plugin.h"

#import "plugin_template_plugin-Swift.h"

#import "plugin_template_logger.h"


const String TEMPLATE_READY_SIGNAL = "template_ready";
// TODO: Define all signals


PluginTemplatePlugin* PluginTemplatePlugin::instance = NULL;


void PluginTemplatePlugin::_bind_methods() {
	ClassDB::bind_method(D_METHOD("get_plugin_template"), &PluginTemplatePlugin::get_plugin_template);
	// TODO: Register all methods

	ADD_SIGNAL(MethodInfo(TEMPLATE_READY_SIGNAL, PropertyInfo(Variant::DICTIONARY, "a_dict")));
	// TODO: Register all signals
}

Array PluginTemplatePlugin::get_plugin_template() {
	os_log_debug(plugin_template_log, "::get_plugin_template()");

	Array godot_array = Array();

	return godot_array;
}

PluginTemplatePlugin::PluginTemplatePlugin() {
	os_log_debug(plugin_template_log, "Plugin singleton constructor");

	ERR_FAIL_COND(instance != NULL);

	instance = this;
}

PluginTemplatePlugin::~PluginTemplatePlugin() {
	os_log_debug(plugin_template_log, "Plugin singleton destructor");

	if (instance == this) {
		instance = nullptr;
	}
}
