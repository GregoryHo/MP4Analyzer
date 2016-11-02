package greg.ns.com.analyser.data;

/**
 * Created by Gregory on 2016/9/30.
 */
public class Sample2ChunkDataSet {

	// The first chunk index
	private int firstChunk;

	// Current sample count of chunk
	private int samplesPerChunk;

	// sSample description index
	private int sampleDescriptionIndex;

	public Sample2ChunkDataSet(int firstChunk, int samplesPerChunk, int sampleDescriptionIndex) {

		this.firstChunk = firstChunk;
		this.samplesPerChunk = samplesPerChunk;
		this.sampleDescriptionIndex = sampleDescriptionIndex;
	}

	public int getFirstChunk() {
		return firstChunk;
	}

	public int getSamplesPerChunk() {
		return samplesPerChunk;
	}

	public int getSampleDescriptionIndex() {
		return sampleDescriptionIndex;
	}
}
