package sampling.evaluation.snapbuddy3.image;

import java.awt.*;
import java.util.*;
import java.awt.image.*;

public abstract class WholeImageFilter extends AbstractBufferedImageOp
{
    protected Rectangle transformedSpace;
    protected Rectangle originalSpace;
    
    public BufferedImage filter(final BufferedImage src, BufferedImage dst) {
        final int width = src.getWidth();
        final int height = src.getHeight();
        final int type = src.getType();
        final WritableRaster srcRaster = src.getRaster();
        this.originalSpace = new Rectangle(0, 0, width, height);
        this.transformSpace(this.transformedSpace = new Rectangle(0, 0, width, height));
        if (dst == null) {
            final ColorModel dstCM = src.getColorModel();
            dst = new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(this.transformedSpace.width, this.transformedSpace.height), dstCM.isAlphaPremultiplied(), (Hashtable)null);
        }
        final WritableRaster dstRaster = dst.getRaster();
        int[] inPixels = this.getRGB(src, 0, 0, width, height, null);
        inPixels = this.filterPixels(width, height, inPixels/*, this.transformedSpace*/);
        this.setRGB(dst, 0, 0, this.transformedSpace.width, this.transformedSpace.height, inPixels);
        return dst;
    }
    
    protected void transformSpace(final Rectangle rect) {
    }
    
    protected abstract int[] filterPixels(final int p0, final int p1, final int[] p2 /*, final Rectangle p3*/);
}
