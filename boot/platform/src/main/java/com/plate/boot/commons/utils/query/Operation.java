package com.plate.boot.commons.utils.query;

import lombok.Getter;

/**
 * 这是一个表示操作的类。
 * <p>
 * 该类可能包含执行特定操作的方法。
 * 例如，它可能包含数学运算、数据操作或其他类型的处理。
 * 这里没有具体的实现细节，因为代码片段没有提供足够的信息。
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 * @version 1.0
 */
@Getter
public enum Operation {
    EQ("="),
    EQUAL("="),
    AFTER(">"),
    GREATER_THAN_EQUAL(">="),
    GTE(">="),
    GREATER_THAN(">"),
    GT(">"),
    BEFORE("<"),
    LESS_THAN_EQUAL("<="),
    LTE("<="),
    LESS_THAN("<"),
    LT("<"),
    BETWEEN("between"),
    NOT_BETWEEN("not between"),
    NOT_IN("not in"),
    IN("in"),
    IS_NOT_NULL("is not null"),
    NOT_NULL("is not null"),
    IS_NULL("is null"),
    NULL("is null"),
    NOT_LIKE("not like"),
    LIKE("like"),
    STARTING_WITH("like '%s'"),
    ENDING_WITH("like '%s'"),
    IS_NOT_LIKE("not like"),
    CONTAINING("like '%s'"),
    NOT_CONTAINING("not like"),
    NOT("!="),
    IS_TRUE("is true"),
    TRUE("is true"),
    IS_FALSE("is false"),
    FALSE("is false");

    private final String sqlOperator;

    Operation(String sqlOperator) {
        this.sqlOperator = sqlOperator;
    }

}
