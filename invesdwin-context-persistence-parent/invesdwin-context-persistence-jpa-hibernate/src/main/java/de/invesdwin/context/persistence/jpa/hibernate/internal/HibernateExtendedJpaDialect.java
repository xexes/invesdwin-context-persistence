package de.invesdwin.context.persistence.jpa.hibernate.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;
import java.util.LinkedList;

import javax.annotation.concurrent.ThreadSafe;
import javax.persistence.EntityManager;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.TransactionDefinition;

/**
 * http://stackoverflow.com/questions/5234240/hibernatespringjpaisolation-does-not-work
 * 
 */
@ThreadSafe
public class HibernateExtendedJpaDialect extends HibernateJpaDialect {

    private final ThreadLocal<Deque<ConnectionContext>> curContext = new ThreadLocal<Deque<ConnectionContext>>() {
        @Override
        protected Deque<ConnectionContext> initialValue() {
            return new LinkedList<ConnectionContext>();
        }
    };

    @Override
    public Object beginTransaction(final EntityManager entityManager, final TransactionDefinition definition)
            throws SQLException {

        final boolean readOnly = definition.isReadOnly();
        final Connection connection = this.getJdbcConnection(entityManager, readOnly).getConnection();
        final Deque<ConnectionContext> deque = curContext.get();
        final ConnectionContext context = new ConnectionContext(connection, definition);
        deque.addLast(context);

        if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
            getSession(entityManager).getTransaction().setTimeout(definition.getTimeout());
        }

        entityManager.getTransaction().begin();

        return prepareTransaction(entityManager, readOnly, definition.getName());
    }

    @Override
    public void cleanupTransaction(final Object transactionData) {
        super.cleanupTransaction(transactionData);
        final Deque<ConnectionContext> deque = curContext.get();
        final ConnectionContext context = deque.removeLast();
        if (deque.isEmpty()) {
            curContext.remove();
        }
        context.reset();
    }

    private static class ConnectionContext {
        private final Connection connection;
        private final Integer originalIsolation;

        ConnectionContext(final Connection connection, final TransactionDefinition definition) throws SQLException {
            this.connection = connection;
            this.originalIsolation = DataSourceUtils.prepareConnectionForTransaction(connection, definition);
        }

        public void reset() {
            DataSourceUtils.resetConnectionAfterTransaction(connection, originalIsolation);
        }

    }
}