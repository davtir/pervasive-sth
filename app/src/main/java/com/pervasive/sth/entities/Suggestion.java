package com.pervasive.sth.entities;

import java.io.Serializable;

/**
 * Created by Alex on 25/06/2016.
 */
@SuppressWarnings("serial")
public class Suggestion implements Serializable {

    private final String message;
    private final double result;
    private final int type;

    public Suggestion (String mex, double res, int type) {
        message = mex;
        result = res;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }


    public double getResult() {
        return result;
    }

}
