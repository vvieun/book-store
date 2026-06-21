package ru.bookstore.repository.mongo.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import ru.bookstore.domain.Order;
import ru.bookstore.repository.OrderRepository;
import ru.bookstore.repository.mongo.mapper.OrderMongoMapper;
import ru.bookstore.repository.mongo.service.MongoSequenceService;
import ru.bookstore.repository.mongo.spring_data.OrderMongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Profile("mongo")
public class OrderRepositoryMongoImpl implements OrderRepository {

    private final OrderMongoRepository mongoRepo;
    private final OrderMongoMapper mapper;
    private final MongoSequenceService seq;

    @Override
    public List<Order> findByUserUserIdOrderByCreatedAtDesc(Long userId) {
        return mongoRepo.findByUserIdOrderByCreatedAtDesc(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Order> findByStatus(String status) {
        return mongoRepo.findByStatus(status).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return mongoRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return mongoRepo.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Order> findAllOrderByCreatedAtDescWithDetails() {
        return mongoRepo.findAllByOrderByCreatedAtDesc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Order> findByIdWithDetails(Long id) {
        return mongoRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Order save(Order order) {
        var doc = mapper.toDoc(order);
        if (doc.getOrderId() == null) {
            doc.setOrderId(seq.next("orders"));
        }
        if (doc.getCreatedAt() == null) {
            doc.setCreatedAt(LocalDateTime.now());
        }
        return mapper.toDomain(mongoRepo.save(doc));
    }

    @Override
    public long count() {
        return mongoRepo.count();
    }
}

