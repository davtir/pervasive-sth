package com.pervasive.sth.entities;

import java.io.Serializable;

/**
 * @brief this class represents the suggenstions
 */
@SuppressWarnings("serial")
public class Suggestion implements Serializable {

	/*
	 * the _message associated to the suggestion
	 */
	private final String _message;

	/*
	 * the possible values associated to the suggestion
	 */
	private final double _result;

	/*
	 * the type of the suggestion
	 */
	private final int _type;

	public Suggestion(String mex, double res, int type) {
		_message = mex;
		_result = res;
		_type = type;
	}

	public int getType() {
		return _type;
	}

	public String getMessage() {
		return _message;
	}


	public double getResult() {
		return _result;
	}

}
