package cn.org.starpivot.file.support;

import cn.org.starpivot.api.file.FileMediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 由 MIME 与扩展名解析 {@link FileMediaType}。
 */
@Component
public class FileMediaTypeResolver {

    private static final Map<FileMediaType, Set<String>> EXTENSIONS = Map.of(
            FileMediaType.IMAGE, Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg"),
            FileMediaType.VIDEO, Set.of("mp4", "webm", "mov", "avi", "mkv"),
            FileMediaType.AUDIO, Set.of("mp3", "wav", "ogg", "m4a", "flac", "aac"),
            FileMediaType.DOCUMENT, Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv"),
            FileMediaType.OTHER, Set.of("zip", "rar", "7z", "tar", "gz")
    );

    public FileMediaType resolve(String contentType, String filename) {
        FileMediaType byMime = resolveByMime(contentType);
        if (byMime != null) {
            return byMime;
        }
        return resolveByExtension(filename);
    }

    public FileMediaType resolve(MultipartFile file) {
        return resolve(file.getContentType(), file.getOriginalFilename());
    }

    private FileMediaType resolveByMime(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return null;
        }
        String mime = contentType.toLowerCase(Locale.ROOT).trim();
        if (mime.startsWith("image/")) {
            return FileMediaType.IMAGE;
        }
        if (mime.startsWith("video/")) {
            return FileMediaType.VIDEO;
        }
        if (mime.startsWith("audio/")) {
            return FileMediaType.AUDIO;
        }
        if ("application/pdf".equals(mime)
                || mime.startsWith("application/vnd.openxmlformats-officedocument")
                || mime.startsWith("application/msword")
                || mime.startsWith("application/vnd.ms-")
                || "text/plain".equals(mime)
                || "text/csv".equals(mime)) {
            return FileMediaType.DOCUMENT;
        }
        if (mime.contains("zip") || mime.contains("rar") || mime.contains("7z")
                || mime.contains("gzip") || mime.contains("x-tar")) {
            return FileMediaType.OTHER;
        }
        return null;
    }

    private FileMediaType resolveByExtension(String filename) {
        String ext = extractExtension(filename);
        if (!StringUtils.hasText(ext)) {
            return FileMediaType.OTHER;
        }
        for (Map.Entry<FileMediaType, Set<String>> entry : EXTENSIONS.entrySet()) {
            if (entry.getValue().contains(ext)) {
                return entry.getKey();
            }
        }
        return FileMediaType.OTHER;
    }

    public String extractExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
