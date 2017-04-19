
import java.nio.Buffer;
import java.util.ArrayList;

/**
 * Created by zw on 18/04/2017.
 */
class VariableByteEncodingDemo {
    public static ArrayList<Byte> prepend(ArrayList<Byte> bytes, Byte b) {
        // prepend add an element to the beginning of a list
        bytes.add(0, b);
        return bytes;
    }

    private ArrayList<Byte> extend(ArrayList<Byte> bytes1, ArrayList<Byte> bytes2) {
         bytes1.addAll(bytes2);
         return bytes1;
    }

    private static void readInt(int n, int[] abyte) {
        String bin = Integer.toBinaryString(n);

        for (int i = 0; i < (8 - bin.length()); i++) {
            abyte[i] = 0;
        }

        for (int i = 0; i < bin.length(); i++) {
            abyte[i + (8 - bin.length())] = bin.charAt(i) - 48; // ASCII code for '0' is 48
        }
    }

    public static Byte getByteFromIntArray(int[] abyte) {
        String s = "";
        for (int i = 0; i < abyte.length; i++) {
            s += abyte[i];
        }

        Byte b = new Byte(s);
        System.out.println(b.toString());
        return b;
    }

    public static ArrayList<Byte> vbEncodeNumber(int n) {
        ArrayList<Byte> bytestream = new ArrayList<>();
        int abyte[] = new int[8];

        while (true) {
            readInt(n % 128, abyte);

            bytestream.add(0, getByteFromIntArray(abyte));
            if (n < 128) {
                break;
            }
            n = n / 128;
        }

        //retrieving the last byte
        Byte last = bytestream.get(bytestream.size() - 1);

        //setting the continuation bit to 1
        last = switchFirst(last);
        bytestream.set(bytestream.size() - 1, last);

        return bytestream;
    }

    public static Byte switchFirst(Byte b) {

        String s = b.toString();
        StringBuffer sb = new StringBuffer(s);
        sb.setCharAt(0, '1');

        s = sb.toString();

        b = new Byte(s);
        return b;
    }

    public static ArrayList<Byte> vbEncode(ArrayList<Integer> numbers) {
        ArrayList<Byte> code = new ArrayList<>();
        for (int n: numbers) {
            code.addAll(vbEncodeNumber(n));
        }

        return code;
    }


    public static void main(String args[]) {
//        System.out.println(vbEncodeNumber(3));

//        ArrayList<Integer> test = new ArrayList<>();
//        test.add(3);
//        test.add(824);
//        test.add(1234);
//
//        ArrayList<Byte> code = vbEncode(test);
//        for (int i = 0; i < code.size(); i++) {
//            System.out.print(code.get(i).toString() + " ");
//        }
//        System.out.println();
    }
}
