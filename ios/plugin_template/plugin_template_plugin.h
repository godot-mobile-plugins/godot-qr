//
// © 2026-present https://github.com/cengiz-pz
//

#ifndef plugin_template_plugin_h
#define plugin_template_plugin_h

#import <Foundation/Foundation.h>

#include "core/object/object.h"
#include "core/object/class_db.h"


@class PluginTemplate;


extern const String TEMPLATE_READY_SIGNAL;
// TODO: Declare all signals


class PluginTemplatePlugin : public Object {
	GDCLASS(PluginTemplatePlugin, Object);

private:
	static PluginTemplatePlugin* instance; // Singleton instance

	static void _bind_methods();

public:
	Array get_plugin_template();
	// TODO: Declare all methods

	PluginTemplatePlugin();
	~PluginTemplatePlugin();
};

#endif /* plugin_template_plugin_h */
