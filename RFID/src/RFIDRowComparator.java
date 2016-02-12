import java.util.Comparator;

/**
 * Created by dcmathe on 2/5/16.
 */
public class RFIDRowComparator implements Comparator {

        @Override
        public int compare(Object row1, Object row2) {
            RFIDRow freq1 = (RFIDRow)row1;
            RFIDRow freq2 = (RFIDRow)row2;

            // ascending order (descending order would be: name2.compareTo(name1))
            return (Double.compare(freq1.mFrequency, freq2.mFrequency));

        }

    //use:
    //        RFIDRow[] sortableArray = rfidRows.toArray(new RFIDRow[rfidRows.size()]);
//
//        Arrays.sort(sortableArray, new RFIDRowComparator());
//
//        //find # of unique frequencies
//        Set<Integer> uniqueFreqs = new HashSet<>();
//
//        for (int i = 0; i<sortableArray.length; i++)
//            uniqueFreqs.add(sortableArray[i].mFrequency);
}
