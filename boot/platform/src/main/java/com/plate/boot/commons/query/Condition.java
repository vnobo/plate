package com.plate.boot.commons.query;

import com.google.common.base.CaseFormat;
import com.plate.boot.commons.utils.DatabaseUtils;
import lombok.Getter;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.data.util.Pair;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Getter
public class Condition extends HashMap<String, Object> {

    private final @Nullable Criteria criteria;
    private final String prefix;
    private final StringBuilder sqlBuilder = new StringBuilder();

    private Condition(@Nullable Criteria criteria, String prefix) {
        this.criteria = criteria;
        this.prefix = prefix;
        unroll(this.criteria, sqlBuilder);
    }

    public static Condition of(Criteria criteria) {
        return new Condition(criteria, "");
    }

    public static Condition of(Criteria criteria, String prefix) {
        return new Condition(criteria, prefix);
    }

    /**
     * Generates the WHERE clause of the SQL statement.
     *
     * <p>This method generates the WHERE clause based on the conditions added to the QueryFragment.
     *
     * @return the WHERE clause as a String
     */
    public String toSql() {
        return sqlBuilder.toString();
    }

    private void unroll(CriteriaDefinition criteria, StringBuilder stringBuilder) {

        CriteriaDefinition current = criteria;

        // reverse unroll criteria chain
        Map<CriteriaDefinition, CriteriaDefinition> forwardChain = new HashMap<>();

        while (current.hasPrevious()) {
            forwardChain.put(current.getPrevious(), current);
            current = current.getPrevious();
        }

        // perform the actual mapping
        render(current, stringBuilder);
        while (forwardChain.containsKey(current)) {

            CriteriaDefinition criterion = forwardChain.get(current);

            if (criterion.getCombinator() != CriteriaDefinition.Combinator.INITIAL) {
                stringBuilder.append(' ').append(criterion.getCombinator().name()).append(' ');
            }

            render(criterion, stringBuilder);

            current = criterion;
        }
    }

    private void unrollGroup(List<? extends CriteriaDefinition> criteria, StringBuilder stringBuilder) {

        stringBuilder.append("(");

        boolean first = true;
        for (CriteriaDefinition criterion : criteria) {

            if (criterion.isEmpty()) {
                continue;
            }

            if (!first) {
                CriteriaDefinition.Combinator combinator = criterion.getCombinator()
                        == CriteriaDefinition.Combinator.INITIAL ? CriteriaDefinition.Combinator.AND
                        : criterion.getCombinator();
                stringBuilder.append(' ').append(combinator.name()).append(' ');
            }

            unroll(criterion, stringBuilder);
            first = false;
        }

        stringBuilder.append(")");
    }

    private void render(CriteriaDefinition criteria, StringBuilder stringBuilder) {
        if (criteria.isEmpty()) {
            return;
        }

        if (criteria.isGroup()) {
            unrollGroup(criteria.getGroup(), stringBuilder);
            return;
        }
        var column = Objects.requireNonNull(criteria.getColumn()).toSql(IdentifierProcessing.NONE);

        if (this.prefix != null && !this.prefix.isEmpty()) {
            column = this.prefix + "." + column;
        }
        // 优化列名替换逻辑：合并连续下划线并去除首尾下划线
        var relColumn = column.replaceAll("[^\\p{Alnum}]+", "_").replaceAll("^_|_$", "");
        stringBuilder.append(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column))
                .append(' ').append(Objects.requireNonNull(criteria.getComparator()).getComparator());

        switch (criteria.getComparator()) {
            case BETWEEN:
            case NOT_BETWEEN:
                Pair<Object, Object> pair = (Pair<Object, Object>) criteria.getValue();
                var key1 = relColumn + "1";
                var key2 = relColumn + "2";
                if (pair != null) {
                    this.add(key1, pair.getFirst());
                    this.add(key2, pair.getSecond());
                }
                stringBuilder.append(" :").append(key1).append(" AND :").append(key2);
                break;

            case IS_NULL:
            case IS_NOT_NULL:
            case IS_TRUE:
            case IS_FALSE:
                break;

            case IN:
            case NOT_IN:
                stringBuilder.append(" (");
                Collection<?> values = (Collection<?>) criteria.getValue();
                if (values != null && !values.isEmpty()) {
                    StringJoiner joiner = new StringJoiner(", ");
                    AtomicInteger indexRef = new AtomicInteger(0);
                    for (Object item : values) {
                        var key = relColumn + indexRef.get();
                        this.add(key, item);
                        joiner.add(":" + key);
                        indexRef.incrementAndGet();
                    }
                    stringBuilder.append(joiner);
                }
                stringBuilder.append(')');
                break;

            default:
                // 优化null处理逻辑
                Object value = criteria.getValue();
                if (value == null) {
                    throw new IllegalArgumentException("Criteria value cannot be null for column: " + relColumn);
                }
                this.add(relColumn, value);
                stringBuilder.append(" :").append(relColumn);
        }
    }

    private void add(String column, Object value) {
        if (value == null) {
            return;
        }
        var k = TypeInformation.of(value.getClass());
        var v = DatabaseUtils.R2DBC_CONVERTER.writeValue(value, k);
        this.put(column, v);
    }

}
