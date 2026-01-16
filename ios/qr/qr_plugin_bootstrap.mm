//
// © 2026-present https://github.com/cengiz-pz
//

#import <Foundation/Foundation.h>

#import "qr_plugin_bootstrap.h"
#import "qr_plugin.h"
#import "qr_logger.h"

#import "core/config/engine.h"


QRPlugin *qr_plugin;


void qr_plugin_init() {
	os_log_debug(qr_log, "QRPlugin: Initializing plugin at timestamp: %f", [[NSDate date] timeIntervalSince1970]);

	qr_plugin = memnew(QRPlugin);
	Engine::get_singleton()->add_singleton(Engine::Singleton("QRPlugin", qr_plugin));
	os_log_debug(qr_log, "QRPlugin: Singleton registered");
}


void qr_plugin_deinit() {
	os_log_debug(qr_log, "QRPlugin: Deinitializing plugin");
	qr_log = NULL; // Prevent accidental reuse

	if (qr_plugin) {
		memdelete(qr_plugin);
		qr_plugin = nullptr;
	}
}
