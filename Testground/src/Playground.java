import java.util.ArrayList;
import java.util.Map.*;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by zw on 27/04/2017.
 */
public class Playground {

    public static ArrayList<SortedMap<Integer, Integer>> getSlicesFrom(TreeMap<Integer, Integer> index, int numOfGroups) {

        ArrayList<SortedMap<Integer, Integer>> slices = new ArrayList<>();
        int groupSize = index.size() / numOfGroups;
        ArrayList<Integer> marks = new ArrayList<>();
        int counter = 0;

        for (Entry<Integer, Integer> entry: index.entrySet()) {
            if (counter == 0 || counter % groupSize == 0) {
                marks.add(entry.getKey());
            }
            counter += 1;
        }

        int numOfSlices = marks.size();
        marks.add(index.lastKey());

        for (int i = 1; i <= numOfSlices; i++) {
            if (i != numOfSlices) {
                slices.add(index.subMap(marks.get(i - 1), true, marks.get(i), false));
            } else {
                slices.add(index.subMap(marks.get(i - 1), true, marks.get(i), true));
            }
        }
        System.out.println("The marks are: ");
        System.out.println(marks);
        return slices;
    }

    public static int searchTerm(int item, ArrayList<SortedMap<Integer, Integer>> index) {
        ArrayList<Integer> marks = new ArrayList<>();
        for (SortedMap<Integer, Integer> group: index) {
            marks.add(group.firstKey());
        }

        int left = marks.get(0);
        int right = marks.get(marks.size() - 1);


        return 0;
    }

    public static void main(String[] args) {
        TreeMap<Integer, Integer> sampleData = new TreeMap<>();


        Random generator = new Random();

        int base = 20;
//        int num = generator.nextInt(100) + base;
        int num = 180;
        System.out.println("Total number of items: " + num);


        for (int i = 0; i < num; i++) {
            sampleData.put(i, i);
        }
        System.out.println(sampleData);

        int numOfGroups = 20;
        ArrayList<SortedMap<Integer, Integer>> slices = getSlicesFrom(sampleData, numOfGroups);
        System.out.println("The size of each group is: " + (num / numOfGroups));
        System.out.println(slices);


        TreeMap<Integer, Integer> myIndex = new TreeMap<>();
        for (int i = 1; i <= 10; i++) {
            myIndex.put(i * 10, i *10);
        }
        System.out.println(myIndex);
        System.out.println(myIndex.floorKey(12));
        System.out.println(myIndex.floorKey(29));
        System.out.println(0 % 20);
    }

}
