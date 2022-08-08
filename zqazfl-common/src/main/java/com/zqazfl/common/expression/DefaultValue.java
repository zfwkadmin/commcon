package com.zqazfl.common.expression;

import com.zqazfl.common.utils.JdbcUtilUse;
import com.zqazfl.common.utils.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component("Exp_DefaultData")
public class DefaultValue {
    @Resource(name = "JdbcUtilImpl")
    JdbcUtilUse jdbcUtilUse;
    /**
     *
     * @param seq 获取主键序列
     * @return
     */
    public  String getSeq(String seq){
        String id = jdbcUtilUse.getJdbcTemplate().queryForObject("select "+seq+".nextval from dual ",String.class);// 主表id
        return  id;
    }

    /**
     * 获取日期
     * @param column
     * @return
     */
    public String getDate(String column){
        Date date = new Date();
        SimpleDateFormat sdf1 = null;
        if(StringUtil.getString(column).equals("now")){
            //sdf1 =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");[YYYYescape] YYYY-MM-DDTHH:mm:ssZ[Z]
            sdf1 =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        }else if(StringUtil.getString(column).equals("day")){
            sdf1 =new SimpleDateFormat("yyyy-MM-dd");
        }
        String returnDate = sdf1.format(date);
        return returnDate;
    }

}
