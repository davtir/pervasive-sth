package com.pervasive.sth.entities;

import java.util.Arrays;

/**
 * @brief this class represent the media files
 */
public class Media {

	/*
	 * the name of the media file
	 */
	private final String _mediaName;

	/*
	 * array of bytes representing the media content
	 */
	private final byte[] _data;

	/**
	 *
	 * @param mediaName
	 * @param data
	 * @brief initialize media file fields
	 */
	public Media(String mediaName, byte[] data) {
		_mediaName = mediaName;
		_data = data;
	}

	public String getMediaName() {
		return _mediaName;
	}

	public byte[] getData() {
		return _data;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Media media = (Media) o;
		if (_mediaName != null ? !_mediaName.equals(media._mediaName) : media._mediaName != null) {
			return false;
		}
		return Arrays.equals(_data, media._data);

	}

	@Override
	public int hashCode() {
		int result = _mediaName != null ? _mediaName.hashCode() : 0;
		result = 31 * result + Arrays.hashCode(_data);
		return result;
	}
}
