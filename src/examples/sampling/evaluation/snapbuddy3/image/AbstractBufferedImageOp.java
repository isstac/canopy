package sampling.evaluation.snapbuddy3.image;

import java.awt.image.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.*;

public abstract class AbstractBufferedImageOp implements BufferedImageOp, Cloneable
{
    public BufferedImage createCompatibleDestImage(final BufferedImage src, ColorModel dstCM) {
        if (dstCM == null) {
            dstCM = src.getColorModel();
        }
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), (Hashtable)null);
    }
    
    public Rectangle2D getBounds2D(final BufferedImage src) {
        final ClassgetBounds2D replacementClass = new ClassgetBounds2D(src);
        return replacementClass.doIt0();
    }
    
    public Point2D getPoint2D(final Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = (Point2D)new Point2D.Double();
        }
        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        return dstPt;
    }
    
    public RenderingHints getRenderingHints() {
        return null;
    }
    
    public int[] getRGB(final BufferedImage image, final int x, final int y, final int width, final int height, final int[] pixels) {
        final int type = image.getType();
        if (type == 2 || type == 1) {
            return (int[])image.getRaster().getDataElements(x, y, width, height, (Object)pixels);
        }
        return image.getRGB(x, y, width, height, pixels, 0, width);
    }
    
    public void setRGB(final BufferedImage image, final int x, final int y, final int width, final int height, final int[] pixels) {
        final int type = image.getType();
        if (type == 2 || type == 1) {
            image.getRaster().setDataElements(x, y, width, height, (Object)pixels);
        }
        else {
            image.setRGB(x, y, width, height, pixels, 0, width);
        }
    }
    
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }
    
    public class ClassgetBounds2D
    {
        private BufferedImage src;
        
        public ClassgetBounds2D(final BufferedImage src) {
            this.src = src;
        }
        
        public Rectangle2D doIt0() {
            return (Rectangle2D)new Rectangle(0, 0, this.src.getWidth(), this.src.getHeight());
        }
    }
}
