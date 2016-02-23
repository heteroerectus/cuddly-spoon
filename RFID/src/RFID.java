/**
 * Created by dcmathe on 2/4/16.
 */

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.supercsv.cellprocessor.*;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import javax.print.DocFlavor;
import javax.xml.crypto.Data;
import java.lang.String;

public class RFID {

    public static final int RUN_LENGTH = 100;
    private static final String CSV_DIRECTORY = "src/resources/";
    private static String CSV_FILENAME = "2_racks.csv"; //2_racks.csv
    private static ArrayList<RFIDRow> rawList;
    private static DataProcessors dp;

    public static void main(String[] args) throws Exception {

        dp = new DataProcessors();
        rawList = readWithCsvListReader();

        Map<String, Map<String, ArrayList<RFIDRow>>> readerTagMap = new HashMap<>();
        readerTagMap = dp.getReaderTagMap(rawList);

        ArrayList<SKU> skus = populateSKUs(readerTagMap);

        //for each reader in sku
        //get confidence from tags

        int a = 0;
        int b = 0;
        int c = 0;

        for (SKU sku : skus)
        {
            a++;
            for (String reader : sku.getReaderToTags().keySet())
            {
                b++;
                System.out.println("b size " + sku.getReaderToTags().keySet().size());
                //populate arraylist of single tags
                double bestConfidenceOfTags = 999;
                double bestDistanceOfTags = 0;

                for (String tagID : sku.getReaderToTags().get(reader).keySet())
                {
                    c++;
                    System.out.println("c size " + sku.getReaderToTags().get(reader).keySet().size());
                    ArrayList<RFIDRow> tagData = sku.getReaderToTags().get(reader).get(tagID);

                    dp.destripeSingleTag(tagData);

                    //get distance and confidence
                    double confidenceForThisTag = dp.getConfidenceForSingleTag(tagData);
                    double distanceForThisTag = dp.getDistanceForSingleTagSingleReader(tagData);

                    //compare by confidence and return distance for SKU/Reader combo
                    if (confidenceForThisTag < bestConfidenceOfTags) {
                        bestConfidenceOfTags = confidenceForThisTag;
                        bestDistanceOfTags = distanceForThisTag;
                    }
                }

                //do the same for this reader
                System.out.println("sku " + sku.getSkuID() + " reader " + reader + " distance " + bestDistanceOfTags + " confidence " + bestConfidenceOfTags );
            }
        }

        System.out.println("a " + a + " b " + b + " c " + c);
    }

    private static ArrayList<SKU> populateSKUs(Map<String, Map<String, ArrayList<RFIDRow>>> readerTagMap)
    {
        ArrayList<SKU> skus = new ArrayList<>();
        String[][] skuGroupings = new String[4][];

        //populate sku list
        String[] sku0tagIDs = {
                "30143639F8562AC5407B334B",
                "30143639F8562A85407B335B",
                "30143639F8562985407B91FB",
                "301402662C1BBFC5407B4A9D",
                "301402662C1BBF85407AECDC",
                "301402662C1BBF45407B556B"};

        String[] sku1tagIDs = {
                "30143639F8562945407B337B",
                "30143639F8562985407B336B",
                "30143639F8562985407BA30B",
                "30143639F8562AC5407B91EB"};

        String[] sku2tagIDs = {
                "301402662C1BBE85407B557B",
                "301402662C1BBE45407B4ABD",
                "301402662C1BBE05407B558B",
                "301402662C1BBD45407B55AB",
                "301402662C1BBD45407B559B",
                "301402662C1BBD05407B55CB"};

        String[] sku3tagIDs = {
                "30143621783153C5407B4C3D",
                "3014362178315485407B4C2D",
                "3014362178315485407B919B",
                "30143621783154C5407B913B",
                "301402662C1BBA45407B4AFD",
                "301402662C1BBA85407B4ADD",
                "301402662C1BBB85407B4ACD",
                "301402662C1BBBC5407B4AED",
                "301402662C1BBBC5407B4A7D",
                "301402662C1BBCC5407AED4B",
                "301402662C1BBD05407B55BB"};

        skuGroupings[0] = sku0tagIDs;
        skuGroupings[1] = sku1tagIDs;
        skuGroupings[2] = sku2tagIDs;
        skuGroupings[3] = sku3tagIDs;

        SKU sku0 = new SKU("0");
        SKU sku1 = new SKU("1");
        SKU sku2 = new SKU("2");
        SKU sku3 = new SKU("3");


        for (String reader : readerTagMap.keySet()){
            System.out.println("keysize " + readerTagMap.get(reader).keySet());
            for (String tag : readerTagMap.get(reader).keySet()){
                if (Arrays.asList(sku0tagIDs).contains(tag)){
                    AddTagsToSKU(sku0, reader, tag, readerTagMap);
                } else if (Arrays.asList(sku1tagIDs).contains(tag)){
                    AddTagsToSKU(sku1, reader, tag, readerTagMap);
                } else if (Arrays.asList(sku2tagIDs).contains(tag)){
                    AddTagsToSKU(sku2, reader, tag, readerTagMap);
                } else if (Arrays.asList(sku3tagIDs).contains(tag)){
                    AddTagsToSKU(sku3, reader, tag, readerTagMap);
                } else {
                    System.out.println("ERROR: WTF??? : " + tag);
                }
            }
        }

        skus.add(sku0);skus.add(sku1);skus.add(sku2);skus.add(sku3);

//        for (int i = 0; i<skuGroupings.length; i++) {
//            SKU sku = new SKU("SKU" + i);
//
//            int hits = 0;
//            for (String skuTag : skuGroupings[i]) {
//
//                Map<String, ArrayList<RFIDRow>> matchingTags = new HashMap<>();
//
//                for (String reader : readerTagMap.keySet()) {
//
//                    for (String tag : readerTagMap.get(reader).keySet()) {
//                        System.out.println("keysize " + readerTagMap.get(reader).keySet());
//
//                        if (skuTag.equalsIgnoreCase(tag)) {
//                            System.out.println(hits++);
//                            matchingTags.put(tag, readerTagMap.get(reader).get(tag));
//
//                            if (sku.getReaderToTags().get(reader) != null)
//                                sku.getReaderToTags().get(reader).put(tag, readerTagMap.get(reader).get(tag));
//                            else
//                            {
//                                Map<String, Map<String, ArrayList<RFIDRow>>> matchingTagsByReader = new HashMap<>();
//                                Map<String, ArrayList<RFIDRow>> tags =
//                                matchingTagsByReader.put(reader,new HashMap<String, ArrayList<RFIDRow>>());
//                                sku.setReaderToTags(matchingTagsByReader);
//
//                                if (sku.getReaderToTags().get(reader).get(tag) != null)
//                                    sku.getReaderToTags().get(reader).put(tag, readerTagMap.get(reader).get(tag));
//                                else {
//                                    ArrayList<RFIDRow> tagToTagData = new ArrayList<>();
//                                    sku.getReaderToTags().get(reader).put(tag, tagToTagData);
//                                    sku.getReaderToTags().get(reader).put(tag, readerTagMap.get(reader).get(tag));
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            skus.add(sku);
//        }

        return skus;
    }

    private static void AddTagsToSKU(SKU sku, String reader, String tag, Map<String, Map<String, ArrayList<RFIDRow>>> readerTagMap){
        if (sku.getReaderToTags().get(reader) == null){
            sku.getReaderToTags().put(reader, new HashMap<String, ArrayList<RFIDRow>>());
        }
        if (sku.getReaderToTags().get(reader).get(tag) == null){
            sku.getReaderToTags().get(reader).put(tag, readerTagMap.get(reader).get(tag));
        } else {
            sku.getReaderToTags().get(reader).get(tag).addAll(readerTagMap.get(reader).get(tag));
        }
    }

    private static void ParseSkuTags(String[] tags, SKU sku, Map<String, Map<String, ArrayList<RFIDRow>>> readerTagMap){
        for (String tag : tags){
            ParseTag(tag, sku, readerTagMap);
        }
    }

    private static void ParseTag(String tag, SKU sku, Map<String, Map<String, ArrayList<RFIDRow>>> readerTagMap){

    }

    private static CellProcessor[] getProcessors() {

        final CellProcessor[] processors = new CellProcessor[]{
                new NotNull(), // Tag ID
                new Optional(), // UPC
                new Optional(), // Reader ID
                new ParseDouble(), // TimeStamp
                new ParseDouble(), // RSSI
                new ParseDouble(), // Phase
                new ParseInt() // Frequency
        };

        return processors;
    }


    private static ArrayList<RFIDRow> readWithCsvListReader() throws Exception {


        ICsvListReader listReader = null;
        try {
            listReader = new CsvListReader(new FileReader(CSV_DIRECTORY + CSV_FILENAME), CsvPreference.STANDARD_PREFERENCE);

            listReader.getHeader(true); // skip the header (can't be used with CsvListReader)
            final CellProcessor[] processors = getProcessors();

            List<Object> record;
            ArrayList<RFIDRow> singleReaderList = new ArrayList<>();
            int index = 0;

            while ((record = listReader.read(processors)) != null) {

                singleReaderList.add(index, new RFIDRow(
                        (String) record.get(0),
                        (String) record.get(1),
                        (String) record.get(2),
                        (double) record.get(3),
                        (double) record.get(4),
                        (double) record.get(5),
                        (int) record.get(6)));

            }

            return singleReaderList;
        } finally {
            if (listReader != null) {
                listReader.close();
            }
        }
    }


    public static ArrayList<RFIDRow> getRawList()
    {
        return rawList;
    }
}

