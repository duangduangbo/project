package com.lyqxsc.yhpt.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lyqxsc.yhpt.controller.WechartController;
import com.lyqxsc.yhpt.dao.IAddressDao;
import com.lyqxsc.yhpt.dao.IAppraiseDao;
import com.lyqxsc.yhpt.dao.ICollectDao;
import com.lyqxsc.yhpt.dao.ICommodityClassifyDao;
import com.lyqxsc.yhpt.dao.ICommodityDao;
import com.lyqxsc.yhpt.dao.IInvitationCodeDao;
import com.lyqxsc.yhpt.dao.IOrderDao;
import com.lyqxsc.yhpt.dao.IDeputyCommodityDao;
import com.lyqxsc.yhpt.dao.IDistributorDao;
import com.lyqxsc.yhpt.dao.IRentOrderDao;
import com.lyqxsc.yhpt.dao.IShopCarDao;
import com.lyqxsc.yhpt.dao.IUserDao;
import com.lyqxsc.yhpt.domain.Address;
import com.lyqxsc.yhpt.domain.Appraise;
import com.lyqxsc.yhpt.domain.Collect;
import com.lyqxsc.yhpt.domain.Commodity;
import com.lyqxsc.yhpt.domain.CommodityBak;
import com.lyqxsc.yhpt.domain.CommodityClassify;
import com.lyqxsc.yhpt.domain.CommodityPage;
import com.lyqxsc.yhpt.domain.Distributor;
import com.lyqxsc.yhpt.domain.WxHomePage;
import com.lyqxsc.yhpt.urlclass.ClassifyList;
import com.lyqxsc.yhpt.domain.HotCommodity;
import com.lyqxsc.yhpt.domain.InvitationCode;
import com.lyqxsc.yhpt.domain.NewCommodity;
import com.lyqxsc.yhpt.domain.Order;
import com.lyqxsc.yhpt.domain.RentCommodity;
import com.lyqxsc.yhpt.domain.RentOrder;
import com.lyqxsc.yhpt.domain.ShopCar;
import com.lyqxsc.yhpt.domain.User;
import com.lyqxsc.yhpt.domain.UserInfo;

import net.sf.json.JSONObject;

@Service
public class UserService {
	
	@Autowired
	IUserDao userDao;
	
	@Autowired
	IOrderDao orderDao;
	
	@Autowired
	IAddressDao addressDao;
	
	@Autowired
	IRentOrderDao rentOrderDao;
	
	@Autowired
	ICollectDao collectDao; 
	
	@Autowired
	IShopCarDao shopCarDao;
	
	@Autowired
	IAppraiseDao appraiseDao;
	
	@Autowired
	ICommodityDao commodityDao;
	
	@Autowired
	IDistributorDao distributorDao;
	
	@Autowired
	IInvitationCodeDao invitationCodeDao;
	
	@Autowired
	ICommodityClassifyDao commodityClassifyDao;
	
	@Value("${RentCommodityPic}")
	String rentCommodityPic;
	
	@Value("${CommodityPic}")
	String commodityPic;
	
	@Value("${HotPagePic}")
	String hotPagePic;
	
	@Value("${NewPagePic}")
	String newPagePic;
	
	@Value("${ShopPic}")
	String shopPic;
	
	@Value("${HomePic}")
	String homePic;
	
	static int NEWSHOPCOUNT = 10;
	
	// 在线用户集合<id,用户信息>
	Map<String, UserInfo> onlineMap = new HashMap<String, UserInfo>();
	
	// 可出售的商品集合
	List<Commodity> commodityList = new ArrayList<Commodity>();
	
	static final Logger log = LoggerFactory.getLogger(WechartController.class);
	
	public boolean root() {
		User user = userDao.selectUserByOpenID("oACat1eA_RKT1zvIOvuZj4Obc3zQ");
		if(user == null) {
			return false;
		}
		long now = 1554642125630l;
		UserInfo userInfo = new UserInfo();
		userInfo.setId(user.getId());
		userInfo.setUsername(user.getOpenID());
		userInfo.setIp(user.getLastLoginIP());
		userInfo.setLoginTime(now);
		userInfo.setDistributor(user.getDistributor());
		
		String userToken = user.getId() + "O" + now;
		System.out.println(userToken);
		onlineMap.put(userToken, userInfo);
		return true;
	}
	
	/**
	 *  用户注册
	 * @param User
	 * @return -1 用户已存在
	 *         -2 注册失败
	 */
	public User signup(JSONObject wxUserInfo) {
		Long maxID = userDao.getMaxID();
		if(maxID == null) {
			return null;
		}
		
		User user = new User();
		user.setId(maxID+1);
		user.setOpenID((String)wxUserInfo.get("openid"));
		user.setNikeName((String)wxUserInfo.get("nickname"));
//		user.setRealName(String realName);
//		user.setEmail(String email);
//		user.setPhone(String phone);
		user.setSex((int)wxUserInfo.get("sex")+"");
		user.setProvince((String)wxUserInfo.get("province"));
		user.setCity((String)wxUserInfo.get("city"));
		user.setHeadImgUrl((String)wxUserInfo.get("headimgurl"));
//		user.setAddress(String address);
		user.setWallet(0);
		user.setAuthority(1);

		user.setAddTime(System.currentTimeMillis());

		if(userDao.addUser(user) < 0) {
			return null;
		}
		return user;
	}

	/**
	 *  用户登录，并添加到在线用户集合中
	 * @param username 用户名
	 * @param password 密码
	 * @return
	 */
	public User login(JSONObject wxUserInfo, String ip) {
		String openID = (String)wxUserInfo.get("openid");
		User user = userDao.selectUserByOpenID(openID);
		if(user == null) {
			user = signup(wxUserInfo);
			if(user == null) {
				log.warn("sigup error openID : " + openID);
				return null;
			}
		}
		
		/*向数据库更新时间和本次登录的IP*/
		long now = Calendar.getInstance().getTime().getTime();
		userDao.updateLoginState(now, ip, openID);
		
		long id = user.getId();
		
		UserInfo userInfo = new UserInfo();
		userInfo.setId(id);
		userInfo.setUsername(openID);
		userInfo.setIp(ip);
		userInfo.setLoginTime(now);
		userInfo.setDistributor(user.getDistributor());
		
		/*如果用户在线，更新集合，如果不在线，添加集合*/
		for(String userToken:onlineMap.keySet()) {
			if(id == Long.parseLong(userToken.split("O")[0])) {
				logout(userToken);
			}
		}
		
		String userToken = id + "O" + now;
		if(user.getAuthority() == 1) {
			onlineMap.put(userToken, userInfo);
		}
		/*更新返回前端的user IP*/
		user.setUserToken(userToken);
		user.setThisLoginIP(ip);
		user.setThisLoginTime(now);
		return user;
	}

	/**
	 *  用户注销,获取当前时间 
	 * @param openid
	 * @return
	 */
	public boolean logout(String userToken) {
		//确定用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			System.out.println("logout error");
			return false;
		}
		
		//更新退出时间
		userDao.updateLogoutState(userInfo.getLoginTime(), userInfo.getIp(), userInfo.getUsername());
		
		onlineMap.remove(userToken);
		return true;
	}
	
	/**
	 * 添加邀请码
	 */
	public boolean addInvitationCode(String userToken, String code) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		long userID = userInfo.getId();
		if(userInfo.getDistributor() != 0) {
			return false;
		}
		InvitationCode invitationCode = invitationCodeDao.selectInvitationCodeByCode(code);
		if(invitationCode == null) {
			return false;
		}
		long distributorID = invitationCode.getDistributorID();
		if(1 != userDao.bindDistributor(userID, distributorID)) {
			return false;
		}
		if(1 != distributorDao.updateUserNum(distributorID)) {
			return false;
		}
		if(1 != invitationCodeDao.bindInvitationCode(userID, code)) {
			return false;
		}
		userInfo.setDistributor(distributorID);
		onlineMap.replace(userToken, userInfo);
		return true;
	}
	
	/**
	 *	不填写邀请码，直接进入
	 */
	public boolean noInvitationCode(String userToken) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		long userID = userInfo.getId();
		User user = userDao.selectUserByID(userID);
		if(user == null) {
			return false;
		}
		if(user.getDistributor() != 0) {
			return true;
		}
		String province = user.getProvince();
		String city = user.getCity();
		List<Distributor> distributorList = distributorDao.getDistributorByCity(city);
		if(distributorList == null) {
			distributorList = distributorDao.getDistributorByProvince(province);
			if(distributorList == null) {
				distributorList = distributorDao.getDistributorByUserNum();
			}
		}
		
		Distributor distributor = distributorList.get(0);
		long distributorId = distributor.getId();
		if(1 != distributorDao.updateUserNum(distributorId)) {
			log.info("分销商添加用户失败");
			return false;
		}
		long distributorID = distributor.getId();
		if(1 != userDao.bindDistributor(userID, distributorID)) {
			return false;
		}
		userInfo.setDistributor(distributorID);
		onlineMap.replace(userToken, userInfo);
		return true;
	}
	
	/**
	 * 首页
	 * @param userToken
	 * @return
	 */
	public WxHomePage homePage(String userToken) {
		//确定用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		
		WxHomePage homePage = new WxHomePage();
		homePage.setPic(homePic.split(";"));
		
		long distributorID = userInfo.getDistributor();
		Distributor distributor = distributorDao.selectDistributorByID(distributorID);
		List<CommodityBak> commodityList = commodityDao.selectAllCommodityForUser(distributorID);
		List<CommodityBak> commodity = hidePriceList(commodityList,distributor.getGrade());
		homePage.setCommodityList(commodity);
		return homePage;
	}
	
	/**
	 * 关于我们
	 */
	public String aboutUs(String userToken) {
		//确定用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		String msg = "关于我们";
		return msg;
	}
	
	/**
	 * 商城
	 */
	//TODO  home page
	public WxHomePage shop(String userToken) {
		//确定用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		
		WxHomePage homePage = new WxHomePage();
		
		homePage.setPic(shopPic.split(";"));
		
		long distributorID = userInfo.getDistributor();
		Distributor distributor = distributorDao.selectDistributorByID(distributorID);
		List<CommodityBak> commodityList = commodityDao.selectAllCommodityForUser(distributorID);
		List<CommodityBak> commodity = hidePriceList(commodityList,distributor.getGrade());
		homePage.setCommodityList(commodity);
		return homePage;
	}
	
	/**
	 * 分类列表
	 */
	public ClassifyList classList(String userToken){
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		ClassifyList classifyList = new ClassifyList();
		classifyList.setAgentia(commodityClassifyDao.selectClass(1));
		classifyList.setMechanical(commodityClassifyDao.selectClass(2));
		return classifyList;
	}
	
	/**
	 * 查询一类物品集合
	 */
	public List<CommodityBak> selectCommodityByClass(String userToken,String classId){
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		long distributorID = userInfo.getDistributor();
		if(distributorID == 0) {
			return null;
		}
		List<CommodityBak> commodityList = new ArrayList<CommodityBak>();
		String[] classIdBuf = classId.split("!");
		for(String listTemp:classIdBuf) {
			List<CommodityBak> list = commodityDao.selectCommodityBakByClass(distributorID,Integer.parseInt(listTemp));
			commodityList.addAll(list);
		}
		
		return commodityList;
	}
	
	/**
	 * 按名称查询
	 */
	//TODO 后期添加模糊查询
	public List<CommodityBak> selectCommodityByName(String userToken,String name){
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		long distributorID = userInfo.getDistributor();
		if(distributorID == 0) {
			return null;
		}
		Distributor distributor = distributorDao.selectDistributorByID(distributorID);
		int grade = distributor.getGrade();
		List<CommodityBak> listTemp = commodityDao.selectCommodityBakByName(distributorID,name);
		List<CommodityBak> list = hidePriceList(listTemp, grade);
		return list;
	}
	
	/**
	 * 新品   展示出售商品 maxID-10 范围内的商品
	 */
	//TODO 效率低，后期改为后端定时统计数据，有前端直接取值
	public NewCommodity newShopShow(String userToken) {
		//确定用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		long distributorID = userInfo.getDistributor();
		long commodityMaxID = commodityDao.getMaxID();
		
		NewCommodity commodity = new NewCommodity();
		List<Commodity> commodityList = new ArrayList<Commodity>();
		
		while(commodityList.size() < 10) {
			Commodity temp =  commodityDao.selectNewCommodityByDistributor(distributorID,commodityMaxID);
			commodityList.add(temp);
			commodityMaxID = commodityMaxID - 1;
			if(commodityMaxID < 1) {
				break;
			}
		}
		commodity.setCommodity(commodityList);
		commodity.setPic(newPagePic.split(";"));
		return commodity;
	}
	
	/**
	 * 热卖品   
	 */
	//TODO 效率低，后期改为后端定时统计数据，有前端直接取值
	public HotCommodity hotShopShow(String userToken) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		long distributorID = userInfo.getDistributor();
		Integer maxNum = commodityDao.getMaxOrdernum();
		
		HotCommodity commodity = new HotCommodity();
		List<Commodity> commodityList = new ArrayList<Commodity>();
		
		while(commodityList.size() < 10) {
			Commodity temp =  commodityDao.selectHotCommodityByDistributor(distributorID,maxNum);
			commodityList.add(temp);
			maxNum = maxNum - 1;
			if(maxNum < 1) {
				break;
			}
		}
		commodity.setCommodity(commodityList);
		commodity.setPic(homePic.split(";"));
		return commodity;
	}
	
	/**
	 *  查看可出售商品
	 */
	public CommodityPage selectCommodity(String userToken) {
		//判断用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		long distributorID = userInfo.getDistributor();
		
		Distributor distributor = distributorDao.selectDistributorByID(distributorID);
		int grade = distributor.getGrade();
		List<CommodityBak> commodityListTemp = commodityDao.selectCommodityBakForUser(distributorID);
		List<CommodityBak> commodity = hidePriceList(commodityListTemp, grade);
		
		CommodityPage commodityPage = new CommodityPage();
		commodityPage.setCommodity(commodity);
		commodityPage.setPic(commodityPic.split(";"));
		return commodityPage;
	}
	
	
	/**
	 *  查看可租赁商品
	 */
	public CommodityPage selectRentCommodity(String userToken) {
		//判断用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		long distributorId = userInfo.getDistributor();
		
		Distributor distributor = distributorDao.selectDistributorByID(distributorId);
		int grade = distributor.getGrade();
		List<CommodityBak> commodityListTemp = commodityDao.selectRentCommodityBakForUser(distributorId);
		List<CommodityBak> commodity = hidePriceList(commodityListTemp, grade);
		
		CommodityPage commodityPage = new CommodityPage();
		commodityPage.setCommodity(commodity);
		commodityPage.setPic(rentCommodityPic.split(";"));
		return commodityPage;
	}
	
	/**
	 * 根据商品id查询商品，多个物品用！隔开
	 */
	public List<CommodityBak> selectCommodityByID(String userToken,String idbuf) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		long distributorId = userInfo.getDistributor();
		Distributor distributor = distributorDao.selectDistributorByID(distributorId);
		int grade = distributor.getGrade();
		
		String[] idStr = idbuf.split("!");
		List<CommodityBak> commodityList = new ArrayList<CommodityBak>();
		for(String id:idStr) {
			CommodityBak commodity = commodityDao.selectCommodityBakByIDForUser(Long.parseLong(id),distributorId);
			commodityList.add(commodity);
		}
		List<CommodityBak> commodity = hidePriceList(commodityList, grade);
		return commodity;
	}
	
	/**
	 *  点击购买，生成订单
	 * @param id
	 * @param commodityid
	 * @return
	 */
	public Order makeCommodityOrder(String userToken, long commodityid, int count, String ip) {
		//判断用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		long now = Calendar.getInstance().getTime().getTime();
		Commodity commodity = commodityDao.selectCommodityByID(commodityid);
		String username = userDao.selectUsername(userInfo.getId());
		float price = commodity.getPrice();
		
		Order order = new Order();
		order.setOrderNumber(userInfo.getId() + "OO" + now);
		order.setOwner(userInfo.getId());
		order.setOwnerName(username);
		order.setCommodityID(commodity.getId());
		order.setUrl(commodity.getPicurl());
		order.setCommodityName(commodity.getName());
		order.setPrice(price);
		order.setCount(count);
		order.setTotalPrice(count*price);
		order.setPayMoney(0);
		order.setOrderPrice(count*price);
		order.setCompleteTime(0);
		order.setPayOrdertime(System.currentTimeMillis());
		order.setStatus(0);
		order.setPayType(0);
		order.setPayIP(ip);
		order.setLastPayStatus(0);
		order.setAddr(null);
		
		return order;
	}
	
	/**
	 *  提交订单
	 */
	public boolean presentOrder(String userToken, Order order) {
		//判断用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		
		if(orderDao.addOrderList(order) != 1) {
			return false;
		}
		return true;
	}
	
	/**
	 * 批量提交订单
	 */
	public boolean batchPresentOrder(String userToken, List<Order> orderList){
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		long distributorID = userInfo.getDistributor();
		Distributor distributor = distributorDao.selectDistributorByID(distributorID);
		int num = 0;
		for(Order order:orderList) {
			if(orderDao.addOrderList(order) != 1) {
				return false;
			}
			num++;
		}
		userDao.addOrderNum(num,userInfo.getId());
		distributorDao.addOrderNum(num, distributor.getId());
		return true;
	}
	
	
	/**
	 * 点击租赁，生成订单
	 * @param id
	 * @param commodityid
	 * @return
	 */
	public RentOrder makeRentCommodityOrder(String userToken, long id, int count, String ip) {
		//判断用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		long now = Calendar.getInstance().getTime().getTime();
		Commodity rentCommodity = commodityDao.selectCommodityByID(id);
		if(rentCommodity.getDeposit() == 0) {
			return null;
		}
		String username = userDao.selectUsername(userInfo.getId());
		float price = rentCommodity.getPrice();
		float deposit = rentCommodity.getDeposit(); 
		
		RentOrder rentOrder = new RentOrder();
		rentOrder.setOrderNumber(userInfo.getId() + "II" + now);
		rentOrder.setOwner(userInfo.getId());
		rentOrder.setOwnerName(username);
		rentOrder.setRentCommodityID(rentCommodity.getId());
		rentOrder.setUrl(rentCommodity.getPicurl());
		rentOrder.setRentCommodityName(rentCommodity.getName());
		rentOrder.setPrice(price);
		rentOrder.setDeposit(deposit);
		rentOrder.setCount(count);
		rentOrder.setTotalDeposit(count*deposit);
		rentOrder.setTotalPrice(count*price);
		rentOrder.setOrderPrice(count*price + count*deposit);
		rentOrder.setPayMoney(0);
		rentOrder.setCompleteTime(0);
		rentOrder.setMakeOrdertime(System.currentTimeMillis());
		rentOrder.setStatus(0);
		rentOrder.setPayType(0);
		rentOrder.setPayIP(ip);
		rentOrder.setLastPayStatus(0);
		rentOrder.setAddr(null);
		
		return rentOrder;
	}
	
	/**
	 *  提交租赁订单
	 */
	public boolean presentRentOrder(String userToken, RentOrder rentOrder) {
		//判断用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		
		if(rentOrderDao.addRentOrderList(rentOrder) != 1) {
			return false;
		}
		return true;
	}
	
	/**
	 * 批量提交租赁订单
	 */
	public boolean batchPresentRentOrder(String userToken, List<RentOrder> rentOrderList) {
		//判断用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		long distributorID = userInfo.getDistributor();
		Distributor distributor = distributorDao.selectDistributorByID(distributorID);
		int num = 0;
		for(RentOrder order:rentOrderList) {
			if(rentOrderDao.addRentOrderList(order) != 1) {
				return false;
			}
			num++;
		}
		userDao.addRentOrderNum(num,userInfo.getId());
		distributorDao.addRentOrderNum(num, distributor.getId());
		return true;
	}
	
	/**
	 * 获取全部购买订单
	 */
	public List<Order> getAllOrder(String userToken){
		//判断用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		Long id = userInfo.getId();
		List<Order> orderList = orderDao.getAllOrderByUser(id);
		return orderList;
	}
	
	/**
	 * 购买订单详情
	 */
	public Order getOrder(String userToken, String id){
		//判断用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		Order order = orderDao.getOrderByID(id);
		return order;
	}
	
	/**
	 * 取消购买订单
	 */
	public boolean cancelOrder(String userToken,String id,String reason) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		if(orderDao.updateOrderList(0,id,reason) == 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 返回待付款/已付款订单
	 */
	public List<Order> getTypeOrder(String userToken, int type){
		//判断用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		Long id = userInfo.getId();
		List<Order> orderList = orderDao.getOrderStatusByUser(id,type);
		return orderList;
	}
	
	/**
	 * 租赁订单列表
	 */
	public List<RentOrder> getAllRentOrder(String userToken){
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		Long id = userInfo.getId();
		List<RentOrder> orderList = rentOrderDao.getAllRentOrderByID(id);
		return orderList;
	}
	
	/**
	 * 租赁订单详情
	 */
	public RentOrder getRentOrder(String userToken, String id){
		//判断用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		RentOrder order = rentOrderDao.listOneRentOrder(id);
		return order;
	}
	
	
	/**
	 * 取消租赁订单
	 */
	public boolean cancelRentOrder(String userToken,String id,String reason) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		if(rentOrderDao.updateRentOrderList(0,id,reason) == 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 返回待付款/已付款订单
	 */
	public List<RentOrder> getTypeRentOrder(String userToken, int type){
		//判断用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		Long id = userInfo.getId();
		List<RentOrder> orderList = rentOrderDao.getTypeRentOrderByID(id,type);
		return orderList;
	}
	
	
	/**
	 * 购物车清单
	 */
	public List<ShopCar> getShopCar(String userToken){
		//判断用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		Long id = userInfo.getId();
		List<ShopCar> shopList = shopCarDao.getShoppingByUserID(id);
		return shopList;
	}
	
	/**
	 * 购物车 增
	 */
	public boolean addShopCar(String userToken, ShopCar shopCar) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		
		Long maxID = shopCarDao.getMaxID();
		if(maxID == null) {
			return false;
		}
		shopCar.setCarid(maxID+1);
		Commodity commodity = commodityDao.selectCommodityByID(shopCar.getCommodityid()); 
		shopCar.setInventory(commodity.getInventory());
		int ret = shopCarDao.addShopCar(shopCar);
		if(ret == 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 购物车 删
	 */
	public boolean removeShopCar(String userToken, String idStr) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		
		String[] idbuf = idStr.split("!");
		
		for(String id:idbuf) {
			int ret = shopCarDao.removeShopCar(Long.parseLong(id));
			if(ret != 1) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 购物车 改
	 */
	public boolean updateShopCar(String userToken, ShopCar shopCar) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		int ret = shopCarDao.updateShopCar(shopCar);
		if(ret == 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 购物车结账
	 */
	public List<Order> settleShopCar(String userToken, String idbuf){
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		String[] idStr = idbuf.split("!");
		List<Order> orderList = new ArrayList<Order>();
		for(String id:idStr) {
			ShopCar shopCar = shopCarDao.getShoppingByID(Long.parseLong(id));
			orderList.add(makeCommodityOrder(userToken,shopCar.getCommodityid(),shopCar.getCount(),userInfo.getIp()));
		}
		return orderList;
	}
	
	/**
	 * 收藏夹清单 
	 */
	public List<Collect> getCollect(String userToken) {
		//判断用户是否在线
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		Long id = userInfo.getId();
		List<Collect> collectList = collectDao.getShoppingByID(id);
		return collectList;
	}
	
	/**
	 * 收藏夹 增
	 */
	public boolean addCollect(String userToken, Collect collect) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		
		Long maxID = collectDao.getMaxID();
		if(maxID == null) {
			return false;
		}
		collect.setId(maxID+1);
		int ret = collectDao.addCollect(collect);
		if(ret == 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 收藏夹 删
	 */
	public boolean removeCollect(String userToken, String idStr) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		
		String[] idbuf = idStr.split("!");
		
		for(String id:idbuf) {
			int ret = shopCarDao.removeShopCar(Long.parseLong(id));
			if(ret != 1) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 评价列表,根据物品ID查看评论
	 */
	public List<Appraise> listAppraise(String userToken, long id){
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		
		List<Appraise> appraiseList = appraiseDao.getAppraiseByThingID(id);
		return appraiseList;
	}
	
	/**
	 * 我的评价列表，根据用户ID查看评论
	 */
	public List<Appraise> listMyAppraise(String userToken){
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		long id = userInfo.getId();
		
		List<Appraise> appraiseList = appraiseDao.getAppraiseByUserID(id);
		return appraiseList;
	}
	
	/**
	 * 评价 增
	 */
	public boolean addAppraise(String userToken, Appraise appraise) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		Long maxID = appraiseDao.getMaxID();
		if(maxID == null) {
			return false;
		}
		appraise.setId(maxID+1);
		int ret = appraiseDao.addAppraise(appraise);
		if(ret == 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 评价 删
	 */
	public boolean removeAppraise(String userToken, long id) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		
		long userID = userInfo.getId();
		int ret = appraiseDao.removeAppraise(id);
		if(ret == 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 收货地址 增
	 */
	public boolean addAddress(String userToken, Address addr) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		Long maxID = addressDao.getMaxID();
		if(maxID == null) {
			return false;
		}
		addr.setId(maxID + 1);
		addr.setUserId(userInfo.getId());
		int ret = addressDao.addAddress(addr);
		if(ret == 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 收货地址 删
	 */
	public boolean removeAddress(String userToken, long id) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		long userId = userInfo.getId();
		int ret = addressDao.removeAddress(userId, id);
		if(ret == 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 收货地址 改
	 */
	public boolean updateAddress(String userToken, Address addr) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		int ret = addressDao.updateAddress(addr);
		if(ret == 1) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * 收货地址 查
	 */
	public List<Address> selectAddress(String userToken){
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		long userId = userInfo.getId();
		List<Address> addr = addressDao.selectAddressByUser(userId);
		return addr;
	}
	
	/**
	 * 设置默认地址
	 */
	public boolean setMainAddr(String userToken, long id) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return false;
		}
		long userId = userInfo.getId();
		List<Address> addrList = addressDao.selectAddressByUser(userId);
		for(Address addr:addrList) {
			if(addr.getId() == id) {
				addr.setMain(1);
				addressDao.updateAddress(addr);
				continue;
			}
			if(addr.getMain() == 1) {
				addr.setMain(0);
				addressDao.updateAddress(addr);
			}
		}
		return true;
	}
	
	/**
	 * 获取默认地址
	 */
	public Address getMainAddr(String userToken) {
		UserInfo userInfo = onlineMap.get(userToken);
		if(userInfo == null) {
			return null;
		}
		long userId = userInfo.getId();
		Address ret = null;
		List<Address> addrList = addressDao.selectAddressByUser(userId);
		for(Address addr:addrList) {
			ret = addr;
			if(addr.getMain() == 1) {
				break;
			}
		}
		return ret;
	}
	/**
	 * 伪造列表价格
	 */
	private List<CommodityBak> hidePriceList(List<CommodityBak> commodityListTemp,int grade){
		List<CommodityBak> commodity = new ArrayList<CommodityBak>();
		for(CommodityBak commodityBak:commodityListTemp) {
			switch (grade) {
			case 1:
				commodityBak.setPrice(commodityBak.getPrice1());
				commodityBak.setRentPrice(commodityBak.getRentPrice1());
				break;
			case 2:
				commodityBak.setPrice(commodityBak.getPrice2());
				commodityBak.setRentPrice(commodityBak.getRentPrice2());
				break;
			case 3:
				commodityBak.setPrice(commodityBak.getPrice3());
				commodityBak.setRentPrice(commodityBak.getRentPrice3());
				break;
			case 4:
				commodityBak.setPrice(commodityBak.getPrice4());
				commodityBak.setRentPrice(commodityBak.getRentPrice4());
				break;
			case 5:
				commodityBak.setPrice(commodityBak.getPrice5());
				commodityBak.setRentPrice(commodityBak.getRentPrice5());
			case 6:
				commodityBak.setPrice(commodityBak.getPrice6());
				commodityBak.setRentPrice(commodityBak.getRentPrice6());
				break;
			default:
				break;
			}
			commodity.add(commodityBak);
		}
		return commodity;
	}
	
	/**
	 * 伪造价格
	 */
	private CommodityBak hidePrice(CommodityBak commodity, int grade){
		switch (grade) {
		case 1:
			commodity.setPrice(commodity.getPrice1());
			break;
		case 2:
			commodity.setPrice(commodity.getPrice2());
			break;
		case 3:
			commodity.setPrice(commodity.getPrice3());
			break;
		case 4:
			commodity.setPrice(commodity.getPrice4());
			break;
		case 5:
			commodity.setPrice(commodity.getPrice5());
			break;
		default:
			break;
		}
		return commodity;
	}

//	@Override
//	public void afterPropertiesSet() throws Exception {
//		User user = userDao.selectUser("oACat1eA_RKT1zvIOvuZj4Obc3zQ");
//		long now = 1554642125630l;
//		UserInfo userInfo = new UserInfo();
//		userInfo.setId(user.getId());
//		userInfo.setUsername(user.getOpenID());
//		userInfo.setIp(user.getLastLoginIP());
//		userInfo.setLoginTime(now);
//		
//		String userToken = user.getId() + "O" + now;
//		System.out.println(userToken);
//		onlineMap.put(userToken, userInfo);
//	}
}
