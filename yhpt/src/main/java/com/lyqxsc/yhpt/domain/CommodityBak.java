package com.lyqxsc.yhpt.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CommodityBak {
	//ID
	long id;
	//名称
	String name;
	//图片地址
	String picurl;
	//价格
	float price;
	//价格1
	@JsonIgnore
	float price1;
	//价格2
	@JsonIgnore
	float price2;
	//价格3
	@JsonIgnore
	float price3;
	//价格4
	@JsonIgnore
	float price4;
	//价格5
	@JsonIgnore
	float price5;
	//价格6
	@JsonIgnore
	float price6;
	//分类 1:租赁  2:出售  3:租赁+出售
	@JsonIgnore
	String type;
	//库存
	int inventory;
	//销量
	int sales;
	//订单数量日
	int ordernumDay;
	//订单数量月
	int ordernumMouth;
	//押金
	float deposit;
	//商品描述
	String note;
	//分销商
	@JsonIgnore
	long distributor;
	//种类
	@JsonIgnore
	int classId;
	//种类
	String classStr;
	//是否上架  1 上架  0 下架
	int online;
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
	public float getPrice1() {
		return price1;
	}
	public void setPrice1(float price1) {
		this.price1 = price1;
	}
	public float getPrice2() {
		return price2;
	}
	public void setPrice2(float price2) {
		this.price2 = price2;
	}
	public float getPrice3() {
		return price3;
	}
	public void setPrice3(float price3) {
		this.price3 = price3;
	}
	public float getPrice4() {
		return price4;
	}
	public void setPrice4(float price4) {
		this.price4 = price4;
	}
	public float getPrice5() {
		return price5;
	}
	public void setPrice5(float price5) {
		this.price5 = price5;
	}
	public float getPrice6() {
		return price6;
	}
	public void setPrice6(float price6) {
		this.price6 = price6;
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
	public int getSales() {
		return sales;
	}
	public void setSales(int sales) {
		this.sales = sales;
	}
	public int getOrdernumDay() {
		return ordernumDay;
	}
	public void setOrdernumDay(int ordernumDay) {
		this.ordernumDay = ordernumDay;
	}
	public int getOrdernumMouth() {
		return ordernumMouth;
	}
	public void setOrdernumMouth(int ordernumMouth) {
		this.ordernumMouth = ordernumMouth;
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
	public long getDistributor() {
		return distributor;
	}
	public void setDistributor(long distributor) {
		this.distributor = distributor;
	}
	public int getClassId() {
		return classId;
	}
	public void setClassId(int classId) {
		this.classId = classId;
	}
	public String getClassStr() {
		return classStr;
	}
	public void setClassStr(String classStr) {
		this.classStr = classStr;
	}
	public int getOnline() {
		return online;
	}
	public void setOnline(int online) {
		this.online = online;
	}
}