package com.capstone.pronunciation.domain.upload.entity;

import java.time.Instant;

import com.capstone.pronunciation.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Table(name = "upload_files")
@Entity
public class UploadFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "s3_key", nullable = false, length = 512, unique = true)
	private String s3Key;

	@Column(name = "object_url", nullable = false, length = 1000)
	private String objectUrl;

	@Column(name = "original_file_name", nullable = false, length = 255)
	private String originalFileName;

	@Column(name = "content_type", nullable = false, length = 100)
	private String contentType;

	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected UploadFile() {
	}

	public UploadFile(
			User user,
			String s3Key,
			String objectUrl,
			String originalFileName,
			String contentType,
			long sizeBytes,
			Instant createdAt) {
		this.user = user;
		this.s3Key = s3Key;
		this.objectUrl = objectUrl;
		this.originalFileName = originalFileName;
		this.contentType = contentType;
		this.sizeBytes = sizeBytes;
		this.createdAt = createdAt == null ? Instant.now() : createdAt;
	}

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public String getS3Key() {
		return s3Key;
	}

	public String getObjectUrl() {
		return objectUrl;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public String getContentType() {
		return contentType;
	}

	public long getSizeBytes() {
		return sizeBytes;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
