package com.smarttask.smarttask_ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smarttask.smarttask_ai.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByStatus(String status);

	List<Task> findByPriority(String priority);

	long countByStatus(String status);

	long countByPriority(String priority);
}
