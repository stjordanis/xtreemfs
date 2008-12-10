/*  Copyright (c) 2008 Konrad-Zuse-Zentrum fuer Informationstechnik Berlin.

    This file is part of XtreemFS. XtreemFS is part of XtreemOS, a Linux-based
    Grid Operating System, see <http://www.xtreemos.eu> for more details.
    The XtreemOS project has been developed with the financial support of the
    European Commission's IST program under contract #FP6-033576.

    XtreemFS is free software: you can redistribute it and/or modify it under
    the terms of the GNU General Public License as published by the Free
    Software Foundation, either version 2 of the License, or (at your option)
    any later version.

    XtreemFS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with XtreemFS. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * AUTHORS: Christian Lorenz (ZIB)
 */
package org.xtreemfs.common.checksums.algorithms;

import java.nio.ByteBuffer;

import org.xtreemfs.common.checksums.StringChecksumAlgorithm;

/**
 * The SDBM algorithm.
 * 
 * 02.09.2008
 * 
 * @author clorenz
 */
public class SDBM implements StringChecksumAlgorithm {
	private String hash = null;

	private String name = "SDBM";

	/**
	 * Updates checksum with specified data.
	 * 
	 * @param data
	 */
	public void digest(String data) {
		this.hash = sdbmHash(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xtreemfs.common.checksums.ChecksumAlgorithm#digest(java.nio.ByteBuffer)
	 */
	@Override
	public void update(ByteBuffer data) {
		byte[] array;

		if (data.hasArray()) {
			array = data.array();
		} else {
			array = new byte[data.capacity()];
			final int oldPos = data.position();
			data.position(0);
			data.get(array);
			data.position(oldPos);
		}

		this.hash = sdbmHash(new String(array));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xtreemfs.common.checksums.ChecksumAlgorithm#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xtreemfs.common.checksums.ChecksumAlgorithm#getValue()
	 */
	@Override
	public String getValue() {
		String value;
		if (this.hash != null)
			value = this.hash;
		else
			value = "";
		reset();
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xtreemfs.common.checksums.ChecksumAlgorithm#reset()
	 */
	@Override
	public void reset() {
		hash = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xtreemfs.common.checksum.ChecksumAlgorithm#clone()
	 */
	@Override
	public SDBM clone() {
		return new SDBM();
	}

	/**
	 * SDBM algorithm
	 * 
	 * @param str
	 * @return
	 */
	protected static String sdbmHash(String str) {
		long hash = 0;
		for (int c : str.toCharArray()) {
			hash = c + (hash << 6) + (hash << 16) - hash;
		}
		return Long.toHexString(hash);
	}
}
