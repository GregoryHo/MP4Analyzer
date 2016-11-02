package greg.ns.com.analyser;

/**
 * Created by Gregory on 2016/10/11.
 */
public interface AnalysisListener {

	void onFTYPBoxReady();

	void onFTYPBoxError(int requireSize);

	void onMOOVBoxReady(MP4Analyser.Compositor analyser);

	void onMOOVBoxError(int requireSize);

	void onTRAKNotFound();
}
