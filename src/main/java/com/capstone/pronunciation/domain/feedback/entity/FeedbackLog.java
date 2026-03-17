package com.capstone.pronunciation.domain.feedback.entity;

import com.capstone.pronunciation.domain.session.entity.SessionResult;

import jakarta.persistence.*;

@Table(name = "feedback_logs")
@Entity
public class FeedbackLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "result_id", nullable = false)
	private SessionResult result;

	@Column(nullable = false, length = 50)
	private String mode;

	@Lob
	@Column(name = "feedback_text", nullable = false)
	private String feedbackText;

	protected FeedbackLog() {
	}

	public FeedbackLog(SessionResult result, String mode, String feedbackText) {
		this.result = result;
		this.mode = mode;
		this.feedbackText = feedbackText;
	}

	public Long getId() {
		return id;
	}

	public SessionResult getResult() {
		return result;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getFeedbackText() {
		return feedbackText;
	}

	public void setFeedbackText(String feedbackText) {
		this.feedbackText = feedbackText;
	}
}

