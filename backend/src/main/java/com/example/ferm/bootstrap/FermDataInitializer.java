package com.example.ferm.bootstrap;

import com.example.ferm.domain.FermSegment;
import com.example.ferm.repository.FermSegmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FermDataInitializer implements CommandLineRunner {

    private final FermSegmentRepository repository;

    public FermDataInitializer(FermSegmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        for (long i = 1; i <= 11; i++) {
            final long id = i;
            repository.findById(id)
                    .orElseGet(() -> repository.save(new FermSegment(id, "Ferm " + id)));
        }
    }
}
