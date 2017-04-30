import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by zw on 27/04/2017.
 */

/**
 * This class represents a small section from the whole inverted index
 * path, specify the directory, in which includes: indexed_terms_in_binary, postings_records_for_DocIDs
 * and postings_records_for_TFs.
 *
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
