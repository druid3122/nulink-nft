package com.project.nulinknft.repository;

import com.project.nulinknft.entity.BlindBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlindBoxRepository extends JpaRepository<BlindBox, Long>{
}
