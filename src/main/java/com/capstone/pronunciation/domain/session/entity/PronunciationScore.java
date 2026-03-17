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
	private int voiceScore;

	@Column(name = "vision_score", nullable = false)
	private int visionScore;

	protected PronunciationScore() {
	}

	public PronunciationScore(SessionResult result, int voiceScore, int visionScore) {
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

	public int getVoiceScore() {
		return voiceScore;
	}

	public void setVoiceScore(int voiceScore) {
		this.voiceScore = voiceScore;
	}

	public int getVisionScore() {
		return visionScore;
	}

	public void setVisionScore(int visionScore) {
		this.visionScore = visionScore;
	}
}

