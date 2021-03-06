package com.lyqxsc.yhpt.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import com.lyqxsc.yhpt.domain.RentCommodity;

@Mapper
@Component
public interface IDeputyCommodityDao {

	@Select("select * from deputycommoditylist")
	List<RentCommodity> selectAllRentCommodity();
	
	@Select("select * from deputycommoditylist where distributor=#{distributor}")
	List<RentCommodity> selectRentCommodityByDistributor(@Param("distributor") long distributor);
	
//	@Select("select * from rentcommoditylist where id=#{id}")
//	List<RentCommodity> selectRentCommodityByID(@Param("id") long id);
	
	@Select("select * from deputycommoditylist where id=#{id}")
	RentCommodity selectRentCommodityByID(@Param("id") int id);
	
	@Select("select max(id) from deputycommoditylist")
	Long getMaxID();
	
	@Insert("insert into deputycommoditylist(id, name, picurl, price, type, inventory, ordernum, deposit, note, distributor) "
			+ "values(#{id},#{name},#{picurl},#{price},#{type},#{inventory},#{ordernum},#{deposit},#{note},#{distributor})")
	int addRentCommodity(RentCommodity rentcommodity);
	
	@Delete("delete from deputycommoditylist where id=#{id} and name=#{name}")
	int removeRentCommodity(@Param("id") int id,@Param("name")String name);
	
	@Update("update deputycommoditylist set type=#{type},inventory=#{inventory},ordernum=#{ordernum},deposit=#{deposit},note=#{note},distributor=#{distributor}"
			+ "where id=#{id}")
	int updateRentCommodity(RentCommodity rentcommodity);
	
}
