package greg.ns.com.analyser.thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import greg.ns.com.gregthreadpoolmanager.BaseThreadManager;
import greg.ns.com.gregthreadpoolmanager.BaseThreadTask;
import greg.ns.com.gregthreadpoolmanager.runnable.BaseRunnable;

/**
 * Created by Gregory on 2016/10/5.
 */
public class AnalyserThreadManager extends BaseThreadManager<ThreadPoolExecutor> {

	private static final String TAG = "AnalyserThreadManager";

	private static volatile AnalyserThreadManager instance;

	private boolean showLog = false;

	private final AnalysisHandler handler;

	private AnalyserThreadManager() {
		handler = new AnalysisHandler(Looper.getMainLooper());
	}

	public static AnalyserThreadManager getInstance() {
		if (instance == null) {
			synchronized (AnalyserThreadManager.class) {
				if (instance == null) {
					instance = new AnalyserThreadManager();
				}
			}
		}

		return instance;
	}

	@Override
	public ThreadPoolExecutor createThreadPool() {
		return new ThreadPoolExecutor(
				NUMBER_OF_CORES,
				NUMBER_OF_CORES * 2,
				KEEP_ALIVE_TIME,
				KEEP_ALIVE_TIME_UNIT,
				new LinkedBlockingQueue<Runnable>()
		);
	}

	@Override
	public void handleState(BaseThreadTask baseThreadTask, int state) {
		switch (state) {
			// The task finished connecting to the plug
			case BaseRunnable.COMPLETE_STATUS:
				handler.obtainMessage(state, baseThreadTask).sendToTarget();
				break;

			case BaseRunnable.EXCEPTION_STATUS:
				handler.obtainMessage(state, baseThreadTask).sendToTarget();
				break;

			default:
				break;
		}
	}

	@Override
	public BaseThreadTask createBaseThreadTask(BaseRunnable baseRunnable) {
		return new AnalysisThread(baseRunnable);
	}

	private static class AnalysisHandler extends Handler {

		public AnalysisHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			BaseThreadTask threadTask = (BaseThreadTask) msg.obj;

			switch (msg.what) {
				case BaseRunnable.COMPLETE_STATUS:
					if (instance.showLog) {
						Log.e(TAG,
								threadTask.getRunnableObject().getThreadName()
										+ " is completed : "
										+ threadTask.getRunnableObject().getResult()
						);
					}

					instance.recycleTask(threadTask);
					break;

				case BaseRunnable.EXCEPTION_STATUS:
					if (instance.showLog) {
						Log.e(TAG,
								threadTask.getRunnableObject().getThreadName()
										+ " is interrupted : "
										+ threadTask.getRunnableObject().getResult()
						);
					}
					break;

				default:
					// Otherwise, calls the super method
					super.handleMessage(msg);
					break;
			}
		}
	}
}
