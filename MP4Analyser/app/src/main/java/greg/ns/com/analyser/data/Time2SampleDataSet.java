package greg.ns.com.analyser.data;

/**
 * Created by Gregory on 2016/10/3.
 */
public class Time2SampleDataSet {

	// counts of sample
	private int sampleCount;

	// duration of sample
	private int sampleDurations;

	public Time2SampleDataSet(int sampleCount, int sampleDurations) {
		this.sampleCount = sampleCount;
		this.sampleDurations = sampleDurations;
	}

	public int getSampleCount() {
		return sampleCount;
	}

	public int getSampleDurations() {
		return sampleDurations;
	}
}
