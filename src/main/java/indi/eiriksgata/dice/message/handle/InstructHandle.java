package indi.eiriksgata.dice.message.handle;

import indi.eiriksgata.dice.vo.MessageData;
import indi.eiriksgata.dice.exception.DiceInstructException;
import indi.eiriksgata.dice.exception.ExceptionEnum;
import indi.eiriksgata.dice.injection.InstructService;
import indi.eiriksgata.dice.injection.InstructReflex;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * author: create by Keith
 * version: v1.0
 * description: indi.eiriksgata.dice.message.handle
 * date: 2020/10/15
 **/
public class InstructHandle {

    private static final ResourceBundle scanPath = ResourceBundle.getBundle("trpg-dice-config");
    private static final Reflections reflections = new Reflections(scanPath.getString("reflections.scan.path"));

    //唯一公开的调用方法
    public String instructCheck(MessageData<?> data) throws DiceInstructException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        //应当先判断传入的字符串长度和对象属性
        if (data.getMessage() == null || data.getMessage().length() < 1) {
            return trackInstructCases(data);
        }

        //先行判断是否符合指令样式
        if (data.getMessage().charAt(0) == '.' || data.getMessage().charAt(0) == '。') {
            //将所有空格舍去 移除空格应该在指令方法中去实现，这里不再作统一处理
            //data.setMessage(data.getMessage().replaceAll(" ", ""));
            return trackInstructCases(data);
        }
        throw new DiceInstructException(ExceptionEnum.DICE_INSTRUCT_NOT_FOUND);
    }

    private String trackInstructCases(MessageData<?> data) throws DiceInstructException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(InstructService.class);
        Method highestPriority = null;
        Class<?> highestClazz = null;
        String instructStr = null;
        for (Class<?> clazz : typesAnnotatedWith) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(InstructReflex.class)) {
                    String[] instructArr = method.getAnnotation(InstructReflex.class).value();
                    for (String temp : instructArr) {
                        //方法增强检测命令
                        if (instructMessageCheck(data.getMessage(), temp)) {
                            if (highestPriority == null) {
                                highestPriority = method;
                                highestClazz = clazz;
                                instructStr = temp;
                            } else {
                                InstructReflex instructReflex = method.getAnnotation(InstructReflex.class);
                                if (instructReflex.priority() > highestPriority.getAnnotation(InstructReflex.class).priority()) {
                                    highestPriority = method;
                                    highestClazz = clazz;
                                    instructStr = temp;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (highestPriority != null) {
            data.setMessage(data.getMessage().substring(instructStr.length()));
            return (String) highestPriority.invoke(highestClazz.getDeclaredConstructor().newInstance(), data);

        }

        throw new DiceInstructException(ExceptionEnum.DICE_INSTRUCT_NOT_FOUND);
    }


    //仔细的指令检测方法
    private boolean instructMessageCheck(String message, String instructStr) {
        //具体的逻辑判定 该检测主要作为 统一的指令检测 若单条指令需要作检测，请在@InstrctionReflex的方法中实现
        return message.contains(instructStr);
    }


}
