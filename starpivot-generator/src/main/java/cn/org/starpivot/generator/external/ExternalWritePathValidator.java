package cn.org.starpivot.generator.external;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.config.GenExternalProperties;
import cn.org.starpivot.generator.utils.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 服务端写盘路径校验
 */
public final class ExternalWritePathValidator {

    private ExternalWritePathValidator() {
    }

    public static Path resolveOutputRoot(String outputRoot, GenExternalProperties properties) {
        if (!properties.isWriteToDiskEnabled()) {
            throw new BizException("服务端写盘生成功能未启用");
        }
        String base = StringUtils.isNotEmpty(outputRoot)
                ? outputRoot.trim()
                : properties.getDefaultOutputRoot();
        if (StringUtils.isEmpty(base)) {
            base = System.getProperty("user.dir");
        }
        Path root = Paths.get(base).toAbsolutePath().normalize();
        assertAllowed(root, properties.getAllowedWritePaths());
        return root;
    }

    private static void assertAllowed(Path root, List<String> allowedPrefixes) {
        if (allowedPrefixes == null || allowedPrefixes.isEmpty()) {
            return;
        }
        for (String prefix : allowedPrefixes) {
            if (StringUtils.isEmpty(prefix)) {
                continue;
            }
            Path allowed = Paths.get(prefix.trim()).toAbsolutePath().normalize();
            if (root.equals(allowed) || root.startsWith(allowed)) {
                return;
            }
        }
        throw new BizException("写盘路径不在允许范围内：" + root);
    }

    public static void assertTemplateDir(String templateDir) {
        if (StringUtils.isEmpty(templateDir)) {
            return;
        }
        File dir = new File(templateDir.trim());
        if (!dir.isDirectory()) {
            throw new BizException("自定义模板目录不存在：" + templateDir);
        }
    }
}

