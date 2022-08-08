package com.zqazfl.common.smsutils;

import com.zqazfl.common.utils.JdbcUtilUse;

import javax.annotation.Resource;

public class SMSUtils {

    @Resource(name = "JdbcUtilImpl")
    JdbcUtilUse jdbcUtilUse;
    /**
     * 分房顺序号摇选日期提醒 13692 -- 14476
     *
     * @param hzid 户主id
     * @return
     */
    public String xfSerialDateRemind(String hzid) {
        // 操作ID
        int czid = jdbcUtilUse.getJdbcTemplate().queryForObject("SELECT SEQ_SMS.NEXTVAL FROM DUAL", Integer.class);
        String smsSql = "INSERT INTO SMS_NOTIFICATION(ID,DATAID,TEMPLATEID,PHONE,STATE,CZID,CONTENT,LRSJ)" +
                "SELECT SEQ_SMS.NEXTVAL, HZ.ID, '14476', HZ.PHONENUM, 0, " + czid + ", " +
                "'{\"name\":\"' || HZ.HZXM || '\",\"dpcmc\":\"' || SPST.PROSETNAME || '\",\"startTime\":\"' || to_char(XP.STARTTIME, 'yyyy-MM-dd') || '\",\"endTime\":\"' || to_char(XP.ENDTIME, 'yyyy-MM-dd') || '\"}', SYSDATE" +
                " FROM XF_HUZHUINFO HZ" +
                " LEFT JOIN XF_PICIINFO XP ON HZ.PCID = XP.ID " +
                " LEFT JOIN  (SELECT SPS.PROSETNAME ,SPSD.PROJECTID FROM SYS_PROJECT_SET_DETAIL SPSD" +
                " LEFT JOIN  SYS_PROJECT_SET SPS ON SPSD.PROJECTSETID=SPS.ID WHERE SPS.TYPE='297') SPST ON SPST.PROJECTID=HZ.PCID" +
                " WHERE HZ.ID = ?";
        jdbcUtilUse.getJdbcTemplate().update(smsSql, hzid);
        String smsRecordSql = "INSERT INTO SMS_RECORD(ID,DATAID,LRR,LRSJ,TEMPLATEID) VALUES(?,?,'系统自发',SYSDATE,'14476')";
        jdbcUtilUse.getJdbcTemplate().update(smsRecordSql, czid, hzid);
        return "true";
    }

    /**
     * 分房顺序号摇选结果推送 13694 -- 14474
     *
     * @param hzid 户主id
     * @return
     */
    public String xfSerialResultRemind(String hzid) {
        // 操作ID
        int czid = jdbcUtilUse.getJdbcTemplate().queryForObject("SELECT SEQ_SMS.NEXTVAL FROM DUAL", Integer.class);
        String smsSql = "INSERT INTO SMS_NOTIFICATION(ID,DATAID,TEMPLATEID,PHONE,STATE,CZID,CONTENT,LRSJ) " +
                " SELECT SEQ_SMS.NEXTVAL,HZ.ID,'14474',PHONENUM,0, " + czid + "," +
                " '{\"name\":\"' || HZXM || '\",\"dpcmc\":\"' || SPST.PROSETNAME || '\",\"pcjc\":\"' || XP.PCJC || '\",\"serial\":\"' || SERIAL || '\",\"serialTime\":\"' || to_char(SERIALTIME, 'yyyy-MM-dd HH24:mi:ss') || '\"}', SYSDATE" +
                " FROM (SELECT ID,PHONENUM,HZXM,PCID,SERIAL,SERIALTIME FROM XF_HUZHUINFO UNION ALL SELECT ID,WTRPHONENUM,HZXM,PCID,SERIAL,SERIALTIME FROM XF_HUZHUINFO WHERE WTRPHONENUM IS NOT NULL) HZ LEFT JOIN XF_PICIINFO XP ON HZ.PCID = XP.ID " +
                " LEFT JOIN  (SELECT SPS.PROSETNAME ,SPSD.PROJECTID FROM SYS_PROJECT_SET_DETAIL SPSD" +
                " LEFT JOIN  SYS_PROJECT_SET SPS ON SPSD.PROJECTSETID=SPS.ID WHERE SPS.TYPE='297') SPST ON SPST.PROJECTID=HZ.PCID" +
                " WHERE HZ.ID = ?";
        jdbcUtilUse.getJdbcTemplate().update(smsSql, hzid);
        String smsRecordSql = "INSERT INTO SMS_RECORD(ID,DATAID,LRR,LRSJ,TEMPLATEID) VALUES(?,?,'系统自发',SYSDATE,'14474')";
        jdbcUtilUse.getJdbcTemplate().update(smsRecordSql, czid, hzid);
        return "true";
    }

    /**
     * 房号与配套设施等分配结果信息推送 13699 -- 14475
     *
     * @param hzid 户主id
     * @return
     */
    public String xfFYinfoPush(String hzid) {
        // 操作ID
        int czid = jdbcUtilUse.getJdbcTemplate().queryForObject("SELECT SEQ_SMS.NEXTVAL FROM DUAL", Integer.class);
        String smsSql = "INSERT INTO SMS_NOTIFICATION(ID,DATAID,TEMPLATEID,PHONE,STATE,CZID,CONTENT,LRSJ)" +
                "SELECT SEQ_SMS.NEXTVAL,ID,'14475',PHONENUM,0," + czid + "," +
                "'{\"name\":\"' || HZXM || '\",\"dpcmc\":\"' || SPST.PROSETNAME || '\",\"result\":\"' || (SELECT replace(wmsys.wm_concat(to_char(JQ.NAME || '小区-' || FL.LOUDONG || '-' || FY.DANYUAN || '-' || FY.FANGHAO)), ',', '、') wz" +
                " FROM XF_HUZHUINFO HZ" +
                " LEFT JOIN FY_FANGYUANINFO FY" +
                " ON FY.HZID = HZ.ID" +
                " LEFT JOIN FY_LOUDONGINFO FL" +
                " ON FY.LDID = FL.ID" +
                " LEFT JOIN FY_ENUM ZT" +
                " ON FL.ZT = ZT.ID" +
                " LEFT JOIN FY_ENUM JQ" +
                " ON FL.JQ = JQ.ID" +
                " WHERE HZ.ID = ?)  || '\"}',SYSDATE" +
                " FROM (SELECT ID,PHONENUM,HZXM,PCID FROM XF_HUZHUINFO UNION ALL SELECT ID,WTRPHONENUM,HZXM,PCID FROM XF_HUZHUINFO WHERE WTRPHONENUM IS NOT NULL) XH" +
                " LEFT JOIN  (SELECT SPS.PROSETNAME ,SPSD.PROJECTID FROM SYS_PROJECT_SET_DETAIL SPSD" +
                " LEFT JOIN  SYS_PROJECT_SET SPS ON SPSD.PROJECTSETID=SPS.ID WHERE SPS.TYPE='297') SPST ON SPST.PROJECTID=XH.PCID" +
                " WHERE ID=?";
        jdbcUtilUse.getJdbcTemplate().update(smsSql, hzid, hzid);
        String smsRecordSql = "INSERT INTO SMS_RECORD(ID,DATAID,LRR,LRSJ,TEMPLATEID) VALUES(?,?,'系统自发',SYSDATE,'14475')";
        jdbcUtilUse.getJdbcTemplate().update(smsRecordSql, czid, hzid);
        return "true";
    }
}
