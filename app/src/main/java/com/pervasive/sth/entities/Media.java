package com.pervasive.sth.entities;

import java.util.Arrays;

/**
 * Created by Alex on 29/05/2016.
 */
public class Media {

    private final String _mediaName;
    private final byte[] _data;

    public Media(String mediaName, byte[] data) {
        _mediaName = mediaName;
        _data = data;
    }

    public String get_mediaName() {
        return _mediaName;
    }

    public byte[] get_data() {
        return _data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Media media = (Media) o;

        if (_mediaName != null ? !_mediaName.equals(media._mediaName) : media._mediaName != null)
            return false;
        return Arrays.equals(_data, media._data);

    }

    @Override
    public int hashCode() {
        int result = _mediaName != null ? _mediaName.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(_data);
        return result;
    }
}
