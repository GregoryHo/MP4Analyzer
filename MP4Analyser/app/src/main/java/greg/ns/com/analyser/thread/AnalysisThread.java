package greg.ns.com.analyser.thread;

import greg.ns.com.gregthreadpoolmanager.BaseThreadTask;
import greg.ns.com.gregthreadpoolmanager.runnable.BaseRunnable;

/**
 * Created by Gregory on 2016/10/5.
 */
public class AnalysisThread extends BaseThreadTask {

	public AnalysisThread(BaseRunnable runnable) {
		super(runnable);
	}
}
