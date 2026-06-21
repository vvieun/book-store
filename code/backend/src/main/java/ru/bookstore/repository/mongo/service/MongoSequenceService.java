package ru.bookstore.repository.mongo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import ru.bookstore.repository.mongo.model.SequenceDoc;

@Component
@RequiredArgsConstructor
@Profile("mongo")
public class MongoSequenceService {

    private final MongoOperations mongo;

    public long next(String name) {
        Query q = new Query(Criteria.where("_id").is(name));
        Update u = new Update().inc("seq", 1);
        FindAndModifyOptions opts = FindAndModifyOptions.options().returnNew(true).upsert(true);
        SequenceDoc seq = mongo.findAndModify(q, u, opts, SequenceDoc.class);
        return seq == null ? 1L : seq.getSeq();
    }
}

