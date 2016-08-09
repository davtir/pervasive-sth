package com.pervasive.sth.entities;

import com.pervasive.sth.exceptions.TreasureNotFoundYetException;

/**
 * @brief	This class encapsulate the treasure status (found or not) and the
 * 			eventual winner photo
 */
public class TreasureStatus {
	/*
	 * If true, the treasure has been found, false otherwise
	 */
	private boolean _found;

	/*
	 * The winner photo
	 */
	private Media _winner;

	/**
	 * @param	found
	 * @brief	Initialize class fields
	 */
	public TreasureStatus(boolean found) {
		_found = found;
	}

	/**
	 * @return	True if the treasure have been found, false otherwise
	 * @brief	Returns true if the treasure have been found, false otherwise
	 */
	public boolean isFound() {
		return _found;
	}

	/**
	 * @param	found
	 * @brief	Sets _found field to the input value
	 */
	public void setFound(boolean found) {
		_found = found;
	}

	/**
	 *
	 * @return	The media file associated to the last winner
	 * @throws	TreasureNotFoundYetException
	 * @brief	This functions returns the media file associated to the last winner
	 */
	public Media getWinner() throws TreasureNotFoundYetException {
		if ( !_found ) {
			throw new TreasureNotFoundYetException("The treasure is still hidden");
		}
		return _winner;
	}

	/**
	 * @param	_winner
	 * @brief	This functions sets the media file associated to the winner
	 */
	public void setWinner(Media _winner) {
		this._winner = _winner;
	}
}

