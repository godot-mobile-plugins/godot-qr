//
// © 2026-present https://github.com/cengiz-pz
//

#import "qr_logger.h"

// Define and initialize the shared os_log_t instance
os_log_t qr_log;

__attribute__((constructor)) // Automatically runs at program startup
static void initialize_qr_log(void) {
	qr_log = os_log_create("org.godotengine.plugin.qr", "QRPlugin");
}
