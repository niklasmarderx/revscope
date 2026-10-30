package dev.niklasmarder.revscope.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

/** Persistent record of one analysis; heavy detail is stored as a JSON payload. */
@Entity
@Table(name = "analyses")
public class AnalysisEntity {

    @Id
    private String id;
    private String fileName;
    private long fileSize;
    private String arch;
    private String status;
    private int functionCount;
    private Instant createdAt;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String payload;

    protected AnalysisEntity() {
        // for JPA
    }

    public AnalysisEntity(String id, String fileName, long fileSize, String arch, String status,
                          int functionCount, Instant createdAt, String payload) {
        this.id = id;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.arch = arch;
        this.status = status;
        this.functionCount = functionCount;
        this.createdAt = createdAt;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getArch() {
        return arch;
    }

    public String getStatus() {
        return status;
    }

    public int getFunctionCount() {
        return functionCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getPayload() {
        return payload;
    }
}
