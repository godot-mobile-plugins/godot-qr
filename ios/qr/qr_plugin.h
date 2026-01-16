//
// © 2026-present https://github.com/cengiz-pz
//

#ifndef qr_plugin_h
#define qr_plugin_h

#import <Foundation/Foundation.h>

#include "core/object/object.h"
#include "core/object/class_db.h"


@class QR;


extern const String TEMPLATE_READY_SIGNAL;
// TODO: Declare all signals


class QRPlugin : public Object {
	GDCLASS(QRPlugin, Object);

private:
	static QRPlugin* instance; // Singleton instance

	static void _bind_methods();

public:
	Array get_qr();
	// TODO: Declare all methods

	QRPlugin();
	~QRPlugin();
};

#endif /* qr_plugin_h */
