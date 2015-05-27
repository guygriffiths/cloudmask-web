/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ColourPalette {
    /**
     * The name of the default palette that will be used if the user doesn't
     * request a specific palette.
     * 
     * @see ColourPalette#DEFAULT_COLOURS
     */
    public static final String DEFAULT_PALETTE_NAME = "default";

    public static final int MAX_NUM_COLOURS = 250;

    public static final String INVERSE_SUFFIX = "-inv";

    /**
     * This is the palette that will be used if no specific palette has been
     * chosen. This palette is taken from the SGT graphics toolkit.
     * 
     * Equivalent to the string #081D58,#41B6C4,#FFFFD9
     * 
     * @see ColourPalette#DEFAULT_PALETTE_NAME
     */
    private static final int[][] DEFAULT_COLOURS = new int[][] { new int[] { 8, 29, 88, 255 },
            new int[] { 65, 182, 196, 255 }, new int[] { 255, 255, 217, 255 } };

    private static Map<String, int[][]> loadedColourSets = new HashMap<String, int[][]>();

    static {
        /*
         * Make sure these are initialised here (more reliable than relying on
         * them being initialised in file order in case of future refactoring)
         */
        loadedColourSets = new TreeMap<String, int[][]>();

        loadedColourSets.put(DEFAULT_PALETTE_NAME, DEFAULT_COLOURS);
//        int[][] invColourSet = (int[][]) ArrayUtils.clone(DEFAULT_COLOURS);
//        ArrayUtils.reverse(invColourSet);
//        loadedColourSets.put(DEFAULT_PALETTE_NAME + INVERSE_SUFFIX, invColourSet);
    }

    private final int[][] colours;

    public ColourPalette(int[][] palette, int numColorBands) {
        colours = generateColourSet(palette, numColorBands);
    }

    /**
     * Gets a version of this palette with the given number of color bands,
     * either by subsampling or interpolating the existing palette
     * 
     * @param numColorBands
     *            The number of bands of colour to be used in the new palette
     * @return An array of Colors, with length numColorBands
     * @throws IllegalArgumentException
     *             if the requested number of colour bands is less than one or
     *             greater than {@link #MAX_NUM_COLOURS}.
     */
    private static int[][] generateColourSet(int[][] palette, int numColorBands) {
        if (numColorBands < 1 || numColorBands > MAX_NUM_COLOURS) {
            throw new IllegalArgumentException(
                    "numColorBands must be greater than 1 and less than " + MAX_NUM_COLOURS);
        }
        int[][] targetPalette;
        if (numColorBands == palette.length) {
            /* We can just use the source palette directly */
            targetPalette = palette;
        } else {
            /* We need to create a new palette */
            targetPalette = new int[numColorBands][4];
            /*
             * We fix the endpoints of the target palette to the endpoints of
             * the source palette
             */
            targetPalette[0] = palette[0];
            targetPalette[targetPalette.length - 1] = palette[palette.length - 1];

            if (targetPalette.length < palette.length) {
                /*
                 * We only need some of the colours from the source palette We
                 * search through the target palette and find the nearest
                 * colours in the source palette
                 */
                for (int i = 1; i < targetPalette.length - 1; i++) {
                    /*
                     * Find the nearest index in the source palette (Multiplying
                     * by 1.0f converts integers to floats)
                     */
                    int nearestIndex = Math.round(palette.length * i * 1.0f
                            / (targetPalette.length - 1));
                    targetPalette[i] = palette[nearestIndex];
                }
            } else {
                /*
                 * Transfer all the colours from the source palette into their
                 * corresponding positions in the target palette and use
                 * interpolation to find the remaining values
                 */
                int lastIndex = 0;
                for (int i = 1; i < palette.length - 1; i++) {
                    /* Find the nearest index in the target palette */
                    int nearestIndex = Math.round(targetPalette.length * i * 1.0f
                            / (palette.length - 1));
                    targetPalette[nearestIndex] = palette[i];
                    /* Now interpolate all the values we missed */
                    for (int j = lastIndex + 1; j < nearestIndex; j++) {
                        /*
                         * Work out how much we need from the previous colour
                         * and how much from the new colour
                         */
                        float fracFromThis = (1.0f * j - lastIndex) / (nearestIndex - lastIndex);
                        targetPalette[j] = interpolate(targetPalette[nearestIndex],
                                targetPalette[lastIndex], fracFromThis);

                    }
                    lastIndex = nearestIndex;
                }
                /* Now for the last bit of interpolation */
                for (int j = lastIndex + 1; j < targetPalette.length - 1; j++) {
                    float fracFromThis = (1.0f * j - lastIndex)
                            / (targetPalette.length - lastIndex);
                    targetPalette[j] = interpolate(targetPalette[targetPalette.length - 1],
                            targetPalette[lastIndex], fracFromThis);
                }
            }
        }
        return targetPalette;
    }

    /**
     * Linearly interpolates between two RGB colours
     * 
     * @param c1
     *            the first colour
     * @param c2
     *            the second colour
     * @param fracFromC1
     *            the fraction of the final colour that will come from c1
     * @return the interpolated Color
     */
    private static int[] interpolate(int[] c1, int[] c2, float fracFromC1) {
        float fracFromC2 = 1.0f - fracFromC1;
        return new int[] { Math.round(fracFromC1 * c1[0] + fracFromC2 * c2[0]),
                Math.round(fracFromC1 * c1[1] + fracFromC2 * c2[1]),
                Math.round(fracFromC1 * c1[2] + fracFromC2 * c2[2]),
                Math.round(fracFromC1 * c1[3] + fracFromC2 * c2[3]) };
    }

    /**
     * Gets the colour corresponding to a fractional point along the palette
     * 
     * @param value
     *            The fraction along the palette of the colour
     * @return The desired colour
     */
    public int[] getColor(float value) {
        if (value < 0.0f || value > 1.0f) {
            throw new IllegalArgumentException("value must be between 0 and 1");
        }
        /* Find the nearest colour in the palette */
        int i = (int) (value * this.colours.length);
        /*
         * Correct in the special case that value = 1 to keep within bounds of
         * array
         */
        if (i == this.colours.length) {
            i--;
        }
        return this.colours[i];
    }

    public static ColourPalette fromString(String paletteString, int nColourBands) {
        if (paletteString == null || "".equals(paletteString)) {
            paletteString = DEFAULT_PALETTE_NAME;
        }
        if (loadedColourSets.containsKey(paletteString)) {
            return new ColourPalette(loadedColourSets.get(paletteString), nColourBands);
        } else {
            try {
                int[][] colours = colourSetFromString(paletteString);
                return new ColourPalette(colours, nColourBands);
            } catch (Exception e) {
                throw new IllegalArgumentException(paletteString
                        + " is not an existing palette name or a palette definition");
            }
        }
    }

    public static Set<String> getPredefinedPalettes() {
        return loadedColourSets.keySet();
    }

    private static int[][] colourSetFromString(String paletteString) {
        String[] colourStrings = paletteString.split("[,\n:]");
        int[][] colours = new int[colourStrings.length][4];
        for (int i = 0; i < colourStrings.length; i++) {
            try {
                colours[i] = parseColour(colourStrings[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return colours;
    }

    public static int[] parseColour(String colourString) {
        if ("transparent".equalsIgnoreCase(colourString)) {
            return new int[] { 0, 0, 0, 0 };
        }
        if ("extend".equalsIgnoreCase(colourString)) {
            /*
             * In the context of palette out-of-range values, null represents
             * extending the colour. In other cases it doesn't represent a
             * value, so null is not a disaster.
             */
            return null;
        }
        if (!colourString.startsWith("0x") && !colourString.startsWith("#")) {
            throw new IllegalArgumentException("Invalid format for colour");
        }
        if (colourString.length() == 7) {
            /*
             * We have #RRGGBB
             */

            int red = Integer.parseInt(colourString.substring(1, 3), 16);
            int green = Integer.parseInt(colourString.substring(3, 5), 16);
            int blue = Integer.parseInt(colourString.substring(5, 7), 16);
            return new int[] { red, green, blue, 255 };
        } else if (colourString.length() == 8) {
            /*
             * We have 0xRRGGBB
             */
            int red = Integer.parseInt(colourString.substring(2, 4), 16);
            int green = Integer.parseInt(colourString.substring(4, 6), 16);
            int blue = Integer.parseInt(colourString.substring(6, 8), 16);
            return new int[] { red, green, blue, 255 };
        } else if (colourString.length() == 9) {
            /*
             * We have #AARRGGBB
             */
            int alpha = Integer.parseInt(colourString.substring(1, 3), 16);
            int red = Integer.parseInt(colourString.substring(3, 5), 16);
            int green = Integer.parseInt(colourString.substring(5, 7), 16);
            int blue = Integer.parseInt(colourString.substring(7, 9), 16);
            return new int[] { red, green, blue, alpha };
        } else if (colourString.length() == 10) {
            /*
             * We have 0xAARRGGBB
             */
            int alpha = Integer.parseInt(colourString.substring(2, 4), 16);
            int red = Integer.parseInt(colourString.substring(4, 6), 16);
            int green = Integer.parseInt(colourString.substring(6, 8), 16);
            int blue = Integer.parseInt(colourString.substring(8, 10), 16);
            return new int[] { red, green, blue, alpha };
        } else {
            throw new IllegalArgumentException("Invalid format for colour");
        }
    }
}
