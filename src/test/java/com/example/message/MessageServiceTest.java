package com.example.message;

import static org.junit.jupiter.api.Assertions.*;

import com.example.constraint.MessageConstraints;
import com.example.error.ForbiddenOperationException;
import com.example.error.ResourceNotFoundException;
import com.example.user.User;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MessageServiceTest {

    private static final AtomicLong FUTURE_TIME_OFFSET_SECONDS = new AtomicLong();

    @Inject
    MessageService messageService;

    @Inject
    EntityManagerFactory entityManagerFactory;

    @Inject
    EntityManager entityManager;

    @Test
    @TestTransaction
    void create_persistsMessageForExistingUser() {
        var user = User.findByEmail("alice@example.com");

        var message = messageService.create(user.id, "service message");

        assertNotNull(message.id());
        assertEquals("service message", message.body());
        assertEquals(user.id, message.userId());
    }

    @Test
    @TestTransaction
    void create_throwsForMissingUser() {
        assertThrows(IllegalStateException.class, () -> messageService.create(-1L, "missing user message"));
    }

    @Test
    @TestTransaction
    void list_filtersBySinceAndReturnsOldToNew() {
        var user = User.findByEmail("alice@example.com");
        Long oldMessageId = messageService.create(user.id, "old service message").id();
        Long newMessageId = messageService.create(user.id, "new service message").id();
        var base = nextFutureTime();
        backdate(oldMessageId, base);
        backdate(newMessageId, base.plusSeconds(1));

        var messages = messageService.list(base.plusMillis(500), null);

        assertEquals(List.of(newMessageId), messages.stream().map(message -> message.id()).toList());
    }

    @Test
    @TestTransaction
    void list_appliesLimitBeforeReturningOldToNew() {
        var user = User.findByEmail("alice@example.com");
        Long oldMessageId = messageService.create(user.id, "limited old service message").id();
        Long newMessageId = messageService.create(user.id, "limited new service message").id();
        var base = nextFutureTime();
        backdate(oldMessageId, base);
        backdate(newMessageId, base.plusSeconds(1));

        var messages = messageService.list(base.minusMillis(500), 1);

        assertEquals(List.of(newMessageId), messages.stream().map(message -> message.id()).toList());
    }

    @Test
    @TestTransaction
    void list_fetchesAuthorsWithTheMessages() {
        var user = User.findByEmail("alice@example.com");
        Long oldMessageId = messageService.create(user.id, "fetch old service message").id();
        Long newMessageId = messageService.create(user.id, "fetch new service message").id();
        var base = nextFutureTime();
        backdate(oldMessageId, base);
        backdate(newMessageId, base.plusSeconds(1));

        Statistics statistics = entityManagerFactory.unwrap(SessionFactoryImplementor.class).getStatistics();
        entityManager.clear();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        var messages = messageService.list(base.plusMillis(500), null);
        assertEquals(List.of(newMessageId), messages.stream().map(message -> message.id()).toList());
        assertEquals("alice", messages.get(0).authorName());
        assertEquals(1L, statistics.getPrepareStatementCount());
    }

    @Test
    @TestTransaction
    void list_usesDefaultLimitWhenLimitIsNull() {
        var user = User.findByEmail("alice@example.com");
        var base = nextFutureTime();
        var ids = new ArrayList<Long>();
        for (int i = 0; i < MessageConstraints.DEFAULT_LIST_LIMIT + 1; i++) {
            Long id = messageService.create(user.id, "default limited service message " + i).id();
            backdate(id, base.plusSeconds(i));
            ids.add(id);
        }

        var messages = messageService.list(base.minusMillis(500), null);

        assertEquals(MessageConstraints.DEFAULT_LIST_LIMIT, messages.size());
        assertEquals(ids.subList(1, ids.size()), messages.stream().map(message -> message.id()).toList());
    }

    /**
     * Message.createdAtはupdatable=falseのため、エンティティのフィールド代入では
     * UPDATE文が生成されない。バルクUPDATEで直接カラムを書き換える。
     */
    private void backdate(Long messageId, Instant createdAt) {
        entityManager.createQuery("update Message m set m.createdAt = :createdAt where m.id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", messageId)
                .executeUpdate();
    }

    @Test
    void list_rejectsInvalidLimit() {
        assertThrows(IllegalArgumentException.class, () -> messageService.list(null, 0));
        assertThrows(IllegalArgumentException.class, () -> messageService.list(null, MessageConstraints.MAX_LIST_LIMIT + 1));
    }

    @Test
    @TestTransaction
    void delete_removesMessage() {
        var user = User.findByEmail("alice@example.com");
        var message = messageService.create(user.id, "delete service message");

        messageService.delete(message.id(),user.id, false);

        assertNull(Message.findById(message.id()));
    }

    @Test
    @TestTransaction
    void delete_byAdmin_removesMessage() {
        var user = User.findByEmail("alice@example.com");
        var message = messageService.create(user.id, "delete admin service message");
        var admin = User.findByEmail("admin@example.com");

        messageService.delete(message.id(),admin.id, true);

        assertNull(Message.findById(message.id()));
    }

    @Test
    @TestTransaction
    void delete_byOtherUser_throwsForbidden() {
        var user = User.findByEmail("alice@example.com");
        var message = messageService.create(user.id, "delete forbidden service message");
        Long otherId = user.id + 999L;

        assertThrows(ForbiddenOperationException.class,
                () -> messageService.delete(message.id(),otherId, false));
    }

    @Test
    @TestTransaction
    void delete_notFound_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class,
                () -> messageService.delete(Long.MAX_VALUE, 1L, false));
    }

    private Instant nextFutureTime() {
        return Instant.parse("2099-01-01T00:00:00Z")
                .plusSeconds(FUTURE_TIME_OFFSET_SECONDS.getAndAdd(10));
    }
}
