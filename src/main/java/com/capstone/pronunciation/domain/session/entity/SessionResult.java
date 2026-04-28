package com.capstone.pronunciation.domain.session.entity;

import java.time.Instant;

import com.capstone.pronunciation.domain.quiz.entity.QuizQuestion;

import jakarta.persistence.*;

@Table(name = "session_results")
@Entity
public class SessionResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	private LearningSession session;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id", nullable = false)
	private QuizQuestion question;

	@OneToOne(mappedBy = "result", fetch = FetchType.LAZY)
	private PronunciationScore pronunciationScore;

	@OneToOne(mappedBy = "result", fetch = FetchType.LAZY)
	private AnswerSubmission submission;

	@Column(nullable = false, precision = 5, scale = 1)
	private double score;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected SessionResult() {
	}

	public SessionResult(LearningSession session, QuizQuestion question, double score) {
		this.session = session;
		this.question = question;
		this.score = score;
		this.createdAt = Instant.now();
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

	public LearningSession getSession() {
		return session;
	}

	public QuizQuestion getQuestion() {
		return question;
	}

	public PronunciationScore getPronunciationScore() {
		return pronunciationScore;
	}

	public AnswerSubmission getSubmission() {
		return submission;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
