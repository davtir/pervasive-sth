package com.pervasive.sth.entities;

import java.util.Arrays;

/**
 * Created by Alex on 29/05/2016.
 */
public class Media {

	private final String mediaName_;
	private final byte[] data_;

	public Media(String mediaName, byte[] data) {
		mediaName_ = mediaName;
		data_ = data;
	}

	public String getMediaName() {
		return mediaName_;
	}

	public byte[] getData() {
		return data_;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		Media media = (Media) o;
		if ( mediaName_ != null ? !mediaName_.equals(media.mediaName_) : media.mediaName_ != null ) {
			return false;
		}
		return Arrays.equals(data_, media.data_);

	}

	@Override
	public int hashCode() {
		int result = mediaName_ != null ? mediaName_.hashCode() : 0;
		result = 31 * result + Arrays.hashCode(data_);
		return result;
	}
}
