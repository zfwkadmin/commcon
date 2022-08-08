package com.zqazfl.common.utils.encrypt;

import com.zqazfl.common.utils.StringUtil;

import java.util.*;
import java.util.Map.Entry;

//import com.founder.fix.apputil.util.MD5;


public class SignUtil {
	
//	/**
//	 * 后台参数加密方法
//	 * @param param 需要加密的请求参数
//	 * @param offAES 是否启用AES加密
//	 * @return
//	 * @throws Exception
//	 */
//	public static Map<String, Object> addSign(Map<String,Object> param) throws Exception{
//		String appkey= StringUtil.getString(AppInfo.getSystemConfig("appKey"));
//		String secret=StringUtil.getString(AppInfo.getSystemConfig("secret"));
//		Map<String,Object> newparam=new HashMap<String, Object>();
//		if(param!=null){
//			newparam.putAll(param);
//		}
//		newparam.put("_app_key", appkey);
//		Date date=new Date();
//		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String timestamp=sdf.format(date);
//		newparam.put("_timestamp", timestamp);
//		String _sign=creatSign(newparam, secret);
//		newparam.put("_sign", _sign);
//
//		return newparam;
//	}
	
	
	/**
	 * 拼装签名  xl 2017-12-18
	 * @param map 所有参数
	 * @param serect 开发者识别号
	 * @return
	 * @throws Exception
	 */
	public static String creatSign(Map<String, Object> map, String serect) throws Exception{
		//获取主要参数拼装签名
		String Signstr="";
		String sign="";
		Signstr+=serect;
		
		//参数排序数组
		List<String> sortList=new ArrayList<String>();
		for(Entry<String, Object> entry : map.entrySet()){
			String key=StringUtil.getString(entry.getKey());
			if(!StringUtil.isEqual(key, "_sign")){
				sortList.add(key);
			}
		}
		Collections.sort(sortList,new Comparator<String>()  
				{
			  
		            public int compare(String o1, String o2){  
		            	return o1.compareTo(o2);  
			        }  
		        }
		);
		for(int i=0;i<sortList.size();i++){
			String value=StringUtil.getString(map.get(sortList.get(i)));
			Signstr+=StringUtil.getString(sortList.get(i))+value;
		}
		Signstr+=serect;
		sign=MD5Util.getMD5(Signstr.getBytes("UTF-8"));
		return sign;
	}
	
}
