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

import java.awt.Color;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.user.client.Random;

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
        canvas.setSize("720px", "720px");
        
        canvas.addMouseWheelHandler(new MouseWheelHandler() {
            @Override
            public void onMouseWheel(MouseWheelEvent event) {
                int deltaY = event.getDeltaY();
                if(deltaY > 0) {
                    zoomOut();
                } else if (deltaY < 0) {
                    zoomIn();
                }
            }
        });
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

    private void draw() {
        Context2d context2d = canvas.getContext2d();

        for (int canvasI = 0, dataI = (width / 2) + xOff - (width / (2 * zoom)); canvasI < width; canvasI += zoom, dataI++) {
            for (int canvasJ = 0, dataJ = (height / 2) + yOff - (height / (2 * zoom)); canvasJ < height; canvasJ += zoom, dataJ++) {
                float value = data[dataI + dataJ * height];
                value = (value - scaleMin) / (scaleMax - scaleMin);
                try {
                    int[] c = palette.getColor(value);
                    context2d.setFillStyle(CssColor.make(c[0], c[1], c[2]));
                    context2d.fillRect(canvasI, canvasJ, zoom, zoom);
                } catch (IllegalArgumentException e) {
                    GWT.log("value " + value + "\n" + scaleMin + " -> " + scaleMax);
                    return;
                }
            }
        }
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
}
