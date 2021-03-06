package com.lyqxsc.yhpt.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.lyqxsc.yhpt.dao.IAdminDao;
import com.lyqxsc.yhpt.dao.ICommodityClassifyDao;
import com.lyqxsc.yhpt.dao.ICommodityDao;
import com.lyqxsc.yhpt.dao.ICouponDao;
import com.lyqxsc.yhpt.dao.IDistributorDao;
import com.lyqxsc.yhpt.dao.IOrderDao;
import com.lyqxsc.yhpt.dao.IDeputyCommodityDao;
import com.lyqxsc.yhpt.dao.IRentOrderDao;
import com.lyqxsc.yhpt.dao.IUserDao;
import com.lyqxsc.yhpt.domain.Admin;
import com.lyqxsc.yhpt.domain.AdminHomePage;
import com.lyqxsc.yhpt.domain.Commodity;
import com.lyqxsc.yhpt.domain.CommodityClassify;
import com.lyqxsc.yhpt.domain.Coupon;
import com.lyqxsc.yhpt.domain.Distributor;
import com.lyqxsc.yhpt.domain.Order;
import com.lyqxsc.yhpt.domain.RentCommodity;
import com.lyqxsc.yhpt.domain.RentOrder;
import com.lyqxsc.yhpt.domain.User;
import com.lyqxsc.yhpt.domain.UserInfo;
import com.lyqxsc.yhpt.urlclass.ClassifyList;
import com.lyqxsc.yhpt.utils.RetJson;

@Service
public class AdminService {

	@Autowired
	IAdminDao adminDao;
	
	@Autowired
	IUserDao userDao;
	
	@Autowired
	IDistributorDao distributorDao;
	
	@Autowired
	ICommodityDao commodityDao;
	
	@Autowired
	ICommodityClassifyDao commodityClassifyDao;
	
	@Autowired
	IOrderDao orderDao;
	
	@Autowired
	IRentOrderDao rentOrderDao;
	
	@Autowired
	ICouponDao couponDao;
	
	Map<String, UserInfo> onlineMap = new HashMap<String, UserInfo>();
	
	/**
	 *  管理员注册
	 * @param Admin
	 * @return -1 用户名已存在
	 *         -2 注册失败
	 */
	public int signupAdmin(String userToken, Admin param) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return -1;
		}
		
		//判断用户名是否存在
		Admin admin = adminDao.adminIsExist(param.getUsername());
		if(admin != null) {
			return -1;
		}
		
		Long maxID = adminDao.getMaxAdminID();
		if(maxID == null || maxID >= 100) {
			return -1;
		}
		param.setAddTime(Calendar.getInstance().getTime().getTime());
		param.setId(maxID+1);
		
		param.setParent(0);
		param.setGrade(0);
		param.setAuthority(1);
		
		if(adminDao.addAdmin(param) < 0) {
			return -2;
		}
		return 0;
	}
	
	/**
	 * 注册分销商
	 */
	public int signupDistributor(String userToken, Admin param) {
		UserInfo adminInfo = onlineMap.get(userToken);
		//root id = 0
		if(adminInfo == null) {
			return -1;
		}
		
		//判断用户名是否存在
		Admin ret = adminDao.adminIsExist(param.getUsername());
		if(ret != null) {
			return -1;
		}
		
		Admin admin = adminDao.selectAdminByID(adminInfo.getId());
		
		Long maxID = adminDao.getMaxID();
		if(maxID == null) {
			return -1;
		}
		
		if(maxID < 100) {
			maxID = 100l;
		}
		param.setAddTime(Calendar.getInstance().getTime().getTime());
		param.setId(maxID+1);
		
		param.setParent(adminInfo.getId());
		param.setGrade(admin.getGrade()+1);
		param.setAuthority(1);
		
		if(adminDao.addAdmin(param) < 0) {
			return -2;
		}
		return 0;
	}
	
	
	/**
	 *  登录，并添加到在线用户集合中
	 * @param username 用户名
	 * @param password 密码
	 * @return
	 */
	public Admin login(String username, String password, String ip) {
		Admin admin = adminDao.selectAdmin(username, password);
		if(admin == null) {
			return null;
		}
		
		/*向数据库更新时间和本次登录的IP*/
		long id = admin.getId();
		long now = Calendar.getInstance().getTime().getTime();
		adminDao.updateLoginState(now, ip, id);
		
		UserInfo adminInfo = new UserInfo();
		adminInfo.setId(id);
		adminInfo.setUsername(username);
		adminInfo.setIp(ip);
		adminInfo.setLoginTime(now);
		
		for(String userToken:onlineMap.keySet()) {
			if(id == Long.parseLong(userToken.split("O")[0])) {
				logout(userToken);
			}
		}
		String userToken = id + "O" + now;
		onlineMap.put(userToken, adminInfo);
		
		/*更新返回前端的admin ip和time*/
		admin.setUserToken(userToken);
		admin.setThisLoginIP(ip);
		admin.setThisLoginTime(now);
		return admin;
	}
	
	/**
	 *  管理员注销,获取当前时间 
	 * @param id
	 * @return
	 */
	public boolean logout(String userToken) {
		//确定用户是否在线
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		
		//更新退出时间
		adminDao.updateLogoutState(adminInfo.getLoginTime(), adminInfo.getIp(), adminInfo.getId());
		
		onlineMap.remove(userToken);
		return true;
	}
	
	/**
	 * 修改信息
	 */
	//TODO需要判断字段是否为空
	public boolean updateAdmin(String userToken, Admin param) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		adminDao.updateAdmin(adminInfo.getId(), param);
		
		return true;
	}
	
	/**
	 * 首页
	 * @param userToken
	 */
	//后期改为定时统计，由前端直接取数据
	//TODO  完善
	public AdminHomePage homepage(String userToken, int classId) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		int totalSales = commodityDao.getTotalSales();
		int totalOrder = orderDao.getTotalOrder()+rentOrderDao.getTotalOrder();
		
		List<String>  keyword = null;
		List<Commodity> salesDay = null;
		List<Commodity> salesHot = commodityDao.selectCommodityByClass(0, classId);
		
		AdminHomePage home = new AdminHomePage();
		home.setTotalOrder(totalOrder);
		home.setTotalSales(totalSales);
		home.setKeyword(keyword);
		home.setSalesDay(salesDay);
		home.setSalesHot(salesHot);
		
		return home;
	}
	
	/**
	 * 添加物品分类
	 */
	public boolean addClassify(String userToken, int type, String classStr) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		
		int id = commodityClassifyDao.getMaxID();
		CommodityClassify classify = new CommodityClassify();
		classify.setClassId(id+1);
		classify.setType(type);
		classify.setClassStr(classStr);
		if(1 != commodityClassifyDao.insert(classify)) {
			return false;
		}
		return true;
	}
	
	/**
	 * 删除分类
	 */
	public boolean removeClassify(String userToken, int id) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}

		if(1 != commodityClassifyDao.delete(id)) {
			return false;
		}
		return true;
	}
	
	/**
	 * 查询分类
	 */
	public ClassifyList selectClassify(String userToken) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		ClassifyList list = new ClassifyList();
		List<CommodityClassify> mechanical = commodityClassifyDao.selectClass(2);
		List<CommodityClassify> agentia = commodityClassifyDao.selectClass(1);
		list.setAgentia(agentia);
		list.setMechanical(mechanical);
		return list;
	}
	
	/**
	 * 商品列表
	 */
	public List<Commodity> listCommodity(String userToken){
		//确定用户是否在线
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		List<Commodity> commodityList = commodityDao.selectAll();
		return commodityList;
	}
	
	/**
	 * 商品详情
	 */
	public Commodity getCommodityInfo(String userToken, long id) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		Commodity commodity = commodityDao.selectCommodityByID(id);
		return commodity;
	}
	
	/**
	 * 添加商品
	 */
	public boolean addCommodity(String userToken, Commodity commodity, MultipartFile pic) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		
		String path = "D:\\test1\\";
    	String name = System.currentTimeMillis() + ".png";
    	String filename = path+name;
		if(!savePic(pic, filename)) {
			return false;
		}
		
		Long maxID = commodityDao.getMaxID();
		if(maxID == null) {
			return false;
		}
		commodity.setId(maxID+1);
		//总部分销商编号为0
		commodity.setDistributor(0);
		int ret = commodityDao.addCommodity(commodity);
		if(ret != 1) {
			return false;
		}
		return true;
	}
	
	/**
	 * 添加商品时保存图片
	 */
	private boolean savePic(MultipartFile file, String filename) {
		if (!file.isEmpty()) {
            try {
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(filename)));    
                System.out.println(file.getName());
                out.write(file.getBytes());    
                out.flush();    
                out.close();
                System.out.println("save pic seccess");
                return true; 
            } catch (IOException e) {    
                e.printStackTrace();    
                return false;
            } 
		}
        return false;  
	}
	
	
	
	/**
	 * 添加商品数量
	 */
	public boolean addCommodityCount(String userToken, long commodityID, int count) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		
		if(commodityDao.setCommodityCount(commodityID, count) != 1) {
			return false;
		}
		return true;
	}
	
	/**
	 * 商品删除
	 */
	public boolean removeCommodity(String userToken, long commodityID) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		int ret = commodityDao.removeCommodity(commodityID);
		if(ret != 1) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * 商品租赁列表
	 */
	public List<Commodity> listRentCommodity(String userToken){
		//确定用户是否在线
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		
		List<Commodity> rentCommodityList = commodityDao.selectRentCommodity();
		return rentCommodityList;
	}
	
	/**
	 * 商品上架/下架
	 * @param userToken
	 * @param id
	 * @param option 1 上架
	 *               0 下架
	 * @return
	 */
	public boolean onlineCommodity(String userToken, long id, int option){
		//确定用户是否在线
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		
		if(commodityDao.updateOnline(id,option) != 1) {
			return false;
		}
		return true;
	}
	
	/**
	 * 商品库存管理
	 */
	public List<Commodity> inventoryWarning(String userToken,int num){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		
		List<Commodity> commodityList = commodityDao.inventoryWarning(num,0);
		List<Commodity> commodityList1 = commodityDao.inventoryWarning(num,0);
		commodityList.addAll(commodityList1);
		return commodityList;
	}
	
//	/**
//	 * 添加租赁商品
//	 */
//	public boolean addRentCommodity(String userToken, RentCommodity rentCommodity) {
//		UserInfo adminInfo = onlineMap.get(userToken);
//		if(adminInfo == null) {
//			return false;
//		}
//		
//		Long maxID = rentCommodityDao.getMaxID();
//		if(maxID == null) {
//			return false;
//		}
//		rentCommodity.setId(maxID+1);
//		
//		int ret = rentCommodityDao.addRentCommodity(rentCommodity);
//		if(ret != 1) {
//			return false;
//		}
//		return true;
//	}
	
	/**
	 * 租赁商品下架
	 */
//	public boolean removeRentCommodity(String userToken, int rentCommodityID, String rentCommodityName) {
//		UserInfo adminInfo = onlineMap.get(userToken);
//		if(adminInfo == null) {
//			return false;
//		}
//		int ret = rentCommodityDao.removeRentCommodity(rentCommodityID, rentCommodityName);
//		if(ret != 1) {
//			return false;
//		}
//		return true;
//	}
	
	/**
	 * 用户列表
	 */
	public List<User> listAllUser(String userToken){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		List<User> userList = userDao.selectAllUser();
		return userList;
	}
	
	/**
	 * 用户详情
	 */
	public User getUser(String userToken, long id){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		User user = userDao.selectUserByID(id);
		return user;
	}
	
	
	/**
	 * 新增用户
	 */
	public int addUser(String userToken, User user) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return -1;
		}
		
		if(userDao.selectUserByOpenID(user.getOpenID()) != null) {
			return -2;
		}
		
		long maxID = userDao.getMaxID();
		user.setId(maxID+1);
		
		if(userDao.addUser(user) != 1) {
			return -3;
		}
		return 0;
	}
	
	/**
	 * 删除用户
	 */
	public boolean removeUser(String userToken, long id) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		
		int ret = userDao.removeUser(id);
		if(ret != 1) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * 修改用户
	 * 启用1/禁用2/删除4/拉黑3
	 */
	public boolean updateUser(String userToken, long userId, int code) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		int ret;
		if(code == 4) {
			ret = userDao.removeUser(userId);
		}
		else {
			ret = userDao.updateUserAuthority(userId, code);
		}
		if(ret != 1) {
			return false;
		}
		return true;
	}
	
	/**
	 * 分销商列表
	 */
	public List<Distributor> listAllDistributor(String userToken){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		
		List<Distributor> distributorList = distributorDao.selectAllDistributor();
		return distributorList;
	}
	
	
	/**
	 * 新增经销商
	 */
	public int addDistributor(String userToken, Distributor distributor) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return -1;
		}
		
		if(distributorDao.selectDistributorByUsername(distributor.getUsername()) != null) {
			return -2;
		}
		long maxID = distributorDao.getMaxID();
		distributor.setId(maxID+1);
		
		if(distributorDao.addDistributor(distributor) != 1) {
			return -3;
		}
		
		return 0; 
	}
	
	
	/**
	 * 删除经销商
	 */
	public boolean removeDistributor(String userToken, long distributorID) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		
		if(distributorDao.removeDistributor(distributorID) != 1) {
			return false;
		}
		
		return true; 
	}
	
	/**
	 * 删除经销商
	 */
	public boolean updateDistributor(String userToken, Distributor distributor) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		if(distributorDao.updateDistributor(distributor) == 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 经销商详情
	 */
	public Distributor getDistributor(String userToken, long id){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		Distributor distributor = distributorDao.selectDistributorByID(id);
		return distributor;
	}
	
	
	/**
	 * 允许经销商开设店铺
	 */
	public boolean authorizeDistributor(String userToken, long distributorID) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		
		if(distributorDao.authorizeDistributor(distributorID,1) != 1) {
			return false;
		}
		return true;
	}
	
	/**
	 * 取消分销商资格
	 */
	public boolean unAuthorizeDistributor(String userToken, long distributorID) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		
		if(distributorDao.authorizeDistributor(distributorID,0) != 1) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * 商品订单列表
	 */
	public List<Order> listAllOrder(String userToken){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		
		List<Order> orderList = orderDao.selectAllOrderList();
		return orderList;
	}
		
	/**
	 * 查看已处理订单
	 * 订单状态 0待支付, 1已支付, 2待发货, 3待收货，4待评价, 5交易完成, 6交易已取消
	 */
	public List<Order> listDoOrder(String userToken){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		List<Order> orderList = orderDao.getOrderListByStatus(3);
		return orderList;
	}
	
	/**
	 * 查看未处理订单
	 */
	public List<Order> listUndoOrder(String userToken){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		List<Order> orderList = orderDao.getOrderListByStatus(1);
		return orderList;
	}
	
	/**
	 * 查看订单详情
	 */
	public Order getOrder(String userToken, String id){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		Order order = orderDao.getOrderByID(id);
		return order;
	}
	
	/**
	 * 商品发货
	 */
	public boolean sendOrder(String userToken, String id, String count) {
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		if(commodityDao.setCommodityCount(Long.parseLong(id),(-1)*Integer.parseInt(count)) != 1) {
			return false;
		}
		
		if(orderDao.updateOrderList(3,id,null) != 1) {
			return false;
		}
		return true;
	}
	
	/**
	 * 处理订单
	 */
	//TODO
	
	/**
	 * 租赁订单列表
	 */
	public List<RentOrder> listAllRentOrder(String userToken){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		
		List<RentOrder> orderList = rentOrderDao.selectAllRentOrderList();
		return orderList;
	}
	
	/**
	 * 查看已处理租赁订单
	 * 订单状态 0待支付, 1已支付, 2待发货, 3待收货，4待评价, 5交易完成, 6交易已取消
	 */
	public List<RentOrder> listDoRentOrder(String userToken){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		List<RentOrder> orderList = rentOrderDao.getRentOrderListByStatus(3);
		return orderList;
	}
	
	/**
	 * 查看为处理租赁订单
	 * 订单状态 0待支付, 1已支付, 2待发货, 3待收货，4待评价, 5交易完成, 6交易已取消
	 */
	public List<RentOrder> listUndoRentOrder(String userToken){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		List<RentOrder> orderList = rentOrderDao.getRentOrderListByStatus(1);
		return orderList;
	}
	
	/**
	 * 查看租赁订单详情
	 */
	public RentOrder listOneRentOrder(String userToken, String id){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		RentOrder order = rentOrderDao.listOneRentOrder(id);
		return order;
	}
	
	/**
	 * 租赁商品发货
	 */
//	public boolean sendRentOrder(String userToken, String id) {
//		UserInfo adminInfo = onlineMap.get(userToken);
//		if(adminInfo == null) {
//			return false;
//		}
//		if(rentOrderDao.updateRentOrderList(3,id) != 1) {
//			return false;
//		}
//		return true;
//	}
	
	
	
	
	/**
	 * 处理租赁订单
	 */
	//TODO
	
	/**
	 * 优惠券列表
	 */
	public List<Coupon> listCoupon(String userToken){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return null;
		}
		List<Coupon> list = couponDao.selectCouponrList();
		return list;
	}
	
	/**
	 * 添加优惠券
	 */
	public boolean addCoupon(String userToken, Coupon coupon){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		
		long id = couponDao.selectMaxId();
		long now = System.currentTimeMillis();
		long addPerson = adminInfo.getId();
		coupon.setId(id+1);
		coupon.setAddTime(now);
		coupon.setAddPerson(addPerson);
		
		if(couponDao.addCoupon(coupon) != 1) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * 删除优惠券
	 */
	public boolean removeCoupon(String userToken, long id){
		UserInfo adminInfo = onlineMap.get(userToken);
		if(adminInfo == null) {
			return false;
		}
		
		if(couponDao.removeCoupon(id) != 1) {
			return false;
		}
		return true;
	}
	
}
