package greg.ns.com.analyser.data;

/**
 * Created by Gregory on 2016/10/4.
 */
public class TRAKDataSet {

	private MDHDDataSet mdhdDataSet;

	private STBLDataSet stblDataSet;

	public TRAKDataSet(MDHDDataSet mdhdDataSet, STBLDataSet stblDataSet) {
		this.mdhdDataSet = mdhdDataSet;
		this.stblDataSet = stblDataSet;
	}

	public MDHDDataSet getMdhdDataSet() {
		return mdhdDataSet;
	}

	public STBLDataSet getStblDataSet() {
		return stblDataSet;
	}
}
