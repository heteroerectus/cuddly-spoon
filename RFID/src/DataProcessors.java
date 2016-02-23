import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by dcmatheX on 2/5/16.
 */



public class DataProcessors {

    public static final int c = 299792458;

    public DataProcessors() {
        super();
    }

    public double getAverage(ArrayList<Double> phases) {
        Double[] sortedArray = phases.toArray(new Double[phases.size()]);

        double sum = 0;
        for (int i = 0; i < sortedArray.length; i++) {
            sum += sortedArray[i];
        }

        return sum / sortedArray.length;
    }

    public double getAverage(double[] phases) {
        double sum = 0;
        for (int i = 0; i < phases.length; i++) {
            sum += phases[i];
        }

        return sum / phases.length;
    }

    public double getStandardDeviation(double[] phases) {
        double mean = getAverage(phases);
        double variance = 0;

        for (int i = 0; i < phases.length; i++) {
            phases[i] = Math.pow((phases[i] - mean), 2);
            variance += phases[i];
        }

        variance = variance / (phases.length - 1);

        return Math.sqrt(variance);
    }

    public double getMedian(ArrayList<Double> phases) {
        Double[] sortedArray = phases.toArray(new Double[phases.size()]);
        Arrays.sort(sortedArray);

        double median;
        if (sortedArray.length % 2 == 0)
            median = (sortedArray[sortedArray.length / 2] + (double) sortedArray[sortedArray.length / 2 - 1]) / 2;
        else
            median = sortedArray[sortedArray.length / 2];

        return median;
    }

    public double getWavelengthFromFrequency(double freqIn) {
        //lambda = speed of light(m/s) / frequency
        return c / (freqIn * 1000);
    }

    public double getFrequencyFromWavelength(double wavelengthIn) {
        //lambda = speed of light(m/s) / frequency
        return c / (wavelengthIn);
    }

    public Map<String, Map<String, ArrayList<RFIDRow>>> getReaderTagMap(ArrayList<RFIDRow> recordList) {
        Map<String, Map<String, ArrayList<RFIDRow>>> readerToTags = new HashMap<>();

        for (RFIDRow record : recordList) {
            String reader = record.mReaderID;
            String tag = record.mTagID;

            if (readerToTags.containsKey(reader)) {
                Map<String, ArrayList<RFIDRow>> tagToData = readerToTags.get(reader);

                if (tagToData.containsKey(tag)) {
                    tagToData.get(tag).add(record);
                } else {
                    ArrayList<RFIDRow> tagreadList = new ArrayList<>();
                    tagreadList.add(record);
                    tagToData.put(tag, tagreadList);
                    readerToTags.put(reader, tagToData);
                }
            } else {
                Map<String, ArrayList<RFIDRow>> tagToData = new HashMap<>();
                ArrayList<RFIDRow> tagreadList = new ArrayList<>();
                tagreadList.add(record);
                tagToData.put(tag, tagreadList);
                readerToTags.put(reader, tagToData);
            }
        }
        return readerToTags;
    }

    //Turvey's method
    public double getDistanceForSingleTagSingleReader(ArrayList<RFIDRow> singleTag) {
        int minFreq = singleTag.get(0).mFrequency;
        int maxFreq = singleTag.get(singleTag.size() - 1).mFrequency;

        int minCount = 0;
        int maxCount = 0;
        double minTotal = 0;
        double maxTotal = 0;

        for (int i = 0; i < singleTag.size(); i++) {
            if (singleTag.get(i).mFrequency == minFreq) {
                minCount++;
                minTotal += singleTag.get(i).mAdjustedPhase;
            }

            if (singleTag.get(i).mFrequency == maxFreq) {
                maxCount++;
                maxTotal += singleTag.get(i).mAdjustedPhase;
            }
        }

        double minFreqAvgPhase = minTotal / minCount;
        double maxFreqAvgPhase = maxTotal / maxCount;

        double slope = 1000 * (maxFreqAvgPhase - minFreqAvgPhase) / (maxFreq - minFreq);

        double estDist = -22.187 * slope - 2.2403;

        return estDist;
    }

    public ArrayList<RFIDRow> getSingleTag(ArrayList<RFIDRow> rawList, String tagID) {
        //String tag = "301402662C1BBD05407B55BB"; //"30143639F8562AC5407B91EB";
        ArrayList<RFIDRow> singleTag = new ArrayList<>();
        DataProcessors dp = new DataProcessors();

        for (int i = 0; i < rawList.size(); i++) {
            if (rawList.get(i).mTagID.equalsIgnoreCase(tagID))
                singleTag.add(rawList.get(i));
        }

        singleTag.sort(new PhaseFreqComparator());
        return singleTag;
    }

    public void destripeSingleTag(ArrayList<RFIDRow> singleTag) {
        try {
            singleTag.get(0).phaseOffset = 0;
            int prevPhaseOffset = 0;
            double prevPhase = singleTag.get(0).mPhase;

            //for each record, check phase vs prev phase and sorts out sawtooth and gets rid of outliers
            for (int i = 1; i < singleTag.size(); i++) {
                if (singleTag.get(i).mPhase - prevPhase > (Math.PI / 2)) {
                    singleTag.get(i).phaseOffset = prevPhaseOffset + 1;
                } else if (singleTag.get(i).mPhase - prevPhase < -(Math.PI / 2)) {
                    singleTag.get(i).phaseOffset = prevPhaseOffset - 1;
                } else {
                    singleTag.get(i).phaseOffset = prevPhaseOffset;
                }

                prevPhase = singleTag.get(i).mPhase;
                prevPhaseOffset = singleTag.get(i).phaseOffset;
            }

            for (int i = 0; i < singleTag.size(); i++) {
                singleTag.get(i).mAdjustedPhase = singleTag.get(i).mPhase - singleTag.get(i).phaseOffset * Math.PI;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public double getConfidenceForSingleTag(ArrayList<RFIDRow> singleTag) {
        double[] adjustedPhases = new double[singleTag.size()];
        for (int i = 0; i < singleTag.size(); i++) {
            adjustedPhases[i] = singleTag.get(i).mAdjustedPhase;
        }

        return getStandardDeviation(adjustedPhases);
    }
}



