/*
 * @(#) SendOtp.java 2020-11-03
 *
 * Copyright 2020 NetEase.com, Inc. All rights reserved.
 */

package com.zqazfl.common.utils.PhoneVerify;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 短信发送示例
 *
 * @version 2020-11-03
 * @formatter:off 发生不同类型的短信本质上是一样的。都是通过同一个接口发生，参数大同小异。
 * 核心参数说明：
 * · 业务ID：根据自己的业务需求选择匹配的业务。如，你需要发送一条国内验证码短信，则选择国内验证短信业务的ID。可以登录易盾官网查看已开通的业务。
 * · 模板ID：事先创建好短信模板，且已通过审核。模板必须与所选业务匹配。你可以在创建模板的时候选择其所属业务。
 * · 收信方号码：国内手机号通常是11位数字。国际号码则需要额外指定国际电话区号。
 *
 * 扩展参数说明：
 * · 模板变量：你可以在创建模板时，在其内容中添加占位符来表示变量。如，验证码短信模板中，可以添加 ${code}，并在发送短信时提供此变量的值。
 * · 国际电话区号：发送国际短信时需要提供此参数（国内短信不提供）。如，美国是1，英国是44，法国是33，俄罗斯是79。注：不要将此区号作为收信方号码的前缀。
 * @formattor:on
 */
public class Send {
    public static final String URI_SEND_SMS = "https://sms.dun.163.com/v2/sendsms";
    public static final String URI_VERIFY_OTP = "https://sms.dun.163.com/v2/verifysms";

    /**
     * SECRET_ID 和 SECRET_KEY 是产品密钥。可以登录易盾官网找到自己的凭证信息。请妥善保管，避免泄露。
     */
//    private static String SECRET_ID = "206d94d0112c5e99401e74fad67b1f72";
//    private static String SECRET_KEY = "a7318a810c04f6df742132a501361150";
    public static String SECRET_ID = "2b2bf472df146ef1aff6d70d0f8ae535";
    public static String SECRET_KEY = "61d9841f50ce3f46e06758fe892fb10c";
    public static String YZM_BUSINESSID = "e464ad54cfea4be7b01ec56e52ff9b0f";
    public static String TZ_BUSINESSID = "6c03d1ea557c4a4b933e16f1dbf2790c";

    /**
     * 示例：发送一条验证码短信
     */
    public static void sendOtp() {
        // 这是你的 国内验证码短信 业务的ID。可以登录易盾官网查看此业务ID。
        String businessId = YZM_BUSINESSID;
        // 这是你事先创建好的模板，且已通过审核。
        String templateId = "xxxxx";
        // 这是你自己的业务系统生成的验证码。如果你希望由易盾生成验证码，并通过验证码校验接口来验证，请参考 OptVerifyDemo。
        String code = "123456";
        // 这是收信方号码。如，134开头的号码一般是中国移动的号码。
        String to = "xxxxxxxxxxx";

        // 此处假设你的模板中只有验证码这一个变量，且变量名为 code。如，模板内容为 “您的验证码为${code}，5分钟内有效，请勿泄露。”
        Map<String, String> variables = new HashMap<>(4);
        variables.put("code", code);

        Map<String, String> param = createParam(businessId, templateId, variables, to);
        SendResponse response = send(param);

        System.out.println("response: " + response);
    }

    /**
     * 示例：发送一条通知短信
     */
    public static SendResponse sendArn(String templateId, String to, String content) {
        // 这是你的 国内通知类短信 业务的ID。可以登录易盾官网查看此业务ID。
//        String businessId = "e9505ab1c5b84a608b40d49ced6060ad";
        String businessId = TZ_BUSINESSID;

        // 这是你事先创建好的模板，且已通过审核。
        // String templateId = "13397";
        // 这是收信方号码。如，134开头的号码一般是中国移动的号码。
        // String to = "15735107327";

        // 此处假设你的模板中只有orderId这一个变量。如，模板内容为 “您的订单已发货，订单号为 ${orderId}。”
        Map<String, String> variables = new HashMap<>();

        Map maps = JSON.parseObject(content);
        for (Object obj : maps.keySet()){
            variables.put(obj.toString(), maps.get(obj).toString());
        }
        System.out.println(variables);

        // variables.put("orderId", "1020304050");
        // variables.put("name","段壮");
        // variables.put("startTime","2021年01月20日");
        // variables.put("endTime","2020年01月23日");
        // System.out.println(variables);

        Map<String, String> param = createParam(businessId, templateId, variables, to);
        System.out.println(variables);
        SendResponse response = send(param);
        System.out.println("response: " + response);
        return response;
    }

    /**
     * 示例：发送一条营销短信
     */
    public static void sendMkt() {
        // 这是你的 国内营销类短信 业务的ID。可以登录易盾官网查看此业务ID。
        String businessId = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
        // 这是你事先创建好的模板，且已通过审核。
        String templateId = "xxxxx";
        // 这是收信方号码。如，134开头的号码一般是中国移动的号码。
        String to = "xxxxxxxxxxx";

        // 此处假设你的模板没有变量。如，模板内容为 “即日起至12月底，每日登录APP签到，就有机会参与新年抽奖活动。”
        Map<String, String> variables = Collections.emptyMap();

        Map<String, String> param = createParam(businessId, templateId, variables, to);
        SendResponse response = send(param);

        System.out.println("response: " + response);
    }

    /**
     * 示例：发送一条国际验证码短信
     */
    public static void sendInternationalOtp() {
        // 这是你的 国际验证码短信 业务的ID。可以登录易盾官网查看此业务ID。
        String businessId = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
        // 这是你事先创建好的模板，且已通过审核。
        String templateId = "xxxxx";
        // 这是你自己的业务系统生成的验证码。如果你希望由易盾生成验证码，并通过验证码校验接口来验证，请参考 OptVerifyDemo。
        String code = "123456";
        // 这是收信方号码。不包含国际电话区号。
        String to = "xxxxxxxxxxx";
        // 这是收信方的国际电话区号。如，美国是1，英国是44，法国是33，俄罗斯是79。
        String countryCallingCode = "1";

        // 此处假设你的模板中只有验证码这一个变量，且变量名为 code。如，模板内容为 “Your verification code is ${code}, valid for 5 minutes.”
        Map<String, String> variables = new HashMap<>(4);
        variables.put("code", code);

        Map<String, String> param = createParamForInternational(
                businessId, templateId, variables, to, countryCallingCode);
        SendResponse response = send(param);

        System.out.println("response: " + response);
    }

    private static SendResponse send(Map<String, String> params) {
        return RequestUtils.postForEntity(URI_SEND_SMS, params, SendResponse.class);
    }

    /**
     * 构建国内短信的请求参数
     *
     * @param businessId 业务ID
     * @param templateId 短信模板ID。你需要先在易盾官网创建模板并通过审核后才能使用。模板ID需要与业务ID匹配。即，该模板属于目标业务。
     * @param variables  短信模板中的变量值。如，你的模板内容为 “您的验证码为${code}，有效期${time}分钟。”，则此参数需指明 code 和 time 的值。
     * @param to         收信方的号码。如，134开头的号码一般是中国移动的号码。
     */
    private static Map<String, String> createParam(
            String businessId, String templateId, Map<String, String> variables, String to) {
        return createSendParam(businessId, templateId, variables, to, null);
    }

    /**
     * 构建国际短信的请求参数
     *
     * @param businessId         业务ID
     * @param templateId         短信模板ID。你需要先在易盾官网创建模板并通过审核后才能使用。模板ID需要与业务ID匹配。即，该模板属于目标业务。
     * @param variables          短信模板中的变量值。如，你的模板内容为 “Your verification code is ${code}, valid for ${time} minutes.”，则此参数需指明 code
     *                           和 time 的值。
     * @param to                 收信方的号码。不包含国际电话区号。
     * @param countryCallingCode 收信方号码的国际电话区号。如，美国是1，英国是44，法国是33，俄罗斯是79。
     */
    private static Map<String, String> createParamForInternational(
            String businessId, String templateId, Map<String, String> variables, String to, String countryCallingCode) {
        return createSendParam(businessId, templateId, variables, to, countryCallingCode);
    }

    private static Map<String, String> createSendParam(
            String businessId, String templateId, Map<String, String> variables, String to, String countryCallingCode) {
        Map<String, String> params = new HashMap<>();

        params.put("nonce", ParamUtils.createNonce());
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("version", "v2");

        params.put("secretId", SECRET_ID);
        params.put("businessId", businessId);

        params.put("templateId", templateId);
        params.put("mobile", to);

        // 如果要发送国际短信，则需要指明国际电话区号。如果不是国际短信，则不要指定此参数
        if (StringUtils.isNotBlank(countryCallingCode)) {
            params.put("internationalCode", countryCallingCode);
        }

        params.put("paramType", "json");
        params.put("params", ParamUtils.serializeVariables(variables));

        // 在最后一步生成此次请求的签名
        params.put("signature", ParamUtils.genSignature(SECRET_KEY, params));

        return params;
    }
}
