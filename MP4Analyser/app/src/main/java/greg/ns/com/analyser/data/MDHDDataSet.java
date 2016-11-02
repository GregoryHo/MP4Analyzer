package greg.ns.com.analyser.data;

/**
 * Created by Gregory on 2016/10/4.
 */
public class MDHDDataSet {

	private int timeScale;

	private int duration;

	public MDHDDataSet(int timeScale, int duration) {
		this.timeScale = timeScale;
		this.duration = duration;
	}

	public int getTimeScale() {
		return timeScale;
	}

	public int getDuration() {
		return duration;
	}
}
