/*
 * Copyright 1996-2004 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.futureconcepts.awt.image;

import com.futureconcepts.awt.image.ImageConsumer;
import java.util.Hashtable;
import com.futureconcepts.awt.Rectangle;

/**
 * An ImageFilter class for scaling images using the simplest algorithm.
 * This class extends the basic ImageFilter Class to scale an existing
 * image and provide a source for a new image containing the resampled
 * image.  The pixels in the source image are sampled to produce pixels
 * for an image of the specified size by replicating rows and columns of
 * pixels to scale up or omitting rows and columns of pixels to scale
 * down.
 * <p>It is meant to be used in conjunction with a FilteredImageSource
 * object to produce scaled versions of existing images.  Due to
 * implementation dependencies, there may be differences in pixel values
 * of an image filtered on different platforms.
 *
 * @see FilteredImageSource
 * @see ImageFilter
 *
 * @author      Jim Graham
 */
public class ReplicateScaleFilter extends ImageFilter {

    /**
     * The width of the source image.
     */
    protected int srcWidth;

    /**
     * The height of the source image.
     */
    protected int srcHeight;

    /**
     * The target width to scale the image.
     */
    protected int destWidth;

    /**
     * The target height to scale the image.
     */
    protected int destHeight;

    /**
     * An <code>int</code> array containing information about a
     * row of pixels.
     */
    protected int srcrows[];

    /**
     * An <code>int</code> array containing information about a
     * column of pixels.
     */
    protected int srccols[];

    /**
     * A <code>byte</code> array initialized with a size of
     * {@link #destWidth} and used to deliver a row of pixel
     * data to the {@link ImageConsumer}.
     */
    protected Object outpixbuf;

    /**
     * Constructs a ReplicateScaleFilter that scales the pixels from
     * its source Image as specified by the width and height parameters.
     * @param width the target width to scale the image
     * @param height the target height to scale the image
     * @throws IllegalArgumentException if <code>width</code> equals
     *         zero or <code>height</code> equals zero
     */
    public ReplicateScaleFilter(int width, int height) {
        if (width == 0 || height == 0) {
            throw new IllegalArgumentException("Width ("+width+
                                                ") and height ("+height+
                                                ") must be non-zero");
        }
        destWidth = width;
        destHeight = height;
    }

    /**
     * Passes along the properties from the source object after adding a
     * property indicating the scale applied.
     * This method invokes <code>super.setProperties</code>,
     * which might result in additional properties being added.
     * <p>
     * Note: This method is intended to be called by the
     * <code>ImageProducer</code> of the <code>Image</code> whose pixels
     * are being filtered. Developers using
     * this class to filter pixels from an image should avoid calling
     * this method directly since that operation could interfere
     * with the filtering operation.
     */
    public void setProperties(Hashtable<?,?> props) {
        Hashtable<Object,Object> p = (Hashtable<Object,Object>)props.clone();
        String key = "rescale";
        String val = destWidth + "x" + destHeight;
        Object o = p.get(key);
        if (o != null && o instanceof String) {
            val = ((String) o) + ", " + val;
        }
        p.put(key, val);
        super.setProperties(p);
    }

    /**
     * Override the dimensions of the source image and pass the dimensions
     * of the new scaled size to the ImageConsumer.
     * <p>
     * Note: This method is intended to be called by the
     * <code>ImageProducer</code> of the <code>Image</code> whose pixels
     * are being filtered. Developers using
     * this class to filter pixels from an image should avoid calling
     * this method directly since that operation could interfere
     * with the filtering operation.
     * @see ImageConsumer
     */
    public void setDimensions(int w, int h) {
        srcWidth = w;
        srcHeight = h;
        if (destWidth < 0) {
            if (destHeight < 0) {
                destWidth = srcWidth;
                destHeight = srcHeight;
            } else {
                destWidth = srcWidth * destHeight / srcHeight;
            }
        } else if (destHeight < 0) {
            destHeight = srcHeight * destWidth / srcWidth;
        }
        consumer.setDimensions(destWidth, destHeight);
    }

    private void calculateMaps() {
        srcrows = new int[destHeight + 1];
        for (int y = 0; y <= destHeight; y++) {
            srcrows[y] = (2 * y * srcHeight + srcHeight) / (2 * destHeight);
        }
        srccols = new int[destWidth + 1];
        for (int x = 0; x <= destWidth; x++) {
            srccols[x] = (2 * x * srcWidth + srcWidth) / (2 * destWidth);
        }
    }
}
