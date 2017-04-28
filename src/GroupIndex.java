/**
 * Created by zw on 27/04/2017.
 */
public class GroupIndex implements Comparable<GroupIndex> {
    private String path;
    private String theFirstTerm;

    public GroupIndex(String path, String theFirstTerm) {
        this.path = path;
        this.theFirstTerm = theFirstTerm;
    }

    @Override
    public int compareTo(GroupIndex o) {
        return this.theFirstTerm.compareTo(o.theFirstTerm);
    }
}
