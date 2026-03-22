package com.capstone.pronunciation.domain.session.entity;

import java.time.Instant;

import com.capstone.pronunciation.domain.user.entity.User;

import jakarta.persistence.*;

@Table(name = "learning_sessions")
@Entity
public class LearningSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "start_time", nullable = false)
	private Instant startTime;

	@Column(name = "end_time")
	private Instant endTime;

	@Column(name = "selected_level")
	private Integer selectedLevel;

	protected LearningSession() {
	}

	public LearningSession(User user, Instant startTime, Instant endTime, Integer selectedLevel) {
		this.user = user;
		this.startTime = startTime;
		this.endTime = endTime;
		this.selectedLevel = selectedLevel;
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public Instant getStartTime() {
		return startTime;
	}

	public void setStartTime(Instant startTime) {
		this.startTime = startTime;
	}

	public Instant getEndTime() {
		return endTime;
	}

	public void setEndTime(Instant endTime) {
		this.endTime = endTime;
	}

	public Integer getSelectedLevel() {
		return selectedLevel;
	}

	public void setSelectedLevel(Integer selectedLevel) {
		this.selectedLevel = selectedLevel;
	}
}
