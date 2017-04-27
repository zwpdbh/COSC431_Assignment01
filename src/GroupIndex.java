/**
 * Created by zw on 27/04/2017.
 */
public class GroupIndex implements Comparable<GroupIndex> {
    private String fileName;
    private String theFirstTerm;

    public GroupIndex(String fileName, String theFirstTerm) {
        this.fileName = fileName;
        this.theFirstTerm = theFirstTerm;
    }

    @Override
    public int compareTo(GroupIndex o) {
        return this.theFirstTerm.compareTo(o.theFirstTerm);
    }
}
