package com.project.nulinknft.service;

import com.project.nulinknft.entity.BlindBox;
import com.project.nulinknft.repository.BlindBoxRepository;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class BlindBoxService {

    private final BlindBoxRepository blindBoxRepository;

    public BlindBoxService(BlindBoxRepository blindBoxRepository) {
        this.blindBoxRepository = blindBoxRepository;
    }

    @Transactional
    public void create(BlindBox box){
        blindBoxRepository.save(box);
    }
}
