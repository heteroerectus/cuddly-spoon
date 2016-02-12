import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by dcmatheX on 2/5/16.
 */



public class DataProcessors {

    public static final int c = 299792458;
    public static final double poo = 0.0491;

    public DataProcessors()
    {
        super();
    }

    public ArrayList<RFIDRow> parseSingleTagSingleReader(String tagID, String readerID) {
        ArrayList<RFIDRow> tempList = new ArrayList<>();

        for (int i = 0; i < RFID.getRawList().size(); i++) {
            if (RFID.getRawList().get(i).mTagID.equals(tagID) && RFID.getRawList().get(i).mReaderID.equals(readerID)) {
                tempList.add(RFID.getRawList().get(i));
            }
        }

        return tempList;
    }

    public Map<Double, ArrayList<Double>> getPhasesWithinEachWavelength (ArrayList<RFIDRow> rfidRows)
    {
        Map<Double, ArrayList<Double>> phasesPerFrequecy = new HashMap<>();

         /*
        double rssiMax = -9999;
        //filter by strongest RSSI
        for (int i = 0; i<rfidRows.size(); i++) {
            double rssi = rfidRows.get(i).mRSSI;
            if (rssi > rssiMax)
                rssiMax = rssi;
        }


        TreeMap<String, ArrayList<RFIDRow>> rfidRowsByTag = new TreeMap<String, ArrayList<RFIDRow>>();

        for (RFIDRow row : rfidRows) {
            ArrayList<RFIDRow> tagRows = rfidRowsByTag.get(row.mTagID);

        }
*/
        for (int i = 0; i<rfidRows.size(); i++)
        {
            //parse out one tag
            if (rfidRows.get(i).mTagID.equals("1006835900000000000008D4")) { //increment last number from 0-4

                double wavelength = getWavelengthFromFrequency(rfidRows.get(i).mFrequency);
                ArrayList<Double> value = phasesPerFrequecy.get(wavelength); //key

                if (value == null) {
                    value = new ArrayList<>();
                    phasesPerFrequecy.put(wavelength, value);
                }

                value.add(rfidRows.get(i).mPhase);
            }
        }

        return phasesPerFrequecy;
    }

    public double getAverage (ArrayList<Double> phases)
    {
        Double[] sortedArray = phases.toArray(new Double[phases.size()]);

        double sum = 0;
        for (int i = 0; i<sortedArray.length; i++)
        {
            sum+=sortedArray[i];
        }

        return sum/sortedArray.length;
    }

    public double[] getStandardDeviation(ArrayList<Double> phases)
    {
        double[] std = new double[phases.size()];
        double mean = getAverage(phases);
        for(int i = 0; i < phases.size(); i++){
            std[i] = Math.pow((phases.get(i).doubleValue() - mean),2);
        }

        return std;
    }

    public double getMedian (ArrayList<Double> phases)
    {
        Double[] sortedArray = phases.toArray(new Double[phases.size()]);
        Arrays.sort(sortedArray);

        double median;
        if (sortedArray.length % 2 == 0)
            median = (sortedArray[sortedArray.length/2] + (double)sortedArray[sortedArray.length/2 - 1])/2;
        else
            median = sortedArray[sortedArray.length/2];

        return median;
    }

    public double getWavelengthFromFrequency(double freqIn)
    {
        //lambda = speed of light(m/s) / frequency
        return c / (freqIn * 1000);
    }

    public double getFrequencyFromWavelength(double wavelengthIn)
    {
        //lambda = speed of light(m/s) / frequency
        return c / (wavelengthIn);
    }

    public double[] getDistanceCandidates (double lambda, double alpha)
    {
        //System.out.println("alpha " + alpha + " lambda " + lambda);
        double[] n = new double[RFID.RUN_LENGTH];
        for (int i = 0; i<RFID.RUN_LENGTH; i++)
            n[i] = lambda * (alpha / 2 / Math.PI) - (i * lambda);

        return n;

    }

    public double calculateDistanceWithEquation (ArrayList<MedianPhasePerWavelength> rawList)
    {
        double smallestPhase = 100;
        int smallestPhaseIndex = 0;
        int largestPhaseIndex = 0;
        double largestPhase = 0;

        for (int i = 0; i<rawList.size(); i++)
        {
            double currentPhase = rawList.get(i).phaseMedian;
            if (smallestPhase > currentPhase) {
                smallestPhase = currentPhase;
                smallestPhaseIndex = i;
            }
            if (largestPhase < currentPhase) {
                largestPhase = currentPhase;
                largestPhaseIndex = i;
            }
        }

        double phaseMedianDelta = rawList.get(largestPhaseIndex).phaseMedian - rawList.get(smallestPhaseIndex).phaseMedian;
        double freqDelta = getFrequencyFromWavelength(rawList.get(largestPhaseIndex).wavelength) - getFrequencyFromWavelength(rawList.get(smallestPhaseIndex).wavelength);

        System.out.println("Phase median large/small " + rawList.get(largestPhaseIndex).phaseMedian + " " + rawList.get(smallestPhaseIndex).phaseMedian);
        System.out.println("Phase median delta " + phaseMedianDelta);
        return (c * phaseMedianDelta) / ((Math.PI * 4)* freqDelta);
    }

    public double calculateDistanceWithEquation (double slope)
    {
        double theSlope = -slope * (c / (Math.PI*4));
        System.out.println(slope);
        return theSlope;
    }

    public double getSlope(ArrayList<MedianPhasePerWavelength> rawList )
    {
        Collections.sort(rawList, (p1, p2) -> p1.wavelength<p2.wavelength ? -1 : 1);

        double phaseTotal = 0;
        double frequencyTotal = 0;
        int size = rawList.size()/2;
        double lsrNumerator = 0;
        double lsrDenominator = 0;

        //Calculate phaseMedianAverage
        for (int i = 0; i < size; i++)
        {
            phaseTotal += rawList.get(i).phaseMedian;
            frequencyTotal += getFrequencyFromWavelength(rawList.get(i).wavelength);


        }

        double phaseMedianAverage = phaseTotal/size;
        double frequencyAverage = frequencyTotal/size;

        //Calculate slope of regression line (least squares method)
        for (int i = 0; i < size; i++)
        {
            lsrNumerator += (rawList.get(i).phaseMedian - phaseMedianAverage) * (getFrequencyFromWavelength(rawList.get(i).wavelength) - frequencyAverage);
            lsrDenominator += Math.pow((rawList.get(i).phaseMedian - phaseMedianAverage), 2);
        }
        return lsrNumerator/lsrDenominator;
    }
}

//    public Pair<Double, Double> phaseDeltaForGivenDistance ()
//    {
//        double[] ranges = new double[] {0.5, 1, 1.5, 2, 2.5, 3.0, 3.5, 4.0, 4.5, 5, 5.5, 6, 6.5, 7, 7.5, 8, 8.5, 9, 9.5, 10};
//        int[] freqDeltas = new int[] {600000, 1200000, 1800000};
//
//        for (int i = 0; i<ranges.length; i++)
//        {
//            for (int j = 0; j < freqDeltas.length; j++) {
//
//                double deltaPhase = ranges[i] * ((Math.PI*4)/c) * freqDeltas[j];
//            }
//
//
//            Pair<Double, Double>
//        }
//    }




