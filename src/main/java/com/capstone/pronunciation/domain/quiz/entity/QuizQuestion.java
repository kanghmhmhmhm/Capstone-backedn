package com.capstone.pronunciation.domain.quiz.entity;

import com.capstone.pronunciation.domain.curriculum.entity.CurriculumStage;

import jakarta.persistence.*;

@Table(name = "quiz_questions")
@Entity
public class QuizQuestion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "stage_id", nullable = false)
	private CurriculumStage stage;

	@Column(nullable = false, length = 1000)
	private String sentence;

	@Column(length = 255)
	private String answer;

	@Column(name = "phonetic_symbol", length = 50)
	private String phoneticSymbol;

	@Column(nullable = false, columnDefinition = "integer default 1")
	private int difficulty;

	protected QuizQuestion() {
	}

	public QuizQuestion(CurriculumStage stage, String sentence, String answer) {
		this.stage = stage;
		this.sentence = sentence;
		this.answer = answer;
		this.difficulty = stage.getDifficulty();
	}

	public QuizQuestion(CurriculumStage stage, String sentence, String answer, String phoneticSymbol) {
		this.stage = stage;
		this.sentence = sentence;
		this.answer = answer;
		this.phoneticSymbol = phoneticSymbol;
		this.difficulty = stage.getDifficulty();
	}

	public QuizQuestion(CurriculumStage stage, String sentence, String answer, String phoneticSymbol, int difficulty) {
		this.stage = stage;
		this.sentence = sentence;
		this.answer = answer;
		this.phoneticSymbol = phoneticSymbol;
		this.difficulty = difficulty;
	}

	public Long getId() {
		return id;
	}

	public CurriculumStage getStage() {
		return stage;
	}

	public void setStage(CurriculumStage stage) {
		this.stage = stage;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getPhoneticSymbol() {
		return phoneticSymbol;
	}

	public void setPhoneticSymbol(String phoneticSymbol) {
		this.phoneticSymbol = phoneticSymbol;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}

	@PrePersist
	protected void applyDefaultDifficulty() {
		if (difficulty <= 0) {
			difficulty = stage != null ? stage.getDifficulty() : 1;
		}
	}
}
