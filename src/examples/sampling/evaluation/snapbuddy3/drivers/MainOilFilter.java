package sampling.evaluation.snapbuddy3.drivers;

import sampling.evaluation.snapbuddy3.image.OilFilter;

import gov.nasa.jpf.symbc.Debug;

public class MainOilFilter
{
    public static int N;
    
    public static void main(final String[] args) {
    	
    	// fix image to one pixel
    	N = Integer.parseInt(args[0]);

    	int width = N;
    	int height = N;
    	
        // symbolic pixels
    	int pixels[] = new int[width*height];
    	for (int x = 0; x < width; x++) {
    		for (int y = 0; y < height; y++) {
    			pixels[y*width + x] = Debug.makeSymbolicInteger("p"+ y*width + x);
    		}
    	}
    	
    	// test the Intensify filter
    	OilFilter of = new OilFilter();
    	of.setRange(1);
    	of.setLevels(1);
    	of.filterPixels(width,height,pixels);
    }
}