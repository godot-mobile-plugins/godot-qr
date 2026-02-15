//
// © 2026-present https://github.com/cengiz-pz
//

#import "qr_plugin.h"

#import "qr_plugin-Swift.h"
#import "qr_logger.h"
#import "image_info_wrapper.h"
#import "scan_error.h"


const String QR_DETECTED_SIGNAL = "qr_detected";
const String QR_SCAN_FAILED_SIGNAL = "qr_scan_failed";


QRPlugin* QRPlugin::instance = NULL;
static QR *qr = nil;


void QRPlugin::_bind_methods() {
	ClassDB::bind_method(D_METHOD("generate_qr"), &QRPlugin::generate_qr);
	ClassDB::bind_method(D_METHOD("scan_qr"), &QRPlugin::scan_qr);

	ADD_SIGNAL(MethodInfo(QR_DETECTED_SIGNAL, PropertyInfo(Variant::STRING, "a_uri")));
	ADD_SIGNAL(MethodInfo(QR_SCAN_FAILED_SIGNAL, PropertyInfo(Variant::DICTIONARY, "a_dict")));
}

Dictionary QRPlugin::generate_qr(String uri, int size, int foreground, int background) {
	os_log_debug(qr_log, "::generate_qr()");

	NSString *nsUri = [NSString stringWithUTF8String:uri.utf8().get_data()];

	ImageInfo *imageInfo = [qr generateQRWithUri:nsUri size:size foreground:foreground background:background];

	if (imageInfo == nil) {
		os_log_error(qr_log, "generate_qr() failed");
		return Dictionary(); // Handle error case
	}

	ImageInfoWrapper *wrapper = [[ImageInfoWrapper alloc] initWithImageInfo: imageInfo];

	return [wrapper getRawData];
}

void QRPlugin::scan_qr(Dictionary imageDict) {
	os_log_debug(qr_log, "::scan_qr()");

	ImageInfoWrapper *imageInfoWrapper = [[ImageInfoWrapper alloc] initWithData: imageDict];
	ScanResult *result = [qr scanQR:[imageInfoWrapper createImageInfo]];

	if (result.code == CodeSUCCESS) {
		emit_signal(QR_DETECTED_SIGNAL, [result.uri UTF8String]);
	} else {
		ScanError *scanError = [[ScanError alloc] initWithScanResult: result];
		emit_signal(QR_SCAN_FAILED_SIGNAL, [scanError buildRawData]);
	}
}

QRPlugin::QRPlugin() {
	os_log_debug(qr_log, "Plugin singleton constructor");

	ERR_FAIL_COND(instance != NULL);

	instance = this;

	if (qr == nil) {
		qr = [[QR alloc] init];
	}
}

QRPlugin::~QRPlugin() {
	os_log_debug(qr_log, "Plugin singleton destructor");

	if (instance == this) {
		instance = nullptr;
	}
}
