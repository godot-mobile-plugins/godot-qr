//
// © 2026-present https://github.com/cengiz-pz
//

#import "scan_error.h"


static String const kCodeProperty = "code";
static String const kDescriptionProperty = "description";


@implementation ScanError

- (instancetype) initWithScanResult:(ScanResult *)scanResult {
	self = [super init];
	if (self) {
		_scanResult = scanResult;
	}
	return self;
}

- (Dictionary) buildRawData {
	Dictionary dict = Dictionary();

	dict[kCodeProperty] = (int) self.scanResult.code;
	dict[kDescriptionProperty] = [self.scanResult.resultDescription UTF8String];

	return dict;
}

@end
