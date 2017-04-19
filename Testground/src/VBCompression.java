import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.log;

/**
 * Created by zw on 19/04/2017.
 */
public class VBCompression {
    public static void main(String[] args) {

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

    /**
     * 对于已经排序好的数字序列，不再对原始值进行编码，而是对与前一个值的差值进行编码
     * <p/>
     * [ 1, 2, 3, 4, 5, 6, 7  ]
     * -->  [1, 1, 1, 1, 1, 1, 1]f
     *
     * @param numbers
     * @return
     */
    public static byte[] encodeInterpolate(List<Integer> numbers) {
        ByteBuffer buf = ByteBuffer.allocate(numbers.size() * (Integer.SIZE / Byte.SIZE));
        int last = -1;
        for (int i = 0; i < numbers.size(); i++) {
            Integer num = numbers.get(i);
            if (i == 0) {
                buf.put(encodeNumber(num));
            } else {
                buf.put(encodeNumber(num - last));
            }
            last = num;
        }

        for (Integer number : numbers) {
            buf.put(encodeNumber(number));
        }
        buf.flip();
        byte[] rv = new byte[buf.limit()];
        buf.get(rv);
        return rv;
    }

    public static List<Integer> decodeInterpolate(byte[] byteStream) {
        List<Integer> numbers = new ArrayList<Integer>();
        int n = 0;
        int last = -1;
        boolean notFirst = false;
        for (byte b : byteStream) {
            if ((b & 0xff) < 128) {
                n = 128 * n + b;
            } else {
                int num;
                if (notFirst) {
                    num = last + (128 * n + ((b - 128) & 0xff));

                } else {
                    num = 128 * n + ((b - 128) & 0xff);
                    notFirst = true;
                }
                last = num;
                numbers.add(num);
                n = 0;
            }
        }
        return numbers;
    }

}
