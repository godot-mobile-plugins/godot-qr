//
// © 2026-present https://github.com/cengiz-pz
//

#import "plugin_template_logger.h"

// Define and initialize the shared os_log_t instance
os_log_t plugin_template_log;

__attribute__((constructor)) // Automatically runs at program startup
static void initialize_plugin_template_log(void) {
	plugin_template_log = os_log_create("org.godotengine.plugin.plugin_template", "PluginTemplatePlugin");
}
