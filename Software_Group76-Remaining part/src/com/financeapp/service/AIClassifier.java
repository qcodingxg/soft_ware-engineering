package com.financeapp.service;

import com.financeapp.model.Transaction;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.regex.Pattern;

public class AIClassifier {
    private Map<String, List<Pattern>> categoryPatterns;
    private Map<String, String> userCorrections;
    private static final String CORRECTIONS_FILE = "corrections.log";

    public AIClassifier() {
        categoryPatterns = new HashMap<>();
        userCorrections = new HashMap<>();
        initDefaultPatterns();
        loadCorrections();
    }

    private void initDefaultPatterns() {
        // 基础分类规则
        addPattern("Food", Arrays.asList(
            "restaurant", "cafe", "food", "dining", "meal", "takeout", "外卖", "餐厅", "食堂", "超市", "菜市场"
        ));
        addPattern("Transportation", Arrays.asList(
            "uber", "taxi", "subway", "bus", "train", "flight", "地铁", "公交", "打车", "高铁", "飞机"
        ));
        addPattern("Shopping", Arrays.asList(
            "store", "shop", "mall", "amazon", "taobao", "jd", "pinduoduo", "淘宝", "京东", "拼多多", "商场"
        ));
        addPattern("Entertainment", Arrays.asList(
            "movie", "game", "concert", "sports", "娱乐", "电影", "游戏", "演唱会", "运动"
        ));
        addPattern("Utilities", Arrays.asList(
            "electricity", "water", "gas", "internet", "phone", "水电", "燃气", "网络", "话费"
        ));

        // 中国本地化金融场景
        addPattern("Housing", Arrays.asList(
            "rent", "mortgage", "property", "housing", "房租", "房贷", "物业", "装修"
        ));
        addPattern("Investment", Arrays.asList(
            "stock", "fund", "investment", "trading", "股票", "基金", "理财", "投资"
        ));
        addPattern("Insurance", Arrays.asList(
            "insurance", "medical", "health", "life", "保险", "医疗", "健康", "人寿"
        ));
        addPattern("Education", Arrays.asList(
            "school", "education", "training", "course", "学校", "教育", "培训", "课程"
        ));
        addPattern("Healthcare", Arrays.asList(
            "hospital", "clinic", "medicine", "pharmacy", "医院", "诊所", "药品", "药店"
        ));
        addPattern("Gift", Arrays.asList(
            "gift", "present", "red packet", "红包", "礼物", "礼金"
        ));
        addPattern("Social", Arrays.asList(
            "social", "party", "meeting", "社交", "聚会", "应酬"
        ));
    }

    private void addPattern(String category, List<String> keywords) {
        List<Pattern> patterns = new ArrayList<>();
        for (String keyword : keywords) {
            patterns.add(Pattern.compile("(?i).*" + keyword + ".*"));
        }
        categoryPatterns.put(category, patterns);
    }

    public String classify(Transaction transaction) {
        // 如果已经有分类，直接返回
        if (transaction.getCategory() != null && !transaction.getCategory().isEmpty()) {
            return transaction.getCategory();
        }

        String description = transaction.getDescription().toLowerCase();
        
        // 检查用户修正
        String correctedCategory = userCorrections.get(description);
        if (correctedCategory != null) {
            return correctedCategory;
        }

        // 应用分类规则
        for (Map.Entry<String, List<Pattern>> entry : categoryPatterns.entrySet()) {
            for (Pattern pattern : entry.getValue()) {
                if (pattern.matcher(description).matches()) {
                    return entry.getKey();
                }
            }
        }

        // 检查节日期间
        LocalDate date = transaction.getDate();
        if (isHolidayPeriod(date)) {
            return "Holiday";
        }

        return "Uncategorized";
    }

    private boolean isHolidayPeriod(LocalDate date) {
        // 2024年春节
        LocalDate springFestivalStart = LocalDate.of(2024, Month.FEBRUARY, 10);
        LocalDate springFestivalEnd = LocalDate.of(2024, Month.FEBRUARY, 17);
        
        // 2024年清明节
        LocalDate qingmingStart = LocalDate.of(2024, Month.APRIL, 4);
        LocalDate qingmingEnd = LocalDate.of(2024, Month.APRIL, 6);
        
        // 2024年劳动节
        LocalDate laborDayStart = LocalDate.of(2024, Month.MAY, 1);
        LocalDate laborDayEnd = LocalDate.of(2024, Month.MAY, 5);
        
        // 2024年端午节
        LocalDate dragonBoatStart = LocalDate.of(2024, Month.JUNE, 10);
        LocalDate dragonBoatEnd = LocalDate.of(2024, Month.JUNE, 12);
        
        // 2024年中秋节
        LocalDate midAutumnStart = LocalDate.of(2024, Month.SEPTEMBER, 15);
        LocalDate midAutumnEnd = LocalDate.of(2024, Month.SEPTEMBER, 17);
        
        // 2024年国庆节
        LocalDate nationalDayStart = LocalDate.of(2024, Month.OCTOBER, 1);
        LocalDate nationalDayEnd = LocalDate.of(2024, Month.OCTOBER, 7);

        return (date.isAfter(springFestivalStart) && date.isBefore(springFestivalEnd)) ||
               (date.isAfter(qingmingStart) && date.isBefore(qingmingEnd)) ||
               (date.isAfter(laborDayStart) && date.isBefore(laborDayEnd)) ||
               (date.isAfter(dragonBoatStart) && date.isBefore(dragonBoatEnd)) ||
               (date.isAfter(midAutumnStart) && date.isBefore(midAutumnEnd)) ||
               (date.isAfter(nationalDayStart) && date.isBefore(nationalDayEnd));
    }

    public void recordCorrection(String description, String category) {
        userCorrections.put(description.toLowerCase(), category);
        saveCorrections();
    }

    private void loadCorrections() {
        // 实现从文件加载用户修正的逻辑
    }

    private void saveCorrections() {
        // 实现保存用户修正到文件的逻辑
    }
} 