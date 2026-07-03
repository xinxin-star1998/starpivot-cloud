package cn.org.starpivot.approval.service.engine;

import cn.org.starpivot.common.exception.BizException;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * SpEL 表达式求值（context 变量为审批上下文 Map）。
 * <p>
 * 使用受限求值上下文：禁止类型引用、Bean 引用、任意方法/构造器调用，降低表达式注入风险。
 */
@Component
public class SpelEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 求值布尔表达式；表达式为空或 "default" 视为 true。
     */
    public boolean evaluateBoolean(String expression, Map<String, Object> context) {
        if (!StringUtils.hasText(expression) || ApprovalConstantsHelper.isDefaultExpr(expression)) {
            return true;
        }
        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        evalContext.setVariable("context", context != null ? context : Map.of());
        evalContext.addPropertyAccessor(new MapAccessor());
        evalContext.setTypeLocator(typeName -> {
            throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
        });
        evalContext.setMethodResolvers(Collections.emptyList());
        evalContext.setConstructorResolvers(Collections.emptyList());
        evalContext.setBeanResolver(null);
        try {
            Expression exp = parser.parseExpression(expression);
            Boolean result = exp.getValue(evalContext, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (SpelEvaluationException ex) {
            throw new BizException("审批条件表达式无效: " + ex.getMessage());
        }
    }

    /** 内部常量引用，避免 engine 包依赖 constant 循环 */
    static final class ApprovalConstantsHelper {
        static boolean isDefaultExpr(String expression) {
            return "default".equalsIgnoreCase(expression.trim());
        }
    }
}
