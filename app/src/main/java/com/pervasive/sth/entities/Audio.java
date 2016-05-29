package com.pervasive.sth.entities;

import java.util.Arrays;

/**
 * Created by Alex on 29/05/2016.
 */
public class Audio {

    private final String _audioName;
    private final byte[] _data;

    public Audio(String audioName, byte[] data) {
        _audioName = audioName;
        _data = data;
    }

    public String get_audioName() {
        return _audioName;
    }

    public byte[] get_data() {
        return _data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Audio audio = (Audio) o;

        if (_audioName != null ? !_audioName.equals(audio._audioName) : audio._audioName != null)
            return false;
        return Arrays.equals(_data, audio._data);

    }

    @Override
    public int hashCode() {
        int result = _audioName != null ? _audioName.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(_data);
        return result;
    }
}
