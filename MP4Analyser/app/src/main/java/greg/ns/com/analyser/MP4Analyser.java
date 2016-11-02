package greg.ns.com.analyser;

import android.util.SparseArray;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import greg.ns.com.analyser.data.MDHDDataSet;
import greg.ns.com.analyser.data.STBLDataSet;
import greg.ns.com.analyser.data.Sample2ChunkDataSet;
import greg.ns.com.analyser.data.SyncFrameDataSet;
import greg.ns.com.analyser.data.TRAKDataSet;
import greg.ns.com.analyser.data.Time2SampleDataSet;
import greg.ns.com.analyser.thread.AnalyserThreadManager;
import greg.ns.com.gregthreadpoolmanager.runnable.BaseRunnable;

/**
 * Created by Gregory on 2016/10/3.
 */
public class MP4Analyser {

	private MP4Analyser() {
		throw new UnsupportedOperationException();
	}

	public static Compositor analysisWith(byte[] bytes) {
		return new Compositor(bytes);
	}

	public static Compositor analysisWith(InputStream in) {
		return new Compositor(in);
	}

	public static final class Compositor {

		private AnalysisListener analysisListener;

		private byte[] analysisData;

		private int ftypBoxTypeIndex;

		private int moovBoxTypeIndex;

		private int mdatBoxTypeIndex;

		private int mvhdBoxTypeIndex;

		private int duration;

		private List<Integer> trakList;

		private HashMap<Integer, TRAKDataSet> trakDataSetHashMap;

		private SparseArray<List<Integer>> time2SampleTables;

		private SparseArray<List<Integer>> sample2ChunkTables;

		private SparseArray<List<SyncFrameDataSet>> syncFrameTables;

		public Compositor(byte[] bytes) {
			this.analysisData = bytes;
		}

		public Compositor(InputStream in) {
			this(ConvertUtils.inputStream2Bytes(in));
		}

		public Compositor setAnalysisListener(AnalysisListener analysisListener) {
			this.analysisListener = analysisListener;
			return this;
		}

		public Compositor removeAnalysisListener() {
			analysisListener = null;
			return this;
		}

		/**
		 * Analysis ftyp box info, running on the thread.
		 *
		 * @return
		 */
		public Compositor analysisFTYP() {
			AnalyserThreadManager.getInstance().startWork(new BaseRunnable<Boolean>() {
				@Override
				public Boolean runImp() throws Exception {
					ftypBoxTypeIndex = MP4Utils.findFTYPBoxTypeIndex(analysisData, 0);
					if (ftypBoxTypeIndex != -1) {
						int boxSize = MP4Utils.getBoxSize(analysisData, ftypBoxTypeIndex - 4);
						if (ftypBoxTypeIndex - 4 + boxSize > analysisData.length) {
							analysisListener.onFTYPBoxError(ftypBoxTypeIndex - 4 + boxSize);
						} else {
							String majorBrand = ConvertUtils.bytes2ASCII(analysisData, ftypBoxTypeIndex + 4);
							String compatible_1 = ConvertUtils.bytes2ASCII(analysisData, ftypBoxTypeIndex + 12);
							String compatible_2 = ConvertUtils.bytes2ASCII(analysisData, ftypBoxTypeIndex + 16);
							String compatible_3 = ConvertUtils.bytes2ASCII(analysisData, ftypBoxTypeIndex + 20);
							String compatible_4 = ConvertUtils.bytes2ASCII(analysisData, ftypBoxTypeIndex + 24);
							System.out.print("=============File=============" + "\n"
									+ "major brand : " + majorBrand + "\n"
									+ "compatible brand : " + compatible_1 + "\n"
									+ "compatible brand : " + compatible_2 + "\n"
									+ "compatible brand : " + compatible_3 + "\n"
									+ "compatible brand : " + compatible_4 + "\n"
							);
							analysisListener.onFTYPBoxReady();
						}
					}

					return true;
				}

				@Override
				public String getThreadName() {
					return "analysisFTYP";
				}
			});

			return this;
		}

		/**
		 * Analysis moov box info, running on the thread.
		 *
		 * @return
		 */
		public Compositor analysisMOOV() {
			final Compositor compositor = this;
			AnalyserThreadManager.getInstance().startWork(new BaseRunnable<Boolean>() {
				@Override
				public Boolean runImp() throws Exception {
					moovBoxTypeIndex = MP4Utils.findMOOVBoxTypeIndex(analysisData, 0);
					/* Layer 2 (root/moov) - mvhd, trak */
					if (moovBoxTypeIndex != -1) {
						int boxSize = MP4Utils.getBoxSize(analysisData, moovBoxTypeIndex - 4);
						if (moovBoxTypeIndex - 4 + boxSize > analysisData.length) {
							analysisListener.onMOOVBoxError(moovBoxTypeIndex - 4 + boxSize);
						} else {
							if (analysisMVHD() && analysisTRAK()) {
								analysisListener.onMOOVBoxReady(compositor);
							}
						}
					}
					return true;
				}

				@Override
				public String getThreadName() {
					return "analysisMOOV";
				}
			});

			return this;
		}


		/**
		 * Analysis mdat box info, running on the thread.
		 *
		 * @return
		 */
		public Compositor analysisMDAT() {
			AnalyserThreadManager.getInstance().startWork(new BaseRunnable<Boolean>() {
				@Override
				public Boolean runImp() throws Exception {
					mdatBoxTypeIndex = MP4Utils.findMDATBoxTypeIndex(analysisData, 0);

					int mdatBoxSize = MP4Utils.getBoxSize(analysisData, mdatBoxTypeIndex - 4);
					int sliceLength = ConvertUtils.bytes2Int(analysisData, mdatBoxTypeIndex + 4); // The slice length is continues to mdat box type

					/* make new video data */
					byte[] videoData = new byte[mdatBoxSize];
					System.arraycopy(analysisData, mdatBoxTypeIndex - 4, videoData, 0, mdatBoxSize);

					System.out.print("=============MDAT=============" + "\n"
							+ "video data size : " + mdatBoxSize + "\n"
							+ "slice length : " + sliceLength + "\n"
					);

					return true;
				}

				@Override
				public String getThreadName() {
					return "analysisMDAT";
				}
			});

			return this;
		}

		private boolean analysisMVHD() {
			mvhdBoxTypeIndex = MP4Utils.findMVHDBoxTypeIndex(analysisData, moovBoxTypeIndex + 4);
			if (mvhdBoxTypeIndex != -1) {
				int timeScale = ConvertUtils.bytes2Int(analysisData, mvhdBoxTypeIndex + 16);
				duration = ConvertUtils.bytes2Int(analysisData, mvhdBoxTypeIndex + 20);
				System.out.print("=============Movie=============" + "\n"
						+ "duration : " + duration + " ms" + "\n"
						+ "time scale : " + timeScale + " ms" + "\n"
				);

				return true;
			}

			return false;
		}

		private boolean analysisTRAK() {
			trakDataSetHashMap = new HashMap<>();
			trakList = new ArrayList<>();

			int offset = moovBoxTypeIndex + 4;
			while (true) {
				int trakBoxTypeIndex;
				if ((trakBoxTypeIndex = MP4Utils.findTRAKBoxTypeIndex(analysisData, offset)) != -1) {
					trakList.add(trakBoxTypeIndex);
					offset = trakBoxTypeIndex + 4;
				} else {
					break;
				}
			}

			System.out.print("==========Found " + trakList.size() + " Tracks==========" + "\n");

			int size = trakList.size();
			if (size == 0) {
				analysisListener.onTRAKNotFound();
				return false;
			} else {
				time2SampleTables = new SparseArray<>(size);
				syncFrameTables = new SparseArray<>(size);
				sample2ChunkTables = new SparseArray<>(size);
				for (int index = 0; index < size; index++) {
					System.out.print("=============Track" + index + "=============" + "\n");
					STBLDataSet stblDataSet = analysisHDLR(index);
					MDHDDataSet mdhdDataSet = analysisMDHD(index);

					// The hash map that saved trak info form file
					trakDataSetHashMap.put(index, new TRAKDataSet(mdhdDataSet, stblDataSet));

					// Time to sample table for trak #index
					time2SampleTables.put(index, generateTime2SampleTable(stblDataSet));

					// Sample to chunk table for trak #index
					sample2ChunkTables.put(index, generateSample2ChunkTable(stblDataSet));

					// Sync frame table for trak #index
					syncFrameTables.put(index, generateSyncFrameTimeTable(index, stblDataSet));
				}

				return true;
			}
		}

		private MDHDDataSet analysisMDHD(int index) {
			System.out.println("Media :");
			int mdhdBoxTypeIndex = MP4Utils.findMDHDIndex(analysisData, trakList.get(index) + 4);
			if (mdhdBoxTypeIndex != -1) {
				int timeScale = ConvertUtils.bytes2Int(
						analysisData,
						mdhdBoxTypeIndex + 16 /* type (4 bytes) + version (1 byte) + flags (3 bytes)
					+ creation time (4 bytes) + modification time (4 bytes) */
				);

				int duration = ConvertUtils.bytes2Int(
						analysisData,
						mdhdBoxTypeIndex + 20 /* type (4 bytes) + version (1 byte) + flags (3 bytes)
					+ creation time (4 bytes) + modification time (4 bytes) + time scale (4 bytes) */
				);

				System.out.println("  timescale : " + timeScale);
				System.out.println("   duration : " + duration);

				return new MDHDDataSet(timeScale, duration);
			}

			return null;
		}

		/**
		 * Layer 4 (root/moov/trak/media/) - hdlr
		 * Analysis hdlr box
		 *
		 * @param index
		 * @return
		 */
		private STBLDataSet analysisHDLR(int index) {
			int hdlrBoxTypeIndexs = MP4Utils.findHDLRIndex(analysisData, trakList.get(index) + 4);
			if (hdlrBoxTypeIndexs != -1) {
				String type = MP4Utils.getHandlerType(
						analysisData,
						hdlrBoxTypeIndexs + 12 /* type (4 bytes) + version (1 byte) + flags (3 bytes) + pre-defined (4 bytes) */
				);
				System.out.println("Type : " + type);

				return analysisSTBL(index, hdlrBoxTypeIndexs);
			}

			return null;
		}

		/**
		 * Layer 5 (root/moov/trak/media/hdlr) - stbl
		 * Analysis stbl
		 *
		 * @param index
		 * @param hdlrBoxTypeIndex
		 */
		private STBLDataSet analysisSTBL(int index, int hdlrBoxTypeIndex) {
			int stblBoxTypeIndex = MP4Utils.findSTBLIndex(analysisData, hdlrBoxTypeIndex + 4);
			if (stblBoxTypeIndex != -1) {
				analysisSTSD(stblBoxTypeIndex);
				List<Time2SampleDataSet> time2SampleList = analysisSTTS(stblBoxTypeIndex);
				List<Integer> syncFrameList = analysisSTSS(stblBoxTypeIndex);
				List<Sample2ChunkDataSet> sample2ChunkList = analysisSTSC(stblBoxTypeIndex);
				List<Integer> sampleSizeList = analysisSTSZ(stblBoxTypeIndex);
				List<Integer> chunkOffsetList = analysisSTCO(stblBoxTypeIndex);

				return new STBLDataSet(time2SampleList, syncFrameList, sample2ChunkList, sampleSizeList, chunkOffsetList);
			}

			return null;
		}

		private void analysisSTSD(int stblBoxTypeIndex) {
			int stsdBoxTypeIndex = MP4Utils.findSTSDIndex(analysisData, stblBoxTypeIndex + 4);
			if (stsdBoxTypeIndex != -1) {
				String codec = MP4Utils.getCodec(
						analysisData,
						stsdBoxTypeIndex + 16 /* type (4 bytes) +  version (1 byte) + flag (3 bytes) + counts (4 bytes) + description (4 bytes)*/
				);

				System.out.println("Codec : " + codec);
			}
		}

		private List<Time2SampleDataSet> analysisSTTS(int stblBoxTypeIndex) {
			int sttsBoxTypeIndex = MP4Utils.findSTTSIndex(analysisData, stblBoxTypeIndex + 4);
			if (sttsBoxTypeIndex != -1) {
				int timeToSampleCounts = ConvertUtils.bytes2Int(
						analysisData,
						sttsBoxTypeIndex + 8 /* type (4 bytes) + version (1 byte) + flag (3 bytes) */
				);

				return MP4Utils.getTime2SampleDataSet(
						analysisData,
						sttsBoxTypeIndex + 12 /* type (4 bytes) + version (1 byte) + flag (3 bytes) + chunk counts (4 bytes) */,
						timeToSampleCounts
				);
			}

			return null;
		}

		private List<Integer> analysisSTSS(int stblBoxTypeIndex) {
			int stssBoxTypeIndex = MP4Utils.findSTSSIndex(analysisData, stblBoxTypeIndex + 4);
			int stssBoxSize = MP4Utils.getBoxSize(analysisData, stssBoxTypeIndex - 4);
			if (stssBoxSize != -1) {
				return MP4Utils.getSyncFrameList(analysisData, stssBoxTypeIndex + 4, stssBoxSize - 8 /* skip size and type */);
			}

			return null;
		}

		private List<Sample2ChunkDataSet> analysisSTSC(int stblBoxTypeIndex) {
			int stscBoxTypeIndex = MP4Utils.findSTSCIndex(analysisData, stblBoxTypeIndex + 4);
			if (stscBoxTypeIndex != -1) {
				int sampleToChunkCounts = ConvertUtils.bytes2Int(
						analysisData,
						stscBoxTypeIndex + 8 /* type (4 bytes) + version (1 byte) + flag (3 bytes) */
				);

				return MP4Utils.getSample2ChunkList(
						analysisData,
						stscBoxTypeIndex + 12 /* type (4 bytes) + version (1 byte) + flag (3 bytes) + chunk counts (4 bytes) */,
						sampleToChunkCounts
				);
			}

			return null;
		}

		private List<Integer> analysisSTSZ(int stblBoxTypeIndex) {
			int stszBoxTypeIndex = MP4Utils.findSTSZIndex(analysisData, stblBoxTypeIndex + 4);
			if (stszBoxTypeIndex != -1) {
				int stszSmapleCounts = ConvertUtils.bytes2Int(
						analysisData,
						stszBoxTypeIndex + 12 /* type (4 bytes) + version (1 byte) + flag (3 bytes) + sample size (4 bytes) */
				);

				return MP4Utils.getSampleSizeList(analysisData, stszBoxTypeIndex + 16, stszSmapleCounts);
			}

			return null;
		}

		private List<Integer> analysisSTCO(int stblBoxTypeIndex) {
			int stcoBoxTypeIndex = MP4Utils.findSTCOIndex(analysisData, stblBoxTypeIndex + 4);
			if (stcoBoxTypeIndex != -1) {
				int chunkOffsetCounts = ConvertUtils.bytes2Int(
						analysisData,
						stcoBoxTypeIndex + 8 /* type (4 bytes) + version (1 byte) + flag (3 bytes) */
				);

				return MP4Utils.getChunkOffsetList(
						analysisData,
						stcoBoxTypeIndex + 12 /* type (4 bytes) + version (1 byte) + flag (3 bytes) + chunk counts (4 bytes) */,
						chunkOffsetCounts
				);
			}

			return null;
		}

		/**
		 * Gets data total duration (ms)
		 *
		 * @return
		 */
		public int getDuration() {
			return duration;
		}

		/**
		 * Generates sample duration table
		 *
		 * @param stblDataSet
		 */
		private List<Integer> generateTime2SampleTable(STBLDataSet stblDataSet) {
			List<Integer> time2SampleTable = new ArrayList<>();
			if (stblDataSet != null) {
				List<Time2SampleDataSet> time2SampleList = stblDataSet.getTime2SampleDataSets();
				int duration = 0;
				for (int i = 0; i < time2SampleList.size(); i++) {
					Time2SampleDataSet time2SampleDataSet = time2SampleList.get(i);
					for (int j = 0; j < time2SampleDataSet.getSampleCount(); j++) {
						duration += time2SampleDataSet.getSampleDurations();
						time2SampleTable.add(duration);
					}
				}
			}

			return time2SampleTable;
		}

		/**
		 * Print out the time to sample table for trak #index
		 *
		 * @param trakIndex
		 */
		public void printTime2SampleTable(int trakIndex) {
			System.out.println("=============Time to sample table=============");
			if (time2SampleTables != null) {
				List<Integer> time2SampleTable = time2SampleTables.get(trakIndex);
				if (time2SampleTable != null) {
					int startTime = 0;
					for (int index = 0; index < time2SampleTable.size(); index++) {
						System.out.printf("sample_%d, duration : %d - %d %n",
								index, startTime, startTime + time2SampleTable.get(index));
						startTime += time2SampleTable.get(index);
					}
				}
			}
		}

		/**
		 * Generates sync frame table, consist of time stamp and file offset.
		 *
		 * @param stblDataSet
		 */
		private List<SyncFrameDataSet> generateSyncFrameTimeTable(int trakIndex, STBLDataSet stblDataSet) {
			List<SyncFrameDataSet> syncFramTimeTable = new ArrayList<>();
			if (stblDataSet != null) {
				List<Integer> syncFrameList = stblDataSet.getSyncFrameDataSets();
				for (int i = 0; i < syncFrameList.size(); i++) {
					int sampleIndex = syncFrameList.get(i);
					float timeStamp = getTimeStamp(trakIndex, sampleIndex);
					int offset = getChunkOffset(trakIndex, getChunkIndex(trakIndex, sampleIndex)) + getSampleSize(trakIndex, sampleIndex);
					syncFramTimeTable.add(new SyncFrameDataSet(timeStamp, offset));
				}
			}

			return syncFramTimeTable;
		}

		/**
		 * Print out the sync frame time table for trak #index
		 *
		 * @param trakIndex
		 */
		public void printSyncFrameTimeTable(int trakIndex) {
			TRAKDataSet trakDataSet = trakDataSetHashMap.get(trakIndex);
			System.out.println("=============Sync frame time table=============");
			if (trakDataSet != null && syncFrameTables != null) {
				List<SyncFrameDataSet> syncFrameTable = syncFrameTables.get(trakIndex);
				if (syncFrameTable != null) {
					for (int index = 0; index < syncFrameTable.size(); index++) {
						SyncFrameDataSet syncFrameDataSet = syncFrameTable.get(index);
						System.out.printf("Time stamp : %.2f, Offset : %d %n",
								syncFrameDataSet.getTimeStamp() / trakDataSet.getMdhdDataSet().getTimeScale(), syncFrameDataSet.getOffset());
					}
				}
			}
		}

		/**
		 * Generates chunk table
		 *
		 * @param stblDataSet
		 */
		private List<Integer> generateSample2ChunkTable(STBLDataSet stblDataSet) {
			List<Integer> sample2ChunkTable = new ArrayList<>();
			if (stblDataSet != null) {
				List<Sample2ChunkDataSet> sample2ChunkDataSets = stblDataSet.getSample2ChunkDataSets();
				List<Integer> chunkOffsetDataSets = stblDataSet.getChunkOffsetDataSets();

				for (int i = 0; i < sample2ChunkDataSets.size(); i++) {
					int firstChunk = sample2ChunkDataSets.get(i).getFirstChunk();
					int samplePerChunk = sample2ChunkDataSets.get(i).getSamplesPerChunk();
					int descriptionIndex = sample2ChunkDataSets.get(i).getSampleDescriptionIndex();
					int endChunk = (i + 1 < sample2ChunkDataSets.size()) ?
							sample2ChunkDataSets.get(i + 1).getFirstChunk() : chunkOffsetDataSets.size();

					for (; firstChunk < endChunk; firstChunk++) {
						for (int j = 0; j < samplePerChunk; j++) {
							sample2ChunkTable.add(firstChunk);
						}
					}
				}
			}

			return sample2ChunkTable;
		}

		/**
		 *
		 * @param trakIndex
		 */
		public void printSample2ChunkTable(int trakIndex) {
			System.out.println("=============Sample to chunk table=============");
			if (sample2ChunkTables != null) {
				List<Integer> sample2ChunkTable = sample2ChunkTables.get(trakIndex);
				if (sample2ChunkTable != null) {
					for (int index = 0; index < sample2ChunkTable.size(); index++) {
						System.out.printf("sample_%d is at chunk_%d", index, sample2ChunkTable.get(index));
					}
				}
			}
		}

		/**
		 * Gets trak size
		 *
		 * @return
		 */
		public int getTRAKSize() {
			return trakList.size();
		}

		/**
		 * Gets chunk index of sample in trak
		 *
		 * @param trakIndex
		 * @param sampleIndex
		 * @return
		 */
		public int getChunkIndex(int trakIndex, int sampleIndex) {
			if (sample2ChunkTables != null) {
				List<Integer> sample2ChunkTable = sample2ChunkTables.get(trakIndex);
				if (sample2ChunkTable != null) {
					if (sampleIndex >= 0 && sampleIndex < sample2ChunkTable.size()) {
						return sample2ChunkTable.get(sampleIndex);
					}
				}
			}

			return -1;
		}

		/**
		 * Gets chunk offset in current trak
		 *
		 * @param trakIndex
		 * @param chunkIndex
		 * @return
		 */
		public int getChunkOffset(int trakIndex, int chunkIndex) {
			TRAKDataSet trakDataSet = trakDataSetHashMap.get(trakIndex);
			if (trakDataSet != null) {
				List<Integer> chunkOffsetList = trakDataSet.getStblDataSet().getChunkOffsetDataSets();
				if (chunkIndex >= 0 && chunkIndex < chunkOffsetList.size()) {
					return chunkOffsetList.get(chunkIndex);
				}
			}

			return 0;
		}

		/**
		 * Gets sample index of time stamp in current trak
		 *
		 * @param trakIndex
		 * @param time
		 * @return
		 */
		public int getSampleIndex(int trakIndex, float time) {
			TRAKDataSet trakDataSet = trakDataSetHashMap.get(trakIndex);
			if (time2SampleTables != null && trakDataSet != null) {
				List<Integer> time2SampleTable = time2SampleTables.get(trakIndex);
				if (time2SampleTable != null) {
					for (int index = 0; index < time2SampleTable.size(); index++) {
						if (time2SampleTable.get(index) >= time * trakDataSet.getMdhdDataSet().getTimeScale()) {
							return index;
						}
					}
				}
			}

			return -1;
		}

		/**
		 * Gets size of sample in current trak
		 *
		 * @param trakIndex
		 * @param sampleIndex
		 * @return
		 */
		public int getSampleSize(int trakIndex, int sampleIndex) {
			TRAKDataSet trakDataSet = trakDataSetHashMap.get(trakIndex);
			if (trakDataSet != null) {
				List<Integer> sampleSizeList = trakDataSet.getStblDataSet().getSampleSizeDataSets();
				if (sampleIndex < sampleSizeList.size()) {
					return sampleSizeList.get(sampleIndex);
				}
			}

			return 0;
		}

		/**
		 * Gets time stamp of sample in current trak
		 *
		 * @param trakIndex
		 * @param sampleIndex
		 * @return
		 */
		public float getTimeStamp(int trakIndex, int sampleIndex) {
			if (time2SampleTables != null) {
				List<Integer> time2SampleTable = time2SampleTables.get(trakIndex);
				if (time2SampleTable != null) {
					if (sampleIndex >= 0 && sampleIndex < time2SampleTable.size()) {
						return time2SampleTable.get(sampleIndex);
					}
				}
			}

			return -1;
		}
	}
}
