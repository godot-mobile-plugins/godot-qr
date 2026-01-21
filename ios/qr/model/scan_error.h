//
// © 2026-present https://github.com/cengiz-pz
//

#ifndef scan_error_h
#define scan_error_h

#import <Foundation/Foundation.h>

#include "core/object/class_db.h"

#import "qr_plugin-Swift.h"


@interface ScanError : NSObject

@property (nonatomic, strong) ScanResult *scanResult;

- (instancetype) initWithScanResult:(ScanResult *)scanResult;

/**
 * Builds a Godot-compatible Dictionary containing the scan result data
 * @return A Dictionary object with the scan error info
 */
- (Dictionary) buildRawData;

@end

#endif /* scan_error_h */
