package sampling.evaluation.snapbuddy3.image;

//import java.util.*;

public class OilFilter extends WholeImageFilter
{
    private int range;
    private int levels;
    
    public OilFilter() {
        this.range = 3;
        this.levels = 256;
    }
    
    public void setRange(final int range) {
        this.range = range;
    }
    
    public int getRange() {
        return this.range;
    }
    
    public void setLevels(final int levels) {
        this.levels = levels;
    }
    
    public int getLevels() {
        return this.levels;
    }
    
    @Override
    /*protected*/ public int[] filterPixels(final int width, final int height, final int[] inPixels) {
        int index = 0;
        final int[] rHistogram = new int[this.levels];
        final int[] gHistogram = new int[this.levels];
        final int[] bHistogram = new int[this.levels];
        final int[] rTotal = new int[this.levels];
        final int[] gTotal = new int[this.levels];
        final int[] bTotal = new int[this.levels];
        final int[] outPixels = new int[width * height];
        int y = 0;
        while (y < height) {
            for (/*Random randomNumberGeneratorInstance = new Random()*/; y < height /*&& randomNumberGeneratorInstance.nextDouble() < 0.9*/; ++y) {
                for (int x = 0; x < width; ++x) {
                    for (int i = 0; i < this.levels; ++i) {
                        final int[] array = rHistogram;
                        final int n = i;
                        final int[] array2 = gHistogram;
                        final int n2 = i;
                        final int[] array3 = bHistogram;
                        final int n3 = i;
                        final int[] array4 = rTotal;
                        final int n4 = i;
                        final int[] array5 = gTotal;
                        final int n5 = i;
                        final int[] array6 = bTotal;
                        final int n6 = i;
                        final boolean b3 = false;
                        array5[n5] = (array6[n6] = (b3 ? 1 : 0));
                        array3[n3] = (array4[n4] = (b3 ? 1 : 0));
                        array[n] = (array2[n2] = (b3 ? 1 : 0));
                    }
                    for (int row = -this.range; row <= this.range; ++row) {
                        final int iy = y + row;
                        if (0 <= iy && iy < height) {
                            final int ioffset = iy * width;
                            for (int col = -this.range; col <= this.range; ++col) {
                                final int ix = x + col;
                                if (0 <= ix && ix < width) {
                                    final int rgb = inPixels[ioffset + ix];
                                    final int r = rgb >> 16 & 0xFF;
                                    final int g = rgb >> 8 & 0xFF;
                                    final int b = rgb & 0xFF;
                                    final int ri = r * this.levels / 256;
                                    final int gi = g * this.levels / 256;
                                    final int bi = b * this.levels / 256;
                                    final int[] array7 = rTotal;
                                    final int n7 = ri;
                                    array7[n7] += r;
                                    final int[] array8 = gTotal;
                                    final int n8 = gi;
                                    array8[n8] += g;
                                    final int[] array9 = bTotal;
                                    final int n9 = bi;
                                    array9[n9] += b;
                                    final int[] array10 = rHistogram;
                                    final int n10 = ri;
                                    ++array10[n10];
                                    final int[] array11 = gHistogram;
                                    final int n11 = gi;
                                    ++array11[n11];
                                    final int[] array12 = bHistogram;
                                    final int n12 = bi;
                                    ++array12[n12];
                                }
                            }
                        }
                    }
                    int r2 = 0;
                    int g2 = 0;
                    int b2 = 0;
                    for (int j = 1; j < this.levels; ++j) {
                        if (rHistogram[j] > rHistogram[r2]) {
                            r2 = j;
                        }
                        if (gHistogram[j] > gHistogram[g2]) {
                            g2 = j;
                        }
                        if (bHistogram[j] > bHistogram[b2]) {
                            b2 = j;
                        }
                    }
                    r2 = rTotal[r2] / rHistogram[r2];
                    g2 = gTotal[g2] / gHistogram[g2];
                    b2 = bTotal[b2] / bHistogram[b2];
                    outPixels[index] = ((inPixels[index] & 0xFF000000) | r2 << 16 | g2 << 8 | b2);
                    ++index;
                }
            }
        }
        return outPixels;
    }
    
    public String toString() {
        return "Stylize/Oil...";
    }
}
