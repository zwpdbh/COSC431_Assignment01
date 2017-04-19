import java.util.ArrayList;

/**
 * Created by zw on 18/04/2017.
 */
public class Util {
    public static ArrayList<Byte> VBEncodeNumber(Integer n) {
        ArrayList<Byte> bytes = new ArrayList<>();

        return bytes;
    }

    public static ArrayList<Byte> VBEncode(int numbers) {
        ArrayList<Byte> bytestream = new ArrayList<>();


        return bytestream;
    }

    public static int VBDecode(ArrayList<Byte> bytestream) {
        return 0;
    }

    public static ArrayList<Integer> getNumbersFromInteger(int number) {
        ArrayList<Integer> numbers = new ArrayList<>();
        while (number > 0) {
            numbers.add(number % 10);
            number = number / 10;
        }
        return numbers;
    }
}
