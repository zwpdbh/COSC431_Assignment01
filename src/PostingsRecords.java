import java.io.Serializable;

/**
 * Created by zw on 19/04/2017.
 */
public class PostingsRecords {
    long start;
    int size;

    public PostingsRecords(long start, int length) {
        this.start = start;
        this.size = length;
    }

    @Override
    public String toString() {
        return "PostingsRecords{" +
                "start=" + start +
                ", length=" + size +
                '}';
    }
}
