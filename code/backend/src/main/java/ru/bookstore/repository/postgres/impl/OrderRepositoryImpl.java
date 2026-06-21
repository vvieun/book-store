package ru.bookstore.repository.postgres.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import ru.bookstore.domain.Order;
import ru.bookstore.repository.OrderRepository;
import ru.bookstore.repository.postgres.jpa.OrderJpaRepository;
import ru.bookstore.repository.postgres.mapper.OrderEntityMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Profile("postgres")
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepo;
    private final OrderEntityMapper mapper;

    @Override
    public List<Order> findByUserUserIdOrderByCreatedAtDesc(Long userId) {
        return jpaRepo.findByUserUserIdOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(String status) {
        return jpaRepo.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return jpaRepo.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findAllOrderByCreatedAtDescWithDetails() {
        return jpaRepo.findAllOrderByCreatedAtDescWithDetails().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Order> findByIdWithDetails(Long id) {
        return jpaRepo.findByIdWithDetails(id).map(mapper::toDomain);
    }

    @Override
    public Order save(Order order) {
        return mapper.toDomain(jpaRepo.save(mapper.toEntity(order)));
    }

    @Override
    public long count() {
        return jpaRepo.count();
    }
}
