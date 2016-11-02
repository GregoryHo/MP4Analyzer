package greg.ns.com.analyser;

import java.util.ArrayList;
import java.util.List;

import greg.ns.com.analyser.data.Sample2ChunkDataSet;
import greg.ns.com.analyser.data.Time2SampleDataSet;


/**
 * Created by Gregory on 2016/9/29.
 */
public class MP4Utils {

	private MP4Utils() {

	}

	/**
	 * Gets mp4 box size
	 *
	 * @param bytes    data
	 * @param boxIndex the box type start index
	 * @return -1 when boxIndex is < 0
	 *          1 using 64 bits box size (not handling this issue yet)
	 *          > 1 normal case.
	 */
	public static int getBoxSize(byte[] bytes, int boxIndex) {
		if (boxIndex < 0) {
			return -1;
		}

		int size = 0;
		size += (bytes[boxIndex] & 0xFF) << 24;
		size += (bytes[boxIndex + 1] & 0xFF) << 16;
		size += (bytes[boxIndex + 2] & 0xFF) << 8;
		size += bytes[boxIndex + 3] & 0xFF;

		return size;
	}

	/**
	 * Return true if the 4 bytes is a specific header (According to the char c1 ~ c4)
	 *
	 * @param bytes the analysis data
	 * @param offset the starting index
	 * @param c1 character one
	 * @param c2 character two
	 * @param c3 character three
	 * @param c4 character four
	 * @return
	 */
	private static boolean isHeader(byte[] bytes, int offset, char c1, char c2, char c3, char c4) {
		return bytes[offset] == c1 && bytes[offset + 1] == c2
				&& bytes[offset + 2] == c3 && bytes[offset + 3] == c4;
	}

	/***********************************************************************
	 * root/ftyp
	 ***********************************************************************/

	/**
	 * Gets ftyp box type starting index
	 *
	 * @param bytes the analysis data
	 * @param offset the starting index
	 * @return
	 */
	public static int findFTYPBoxTypeIndex(byte[] bytes, int offset) {
		int length = bytes.length;
		for (int index = offset; index < length; index++) {
			if (isHeader(bytes, index, 'f', 't', 'y', 'p')) {
				return index;
			}
		}

		return -1;
	}

	/***********************************************************************
	 * root/mdat
	 ***********************************************************************/

	/**
	 * Gets mdat box type starting index
	 *
	 * @param bytes the analysis data
	 * @param offset the starting index
	 * @return
	 */
	public static int findMDATBoxTypeIndex(byte[] bytes, int offset) {
		int length = bytes.length;
		for (int index = offset; index < length; index++) {
			if (isHeader(bytes, index, 'm', 'd', 'a', 't')) {
				return index;
			}
		}

		return -1;
	}

	/***********************************************************************
	 * root/moov
	 ***********************************************************************/

	/**
	 * Gets moov box type starting index
	 *
	 * @param bytes the analysis data
	 * @param offset the starting index
	 * @return
	 */
	public static int findMOOVBoxTypeIndex(byte[] bytes, int offset) {
		int length = bytes.length;
		for (int index = offset; index < length; index++) {
			if (isHeader(bytes, index, 'm', 'o', 'o', 'v')) {
				return index;
			}
		}

		return -1;
	}

	/***********************************************************************
	 * root/moov/mvhd
	 ***********************************************************************/

	/**
	 * Gets mvhd box type starting index
	 *
	 * @param bytes the analysis data
	 * @param offset the starting index
	 * @return
	 */
	public static int findMVHDBoxTypeIndex(byte[] bytes, int offset) {
		int length = bytes.length;
		for (int index = offset; index < length; index++) {
			if (isHeader(bytes, index, 'm', 'v', 'h', 'd')) {
				return index;
			}
		}

		return -1;
	}

	/***********************************************************************
	 * root/moov/trak
	 ***********************************************************************/

	/**
	 * Gets trak box type starting index
	 *
	 * @param bytes the analysis data
	 * @param offset the starting index
	 * @return
	 */
	public static int findTRAKBoxTypeIndex(byte[] bytes, int offset) {
		int length = bytes.length;
		for (int index = offset; index < length; index++) {
			if (isHeader(bytes, index, 't', 'r', 'a', 'k')) {
				return index;
			}
		}

		return -1;
	}

	/***********************************************************************
	 * root/moov/trak/media/mdhd
	 ***********************************************************************/

	/**
	 * Gets mdhd box type starting index
	 *
	 * @param bytes the analysis data
	 * @param offset the starting index
	 * @return
	 */
	public static int findMDHDIndex(byte[] bytes, int offset) {
		int length = bytes.length;
		for (int index = offset; index < length; index++) {
			if (isHeader(bytes, index, 'm', 'd', 'h', 'd')) {
				return index;
			}
		}

		return -1;
	}

	/***********************************************************************
	 * root/moov/trak/media/hdlr
	 ***********************************************************************/

	/**
	 * Gets hdlr box type starting index
	 *
	 * @param bytes the analysis data
	 * @param offset the starting index
	 * @return
	 */
	public static int findHDLRIndex(byte[] bytes, int offset) {
		int length = bytes.length;
		for (int index = offset; index < length; index++) {
			if (isHeader(bytes, index, 'h', 'd', 'l', 'r')) {
				return index;
			}
		}

		return -1;
	}

	/**
	 * Gets the handler type in hdlr
	 *
	 * @param bytes the analysis data
	 * @param offset the starting index
	 * @return
	 */
	public static String getHandlerType(byte[] bytes, int offset) {
		if (isVideoHandler(bytes, offset)) {
			return "Video";
		}

		if (isAudioHandler(bytes, offset)) {
			return "Audio";
		}

		return "Hint";
	}

	/**
	 * Check if the 4 bytes data as ASCII code is 'vide'
	 *
	 * @param bytes the analysis data
	 * @param index the starting index
	 * @return
	 */
	public static boolean isVideoHandler(byte[] bytes, int index) {
		return bytes[index] == 'v' && bytes[index + 1] == 'i'
				&& bytes[index + 2] == 'd' && bytes[index + 3] == 'e';
	}

	/**
	 * Check if the 4 bytes data as ASCII code is 'soun'
	 *
	 * @param bytes the analysis data
	 * @param index the starting index
	 * @return
	 */
	public static boolean isAudioHandler(byte[] bytes, int index) {
		return bytes[index] == 's' && bytes[index + 1] == 'o'
				&& bytes[index + 2] == 'u' && bytes[index + 3] == 'n';
	}

	/***********************************************************************
	 * root/moov/trak/media/minf/stbl
	 ***********************************************************************/

	/**
	 * Gets stbl box type starting index
	 *
	 * @param bytes the analysis data
	 * @param offset the starting offset
	 * @return
	 */
	public static int findSTBLIndex(byte[] bytes, int offset) {
		int length = bytes.length;
		for (int index = offset; index < length; index++) {
			if (isHeader(bytes, index, 's', 't', 'b', 'l')) {
				return index;
			}
		}

		return -1;
	}

	/***********************************************************************
	 * root/moov/trak/media/minf/stbl/
	 ***********************************************************************/

	/**
	 * Gets stsd box type starting index
	 *
	 * @param bytes the analysis data
	 * @param offset the starting index
	 * @return
	 */
	public static int findSTSDIndex(byte[] bytes, int offset) {
		int length = bytes.length;
		for (int index = offset; index < length; index++) {
			if (isHeader(bytes, index, 's', 't', 's', 'd')) {
				return index;
			}
		}

		return -1;
	}

	/**
	 * Get trak codec in stsd box, only deal with special case.
	 *
	 * @param bytes
	 * @param offset
	 * @return avc1 or mp4a, otherwise "unknown"
	 */
	public static String getCodec(byte[] bytes, int offset) {
		if (isAVC1(bytes, offset)) {
			return "AVC1 (H.264)";
		}

		if (isMP4a(bytes, offset)) {
			return "MP4a (MPEG-4 Audio)";
		}

		return "Unknown";
	}

	private static boolean isAVC1(byte[] bytes, int offset) {
		return bytes[offset] == 'a' && bytes[offset + 1] == 'v'
				&& bytes[offset + 2] == 'c' && bytes[offset + 3] == '1';

	}

	private static boolean isMP4a(byte[] bytes, int offset) {
		return bytes[offset] == 'm' && bytes[offset + 1] == 'p'
				&& bytes[offset + 2] == '4' && bytes[offset + 3] == 'a';
	}

	public static int findSTTSIndex(byte[] bytes, int offset) {
		int length = bytes.length;
		for (int index = offset; index < length; index++) {
			if (isHeader(bytes, index, 's', 't', 't', 's')) {
				return index;
			}
		}

		return -1;
	}

	public static List<Time2SampleDataSet> getTime2SampleDataSet(byte[] bytes, int offset, int counts) {
		List<Time2SampleDataSet> list = new ArrayList<>();
		for (int i = 0; i < counts; i++) {
			int index = offset + (i * 8);
			int sampleCount = ConvertUtils.bytes2Int(bytes, index);
			int sampleDuration = ConvertUtils.bytes2Int(bytes, index + 4);
			list.add(new Time2SampleDataSet(sampleCount, sampleDuration));
		}

		return list;
	}

	public static int findSTSSIndex(byte[] bytes, int offset) {
		int length = bytes.length;
		for (int index = offset; index < length; index++) {
			if (isHeader(bytes, index, 's', 't', 's', 's')) {
				return index;
			}
		}

		return -1;
	}

	public static List<Integer> getSyncFrameList(byte[] stblData, int offset, int counts) {
		List<Integer> frames = new ArrayList<>();
		int length = offset + counts;
		for (int index = offset; index < length; index += 4) {
			int timeStamp = ConvertUtils.bytes2Int(stblData, index);
			frames.add(timeStamp);
		}

		return frames;
	}

	public static int findSTSCIndex(byte[] bytes, int offset) {
		int length = bytes.length;
		for (int index = offset; index < length; index++) {
			if (isHeader(bytes, index, 's', 't', 's', 'c')) {
				return index;
			}
		}

		return -1;
	}

	public static List<Sample2ChunkDataSet> getSample2ChunkList(byte[] bytes, int offset, int counts) {
		List<Sample2ChunkDataSet> list = new ArrayList<>();
		for (int i = 0; i < counts; i++) {
			int index = offset + (i * 12);
			int firstChunk = ConvertUtils.bytes2Int(bytes, index) - 1; /* decrease 1 for start at zero index */
			int samplesPerChunk = ConvertUtils.bytes2Int(bytes, index + 4);
			int sampleDescriptionID = ConvertUtils.bytes2Int(bytes, index + 8);
			list.add(new Sample2ChunkDataSet(firstChunk, samplesPerChunk, sampleDescriptionID));
		}

		return list;
	}

	public static int findSTSZIndex(byte[] bytes, int offset) {
		int length = bytes.length;
		for (int index = offset; index < length; index++) {
			if (isHeader(bytes, index, 's', 't', 's', 'z')) {
				return index;
			}
		}

		return -1;
	}

	public static List<Integer> getSampleSizeList(byte[] bytes, int offset, int counts) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < counts; i++) {
			int index = offset + (i * 4);
			int size = ConvertUtils.bytes2Int(bytes, index);
			list.add(size);
		}

		return list;
	}

	public static int findSTCOIndex(byte[] bytes, int offset) {
		int length = bytes.length;
		for (int index = offset; index < length; index++) {
			if (isHeader(bytes, index, 's', 't', 'c', 'o')) {
				return index;
			}
		}

		return -1;
	}

	public static List<Integer> getChunkOffsetList(byte[] bytes, int offset, int counts) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < counts; i++) {
			int index = offset + (i * 4);
			int size = ConvertUtils.bytes2Int(bytes, index);
			list.add(size);
		}

		return list;
	}
}
