package greg.ns.com.analyser.data;

/**
 * Created by Gregory on 2016/10/19.
 */
public class SyncFrameDataSet {

	private float timeStamp;

	private int offset;

	public SyncFrameDataSet(float timeStamp, int offset) {
		this.timeStamp = timeStamp;
		this.offset = offset;
	}

	public float getTimeStamp() {
		return timeStamp;
	}

	public int getOffset() {
		return offset;
	}
}
