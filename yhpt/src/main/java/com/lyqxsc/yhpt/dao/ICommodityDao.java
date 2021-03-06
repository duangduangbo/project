package com.lyqxsc.yhpt.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import com.lyqxsc.yhpt.domain.Commodity;
import com.lyqxsc.yhpt.domain.CommodityBak;


/**
 * 总部出售商品和租赁商品数据库接口
 */
@Mapper
@Component
public interface ICommodityDao {
	
	@Select("select sum(sales) from commoditylist")
	Integer getTotalSales();
	
	@Select("select max(id) from commoditylist")
	Long getMaxID();
	
	@Select("select max(ordernum) from commoditylist")
	Integer getMaxOrdernum();
	
	@Select("select * from commoditylist where id=#{id}")
	Commodity selectCommodityByID(@Param("id") long id);
	
	//查询所有商品
	@Select("select * from commoditylist")
	List<Commodity> selectAll();
	
	@Select("select * from commoditylist where type=2 or type=3")
	List<Commodity> selectCommodity();
	
	@Select("select * from commoditylist where type=1 or type=3")
	List<Commodity> selectRentCommodity();
	

	@Insert({"insert into commoditylist(id,name,picurl,price,price1,price2,price3,price4,price5,price6,rentPrice,rentPrice1,rentPrice2,rentPrice3,rentPrice4,rentPrice5,rentPrice6,type,inventory,sales,ordernumDay,ordernumMouth,ordernumTotal,deposit,note,distributor,classId,classStr,online) "
			+ "values(#{id},#{name},#{picurl},#{price},#{price1},#{price2},#{price3},#{price4},#{price5},#{price6},#{rentPrice},#{rentPrice1},#{rentPrice2},#{rentPrice3},#{rentPrice4},#{rentPrice5},#{rentPrice6},#{type},#{inventory},#{sales},#{ordernumDay},#{ordernumMouth},#{ordernumTotal},#{deposit},#{note},#{distributor},#{classId},#{classStr},#{online})"})
	int addCommodity(Commodity commodity);
	
	/*
	 * 分销商三连
	 */
	@Select("select * from commoditylist where distributor=#{distributor}")
	List<Commodity> selectAllByDistributor(@Param("distributor") long distributor);
	
	//获取分销商的商品 1租赁 2出售 3租赁出售
	@Select("select * from commoditylist where (type=2 or type=3) and (distributor=#{distributor} or distributor=0)")
	List<Commodity> selectCommodityForUser(@Param("distributor") long distributor);
	
	@Select("select * from commoditylist where (type=2 or type=3) and (distributor=#{distributor} or distributor=0)")
	List<CommodityBak> selectCommodityBakForUser(@Param("distributor") long distributor);
	
	@Select("select * from commoditylist where (type=1 or type=3) and (distributor=#{distributor} or distributor=0)")
	List<Commodity> selectRentCommodityForUser(@Param("distributor") long distributor);
	
	@Select("select * from commoditylist where (type=1 or type=3) and (distributor=#{distributor} or distributor=0)")
	List<CommodityBak> selectRentCommodityBakForUser(@Param("distributor") long distributor);
	
	@Select("select * from commoditylist where distributor=#{distributor} or distributor=0")
	List<CommodityBak> selectAllCommodityForUser(@Param("distributor") long distributor);
	
	//根据物品id查询分销商的商品
	@Select("select * from commoditylist where id=#{id} and (distributor=#{distributor} or distributor=0)")
	Commodity selectCommodityByIDForUser(@Param("id")long id, @Param("distributor") long distributor);
	
	//根据物品id查询分销商的商品
	@Select("select * from commoditylist where id=#{id} and (distributor=#{distributor} or distributor=0)")
	CommodityBak selectCommodityBakByIDForUser(@Param("id")long id, @Param("distributor") long distributor);
	
	@Select("select * from commoditylist where distributor=#{distributor} and id=#{id}")
	Commodity selectNewCommodityByDistributor(@Param("distributor") long distributor,@Param("id") long id);
	
	@Select("select * from commoditylist where distributor=#{distributor} and ordernum=#{ordernum}")
	Commodity selectHotCommodityByDistributor(@Param("distributor") long distributor,@Param("ordernum") int ordernum);

	@Select("select * from commoditylist where distributor=#{distributor} and classId=#{classId}")
	List<Commodity> selectCommodityByClass(@Param("distributor") long distributor, @Param("classId") int classId);
	
	@Select("select * from commoditylist where distributor=#{distributor} and classId=#{classId}")
	List<CommodityBak> selectCommodityBakByClass(@Param("distributor") long distributor, @Param("classId") int classId);
	
	
	@Select("select * from commoditylist where distributor=#{distributor} and name=#{name}")
	List<Commodity> selectCommodityByName(@Param("distributor") long distributor, @Param("name") String name);

	//用户按名称查询物品
	@Select("select * from commoditylist where distributor=#{distributor} and name=#{name}")
	List<CommodityBak> selectCommodityBakByName(@Param("distributor") long distributor, @Param("name") String name);

	
	//告警商品,管理员
	@Select("select * from commoditylist where inventory<=#{inventory} and distributor=#{distributor}")
	List<Commodity> inventoryWarning(@Param("inventory") int num,@Param("id") long id);
	//不告警商品,管理员
	@Select("select * from commoditylist where inventory>#{inventory} and distributor=#{distributor}")
	List<Commodity> inventoryNoWarning(@Param("inventory") int num,@Param("id") long id);
	
	@Delete("delete from commoditylist where id=#{id}")
	int removeCommodity(@Param("id") long id);
	
	@Delete("delete from commoditylist where id=#{id} and distributor=#{distributor}")
	int removeCommodityByDistributor(@Param("id") long id, @Param("distributor")long distributor);
	
	
	@Update("update commoditylist set name=#{name},picurl=#{picurl},price=#{price},type=#{type},inventory=#{inventory},ordernum=#{ordernum},deposit=#{deposit},note=#{note}"
			+ "where id=#{id}")
	int updateCommodity(Commodity commodity);
	
	//物品上架/下架
	@Update("update commoditylist set online=#{online} where id=#{id}")
	int updateOnline(@Param("id") long id,@Param("online") int option);
	
	
	
	//更新商品日订单和月订单
	@Update("update commoditylist set ordernumDay=#{ordernumDay},ordernumMouth=#{ordernumMouth} where id=#{id}")
	int updateOrderNum(@Param("ordernumDay") int ordernumDay,@Param("ordernumMouth") int ordernumMouth,@Param("id") long id);
	
	//设置商品数量
	@Update("update commoditylist set inventory=inventory+#{count} where id=#{id}")
	int setCommodityCount(@Param("id")long id, @Param("count") int count);
	
	
}
