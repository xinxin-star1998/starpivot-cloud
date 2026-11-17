package cn.org.starpivot.system.service;

import cn.org.starpivot.common.cache.CacheConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CacheEvictionTransactionTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void permissionAndDataScopeCachesShouldBeEvictedOnlyAfterCommit() {
        UserPermissionCacheService permissionCacheService = new UserPermissionCacheService(null, redisTemplate);
        DataScopeService dataScopeService = new DataScopeService(null, null, null, null, redisTemplate);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        permissionCacheService.clearUserPermissionCacheByUserId(7L);
        dataScopeService.evictUserDataScope(7L);

        verifyNoInteractions(redisTemplate);
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        assertEquals(2, synchronizations.size());

        synchronizations.forEach(TransactionSynchronization::afterCommit);

        verify(redisTemplate).delete(CacheConstants.userPermissionKey(7L));
        verify(redisTemplate).delete(CacheConstants.dataScopeKey(7L));
    }
}
