//
// © 2026-present https://github.com/cengiz-pz
//

#import "image_info_wrapper.h"

static String const kBufferProperty = "buffer";
static String const kWidthProperty = "width";
static String const kHeightProperty = "height";
static String const kFormatProperty = "format";
static String const kHasMipmapsProperty = "has_mipmaps";

@interface ImageInfoWrapper ()

@property (nonatomic) Dictionary data;

@end

@implementation ImageInfoWrapper

- (instancetype)init {
	self = [super init];
	if (self) {
		_data = Dictionary();
	}
	return self;
}

- (instancetype)initWithData:(Dictionary)data {
	self = [super init];
	if (self) {
		_data = data;
	}
	return self;
}

- (instancetype)initWithImageInfo:(ImageInfo *)imageInfo {
	self = [super init];
	if (self) {
		_data = Dictionary();
		if (imageInfo.buffer) {
			[self setBuffer:imageInfo.buffer];
		}
		[self setWidth:imageInfo.width];
		[self setHeight:imageInfo.height];
		[self setFormat:imageInfo.format];
		[self setHasMipmaps:imageInfo.hasMipmaps];
	}
	return self;
}

- (NSData *)getBuffer {
	NSData *data = nil;
	if (self.data.has(kBufferProperty)) {
		PackedByteArray pba = self.data[kBufferProperty];
		data = [NSData dataWithBytes:pba.ptr() length:pba.size()];
	}
	return data;
}

- (void)setBuffer:(NSData *)buffer {
	PackedByteArray pba = PackedByteArray();
	pba.resize((int)[buffer length]);
	memcpy(pba.ptrw(), [buffer bytes], [buffer length]);
	self.data[kBufferProperty] = pba;
}

- (NSInteger)getWidth {
	if (self.data.has(kWidthProperty)) {
		return (NSInteger)self.data[kWidthProperty].operator int64_t();
	}
	return -1;
}

- (void)setWidth:(NSInteger)width {
	self.data[kWidthProperty] = (int)width;
}

- (NSInteger)getHeight {
	if (self.data.has(kHeightProperty)) {
		return (NSInteger)self.data[kHeightProperty].operator int64_t();
	}
	return -1;
}

- (void)setHeight:(NSInteger)height {
	self.data[kHeightProperty] = (int)height;
}

- (NSInteger)getFormat {
	if (self.data.has(kFormatProperty)) {
		return (NSInteger)self.data[kFormatProperty].operator int64_t();
	}
	return 3;
}

- (void)setFormat:(NSInteger)format {
	self.data[kFormatProperty] = (int)format;
}

- (BOOL)hasMipmaps {
	if (self.data.has(kHasMipmapsProperty)) {
		return (BOOL)self.data[kHasMipmapsProperty];
	}
	return NO;
}

- (void)setHasMipmaps:(BOOL)hasMipmaps {
	self.data[kHasMipmapsProperty] = (BOOL)hasMipmaps;
}

- (ImageInfo *)createImageInfo {
	NSData *buffer = [self getBuffer];
	NSInteger width = [self getWidth];
	NSInteger height = [self getHeight];
	NSInteger format = [self getFormat];
	BOOL hasMipmaps = [self hasMipmaps];
	return [[ImageInfo alloc] initWithBuffer:buffer width:width height:height format:format hasMipmaps:hasMipmaps];
}

- (Dictionary)getRawData {
	return _data;
}

@end
