/*******************************************************************************
 * Copyright (c) 2015 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.cloudmask.web.client;

import javax.swing.text.html.ImageView;

import uk.ac.rdg.resc.edal.position.HorizontalPosition;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;

public class DataCanvas {

    /**
     * The zoom level. Only positive zooms are permitted, and this is the size
     * which each pixel in the data will be drawn at
     */
    protected int zoom = 1;
    protected int xOff = 0;
    protected int yOff = 0;

    protected final int width;
    protected final int height;
    private float[] data;

    private float scaleMin = Float.MAX_VALUE;
    private float scaleMax = -Float.MAX_VALUE;

    private Canvas canvas;

    private ColourPalette palette = ColourPalette.fromString(ColourPalette.DEFAULT_PALETTE_NAME,
            250);

    public DataCanvas(int width, int height) {
        this.width = width;
        this.height = height;
        canvas = Canvas.createIfSupported();
        canvas.setCoordinateSpaceWidth(width);
        canvas.setCoordinateSpaceHeight(height);
        canvas.setSize(width + "px", height + "px");

        canvas.addMouseWheelHandler(new MouseWheelHandler() {
            @Override
            public void onMouseWheel(MouseWheelEvent event) {
                int deltaY = event.getDeltaY();
                if (deltaY > 0) {
                    doZoom(false, event.getX() * canvas.getCoordinateSpaceWidth()
                            / canvas.getCanvasElement().getClientWidth(), event.getClientY()
                            * canvas.getCoordinateSpaceHeight()
                            / canvas.getCanvasElement().getClientHeight());
                    draw();
                } else if (deltaY < 0) {
                    doZoom(true, event.getX() * canvas.getCoordinateSpaceWidth()
                            / canvas.getCanvasElement().getClientWidth(), event.getClientY()
                            * canvas.getCoordinateSpaceHeight()
                            / canvas.getCanvasElement().getClientHeight());
                    draw();
                }
            }
        });

        /*
         * Set bounds
         */
        minXBound = 0;
        minYBound = 0;
        maxXBound = width;
        maxYBound = height;

        /*
         * Set initial values to image bounds
         */
        minX = minXBound;
        minY = minYBound;
        maxX = maxXBound;
        maxY = maxYBound;

        double widgetRatio = ((double) width) / height;
        double viewportRatio = (maxX - minX) / (maxY - minY);

        if (widgetRatio < viewportRatio) {
            double desiredViewportWidth = widgetRatio * (maxY - minY);
            double midpoint = (minX + maxX) / 2.0;
            minX = midpoint - desiredViewportWidth / 2.0;
            maxX = midpoint + desiredViewportWidth / 2.0;
        } else if (viewportRatio < widgetRatio) {
            double desiredViewportHeight = (maxX - minX) / widgetRatio;
            double midpoint = (minY + maxY) / 2.0;
            minY = midpoint - desiredViewportHeight / 2.0;
            maxY = midpoint + desiredViewportHeight / 2.0;
        }
    }

    /**
     * TODO Specify data order here
     * 
     * @param values
     */
    public void setData(float[] values) {
        if (values.length != width * height) {
            throw new IllegalArgumentException("Can only accept an array of size " + width + "x"
                    + height);
        }
        data = values;

        scaleMin = Float.MAX_VALUE;
        scaleMax = -Float.MAX_VALUE;
        for (float v : values) {
            if (v < scaleMin) {
                scaleMin = v;
            }
            if (v > scaleMax) {
                scaleMax = v;
            }
        }
        draw();
    }

    protected void draw() {
        Context2d context2d = canvas.getContext2d();

//        for (int canvasI = 0, dataI = (width / 2) + xOff - (width / (2 * zoom)); canvasI < width; canvasI += zoom, dataI++) {
//            for (int canvasJ = 0, dataJ = (height / 2) + yOff - (height / (2 * zoom)); canvasJ < height; canvasJ += zoom, dataJ++) {
//                float value = data[dataI + dataJ * height];
//                value = (value - scaleMin) / (scaleMax - scaleMin);
//                try {
//                    int[] c = palette.getColor(value);
//                    context2d.setFillStyle(CssColor.make(c[0], c[1], c[2]));
//                    context2d.fillRect(canvasI, canvasJ, zoom, zoom);
//                } catch (IllegalArgumentException e) {
//                    GWT.log("value " + value + "\n" + scaleMin + " -> " + scaleMax);
//                    return;
//                }
//            }
//        }

        GWT.log("Drawing x :" + minX + " -> " + maxX);
        GWT.log("Drawing y :" + minY + " -> " + maxY);

        context2d.setGlobalAlpha(1.0);
        context2d.setFillStyle(CssColor.make(0, 0, 0));
        context2d.fillRect(0, 0, width, height);

        for (double dataI = minX; dataI <= maxX; dataI++) {
            for (double dataJ = minY; dataJ <= maxY; dataJ++) {
                float value = data[((int) dataI + (int) dataJ * height)];
                value = (value - scaleMin) / (scaleMax - scaleMin);
                double[] coords = dataCoord2CanvasCoord(dataI + 0.5, dataJ + 0.5);
                try {
                    int[] c = palette.getColor(value);
                    context2d.setFillStyle(CssColor.make(c[0], c[1], c[2]));
                    /*
                     * This rectangle is a little big so that we don't get
                     * apparent partial transparency from rounding errors when
                     * it's drawn
                     */
                    context2d.fillRect(coords[0], coords[1], zoom + 1, zoom + 1);
                } catch (IllegalArgumentException e) {
                    GWT.log("value " + value + "\n" + scaleMin + " -> " + scaleMax);
                    return;
                }
            }
        }

        context2d.setGlobalAlpha(0.5);
        context2d.setFillStyle(CssColor.make("rgba(0,0,0,0.25)"));
        for (double dataI = minX; dataI <= maxX; dataI++) {
            for (double dataJ = minY; dataJ <= maxY; dataJ++) {
                float value = data[((int) dataI + (int) dataJ * height)];
                double[] coords = dataCoord2CanvasCoord(dataI + 0.5, dataJ + 0.5);
                try {
                    if (value > 12.5) {
                        context2d.fillRect(coords[0], coords[1], zoom + 1, zoom + 1);
                    }
                } catch (IllegalArgumentException e) {
                    GWT.log("value " + value + "\n" + scaleMin + " -> " + scaleMax);
                    return;
                }
            }
        }

//        for (int canvasI = 0, dataI = (int) minX; canvasI < width; canvasI += zoom, dataI++) {
//            if(canvasI == 0 || canvasI == width-1) {
//                GWT.log(canvasI+","+dataI);
//            }
//            for (int canvasJ = 0, dataJ = (int) minY; canvasJ < height; canvasJ += zoom, dataJ++) {
//                float value = data[dataI + dataJ * height];
//                value = (value - scaleMin) / (scaleMax - scaleMin);
//                try {
//                    int[] c = palette.getColor(value);
//                    context2d.setFillStyle(CssColor.make(c[0], c[1], c[2]));
//                    context2d.fillRect(canvasI, canvasJ, zoom, zoom);
//                } catch (IllegalArgumentException e) {
//                    GWT.log("value " + value + "\n" + scaleMin + " -> " + scaleMax);
//                    return;
//                }
//            }
//        }

//        for (int canvasI = 0, dataI = xOff; canvasI < width; canvasI+=zoom, dataI++) {
//            for (int canvasJ = 0, dataJ = yOff; canvasJ < height; canvasJ+=zoom, dataJ++) {
//                float value = data[dataI+dataJ*height];
//                value = (value - scaleMin) / (scaleMax - scaleMin); 
//                try{
//                    int[] c = palette.getColor(value);
//                    context2d.setFillStyle(CssColor.make(c[0], c[1], c[2]));
//                    context2d.fillRect(canvasI, canvasJ, zoom, zoom);
//                } catch (IllegalArgumentException e) {
//                    GWT.log("value "+value+"\n"+scaleMin+" -> "+scaleMax);
//                    return;
//                }
//            }
//        }
    }

    public double[] dataCoord2CanvasCoord(double x, double y) {
        return new double[] { width * (x - minX) / (maxX - minX),
                height * (1.0 - (y - minY) / (maxY - minY)) };
//        return new double[]{minX + (maxX - minX) * (x / width), minY
//                + (maxY - minY) * (1.0 - (y / height))};
    }

    public void zoomIn() {
        if (zoom < width / 4)
            zoom++;
        draw();
    }

    public void zoomOut() {
        if (zoom > 1)
            zoom--;
        draw();
    }

    public void moveUp() {
        yOff -= 1;
        draw();
    }

    public void moveDown() {
        yOff += 1;
        draw();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    /** The minimum valid x co-ordinate for image generation */
    private double minXBound;
    /** The minimum valid y co-ordinate for image generation */
    private double minYBound;
    /** The maximum valid x co-ordinate for image generation */
    private double maxXBound;
    /** The maximum valid y co-ordinate for image generation */
    private double maxYBound;

    /** The current minimum visible x co-ordinate */
    protected double minX;
    /** The current minimum visible y co-ordinate */
    protected double minY;
    /** The current maximum visible x co-ordinate */
    protected double maxX;
    /** The current maximum visible y co-ordinate */
    protected double maxY;

    /**
     * Updates appropriate variables to represent a zoom. Does not update the
     * image, just sets new limits
     * 
     * @param factor
     *            The factor to zoom by. Values greater than one represent a
     *            zoom in, and values between 0 and 1 represent a zoom out
     * @param centreX
     *            The x co-ordinate of the centre of the zoom
     * @param centreY
     *            The y co-ordinate of the centre of the zoom
     */
    protected void doZoom(boolean in, double centreX, double centreY) {
        GWT.log("Zoom centre: " + centreX + "," + centreY);
        double factor = 1.0;
        if (in) {
            zoom++;
            factor = zoom / (zoom - 1.0);
        } else if (zoom > 1) {
            zoom--;
            factor = zoom / (zoom + 1.0);
        }
//        zoom *= factor;
//        if (zoom < 1) {
//            zoom = 1;
//            minX = 0;
//            minY = 0;
//            minX = width;
//            minY = height;
//            return;
//        }
        /*
         * Convenient values
         */
        double widthX = maxX - minX;
        double widthY = maxY - minY;

        /*
         * The final width in co-ordinate space after the zoom
         */
        double finalWidthX = widthX / factor;
        double finalWidthY = widthY / factor;

        double imageFactor = ((double) width) / height;
        double zoomedFactor = finalWidthX / finalWidthY;
        if (imageFactor > zoomedFactor) {
            finalWidthY = finalWidthX / imageFactor;
        } else if (zoomedFactor > imageFactor) {
            finalWidthX = finalWidthY * imageFactor;
        }

        /*
         * How much to shift each side (min/max) by (relatively)
         */
        double minXShiftFactor = (centreX - minX) / widthX;
        double maxXShiftFactor = (maxX - centreX) / widthX;
        double minYShiftFactor = (centreY - minY) / widthY;
        double maxYShiftFactor = (maxY - centreY) / widthY;

        /*
         * Now adjust the co-ordinates
         */
        minX = centreX - finalWidthX * minXShiftFactor;
        maxX = centreX + finalWidthX * maxXShiftFactor;
        minY = centreY - finalWidthY * minYShiftFactor;
        maxY = centreY + finalWidthY * maxYShiftFactor;

        /*
         * Check if the zoom goes far enough out that the view goes outside the
         * image. If so, effectively move the zoom centre
         */
        if (minX < minXBound) {
            maxX += minXBound - minX;
            if (maxX > maxXBound) {
                maxX = maxXBound;
            }
            minX = minXBound;
        }
        if (maxX > maxXBound) {
            minX -= maxX - maxXBound;
            if (minX < minXBound) {
                minX = minXBound;
            }
            maxX = maxXBound;
        }
        if (minY < minYBound) {
            maxY += minYBound - minY;
            if (maxY > maxYBound) {
                maxY = maxYBound;
            }
            minY = minYBound;
        }
        if (maxY > maxYBound) {
            minY -= maxY - maxYBound;
            if (minY < minYBound) {
                minY = minYBound;
            }
            maxY = maxYBound;
        }
    }

    /**
     * Updates appropriate variables to represent a drag. Does not update the
     * image, just sets new limits
     * 
     * @param xPixels
     *            the number of pixels dragged in the x-direction
     * @param yPixels
     *            the number of pixels dragged in the y-direction
     */
    private void doPixelDrag(double xPixels, double yPixels) {
        /*
         * The amount the co-ordinates have changed
         */
        double coordsChangeX = xPixels * (maxX - minX) / width;
        double coordsChangeY = yPixels * (maxY - minY) / height;
        doDrag(coordsChangeX, coordsChangeY);
    }

    /**
     * Updates appropriate variables to represent a drag. Does not update the
     * image, just sets new limits
     * 
     * @param coordsChangeX
     *            the amount to change the x co-ordinate by
     * @param coordsChangeY
     *            the amount to change the y co-ordinate by
     */
    protected void doDrag(double coordsChangeX, double coordsChangeY) {
        if (minX - coordsChangeX < minXBound) {
            coordsChangeX = minX - minXBound;
        }
        if (maxX - coordsChangeX > maxXBound) {
            coordsChangeX = maxX - maxXBound;
        }
        if (minY - coordsChangeY < minYBound) {
            coordsChangeY = minY - minYBound;
        }
        if (maxY - coordsChangeY > maxYBound) {
            coordsChangeY = maxY - maxYBound;
        }
        minX -= coordsChangeX;
        maxX -= coordsChangeX;
        minY -= coordsChangeY;
        maxY -= coordsChangeY;
    }
}
