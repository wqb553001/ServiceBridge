//package com.overpass.ServiceBridge.mapper;
//
//import cn.huimin100.erp.settle.task.annotation.ConvertDefaultDate;
//import cn.huimin100.erp.settle.task.pojo.model.FeeTaskFailRecordDO;
//import cn.huimin100.erp.settle.task.query.FeeTaskFailRecordQuery;
//import org.apache.ibatis.annotations.Mapper;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Mapper
//@Repository
//public interface FailRecordDOMapper {
//    /**
//     * This method was generated by MyBatis Generator.
//     * This method corresponds to the database table fee_task_fail_record
//     *
//     * @mbggenerated Mon Feb 28 17:22:14 CST 2022
//     */
//    int deleteByPrimaryKey(Integer id);
//
//    /**
//     * This method was generated by MyBatis Generator.
//     * This method corresponds to the database table fee_task_fail_record
//     *
//     * @mbggenerated Mon Feb 28 17:22:14 CST 2022
//     */
//    int insert(FeeTaskFailRecordDO record);
//
//    /**
//     * This method was generated by MyBatis Generator.
//     * This method corresponds to the database table fee_task_fail_record
//     *
//     * @mbggenerated Mon Feb 28 17:22:14 CST 2022
//     */
//    int insertSelective(FeeTaskFailRecordDO record);
//
//    /**
//     * This method was generated by MyBatis Generator.
//     * This method corresponds to the database table fee_task_fail_record
//     *
//     * @mbggenerated Mon Feb 28 17:22:14 CST 2022
//     */
//    FeeTaskFailRecordDO selectByPrimaryKey(Integer id);
//
//    /**
//     * This method was generated by MyBatis Generator.
//     * This method corresponds to the database table fee_task_fail_record
//     *
//     * @mbggenerated Mon Feb 28 17:22:14 CST 2022
//     */
//    int updateByPrimaryKeySelective(FeeTaskFailRecordDO record);
//
//    /**
//     * This method was generated by MyBatis Generator.
//     * This method corresponds to the database table fee_task_fail_record
//     *
//     * @mbggenerated Mon Feb 28 17:22:14 CST 2022
//     */
//    int updateByPrimaryKeyWithBLOBs(FeeTaskFailRecordDO record);
//
//    /**
//     * This method was generated by MyBatis Generator.
//     * This method corresponds to the database table fee_task_fail_record
//     *
//     * @mbggenerated Mon Feb 28 17:22:14 CST 2022
//     */
//    int updateByPrimaryKey(FeeTaskFailRecordDO record);
//
//    @ConvertDefaultDate
//    List<FeeTaskFailRecordDO> selectList(FeeTaskFailRecordQuery feeTaskFailRecordQuery);
//}