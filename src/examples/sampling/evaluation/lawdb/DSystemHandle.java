package sampling.evaluation.lawdb;

public class DSystemHandle
{
    public static int PORT;
    public static String ADDRESS;
    
    public DSystemHandle(final String address, final int port) {
        DSystemHandle.ADDRESS = address;
        DSystemHandle.PORT = port;
    }
    
    static {
        DSystemHandle.PORT = 6666;
    }
}
