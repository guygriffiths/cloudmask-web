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

import org.spiffyui.client.widgets.slider.Slider;
import org.spiffyui.client.widgets.slider.SliderEvent;
import org.spiffyui.client.widgets.slider.SliderListener;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Html5ImageTest implements EntryPoint {

    @Override
    public void onModuleLoad() {
        RootLayoutPanel mainWindow = RootLayoutPanel.get();
        final DataCanvas dc1 = new DataCanvas(500, 500);
        float[] data = new float[500 * 500];
        for (int i = 0; i < 500; i++) {
            for (int j = 0; j < 500; j++) {
                data[i + j * 500] = (i + j);// / 50;
            }
        }
        dc1.setData(data, null, null);

//        final DataCanvas dc2 = new DataCanvas(500, 500);
//        data = new float[500 * 500];
//        for (int i = 0; i < 500; i++) {
//            for (int j = 0; j < 500; j++) {
//                data[i + j * 500] = (-j) / 100;
//            }
//        }
//        dc2.setData(data, null, null);

//        dc1.addLinkedView(dc2);

        VerticalPanel panel = new VerticalPanel();

        panel.add(dc1.getCanvas());
//        panel.add(dc2.getCanvas());

        Slider threshold = new Slider("thresh", (int) dc1.getScaleMin() * 100-1,
                (int) dc1.getScaleMax() * 100+1, new int[] { (int) dc1.getScaleMin() * 100,
                        (int) dc1.getScaleMax() * 100});
        threshold.addListener(new SliderListener() {
            @Override
            public void onStop(SliderEvent e) {
            }
            
            @Override
            public void onStart(SliderEvent e) {
            }
            
            @Override
            public boolean onSlide(SliderEvent e) {
                return true;
            }
            
            @Override
            public void onChange(SliderEvent e) {
                int[] values = e.getValues();
                dc1.setThresholds(values[0] / 100.0f, values[1] / 100.0f);
            }
        });
        panel.add(threshold);

        mainWindow.add(panel);
    }

    public Canvas getCanvas() {
        int width = 512;
        int height = 512;
        Canvas canvas = Canvas.createIfSupported();
        canvas.setCoordinateSpaceWidth(width);
        canvas.setCoordinateSpaceHeight(height);
        canvas.setSize("512px", "512px");
        Context2d context2d = canvas.getContext2d();
        ImageData imageData = context2d.createImageData(width, height);
        int step = 16;
        for (int i = 0; i < width; i += step) {
            for (int j = 0; j < height; j += step) {
                context2d.setFillStyle(CssColor.make(0, Random.nextInt(256), Random.nextInt(256)));
                context2d.fillRect(i, j, step, step);
//                imageData.setAlphaAt(255, i, j);
//                imageData.setBlueAt(i*255/width, i, j);
//                imageData.setGreenAt(j*255/height, i, j);
            }
        }
//        context2d.putImageData(imageData, 0, 0);
//        context2d.setFillStyle("blue");
//        context2d.fillRect(10, 10, 50, 60);
        return canvas;
    }

    private static native void setImageScale(Context2d context)/*-{
		context.imageSmoothingEnabled = false;
    }-*/;
}
