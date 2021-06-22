package com.bobooi.watch.common.exception;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import com.bobooi.watch.common.response.SystemCodeEnum;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author bobo
 * @date 2021/3/31
 */

@Getter
public class ApplicationException extends RuntimeException{
    /**
     * 匹配紧闭的{}
     * 前面带有\时不匹配 TODO 改为带有奇数个\时不匹配
     */
    private static final String TEMPLATE_REGEX = "(?<!\\\\)\\{}";
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile(TEMPLATE_REGEX);

    /**
     * 响应码
     */
    private SystemCodeEnum systemCode;
    /**
     * 接口返回信息
     */
    private String responseMessage;

    public ApplicationException(SystemCodeEnum systemCode) {
        super(systemCode.getDescription());
        this.systemCode = systemCode;
        this.responseMessage = systemCode.getDescription();
    }

    public ApplicationException(SystemCodeEnum systemCode, String responseMessage) {
        super(responseMessage);
        this.systemCode = systemCode;
        this.responseMessage = responseMessage;
    }

    /**
     * @param systemCode      系统响应码
     * @param responseMessage 接口返回消息
     * @param logMessage      日志消息
     */
    private ApplicationException(SystemCodeEnum systemCode, String responseMessage, String logMessage) {
        super(logMessage);
        this.systemCode = systemCode;
        this.responseMessage = responseMessage;
    }

    private static String setTemplate(String content, Object... values) {
        return String.format(content.replaceAll(TEMPLATE_REGEX, "%s"), Arrays.stream(values).map(Object::toString).toArray());
    }

    private static String setTemplate(String content, int shiftting, Object... values) {
        return String.format(content.replaceAll(TEMPLATE_REGEX, "%s"), Arrays.stream(values).skip(shiftting).map(Object::toString).toArray());
    }

    private static int countTemplate(String content) {
        int count = 0;
        Matcher matcher = TEMPLATE_PATTERN.matcher(content);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * @param systemCode              系统响应码
     * @param responseMessageTemplate 接口返回消息模板 使用{}设置模板占位
     * @param logMessageTemplate      日志消息模板 使用{}设置模板占位
     * @param arguments               参数 先后设置到responseMessage和logMessage的模板占位中
     */
    private static ApplicationException generate(final SystemCodeEnum systemCode, final String responseMessageTemplate, final String logMessageTemplate, Object... arguments) {
        String responseMessage = Optional.ofNullable(responseMessageTemplate).map(msg -> setTemplate(msg, arguments)).orElse("");
        String logMessage = Optional.ofNullable(logMessageTemplate)
                .filter(StringUtils::isNotBlank)
                .map(msg -> setTemplate(msg, countTemplate(responseMessageTemplate), arguments))
                .orElse(responseMessage);
        return new ApplicationException(systemCode, responseMessage, logMessage);
    }

    /**
     * 生成以响应码默认描述作为响应消息的异常
     *
     * @see #generate(SystemCodeEnum, String, String, Object...)
     */
    public static ApplicationException withResponse(SystemCodeEnum systemCode) {
        return withResponse(systemCode, systemCode.getDescription());
    }


    /**
     * 生成带响应消息的异常
     *
     * @see #generate(SystemCodeEnum, String, String, Object...)
     */
    public static ApplicationException withResponse(SystemCodeEnum systemCode, String responseMessage, Object... arguments) {
        return generate(systemCode, responseMessage, "", arguments);
    }

    /**
     * 生成带响应消息和日志消息的异常
     *
     * @see #generate(SystemCodeEnum, String, String, Object...)
     */
    public static ApplicationException withResponseAndLog(SystemCodeEnum systemCode, String responseMessage, String logMessage, Object... arguments) {
        return generate(systemCode, responseMessage, logMessage, arguments);
    }

    /**
     * 生成带响应消息和日志消息的异常，且日志消息包含响应消息（拼在前面）
     *
     * @apiNote 如果responseMessage中有模板占位，arguments中需要为logMessage多设置一份
     * @see #generate(SystemCodeEnum, String, String, Object...)
     */
    public static ApplicationException withResponseAndParamLog(SystemCodeEnum systemCode, String responseMessage, String logParams, Object... arguments) {
        return generate(systemCode, responseMessage, responseMessage + " " + logParams, arguments);
    }

    /**
     * 生成带日志消息的异常
     *
     * @apiNote 如果systemCode.description带有{}，需要在arguments中给出
     * @see #generate(SystemCodeEnum, String, String, Object...)
     */
    public static ApplicationException withLog(SystemCodeEnum systemCode, String logMessage, Object... arguments) {
        return generate(systemCode, systemCode.getDescription(), logMessage, arguments);
    }

}
