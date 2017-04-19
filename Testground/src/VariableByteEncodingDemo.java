
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


    public static ArrayList<Byte> vbEncodeNumber(int n) {
        ArrayList<Byte> bytes = new ArrayList<>();
        while (true) {

            if (n < 128) {
                break;
            }
            n = n / 128;
        }
//        bytes[bytes.size()] += 128;
        return bytes;
    }

    public static void main(String args[]) {
        System.out.println("0");
    }
}
