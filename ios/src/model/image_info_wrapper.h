//
// © 2026-present https://github.com/cengiz-pz
//

#ifndef image_info_wrapper_h
#define image_info_wrapper_h

#import <Foundation/Foundation.h>

#include "core/object/object.h"
#include "core/object/class_db.h"

#import "qr_plugin-Swift.h"

@interface ImageInfoWrapper : NSObject

- (instancetype)init;
- (instancetype)initWithData:(Dictionary)data;
- (instancetype)initWithImageInfo:(ImageInfo *)imageInfo;

- (NSData *)getBuffer;
- (void)setBuffer:(NSData *)buffer;

- (NSInteger)getWidth;
- (void)setWidth:(NSInteger)width;

- (NSInteger)getHeight;
- (void)setHeight:(NSInteger)height;

- (NSInteger)getFormat;
- (void)setFormat:(NSInteger)format;

- (BOOL)hasMipmaps;
- (void)setHasMipmaps:(BOOL)hasMipmaps;

- (ImageInfo *)createImageInfo;

- (Dictionary)getRawData;

@end

#endif /* image_info_wrapper_h */
