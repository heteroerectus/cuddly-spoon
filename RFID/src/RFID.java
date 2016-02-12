/**
 * Created by dcmathe on 2/4/16.
 */

import java.io.FileReader;
import java.io.FileWriter;
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
import java.lang.String;

public class RFID {

    public static final int RUN_LENGTH = 100;
    private static final String CSV_DIRECTORY = "src/resources/";
    private static String CSV_FILENAME = "2_Racks.csv";
    private static ArrayList<RFIDRow> rawList;

    public static void main(String[] args) throws Exception {

        //getDistanceForAllFiles();
        getDistanceForSingleFile2(CSV_DIRECTORY + CSV_FILENAME);


        /*
        double[][] nResults = new double[RUN_LENGTH][medians.size()];
        double[][] nDeviations = new double[RUN_LENGTH][medians.size()];

        for (int i = 0; i<candidates.length; i++) {

            //System.out.println("Candidate " + i + " " + candidates[i]);

            for (int j = 0; j<4; j++)
            {
                double x = candidates[i];
                double lambda = medians.get(j).wavelength;
                double alpha = medians.get(j).phaseMedian;

                //System.out.println("alpha " + alpha + " lambda " + lambda);

                nResults[i][j] = (x - (lambda * (alpha / (2 * Math.PI)))) / lambda;
                double result = nResults[i][j];

                nDeviations[i][j] = result - Math.round(result);
            }
        }

        //iterate through each wavelength/candidate/n to find near integer values
        for (int i = 0; i < nResults.length; i++)
        {
            System.out.println(i + " " + nDeviations[i][0] + " " + nDeviations[i][1] + " " + nDeviations[i][2] + " " + nDeviations[i][3] );
        }
        */

    }

    private static void getDistanceForAllFiles () throws Exception
    {
        Files.walk(Paths.get(CSV_DIRECTORY)).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                if (filePath.toString().endsWith(".csv")) {

                    ArrayList<MedianPhasePerWavelength> medians = parseDataFromFile(filePath.toString());

                    DataProcessors dp = new DataProcessors();

                    if (medians.size() > 1)
                        System.out.println(filePath.toString().substring(filePath.toString().length()-9, filePath.toString().length()-6) + " " + dp.calculateDistanceWithEquation(medians));
                    else
                        System.out.println(filePath.toString().substring(filePath.toString().length()-9, filePath.toString().length()-6) + " was equal to " + medians.size());

                    //scott's concept
                    /*
                    double wavelengthDiff = medians.get(0).wavelength - medians.get(3).wavelength;
                    double phaseDiff = (medians.get(0).phaseMedian / (2 * Math.PI) * medians.get(0).wavelength) - (medians.get(3).phaseMedian / (2 * Math.PI) * medians.get(0).wavelength);
                    double distance = Math.abs((phaseDiff * medians.get(2).wavelength) + phaseDiff);
                    System.out.println(filePath.toString().substring(filePath.toString().length()-9, filePath.toString().length()-6) + " " + distance);
                    */

                }
            }
        });
    }

    private static void getDistanceForSingleFile(String filename)
    {
        ArrayList<MedianPhasePerWavelength> medians = parseDataFromFile(filename);
        DataProcessors dp = new DataProcessors();

        System.out.println("Estimated distance from equation: " + filename.substring(filename.length()-9, filename.length()-6) + " " + dp.calculateDistanceWithEquation(medians));

        System.out.println("Estimated distance from slope: " + filename.substring(filename.length()-9, filename.length()-6) + " " + dp.calculateDistanceWithEquation(dp.getSlope(medians)));


        //get distance candidates for first wavelength

//        double[] candidates = dp.getDistanceCandidates(medians.get(0).wavelength, medians.get(0).phaseMedian); //30
//        for (int i = 0; i < candidates.length; i++) {
//            double candidate = candidates[i];
//            System.out.println(candidate);
//        }

    }

    private static double getDistanceForSingleFile2(String filename)
    {
        ArrayList<RFIDRow> rows = parseDataFromFile2(filename);

        int minFreq = rows.get(0).mFrequency;
        int maxFreq = rows.get(rows.size()-1).mFrequency;

        int minCount = 0;
        int maxCount = 0;
        double minTotal = 0;
        double maxTotal = 0;

        for(int i = 0; i < rows.size(); i++)
        {
            if(rows.get(i).mFrequency == minFreq)
            {
                minCount++;
                minTotal += rows.get(i).mAdjustedPhase;
            }

            if(rows.get(i).mFrequency == maxFreq)
            {
                maxCount++;
                maxTotal += rows.get(i).mAdjustedPhase;
            }
        }

        for (int i = 0; i < rawList.size(); i++)
        {
            System.out.println("Index " + i + ":\tFreq: " + rawList.get(i).mFrequency + "\tPhase: " + rawList.get(i).mPhase + "\tphaseOffset: " + rawList.get(i).phaseOffset + "\tmAdjustedPhase: " + rawList.get(i).mAdjustedPhase);
        }

        double minFreqAvgPhase = minTotal/minCount;
        double maxFreqAvgPhase = maxTotal/maxCount;

        double slope = 1000 * (maxFreqAvgPhase-minFreqAvgPhase) / (maxFreq-minFreq);

        double estDist = -22.187 * slope - 2.2403;

        System.out.println("For file " + filename + ", estimated distance = " + estDist);

        return estDist;
    }

    private static ArrayList<RFIDRow> parseDataFromFile2(final String filename)
    {
        CSV_FILENAME = filename;
        try {
            rawList = readWithCsvListReader();
            String tag = "30143639F8562AC5407B91EB";
            ArrayList<RFIDRow> singleTag = new ArrayList<>();


            for(int i = 0; i < rawList.size(); i++)
            {
                if(rawList.get(i).mTagID.equalsIgnoreCase(tag))
                    singleTag.add(rawList.get(i));
            }

            singleTag.sort(new PhaseFreqComparator());

//            for (int i = 0; i < singleTag.size(); i++) {
//                System.out.println("Index " + i + ":\tFreq: " + singleTag.get(i).mFrequency + "\tPhase: " + singleTag.get(i).mPhase);
//            }

            singleTag.get(0).phaseOffset = 0;
            int prevPhaseOffset = 0;
            double prevPhase = singleTag.get(0).mPhase;

            //for each record, check phase vs prev phase and sorts out sawtooth and gets rid of outliers
            for (int i = 1; i < singleTag.size(); i++) {
                if (singleTag.get(i).mPhase - prevPhase > (Math.PI / 2)) {
                    singleTag.get(i).phaseOffset = prevPhaseOffset + 1;
                } else if (singleTag.get(i).mPhase - prevPhase < -(Math.PI / 2)) {
                    singleTag.get(i).phaseOffset = prevPhaseOffset - 1;
                }
                else
                {
                    singleTag.get(i).phaseOffset = prevPhaseOffset;
                }

                prevPhase = singleTag.get(i).mPhase;
                prevPhaseOffset = singleTag.get(i).phaseOffset;
            }

            for(int i = 0; i < singleTag.size(); i++)
            {
                singleTag.get(i).mAdjustedPhase = singleTag.get(i).mPhase - singleTag.get(i).phaseOffset*Math.PI;
            }

            return singleTag;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private static ArrayList<MedianPhasePerWavelength> parseDataFromFile(final String filename)
    {
        CSV_FILENAME = filename;
        try {
            rawList = readWithCsvListReader();

            DataProcessors dp = new DataProcessors();
            Map<Double, ArrayList<Double>> phasesWithinEachWavelength = dp.getPhasesWithinEachWavelength(rawList); //for one tag
            ArrayList<MedianPhasePerWavelength> medianPhases = new ArrayList<>();

            for (Double wavelength : phasesWithinEachWavelength.keySet()) {
                MedianPhasePerWavelength mppwl = new MedianPhasePerWavelength();
                mppwl.wavelength = wavelength;

                //------- std dev
//                double[] standardDev = dp.getStandardDeviation(phasesWithinEachWavelength.get((wavelength)));
//
//                ArrayList<Double> values = new ArrayList<>();
//
//                for (double val : standardDev)
//                    values.add(val);
                //-------

                mppwl.phaseMedian = dp.getMedian(phasesWithinEachWavelength.get(wavelength)); //was using getMedian
                medianPhases.add(mppwl);
            }

            return medianPhases;

        }
        catch (Exception e) {
            System.out.println(e);
        }

        return null;
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
            listReader = new CsvListReader(new FileReader(CSV_FILENAME), CsvPreference.STANDARD_PREFERENCE);

            listReader.getHeader(true); // skip the header (can't be used with CsvListReader)
            final CellProcessor[] processors = getProcessors();

            List<Object> customerList;
            ArrayList<RFIDRow> singleReaderList = new ArrayList<>();
            int index = 0;

            while ((customerList = listReader.read(processors)) != null) {

                singleReaderList.add(index, new RFIDRow(
                        (String) customerList.get(0),
                        (String) customerList.get(1),
                        (String) customerList.get(2),
                        (double) customerList.get(3),
                        (double) customerList.get(4),
                        (double) customerList.get(5),
                        (int) customerList.get(6)));

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

        /*
    private void writeWithCsvListWriter(ArrayList<RFIDRow> rawList) throws Exception
    {
        // create the customer Lists (CsvListWriter also accepts arrays!)
        final List<Object> john = Arrays.asList(new Object[] { "1", "John", "Dunbar",
                new GregorianCalendar(1945, Calendar.JUNE, 13).getTime(),
                "1600 Amphitheatre Parkway\nMountain View, CA 94043\nUnited States", null, null,
                "\"May the Force be with you.\" - Star Wars", "jdunbar@gmail.com", 0L });

        final List<Object> bob = Arrays.asList(new Object[] { "2", "Bob", "Down",
                new GregorianCalendar(1919, Calendar.FEBRUARY, 25).getTime(),
                "1601 Willow Rd.\nMenlo Park, CA 94025\nUnited States", true, 0,
                "\"Frankly, my dear, I don't give a damn.\" - Gone With The Wind", "bobdown@hotmail.com", 123456L });

        ICsvListWriter listWriter = null;
        try {
            listWriter = new CsvListWriter(new FileWriter("target/writeWithCsvListWriter.csv"),
                    CsvPreference.STANDARD_PREFERENCE);

            final CellProcessor[] processors = getProcessors();
            final String[] header = new String[] { "tag", "wavelength", "phaseMediam", "rssi" };

            // write the header
            listWriter.writeHeader(header);

            // write the customer lists
            listWriter.write(john, processors);
            listWriter.write(bob, processors);

        }
        finally {
            if( listWriter != null ) {
                listWriter.close();
            }
        }
    }
*/
}

