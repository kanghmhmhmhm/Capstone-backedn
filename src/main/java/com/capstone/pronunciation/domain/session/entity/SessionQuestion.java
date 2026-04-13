package com.capstone.pronunciation.domain.session.entity;

import com.capstone.pronunciation.domain.quiz.entity.QuizQuestion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Table(
		name = "session_questions",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_session_question_order", columnNames = {"session_id", "question_order"}),
				@UniqueConstraint(name = "uk_session_question_pair", columnNames = {"session_id", "question_id"})
		}
)
@Entity
public class SessionQuestion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	private LearningSession session;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id", nullable = false)
	private QuizQuestion question;

	@Column(name = "question_order", nullable = false)
	private int questionOrder;

	protected SessionQuestion() {
	}

	public SessionQuestion(LearningSession session, QuizQuestion question, int questionOrder) {
		this.session = session;
		this.question = question;
		this.questionOrder = questionOrder;
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

	public int getQuestionOrder() {
		return questionOrder;
	}
}
