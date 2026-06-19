package cn.org.starpivot.generator.external;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.utils.StringUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * 写盘前备份与回退
 */
public final class ExternalWriteBackupHelper {

    public static final String BACKUP_DIR_NAME = ".star-pivot-gen-backup";

    private ExternalWriteBackupHelper() {
    }

    public static String newBackupId() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "bk_" + ts + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static Path backupRoot(Path outputRoot, String backupId) {
        return outputRoot.resolve(BACKUP_DIR_NAME).resolve(backupId);
    }

    public static void backupFileIfExists(Path outputRoot, String backupId, String relativeKey) throws IOException {
        if (StringUtils.isEmpty(backupId)) {
            return;
        }
        String relative = relativeKey.replace('/', File.separatorChar);
        File target = outputRoot.resolve(relative).toFile();
        if (!target.isFile()) {
            return;
        }
        File backupFile = backupRoot(outputRoot, backupId).resolve(relative).toFile();
        FileUtils.forceMkdirParent(backupFile);
        FileUtils.copyFile(target, backupFile);
    }

    public static List<String> rollback(Path outputRoot, String backupId) throws IOException {
        if (StringUtils.isEmpty(backupId)) {
            throw new BizException("备份 ID 不能为空");
        }
        Path backupDir = backupRoot(outputRoot, backupId);
        if (!Files.isDirectory(backupDir)) {
            throw new BizException("备份不存在或已过期：" + backupId);
        }
        List<String> restored = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(backupDir)) {
            List<Path> files = walk.filter(Files::isRegularFile).sorted().toList();
            for (Path backupFile : files) {
                Path relative = backupDir.relativize(backupFile);
                Path target = outputRoot.resolve(relative.toString());
                FileUtils.forceMkdirParent(target.toFile());
                FileUtils.copyFile(backupFile.toFile(), target.toFile());
                restored.add(relative.toString().replace('\\', '/'));
            }
        }
        if (restored.isEmpty()) {
            throw new BizException("备份中无文件可恢复：" + backupId);
        }
        return restored;
    }
}

