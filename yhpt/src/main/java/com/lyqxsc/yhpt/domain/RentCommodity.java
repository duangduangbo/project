package com.lyqxsc.yhpt.domain;

/**
 * 租赁商品实体类
 */
public class RentCommodity {
	//ID
	long id;
	//名称
	String name;
	//图片地址
	String picurl;
	//价格
	float price;
	//分类 1:租赁  2:出售  3:租赁+出售
	String type;
	//库存
	int inventory;
	//订单数量
	int ordernum;
	//押金
	float deposit;
	//商品描述
	String note;
	//分销商
	long distributor;
	//种类
	int classify;
	
	public long getDistributor() {
		return distributor;
	}
	public void setDistributor(long distributor) {
		this.distributor = distributor;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPicurl() {
		return picurl;
	}
	public void setPicurl(String picurl) {
		this.picurl = picurl;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getInventory() {
		return inventory;
	}
	public void setInventory(int inventory) {
		this.inventory = inventory;
	}
	public int getOrdernum() {
		return ordernum;
	}
	public void setOrdernum(int ordernum) {
		this.ordernum = ordernum;
	}
	public float getDeposit() {
		return deposit;
	}
	public void setDeposit(float deposit) {
		this.deposit = deposit;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public int getClassify() {
		return classify;
	}
	public void setClassify(int classify) {
		this.classify = classify;
	}
}
