import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dcmathe on 2/22/16.
 */
public class SKU {
    private String skuID;
    Map<String, Map<String, ArrayList<RFIDRow>>> readerToTags;

    public Map<String, Map<String, ArrayList<RFIDRow>>> getReaderToTags() {
        return readerToTags;
    }

    public void setReaderToTags(Map<String, Map<String, ArrayList<RFIDRow>>> readerToTags) {
        this.readerToTags = readerToTags;
    }

    public String getSkuID() {
        return skuID;
    }

    public void setSkuID(String skuID) {
        this.skuID = skuID;
    }
}
