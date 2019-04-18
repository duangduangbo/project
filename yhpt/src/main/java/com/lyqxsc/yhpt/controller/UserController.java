package com.lyqxsc.yhpt.controller;

import java.util.List;

import javax.servlet.ServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lyqxsc.yhpt.domain.User;
import com.lyqxsc.yhpt.domain.Appraise;
import com.lyqxsc.yhpt.domain.Collect;
import com.lyqxsc.yhpt.domain.Commodity;
import com.lyqxsc.yhpt.domain.CommodityClassify;
import com.lyqxsc.yhpt.domain.HomePage;
import com.lyqxsc.yhpt.domain.HotCommodity;
import com.lyqxsc.yhpt.domain.NewCommodity;
import com.lyqxsc.yhpt.domain.Order;
import com.lyqxsc.yhpt.domain.RentCommodity;
import com.lyqxsc.yhpt.domain.RentOrder;
import com.lyqxsc.yhpt.domain.ShopCar;
import com.lyqxsc.yhpt.service.UserService;
import com.lyqxsc.yhpt.urlclass.AppraiseInfo;
import com.lyqxsc.yhpt.urlclass.CollectInfo;
import com.lyqxsc.yhpt.urlclass.ShopCarInfo;
import com.lyqxsc.yhpt.urlclass.BuyCommodity;
import com.lyqxsc.yhpt.urlclass.UserToken;
import com.lyqxsc.yhpt.urlclass.PresentOrder;
import com.lyqxsc.yhpt.urlclass.PresentRentOrder;
import com.lyqxsc.yhpt.urlclass.UserLogin;
import com.lyqxsc.yhpt.urlclass.UserTokenOne;
import com.lyqxsc.yhpt.utils.RetJson;

@RestController
@CrossOrigin
public class UserController {
	@Autowired
	UserService userService;
	
	/**
	 *  登录
	 * @param param
	 * @return
	 */
//	@RequestMapping(value = "/userlogin", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//	public RetJson login(@RequestBody UserLogin param) {
//		String openID = param.getParameter("openID");
//		String ip = param.getParameter("ip");
//		String openID = param.getOpenID();
//		String ip = "0.0.0.0";
//		
//		if((openID == null)||(ip == null)) {
//			return RetJson.urlError("login error", null);
//		}
//		
//		User user = userService.login(openID,ip);
//		
//		if(user == null) {
//			return RetJson.urlError("login error", null);
//		}
//		return RetJson.success("success",user);
//		return RetJson.success("success");
//	}
	
	
	/**
	 *  注销
	 * @param param
	 * @return
	 */
	@RequestMapping(value = "/userlogout",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson logout(@RequestBody UserToken param) {
		
		String userToken = param.getUserToken();
		if(userToken == null) {
			return RetJson.urlError("logout error, please give me userToken", null);
		}
		
		if(!userService.logout(userToken)) {
			return RetJson.urlError("logout error", null);
		}
		
		return RetJson.success("logout success",null);
	}
	
	
	/**
	 *  注册
	 * @param param
	 * @return
	 */
//	@RequestMapping(value = "/usersigup",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//	public RetJson signup(User param) {
//		if(param == null) {
//			RetJson.urlError("sigup error", null);
//		}
//		
//		int ret = userService.signup(param);
//		if(ret == -1) {
//			return RetJson.urlError("sigup error, user already exists ", null);
//		}
//		else if(ret == -2) {
//			return RetJson.mysqlError("sigup error", null);
//		}
//		else {
//			return RetJson.success("sigup seccess");
//		}
//	}
	
	/**
	 * 填写邀请码
	 */
	@RequestMapping(value = "/addinvitationcode", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson addInvitationCode(@RequestBody UserTokenOne param) {
		String userToken = param.getUserToken();
		String code = param.getString();
		if(userToken == null || code == null) {
			return RetJson.urlError("invitation code error", null);
		}
		if(userService.addInvitationCode(userToken,code)) {
			return RetJson.success("success");
		}
		return RetJson.unknowError("invitation code error", null);
	}
	
	/**
	 * 首页
	 * @return
	 */
	@RequestMapping(value = "/home", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson homePage(@RequestBody UserToken param) {
		String userToken = param.getUserToken();
		if(userToken == null) {
			return RetJson.urlError("no userToken", null);
		}
		
		HomePage homePage = userService.homePage(userToken);
		if(homePage == null) {
			return RetJson.unknowError("用户不在线", null);
		}
		return RetJson.success("success", homePage);
	}
	
	/**
	 * 关于我们
	 */
	@RequestMapping(value = "/home/aboutus", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson aboutUs(@RequestBody UserToken param) {
		String userToken = param.getUserToken();
		if(userToken == null) {
			return RetJson.urlError("no userToken", null);
		}
		
		String msg = userService.aboutUs(userToken);
		if(msg == null) {
			return RetJson.unknowError("用户不在线", null);
		}
		return RetJson.success("success", msg);
	}
	
	/**
	 * 商城
	 * @return
	 */
	//TODO 暂时用首页界面
	@RequestMapping(value = "/shop", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson shop(@RequestBody UserToken param) {
		String userToken = param.getUserToken();
		if(userToken == null) {
			return RetJson.urlError("no userToken", null);
		}
		
		HomePage homePage = userService.shop(userToken);
		if(homePage == null) {
			return RetJson.unknowError("用户不在线", null);
		}
		
		return RetJson.success("success", homePage);
	}
	
	/**
	 * 查询分类列表
	 */
	@RequestMapping(value = "/shop/classlist", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson classList(@RequestBody UserTokenOne param) {
		String userToken = param.getUserToken();
		String type = param.getString();
		if(userToken == null || type == null) {
			return RetJson.urlError("no userToken", null);
		}
		
		List<CommodityClassify> list = userService.classList(userToken,Integer.parseInt(type));
		if(list == null) {
			return RetJson.unknowError("class list error", null);
		}
		
		return RetJson.success("success", list);
	}
	
	/**
	 * 分类查询物品
	 */
	@RequestMapping(value = "/shop/classlist/select", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson selectCommodityByClass(@RequestBody UserTokenOne param) {
		String userToken = param.getUserToken();
		String classId = param.getString();
		if(userToken == null || classId == null) {
			return RetJson.urlError("no userToken", null);
		}
		
		List<Commodity> list = userService.selectCommodityByClass(userToken,Integer.parseInt(classId));
		if(list == null) {
			return RetJson.unknowError("class list error", null);
		}
		
		return RetJson.success("success", list);
	}
	
	/**
	 * 查询物品名称
	 */
	@RequestMapping(value = "/shop/classlist/selectByName", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson selectCommodityByName(@RequestBody UserTokenOne param) {
		String userToken = param.getUserToken();
		String name = param.getString();
		if(userToken == null || name == null) {
			return RetJson.urlError("no userToken", null);
		}
		
		List<Commodity> list = userService.selectCommodityByName(userToken,name);
		if(list == null) {
			return RetJson.unknowError("class list error", null);
		}
		
		return RetJson.success("success", list);
	}
	
	
	/**
	 * 新品  展示出售商品 最新10条
	 */
	@RequestMapping(value = "/shop/newshop", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson newShopShow(@RequestBody UserToken param) {
		String userToken = param.getUserToken();
		if(userToken == null) {
			return RetJson.urlError("no userToken", null);
		}
		
		NewCommodity commodity = userService.newShopShow(userToken);
		if(commodity == null) {
			return RetJson.unknowError("用户不在线", null);
		}
		
		return RetJson.success("success", commodity);
	}
	
	/**
	 * 热卖  最出售最多10条
	 */
	@RequestMapping(value = "/shop/hotshop", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson hotShopShow(@RequestBody UserToken param) {
		String userToken = param.getUserToken();
		if(userToken == null) {
			return RetJson.urlError("no userToken", null);
		}
		
		HotCommodity commodity = userService.hotShopShow(userToken);
		if(commodity == null) {
			return RetJson.unknowError("用户不在线", null);
		}
		
		return RetJson.success("success", commodity);
	}
	
	/**
	 *  查看可出售商品
	 */
	@RequestMapping(value = "/shop/selectcommodity", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson selectCommodity(@RequestBody UserToken param) {
		String userToken = param.getUserToken();
		if(userToken == null) {
			return RetJson.urlError("logout error, please give me userToken", null);
		}
		List<Commodity> commodityList = userService.selectCommodity(userToken);
		if(commodityList == null) {
			return RetJson.unknowError("用户不在线", null);
		}
		return RetJson.success("success", commodityList);
	}
	
	/**
	 *  查看可租赁商品
	 */
	@RequestMapping(value = "/shop/selectrentcommodity", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson selectRentCommodity(@RequestBody UserToken param) {
		String userToken = param.getUserToken();
		if(userToken == null) {
			return RetJson.urlError("logout error, please give me userToken", null);
		}
		List<Commodity> rentCommodityList = userService.selectRentCommodity(userToken);
		if(rentCommodityList == null) {
			return RetJson.unknowError("用户不在线", null);
		}
		return RetJson.success("success", rentCommodityList);
	}
	
	/**
	 * 根据商品id查询商品
	 */
	@RequestMapping(value = "/shop/selectcommoditybyid", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson selectCommodityByID(@RequestBody UserTokenOne param) {
		String userToken = param.getUserToken();
		long id = Long.parseLong(param.getString());
		
		if(userToken == null || id == 0) {
			return RetJson.urlError("logout error, please give me userToken", null);
		}
		Commodity commodity = userService.selectCommodityByID(userToken,id);
		if(commodity == null) {
			return RetJson.unknowError("没有该商品", null);
		}
		return RetJson.success("success", commodity);
	}
	
	/**
	 * 购买
	 */
	@RequestMapping(value = "/shop/buycommodity", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson buyCommodity(@RequestBody BuyCommodity param) {
		String userToken = param.getUserToken();
		long commodityID = param.getCommodityID();
		int count = param.getCount();
		String ip = param.getIp();
		if((userToken == null) || (commodityID == 0) || count == 0) {
			return RetJson.urlError("buy commodity error", null);
		}
		Order order = userService.makeCommodityOrder(userToken,commodityID,count,ip);
		if(order == null) {
			return RetJson.unknowError("buy commodity error", null);
		}
		return RetJson.success("success", order);
	}
	
	/**
	 *  提交购买订单
	 */
	@RequestMapping(value = "/shop/pushorder", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson presentOrder(@RequestBody PresentOrder param) {
		String userToken = param.getUserToken();
		String addr = param.getAddr();
		Order order = param.getOrder();
		if(userToken == null || order == null || addr == null) {
			return RetJson.urlError("push order error", null);
		}
		if(!userService.presentOrder(userToken,order,addr)) {
			return RetJson.urlError("push order error", null);
		}
		return RetJson.success("success");
	}
	
	/**
	 * 租赁
	 */
	@RequestMapping(value = "/shop/rentcommodity", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson rentCommodity(@RequestBody BuyCommodity param) {
		String userToken = param.getUserToken();
		long rentCommodityID = param.getCommodityID();
		int count = param.getCount();
		String ip = param.getIp();
		if((userToken == null) || (rentCommodityID == 0) || (count == 0)) {
			return RetJson.urlError("buy commodity error", null);
		}
		RentOrder rentOrder = userService.makeRentCommodityOrder(userToken,rentCommodityID,count,ip);
		if(rentOrder == null) {
			return RetJson.unknowError("用户不在线", null);
		}
		return RetJson.success("success", rentOrder);
	}
	
	/**
	 *  提交租赁订单
	 */
	@RequestMapping(value = "/shop/pushrentorder", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson presentRentOrder(@RequestBody PresentRentOrder param) {
		String userToken = param.getUserToken();
		String addr = param.getAddr();
		RentOrder rentOrder = param.getRentOrder();
		if(userToken == null || rentOrder == null || addr == null) {
			return RetJson.urlError("push order error", null);
		}
		if(!userService.presentRentOrder(userToken,rentOrder,addr)) {
			return RetJson.urlError("push order error", null);
		}
		return RetJson.success("success");
	}
	
	
	/**
	 * 我的全部订单
	 */
	@RequestMapping(value = "/usercenter/order", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson  allOrder(@RequestBody UserToken param) {
		String userToken = param.getUserToken();
		if(userToken == null) {
			return RetJson.urlError("present order error", null);
		}
		List<Order> orderList = userService.getAllOrder(userToken);
		if(orderList == null) {
			return RetJson.unknowError("false", null);
		}
		return RetJson.success("success", orderList);
	}
	
	/**
	 * 待付款订单列表
	 */
	@RequestMapping(value = "/usercenter/order/nopay", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson  noPayOrder(@RequestBody UserToken param) {
		String userToken = param.getUserToken();
		if(userToken == null) {
			return RetJson.urlError("present order error", null);
		}
		List<Order> orderList = userService.getTypeOrder(userToken, 0);
		if(orderList == null) {
			return RetJson.unknowError("false", null);
		}
		return RetJson.success("success", orderList);
	}
	
	/**
	 * 已付款订单列表
	 */
	@RequestMapping(value = "/usercenter/order/pay", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson  isPayOrder(@RequestBody UserToken param) {
		String userToken = param.getUserToken();
		if(userToken == null) {
			return RetJson.urlError("present order error", null);
		}
		List<Order> orderList = userService.getTypeOrder(userToken, 1);
		if(orderList == null) {
			return RetJson.unknowError("false", null);
		}
		return RetJson.success("success", orderList);
	}
	
	
	
	/**
	 * 购物车
	 */
	@RequestMapping(value = "/usercenter/shopcar", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson shopCar(@RequestBody UserToken param) {
		String userToken = param.getUserToken();
		if(userToken == null) {
			return RetJson.urlError("list shopcar error", null);
		}
		List<ShopCar> shopList = userService.getShopCar(userToken);
		if(shopList == null) {
			return RetJson.unknowError("false", null);
		}
		return RetJson.success("success", shopList);
	}
	
	/**
	 * 购物车 增
	 */
	@RequestMapping(value = "/usercenter/shopcar/add", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson addShopCar(@RequestBody ShopCarInfo param) {
		String userToken = param.getUserToken();
		ShopCar shopCar = param.getShopCar();
		if(userToken == null || shopCar == null) {
			return RetJson.urlError("add shopcar error", null);
		}
		if(userService.addShopCar(userToken, shopCar)) {
			return RetJson.success("success");
		}
		return RetJson.unknowError("false", null);
	}
	
	/**
	 * 购物车 删
	 */
	@RequestMapping(value = "/usercenter/shopcar/remove", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson removeShopCar(@RequestBody UserTokenOne param) {
		String userToken = param.getUserToken();
		long id = Long.parseLong(param.getString());
		if(userToken == null || id == 0) {
			return RetJson.urlError("remove shopcar error", null);
		}
		if(userService.removeShopCar(userToken, id)) {
			return RetJson.success("success");
		}
		return RetJson.unknowError("false", null);
	}
	
	/**
	 * 购物车 改
	 */
	@RequestMapping(value = "/usercenter/shopcar/update", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson updateShopCar(@RequestBody ShopCarInfo param) {
		String userToken = param.getUserToken();
		ShopCar shopCar = param.getShopCar();
		if(userToken == null || shopCar == null) {
			return RetJson.urlError("update shopcar error", null);
		}
		if(userService.updateShopCar(userToken, shopCar)) {
			return RetJson.success("success");
		}
		return RetJson.unknowError("false", null);
	}
	
	/**
	 * 收藏夹列表
	 */
	@RequestMapping(value = "/usercenter/collect", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson listCollect(@RequestBody UserToken param) {
		String userToken = param.getUserToken();
		if(userToken == null) {
			return RetJson.urlError("list collect error", null);
		}
		List<Collect> shopList = userService.getCollect(userToken);
		if(shopList == null) {
			return RetJson.unknowError("false", null);
		}
		return RetJson.success("success", shopList);
	}
	
	/**
	 * 收藏夹 增
	 */
	@RequestMapping(value = "/usercenter/collect/add", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson addCollect(@RequestBody CollectInfo param) {
		String userToken = param.getUserToken();
		Collect collect = param.getCollect();
		if(userToken == null || collect == null) {
			return RetJson.urlError("add collect error", null);
		}
		if(userService.addCollect(userToken, collect)) {
			return RetJson.success("success");
		}
		return RetJson.unknowError("false", null);
	}
	
	/**
	 * 收藏夹 删
	 */
	@RequestMapping(value = "/usercenter/collect/remove", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson removeCollect(@RequestBody UserTokenOne param) {
		String userToken = param.getUserToken();
		long id = Long.parseLong(param.getString());
		if(userToken == null || id == 0) {
			return RetJson.urlError("remove collect error", null);
		}
		if(userService.removeCollect(userToken, id)) {
			return RetJson.success("success");
		}
		
		return RetJson.success("success");
	}
	
	/**
	 * 评价列表,根据物品ID查看评论
	 */
	@RequestMapping(value = "/usercenter/appraise", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson listAppraise(@RequestBody UserTokenOne param) {
		String userToken = param.getUserToken();
		long id = Long.parseLong(param.getString());
		if(userToken == null || id == 0) {
			return RetJson.urlError("list appraise error", null);
		}
		List<Appraise> appraise = userService.listAppraise(userToken, id);
		if(appraise == null) {
			return RetJson.unknowError("false", null);
		}
		return RetJson.success("success",appraise);
	}
	
	/**
	 * 我的评价列表，根据用户ID查看评论
	 */
	@RequestMapping(value = "/usercenter/appraise/myappraise", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson listMyAppraise(@RequestBody UserToken param) {
		String userToken = param.getUserToken();
		if(userToken == null) {
			return RetJson.urlError("list appraise error", null);
		}
		List<Appraise> appraise = userService.listMyAppraise(userToken);
		if(appraise == null) {
			return RetJson.unknowError("false", null);
		}
		return RetJson.success("success",appraise);
	}
	
	/**
	 * 评价 增
	 */
	@RequestMapping(value = "/usercenter/appraise/add", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson addAppraise(@RequestBody AppraiseInfo param) {
		String userToken = param.getUserToken();
		Appraise appraise = param.getAppraise();
		if(userToken == null || appraise == null) {
			return RetJson.urlError("list appraise error", null);
		}
		if(userService.addAppraise(userToken, appraise)) {
			return RetJson.unknowError("false", null);
		}
		return RetJson.success("success",null);
	}
	
	/**
	 * 评价 删
	 */
	@RequestMapping(value = "/usercenter/appraise/remove", method = {RequestMethod.POST, RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public RetJson removeAppraise(@RequestBody UserTokenOne param) {
		String userToken = param.getUserToken();
		long id = Long.parseLong(param.getString());
		if(userToken == null || id == 0) {
			return RetJson.urlError("list appraise error", null);
		}
		if(userService.removeAppraise(userToken, id)) {
			return RetJson.unknowError("false", null);
		}
		return RetJson.success("success",null);
	}
	
	/**
	 * 地址 增
	 */
	
	/**
	 * 地址 删
	 */
	
	/**
	 * 地址 改
	 */
	
	/**
	 * 地址 查
	 */
	
	/**
	 * 获取默认地址
	 */
	
	/**
	 * 设置默认地址
	 */
}
