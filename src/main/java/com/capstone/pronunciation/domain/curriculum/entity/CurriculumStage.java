package com.capstone.pronunciation.domain.curriculum.entity;

import jakarta.persistence.*;

@Table(
		name = "curriculum_stages",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_curriculum_stage_name", columnNames = {"stage_name"})
		}
)
@Entity
public class CurriculumStage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "stage_name", nullable = false, length = 50)
	private String stageName;

	@Column(name = "stage_order", nullable = false)
	private int order;

	@Column(nullable = false)
	private int difficulty;

	protected CurriculumStage() {
	}

	public CurriculumStage(String stageName, int order, int difficulty) {
		this.stageName = stageName;
		this.order = order;
		this.difficulty = difficulty;
	}

	public Long getId() {
		return id;
	}

	public String getStageName() {
		return stageName;
	}

	public void setStageName(String stageName) {
		this.stageName = stageName;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}
}

