import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by zw on 27/04/2017.
 */
public class GroupIndex implements Serializable {
    private String path;

    public HashMap<String, PostingsRecords> subHashMap;

    public GroupIndex(String path) {
        this.path = path;
        this.subHashMap = new HashMap<>();
    }
}
