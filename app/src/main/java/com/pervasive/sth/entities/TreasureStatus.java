package com.pervasive.sth.entities;

/**
 * Created by Alex on 03/07/2016.
 */
public class TreasureStatus {
    private boolean _found;
    private Media _winner;

    public TreasureStatus(boolean found) {
        _found = found;
    }

    public boolean isFound() {
        return _found;
    }

    public void setFound(boolean _found) {
        this._found = _found;
    }

    public Media getWinner() {
        return _winner;
    }

    public void setWinner(Media _winner) {
        this._winner = _winner;
    }
}

