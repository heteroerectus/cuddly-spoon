import java.util.Comparator;

public class PhaseFreqComparator implements Comparator<RFIDRow>
{
    public int compare(RFIDRow a, RFIDRow b)
    {
        if(a.mFrequency == b.mFrequency)
            return (int) Math.round(a.mPhase - b.mPhase);
        else
            return a.mFrequency - b.mFrequency;
    }
}