package indi.eiriksgata.dice.operation.impl;

import indi.eiriksgata.dice.callback.SanCheckCallback;
import indi.eiriksgata.dice.utlis.CalcUtil;
import indi.eiriksgata.dice.utlis.RegularExpressionUtils;

import java.util.List;

/**
 * @author: create by Keith
 * @version: v1.0
 * @description: indi.eiriksgata.dice.operation.impl
 * @date:2020/10/28
 **/
public class SanCheckImpl {


    //本方法需要采取回调的形式使用
    public void sanCheck(String text, String attribute, SanCheckCallback callback) {

        //筛选出检测属性 san
        String sanAttribute = RegularExpressionUtils.getMatcher("san[0-9]+", attribute);
        int sanNumber = Integer.valueOf(sanAttribute.substring("san".length()));

        //分割两个不同的计算方案
        String[] formula = text.split("/");

        //coc 的理智判定 默认百面骰子
        int random = RollBasicsImpl.createRandom(1, 100)[0];
        //检测成功
        if (random <= sanNumber) {
            //理智判定默认百面骰
            new RollBasicsImpl().rollRandom(formula[0], 0L, (value, calculationProcess) -> {
                int surplus = sanNumber - value;
                String resultAttribute = attribute.replaceFirst(sanAttribute, "san" + surplus);
                callback.getResultData(resultAttribute, random, sanNumber, calculationProcess, surplus);

            });
            return;
        }

        //检测大失败
        if (random == 100) {
            //计算大失败的数值
            List<String> regexResult = RegularExpressionUtils.getMatchers("[0-9]?[Dd][0-9]?", formula[1]);
            for (String item : regexResult) {
                if (item.substring(0, 1).equals("D") || item.substring(0, 1).equals("d")) {
                    if (item.length() == 1) {
                        text = text.replaceFirst(item, "100");
                    } else {
                        text = text.replaceFirst(item, item.substring(1));
                    }
                } else {
                    String itemValue = RegularExpressionUtils.getMatcher("[Dd][0-9]+", item);
                    text = text.replaceFirst(item, itemValue.substring(1));
                }
            }
            String formulaResult = new CalcUtil(text).toString();
            String calProcess = text + "=" + formulaResult;
            int surplus = sanNumber - Integer.valueOf(formulaResult);
            String resultAttribute = attribute.replaceFirst(sanAttribute, "san" + surplus);
            callback.getResultData(resultAttribute, random, sanNumber, calProcess, surplus);
        } else {
            //理智判定默认百面骰
            new RollBasicsImpl().rollRandom(formula[1], 0L, (value, calculationProcess) -> {
                int surplus = sanNumber - value;
                String resultAttribute = attribute.replaceFirst(sanAttribute, "san" + surplus);
                callback.getResultData(resultAttribute, random, sanNumber, calculationProcess, surplus);

            });
        }


    }

}

