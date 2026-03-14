//
// © 2026-present https://github.com/cengiz-pz
//

package org.godotengine.plugin.qr.model;

import org.godotengine.godot.Dictionary;


public class ImageInfo {
	private static final String CLASS_NAME = ImageInfo.class.getSimpleName();
	private static final String LOG_TAG = "godot::" + CLASS_NAME;

	private static final String DATA_BUFFER_PROPERTY = "buffer";
	private static final String DATA_WIDTH_PROPERTY = "width";
	private static final String DATA_HEIGHT_PROPERTY = "height";
	private static final String DATA_FORMAT_PROPERTY = "format";
	private static final String DATA_HAS_MIPMAPS_PROPERTY = "has_mipmaps";

	private Dictionary data;


	public ImageInfo() {
		this.data = new Dictionary();
	}


	public ImageInfo(Dictionary data) {
		this.data = data;
	}


	public byte[] getBuffer() {
		return data.containsKey(DATA_BUFFER_PROPERTY) ? (byte[]) data.get(DATA_BUFFER_PROPERTY) : null;
	}


	public void setBuffer(byte[] buffer) {
		data.put(DATA_BUFFER_PROPERTY, buffer);
	}


	public int getWidth() {
		return data.containsKey(DATA_WIDTH_PROPERTY) ? toInt(data.get(DATA_WIDTH_PROPERTY)) : -1;
	}


	public void setWidth(int width) {
		data.put(DATA_WIDTH_PROPERTY, (long) width);
	}


	public int getHeight() {
		return data.containsKey(DATA_HEIGHT_PROPERTY) ? toInt(data.get(DATA_HEIGHT_PROPERTY)) : -1;
	}


	public void setHeight(int height) {
		data.put(DATA_HEIGHT_PROPERTY, (long) height);
	}


	public int getFormat() {
		return data.containsKey(DATA_FORMAT_PROPERTY) ? toInt(data.get(DATA_FORMAT_PROPERTY)) : 3;
	}


	public void setFormat(int format) {
		data.put(DATA_FORMAT_PROPERTY, (long) format);
	}


	public boolean hasMipmaps() {
		return data.containsKey(DATA_HAS_MIPMAPS_PROPERTY) ? (boolean) data.get(DATA_HAS_MIPMAPS_PROPERTY) : false;
	}


	public void setHasMipmaps(boolean hasMipmaps) {
		data.put(DATA_HAS_MIPMAPS_PROPERTY, hasMipmaps);
	}


	public Dictionary getRawData() {
		return data;
	}


	private int toInt(Object godotInt) {
		return ((Long) godotInt).intValue();
	}
}
