package com.overpass.ServiceBridge.constans;

import lombok.Getter;

public class PublicConstants {

    public static final String NEXNEO_WEB_GOODS_UPDATE_FLAG = "nexneo_web_goods_update_flag-";


    /**
    　* 是否标志
    　* @author wqb
    　* @date 2024-4-17 16:11:14
    　*/
    public enum Method {
        GET(0, "GET"),
        POST(1, "POST");

        @Getter
        private Integer code;

        @Getter
        private String name;

        Method(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        //通过code获取描述
        public static String getName(Integer code) {
            Method[] values = Method.values();
            for (int i = 0; i < values.length; i++) {
                Method be = values[i];
                if (be.code.equals(code)) {
                    return be.name;
                }
            }
            return "";
        }

        //通过描述获取code
        public static int getCode(String name) {
            Method[] values = Method.values();
            for (int i = 0; i < values.length; i++) {
                Method be = values[i];
                if (be.name.equalsIgnoreCase(name)) {
                    return be.code;
                }
            }
            return 0;
        }

        //通过 code 获取枚举对象
        public static Method getEnumByCode(Integer code) {
            for (Method flagEnum : Method.values()) {
                if (code.equals(flagEnum.getCode())) {
                    return flagEnum;
                }
            }
            return null;
        }

        //通过 name 获取枚举对象
        public static Method getEnumByName(String name) {
            for (Method flagEnum : Method.values()) {
                if (name.toLowerCase().equals(flagEnum.getName().toLowerCase())) {
                    return flagEnum;
                }
            }
            return null;
        }

    }


    /**
    　* 是否标志
    　* @author ShenPS
    　* @date 2021/9/28 16:53
    　*/
    public enum YesOrNo {
        NO(0, "否"),
        YES(1, "是");

        @Getter
        private Integer code;

        @Getter
        private String name;

        YesOrNo(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        //通过code获取描述
        public static String getName(Integer code) {
            YesOrNo[] values = YesOrNo.values();
            for (int i = 0; i < values.length; i++) {
                YesOrNo be = values[i];
                if (be.code.equals(code)) {
                    return be.name;
                }
            }
            return "";
        }

        //通过描述获取code
        public static int getCode(String name) {
            YesOrNo[] values = YesOrNo.values();
            for (int i = 0; i < values.length; i++) {
                YesOrNo be = values[i];
                if (be.name.equalsIgnoreCase(name)) {
                    return be.code;
                }
            }
            return 0;
        }

        //通过code获取枚举对象
        public static YesOrNo getEnumByCode(Integer code) {
            for (YesOrNo flagEnum : YesOrNo.values()) {
                if (code.equals(flagEnum.getCode())) {
                    return flagEnum;
                }
            }
            return null;
        }

    }

    /**
     * @program: settlement
     * @description: 数据解析格式
     * @author: wqb
     * @create: 2021-11-23 13:47:47
     */
    public enum ExplainStyleEnum {

        STYLE_DATE(1,"日期处理：1-yyyy-MM-dd"),
        STYLE_DATETIME(2,"日期处理：2-yyyy-MM-dd hh:mm:ss"),
        STYLE_2_HALF_UP(3,"数值处理：3-四舍五入保留2位小数"),
        STYLE_2_DOWN(4,"数值处理：4-保留2位其余截去");

        /** 码值  **/
        @Getter
        private Integer code;

        /** 描述 **/
        @Getter
        private String name;

        ExplainStyleEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        //通过描述获取code
        public static int getCode(String name){
            ExplainStyleEnum[] values = ExplainStyleEnum.values();
            for(int i=0;i<values.length;i++){
                ExplainStyleEnum be = values[i];
                if(be.name.equalsIgnoreCase(name)){
                    return be.code;
                }
            }
            return 0;
        }

        //通过code获取描述
        public static String getName(Integer code){
            ExplainStyleEnum[] values = ExplainStyleEnum.values();
            for(int i=0;i<values.length;i++){
                ExplainStyleEnum be = values[i];
                if(be.code.equals(code)){
                    return be.name;
                }
            }
            return "";
        }

        public static ExplainStyleEnum getEnumByCode(Integer code) {
            for (ExplainStyleEnum flagEnum : ExplainStyleEnum.values()) {
                if (code.equals(flagEnum.getCode())) {
                    return flagEnum;
                }
            }
            return null;
        }
    }


    /**
     * @Description 数据拉取周期
     * @Author wqb
     * @CreateTime 2021-10-20 09:23
     * @Version 1.0
     **/
    public enum FeeBuildCycleEnum {
        DAILY_CURRENT(1,    "按日：[日初-日末]", "如 beginTime:'2022-04-01 00:00:00'；endTime:'2022-04-01 23:59:59'"),
        DAILY_NEXT(2,       "按日：[日初-次日初]", "如 beginTime:'2022-04-01 00:00:00'；endTime:'2022-04-02 00:00:00'"),
        MONTHLY_CURRENT(3,  "按月：[月初-月末]", "如 beginTime:'2022-04-01 00:00:00'；endTime:'2022-04-30 23:59:59'"),
        MONTHLY_NEXT(4,     "按月：[月初-下月初]", "如 beginTime:'2022-04-01 00:00:00'；endTime:'2022-05-01 00:00:00'");


        /** 码值 **/
        @Getter
        private Integer code;

        /** 描述 **/
        @Getter
        private String name;
        @Getter
        private String desc;

        FeeBuildCycleEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
            this.desc = name;
        }

        FeeBuildCycleEnum(Integer code, String name, String desc) {
            this.code = code;
            this.name = name;
            this.desc = desc;
        }

        //通过描述获取code
        public static int getCode(String name){
            FeeBuildCycleEnum[] values = FeeBuildCycleEnum.values();
            for(int i=0;i<values.length;i++){
                FeeBuildCycleEnum be = values[i];
                if(be.name.equalsIgnoreCase(name)){
                    return be.code;
                }
            }
            return 0;
        }


        //通过code获取描述
        public static String getName(Integer code){
            FeeBuildCycleEnum[] values = FeeBuildCycleEnum.values();
            for(int i=0;i<values.length;i++){
                FeeBuildCycleEnum be = values[i];
                if(be.code.equals(code)){
                    return be.name;
                }
            }
            return "";
        }

        public static FeeBuildCycleEnum getEnumByCode(Integer code) {
            for (FeeBuildCycleEnum flagEnum : FeeBuildCycleEnum.values()) {
                if (code.equals(flagEnum.getCode())) {
                    return flagEnum;
                }
            }
            return null;
        }
    }

    /**
     　* 处理标志
     　* @author wqb
     　* @date 2022-2-10 09:29:32
     　*/
    public enum HandledFlagEnum {
        USELESS(0, "无需处理"),
        NO(1, "未处理"),
        FINISH(2, "已处理");

        @Getter
        private Integer code;

        @Getter
        private String name;

        HandledFlagEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        //通过code获取描述
        public static String getName(Integer code) {
            HandledFlagEnum[] values = HandledFlagEnum.values();
            for (int i = 0; i < values.length; i++) {
                HandledFlagEnum be = values[i];
                if (be.code.equals(code)) {
                    return be.name;
                }
            }
            return "";
        }

        //通过描述获取code
        public static int getCode(String name) {
            HandledFlagEnum[] values = HandledFlagEnum.values();
            for (int i = 0; i < values.length; i++) {
                HandledFlagEnum be = values[i];
                if (be.name.equalsIgnoreCase(name)) {
                    return be.code;
                }
            }
            return 0;
        }

        //通过code获取枚举对象
        public static HandledFlagEnum getEnumByCode(Integer code) {
            for (HandledFlagEnum flagEnum : HandledFlagEnum.values()) {
                if (code.equals(flagEnum.getCode())) {
                    return flagEnum;
                }
            }
            return null;
        }

    }

    /**
     　* 异常类型
     　* @author wqb
     　* @date 2022-2-28 16:12:49
     　*/
    public enum FailTypeEnum {
        NO_CHECK(1, "check未通过"),
        NO_QUERY(2, "获取数据异常"),
        GROUP_DATA(3, "组装费用单据异常");

        @Getter
        private Integer code;

        @Getter
        private String name;

        FailTypeEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        //通过code获取描述
        public static String getName(Integer code) {
            FailTypeEnum[] values = FailTypeEnum.values();
            for (int i = 0; i < values.length; i++) {
                FailTypeEnum be = values[i];
                if (be.code.equals(code)) {
                    return be.name;
                }
            }
            return "";
        }

        //通过描述获取code
        public static int getCode(String name) {
            FailTypeEnum[] values = FailTypeEnum.values();
            for (int i = 0; i < values.length; i++) {
                FailTypeEnum be = values[i];
                if (be.name.equalsIgnoreCase(name)) {
                    return be.code;
                }
            }
            return 0;
        }

        //通过code获取枚举对象
        public static FailTypeEnum getEnumByCode(Integer code) {
            for (FailTypeEnum flagEnum : FailTypeEnum.values()) {
                if (code.equals(flagEnum.getCode())) {
                    return flagEnum;
                }
            }
            return null;
        }

    }

}