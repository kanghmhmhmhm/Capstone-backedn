package com.capstone.pronunciation.domain.upload.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pronunciation.domain.upload.entity.UploadFile;

public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {
	Optional<UploadFile> findByS3Key(String s3Key);
}
