//
// © 2026-present https://github.com/cengiz-pz
//

#import "qr_plugin.h"

#import "qr_plugin-Swift.h"

#import "qr_logger.h"


const String TEMPLATE_READY_SIGNAL = "template_ready";
// TODO: Define all signals


QRPlugin* QRPlugin::instance = NULL;


void QRPlugin::_bind_methods() {
	ClassDB::bind_method(D_METHOD("get_qr"), &QRPlugin::get_qr);
	// TODO: Register all methods

	ADD_SIGNAL(MethodInfo(TEMPLATE_READY_SIGNAL, PropertyInfo(Variant::DICTIONARY, "a_dict")));
	// TODO: Register all signals
}

Array QRPlugin::get_qr() {
	os_log_debug(qr_log, "::get_qr()");

	Array godot_array = Array();

	return godot_array;
}

QRPlugin::QRPlugin() {
	os_log_debug(qr_log, "Plugin singleton constructor");

	ERR_FAIL_COND(instance != NULL);

	instance = this;
}

QRPlugin::~QRPlugin() {
	os_log_debug(qr_log, "Plugin singleton destructor");

	if (instance == this) {
		instance = nullptr;
	}
}
