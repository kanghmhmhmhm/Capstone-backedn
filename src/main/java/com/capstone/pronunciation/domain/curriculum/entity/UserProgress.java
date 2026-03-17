package com.capstone.pronunciation.domain.curriculum.entity;

import com.capstone.pronunciation.domain.user.entity.User;

import jakarta.persistence.*;

@Table(
		name = "user_progress",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_user_stage_progress", columnNames = {"user_id", "stage_id"})
		}
)
@Entity
public class UserProgress {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "stage_id", nullable = false)
	private CurriculumStage stage;

	@Column(nullable = false)
	private boolean completed;

	protected UserProgress() {
	}

	public UserProgress(User user, CurriculumStage stage, boolean completed) {
		this.user = user;
		this.stage = stage;
		this.completed = completed;
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public CurriculumStage getStage() {
		return stage;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
}

