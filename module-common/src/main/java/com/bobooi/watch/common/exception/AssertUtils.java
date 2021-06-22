package com.bobooi.watch.common.exception;

import com.bobooi.watch.common.response.SystemCodeEnum;

import static com.bobooi.watch.common.response.SystemCodeEnum.ARGUMENT_WRONG;

/**
 * @author bobo
 * @date 2021/3/31
 */

public class AssertUtils {
    public static void isTrue(boolean expression, ApplicationException exception) {
        if (!expression) {
            throw exception;
        }
    }

    public static void isTrue(boolean expression, SystemCodeEnum systemCode, String message) {
        if (!expression) {
            throw ApplicationException.withResponse(systemCode, message);
        }
    }

    public static void isTrue(boolean expression, SystemCodeEnum systemCode) {
        if (!expression) {
            throw ApplicationException.withResponse(systemCode);
        }
    }

    public static void isTrue(boolean expression, String argumentWrongMessage) {
        if (!expression) {
            throw ApplicationException.withResponse(ARGUMENT_WRONG, argumentWrongMessage);
        }
    }

    public static void isFalse(boolean expression, ApplicationException exception) {
        if (expression) {
            throw exception;
        }
    }

    public static void isNull(Object object, ApplicationException exception) {
        if (object != null) {
            throw exception;
        }
    }

    public static void notNull(Object object, ApplicationException exception) {
        if (object == null) {
            throw exception;
        }
    }

    public static void notNull(Object object, String argumentWrongMessage) {
        if (object == null) {
            throw ApplicationException.withResponse(ARGUMENT_WRONG, argumentWrongMessage);
        }
    }
}
