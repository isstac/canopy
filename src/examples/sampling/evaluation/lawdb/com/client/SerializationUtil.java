package sampling.evaluation.lawdb.com.client;

import java.io.*;

public class SerializationUtil
{
    public static Object deserialize(final String fileName) throws IOException, ClassNotFoundException {
        final FileInputStream fis = new FileInputStream(fileName);
        final ObjectInputStream ois = new ObjectInputStream((InputStream)fis);
        final Object obj = ois.readObject();
        ois.close();
        return obj;
    }
    
    public static void serialize(final Object obj, final String fileName) throws IOException {
        final FileOutputStream fos = new FileOutputStream(fileName);
        final ObjectOutputStream oos = new ObjectOutputStream((OutputStream)fos);
        oos.writeObject(obj);
        fos.close();
    }
}
