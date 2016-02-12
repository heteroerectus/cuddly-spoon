/**
 * Created by dcmatheX on 2/4/16.
 */
public class RFIDRow {

    public String mTagID;
    public String mUPC;
    public String mReaderID;
    public double mTimestamp;
    public double mRSSI;
    public double mPhase;
    public int mFrequency;

    public RFIDRow(String tagId, String UPC, String readerID, double timestamp, double RSSI, double phase, int freq) {
        mTagID = tagId;
        mUPC = UPC;
        mReaderID = readerID;
        mTimestamp = timestamp;
        mRSSI = RSSI;
        mPhase = phase;
        mFrequency = freq;

    }
}
