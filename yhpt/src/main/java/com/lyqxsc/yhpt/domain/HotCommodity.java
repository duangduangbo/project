package com.lyqxsc.yhpt.domain;

import java.util.List;

public class HotCommodity {
	String[] pic;
	List<Commodity> commodity;

	public String[] getPic() {
		return pic;
	}
	public void setPic(String[] pic) {
		this.pic = pic;
	}
	public List<Commodity> getCommodity() {
		return commodity;
	}
	public void setCommodity(List<Commodity> commodity) {
		this.commodity = commodity;
	}
}
