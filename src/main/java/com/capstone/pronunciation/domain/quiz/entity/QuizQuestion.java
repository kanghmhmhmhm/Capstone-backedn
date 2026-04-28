package com.capstone.pronunciation.domain.quiz.entity;

import java.util.Collections;
import java.util.List;

import com.capstone.pronunciation.domain.curriculum.entity.CurriculumStage;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import jakarta.persistence.*;

@Table(name = "quiz_questions")
@Entity
public class QuizQuestion {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

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

	@Lob
	@Column(name = "animation_data")
	private String animationData;

	@Lob
	@Column(name = "choice_options")
	private String choiceOptions;

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
		this(stage, sentence, answer, phoneticSymbol, null, stage.getDifficulty(), List.of());
	}

	public QuizQuestion(CurriculumStage stage, String sentence, String answer, String phoneticSymbol, String animationData) {
		this(stage, sentence, answer, phoneticSymbol, animationData, stage.getDifficulty(), List.of());
	}

	public QuizQuestion(CurriculumStage stage, String sentence, String answer, String phoneticSymbol, int difficulty) {
		this(stage, sentence, answer, phoneticSymbol, null, difficulty, List.of());
	}

	public QuizQuestion(
			CurriculumStage stage,
			String sentence,
			String answer,
			String phoneticSymbol,
			int difficulty,
			List<String> choiceOptions) {
		this(stage, sentence, answer, phoneticSymbol, null, difficulty, choiceOptions);
	}

	public QuizQuestion(
			CurriculumStage stage,
			String sentence,
			String answer,
			String phoneticSymbol,
			String animationData,
			int difficulty,
			List<String> choiceOptions) {
		this.stage = stage;
		this.sentence = sentence;
		this.answer = answer;
		this.phoneticSymbol = phoneticSymbol;
		this.animationData = animationData;
		setChoiceOptions(choiceOptions);
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

	public String getAnimationData() {
		return animationData;
	}

	public void setAnimationData(String animationData) {
		this.animationData = animationData;
	}

	public List<String> getChoiceOptions() {
		if (choiceOptions == null || choiceOptions.isBlank()) {
			return List.of();
		}
		try {
			return OBJECT_MAPPER.readValue(choiceOptions, STRING_LIST_TYPE);
		} catch (Exception ignored) {
			return Collections.emptyList();
		}
	}

	public void setChoiceOptions(List<String> choiceOptions) {
		if (choiceOptions == null || choiceOptions.isEmpty()) {
			this.choiceOptions = null;
			return;
		}
		try {
			this.choiceOptions = OBJECT_MAPPER.writeValueAsString(choiceOptions);
		} catch (Exception e) {
			throw new IllegalArgumentException("choiceOptions를 저장할 수 없습니다.", e);
		}
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
