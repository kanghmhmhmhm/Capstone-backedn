package com.capstone.pronunciation.domain.session.entity;

import java.time.Instant;

import com.capstone.pronunciation.domain.upload.entity.UploadFile;

import jakarta.persistence.*;

@Table(
		name = "answer_submissions",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_answer_submissions_result_id", columnNames = {"result_id"})
		}
)
@Entity
public class AnswerSubmission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "result_id", nullable = false, unique = true)
	private SessionResult result;

	@Lob
	@Column
	private String transcript;

	@Column(nullable = false, length = 50)
	private String provider;

	@Lob
	@Column(name = "provider_payload")
	private String providerPayload;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "upload_file_id")
	private UploadFile uploadFile;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected AnswerSubmission() {
	}

	public AnswerSubmission(
			SessionResult result,
			String transcript,
			String provider,
			String providerPayload,
			UploadFile uploadFile,
			Instant createdAt) {
		this.result = result;
		this.transcript = transcript;
		this.provider = provider;
		this.providerPayload = providerPayload;
		this.uploadFile = uploadFile;
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

	public SessionResult getResult() {
		return result;
	}

	public String getTranscript() {
		return transcript;
	}

	public void setTranscript(String transcript) {
		this.transcript = transcript;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getProviderPayload() {
		return providerPayload;
	}

	public void setProviderPayload(String providerPayload) {
		this.providerPayload = providerPayload;
	}

	public UploadFile getUploadFile() {
		return uploadFile;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
