package com.capstone.pronunciation.domain.session.entity;

import jakarta.persistence.*;

@Table(name = "pronunciation_scores")
@Entity
public class PronunciationScore {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "result_id", nullable = false, unique = true)
	private SessionResult result;

	@Column(name = "voice_score", nullable = false)
	private double voiceScore;

	@Column(name = "vision_score", nullable = false)
	private double visionScore;

	protected PronunciationScore() {
	}

	public PronunciationScore(SessionResult result, double voiceScore, double visionScore) {
		this.result = result;
		this.voiceScore = voiceScore;
		this.visionScore = visionScore;
	}

	public Long getId() {
		return id;
	}

	public SessionResult getResult() {
		return result;
	}

	public double getVoiceScore() {
		return voiceScore;
	}

	public void setVoiceScore(double voiceScore) {
		this.voiceScore = voiceScore;
	}

	public double getVisionScore() {
		return visionScore;
	}

	public void setVisionScore(double visionScore) {
		this.visionScore = visionScore;
	}
}
