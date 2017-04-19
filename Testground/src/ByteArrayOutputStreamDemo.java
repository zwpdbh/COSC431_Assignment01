import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zw on 19/04/2017.
 */


class ByteArrayOutputStreamDemo {
    public static void main(String args[]) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String s = "This should end up in the array";
        byte buf[] = s.getBytes();
        try {
            byteArrayOutputStream.write(buf);
        } catch (IOException e) {
            System.out.println("Error Writing to Buffer");
            return;
        }
        System.out.println("Buffer as a string");
        System.out.println(byteArrayOutputStream.toString());
        System.out.println("Into array");
        byte b[] = byteArrayOutputStream.toByteArray();
        for (int i = 0; i < b.length; i++) System.out.print((char) b[i]);
        System.out.println("\nTo an OutputStream()");

        // Use try-with-resources to manage the file stream.
        try (FileOutputStream f2 = new FileOutputStream("test.txt")) {
            byteArrayOutputStream.writeTo(f2);
        } catch (IOException e) {
            System.out.println("I/O Error: " + e);
            return;
        }
        System.out.println("Doing a reset");
        byteArrayOutputStream.reset();
        for (int i = 0; i < 3; i++) byteArrayOutputStream.write('X');
        System.out.println(byteArrayOutputStream.toString());
    }
}
