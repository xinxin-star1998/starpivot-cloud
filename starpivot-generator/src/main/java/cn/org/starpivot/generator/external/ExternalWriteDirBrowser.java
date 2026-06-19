package cn.org.starpivot.generator.external;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.config.GenExternalProperties;
import cn.org.starpivot.generator.domain.dto.external.ExternalWriteDirEntryVO;
import cn.org.starpivot.generator.domain.dto.external.ExternalWriteDirListVO;
import cn.org.starpivot.generator.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * 服务端写盘目录浏览（在允许路径范围内）
 */
public final class ExternalWriteDirBrowser {

    private static final String BACKUP_DIR_NAME = ".star-pivot-gen-backup";

    private ExternalWriteDirBrowser() {
    }

    public static ExternalWriteDirListVO listDirectories(String path, GenExternalProperties properties) {
        if (StringUtils.isEmpty(path)) {
            return listRoots(properties);
        }
        Path dir = Paths.get(path.trim()).toAbsolutePath().normalize();
        assertBrowsable(dir, properties);
        if (!Files.isDirectory(dir)) {
            throw new BizException("不是有效目录：" + dir);
        }
        List<ExternalWriteDirEntryVO> directories = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(Files::isDirectory)
                    .filter(p -> !BACKUP_DIR_NAME.equals(p.getFileName().toString()))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                    .forEach(p -> directories.add(ExternalWriteDirEntryVO.builder()
                            .name(p.getFileName().toString())
                            .path(p.toString())
                            .build()));
        } catch (IOException e) {
            throw new BizException("读取目录失败：" + e.getMessage());
        }
        Path parent = dir.getParent();
        String parentPath = null;
        if (parent != null && isBrowsable(parent, properties)) {
            parentPath = parent.toString();
        }
        return ExternalWriteDirListVO.builder()
                .current(dir.toString())
                .parent(parentPath)
                .directories(directories)
                .build();
    }

    private static ExternalWriteDirListVO listRoots(GenExternalProperties properties) {
        Set<String> roots = new LinkedHashSet<>();
        if (properties.getAllowedWritePaths() != null && !properties.getAllowedWritePaths().isEmpty()) {
            for (String prefix : properties.getAllowedWritePaths()) {
                if (StringUtils.isNotEmpty(prefix)) {
                    roots.add(Paths.get(prefix.trim()).toAbsolutePath().normalize().toString());
                }
            }
        } else {
            if (StringUtils.isNotEmpty(properties.getDefaultOutputRoot())) {
                roots.add(Paths.get(properties.getDefaultOutputRoot().trim()).toAbsolutePath().normalize().toString());
            }
            roots.add(Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize().toString());
        }
        List<ExternalWriteDirEntryVO> entries = new ArrayList<>();
        for (String root : roots) {
            Path p = Paths.get(root);
            if (Files.isDirectory(p)) {
                entries.add(ExternalWriteDirEntryVO.builder()
                        .name(p.getFileName() != null ? p.getFileName().toString() : root)
                        .path(p.toString())
                        .build());
            }
        }
        return ExternalWriteDirListVO.builder()
                .current("")
                .directories(entries)
                .build();
    }

    private static void assertBrowsable(Path dir, GenExternalProperties properties) {
        if (!isBrowsable(dir, properties)) {
            throw new BizException("目录不在允许浏览范围内：" + dir);
        }
    }

    private static boolean isBrowsable(Path dir, GenExternalProperties properties) {
        try {
            ExternalWritePathValidator.resolveOutputRoot(dir.toString(), properties);
            return true;
        } catch (BizException e) {
            return false;
        }
    }
}

