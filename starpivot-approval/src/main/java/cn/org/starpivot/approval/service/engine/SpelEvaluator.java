package cn.org.starpivot.approval.service.engine;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * SpEL 表达式求值（context 变量为审批上下文 Map）。
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
        Expression exp = parser.parseExpression(expression);
        Boolean result = exp.getValue(evalContext, Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    /** 内部常量引用，避免 engine 包依赖 constant 循环 */
    static final class ApprovalConstantsHelper {
        static boolean isDefaultExpr(String expression) {
            return "default".equalsIgnoreCase(expression.trim());
        }
    }
}
