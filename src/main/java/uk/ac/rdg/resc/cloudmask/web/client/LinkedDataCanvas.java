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

import java.util.ArrayList;
import java.util.List;

public class LinkedDataCanvas extends DataCanvas {
    private List<LinkedDataCanvas> linkedViews;

    public LinkedDataCanvas(int width, int height) {
        super(width, height);
        linkedViews = new ArrayList<>();
    }

    public void addLinkedView(LinkedDataCanvas view) {
        if (view.width != width || view.height != height) {
            throw new IllegalArgumentException("Linked views must have the same image bounds");
        }

        view.minX = minX;
        view.maxX = maxX;
        view.minY = minY;
        view.maxY = maxY;

        for (LinkedDataCanvas other : linkedViews) {
            other.linkedViews.add(view);
            view.linkedViews.add(other);
        }
        linkedViews.add(view);
        view.linkedViews.add(this);
    }

    public void removeLinkedView(LinkedDataCanvas view) {
        for (LinkedDataCanvas other : linkedViews) {
            other.linkedViews.remove(view);
        }
        linkedViews.remove(view);
        view.linkedViews.clear();
    }

    @Override
    protected void doZoom(boolean in, double centreX, double centreY) {
        super.doZoom(in, centreX, centreY);
        for (LinkedDataCanvas view : linkedViews) {
            view.doLinkedZoom(in, centreX, centreY);
        }
    }

    private void doLinkedZoom(boolean in, double centreX, double centreY) {
        super.doZoom(in, centreX, centreY);
    }

    @Override
    protected void doDrag(double coordsChangeX, double coordsChangeY) {
        super.doDrag(coordsChangeX, coordsChangeY);
        for (LinkedDataCanvas view : linkedViews) {
            view.doLinkedDrag(coordsChangeX, coordsChangeY);
        }
    }

    protected void doLinkedDrag(double coordsChangeX, double coordsChangeY) {
        super.doDrag(coordsChangeX, coordsChangeY);
    }

    @Override
    public void draw() {
        super.draw();
        for (LinkedDataCanvas view : linkedViews) {
            view.drawJustThisCanvas();
        }
    }

    public void drawJustThisCanvas() {
        super.draw();
    }
}
