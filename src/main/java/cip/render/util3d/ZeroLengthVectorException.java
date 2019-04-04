/*
 * ZeroLengthVectorException.java
 *
 * Created on September 11, 2002, 6:37 PM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 */
package cip.render.util3d;

/**
 * The exception typically thrown in normalization functions when the vector being
 * normalized is so close to zero length (relative to floating point percision) that the
 * divide by length operation generates meaningless results or a floating point divide
 * by zero exception.
 * <p>
 * The <tt>ZeroLengthVectorException</tt> is a primordial exception so there is
 * no contructor that takes a reason or another exception to which this one is linked.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class ZeroLengthVectorException extends RuntimeException {
    /**
     * Creates a new instance of <tt>ZeroLengthVectorException</tt>.
     */
    public ZeroLengthVectorException() {
    }
}
