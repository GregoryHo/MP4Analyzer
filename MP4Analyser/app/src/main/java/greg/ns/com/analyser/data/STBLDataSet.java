package greg.ns.com.analyser.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gregory on 2016/10/4.
 */
public class STBLDataSet {

	private List<Time2SampleDataSet> time2SampleDataSets;

	private List<Integer> syncFrameDataSets;

	private List<Sample2ChunkDataSet> sample2ChunkDataSets;

	private List<Integer> sampleSizeDataSets;

	private List<Integer> chunkOffsetDataSets;

	public STBLDataSet(List<Time2SampleDataSet> time2SampleDataSets, List<Integer> syncFrameDataSets,
	                   List<Sample2ChunkDataSet> sample2ChunkDataSets, List<Integer> sampleSizeDataSets, List<Integer> chunkOffsetDataSets) {
		this.time2SampleDataSets = time2SampleDataSets == null ? new ArrayList<Time2SampleDataSet>() : time2SampleDataSets;
		this.syncFrameDataSets = syncFrameDataSets == null ? new ArrayList<Integer>() : syncFrameDataSets;
		this.sample2ChunkDataSets = sample2ChunkDataSets == null ? new ArrayList<Sample2ChunkDataSet>() : sample2ChunkDataSets;
		this.sampleSizeDataSets = sampleSizeDataSets == null ? new ArrayList<Integer>() : sampleSizeDataSets;
		this.chunkOffsetDataSets = chunkOffsetDataSets == null ? new ArrayList<Integer>() : chunkOffsetDataSets;
	}

	public List<Time2SampleDataSet> getTime2SampleDataSets() {
		return time2SampleDataSets;
	}

	public List<Integer> getSyncFrameDataSets() {
		return syncFrameDataSets;
	}

	public List<Sample2ChunkDataSet> getSample2ChunkDataSets() {
		return sample2ChunkDataSets;
	}

	public List<Integer> getSampleSizeDataSets() {
		return sampleSizeDataSets;
	}

	public List<Integer> getChunkOffsetDataSets() {
		return chunkOffsetDataSets;
	}
}
