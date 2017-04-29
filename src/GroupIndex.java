import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by zw on 27/04/2017.
 */
public class GroupIndex implements Serializable {
    public String path;

    public HashMap<String, PostingsRecords> subHashMap;

    public void setSubHashMap(HashMap<String, PostingsRecords> subHashMap) {
        this.subHashMap = subHashMap;
    }

    public GroupIndex(String path) {
        this.path = path;
        this.subHashMap = new HashMap<>();
    }
}
