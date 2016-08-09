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
	private final double _value;

	/*
	 * the type of the suggestion
	 */
	private final int _type;

	/**
	 *
	 * @param mex
	 * @param res
	 * @param type
	 * @brief	Initilize class instance
	 */
	public Suggestion(String mex, double res, int type) {
		_message = mex;
		_value = res;
		_type = type;
	}

	/**
	 * @return	The suggestion type
	 * @brief	Returns the suggestion type
	 */
	public int getType() {
		return _type;
	}

	/**
	 * @return	The message associated to the suggestion
	 * @brief	Returns the message associated to the suggestion
	 */
	public String getMessage() {
		return _message;
	}

	/**
	 * @return	The value associated to the suggestion
	 * @brief	Returns the value associated to the suggestion
	 */
	public double getValue() {
		return _value;
	}

}
