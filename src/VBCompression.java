import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.log;

/**
 * Created by zw on 19/04/2017.
 */
public class VBCompression {
    public static void main(String[] args) {

        List<Integer> numbers = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            numbers.add(i*i);
        }

        // encode
        byte[] code = encode(numbers);
        for (int i = 0; i < code.length; i++) {
            System.out.print(code[i]);
        }
        System.out.println();


        // decode
        List<Integer> results = new LinkedList<>();
        results = decode(code);
        for (Integer each: results) {
            System.out.println(each);
        }

    }

    private static byte[] encodeNumber(int n) {
        if (n == 0) {
            return new byte[]{0};
        }
        int i = (int) (log(n) / log(128)) + 1;
        byte[] rv = new byte[i];
        int j = i - 1;
        do {
            rv[j--] = (byte) (n % 128);
            n /= 128;
        } while (j >= 0);
        rv[i - 1] += 128;
        return rv;
    }

    public static byte[] encode(List<Integer> numbers) {
        ByteBuffer buf = ByteBuffer.allocate(numbers.size() * (Integer.SIZE / Byte.SIZE));
        for (Integer number : numbers) {
            buf.put(encodeNumber(number));
        }
        buf.flip();
        byte[] rv = new byte[buf.limit()];
        buf.get(rv);
        return rv;
    }

    public static List<Integer> decode(byte[] byteStream) {
        List<Integer> numbers = new ArrayList<Integer>();
        int n = 0;
        for (byte b : byteStream) {
            if ((b & 0xff) < 128) {
                n = 128 * n + b;
            } else {
                int num = (128 * n + ((b - 128) & 0xff));
                numbers.add(num);
                n = 0;
            }
        }
        return numbers;
    }

    public static ArrayList<Integer> fromListToGaps(ArrayList<Integer> list) {
        for (int i = list.size() - 1; i > 0; i--) {
            int gap = list.get(i) - list.get(i - 1);
            list.set(i, gap);
        }

        return list;
    }

    public static List<Integer> fromGapsToList(List<Integer> gaps) {
        for (int i = 0; i < gaps.size() -1; i++) {
            int value = gaps.get(i) + gaps.get(i+1);
            gaps.set(i + 1, value);
        }

        return gaps;
    }

}
