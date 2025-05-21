package com.framework.apiserver.repository;

import com.framework.apiserver.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, String> {
}
