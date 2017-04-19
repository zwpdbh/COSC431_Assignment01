import java.io.Serializable;

/**
 * Created by zw on 19/04/2017.
 */
public class PostingsRecords implements Serializable {
    long start;
    int length;

    public PostingsRecords(long start, int length) {
        this.start = start;
        this.length = length;
    }

    @Override
    public String toString() {
        return "PostingsRecords{" +
                "start=" + start +
                ", length=" + length +
                '}';
    }
}
