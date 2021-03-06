package com.ctrip.xpipe.redis.console.dao;

import com.ctrip.xpipe.redis.console.migration.model.*;
import com.ctrip.xpipe.redis.console.migration.model.impl.DefaultMigrationShard;
import com.ctrip.xpipe.redis.console.migration.model.impl.DefaultShardMigrationResult;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import com.ctrip.xpipe.redis.console.AbstractConsoleIntegrationTest;

import java.util.List;

public class MigrationEventDaoTest extends AbstractConsoleIntegrationTest {

    @Autowired
    MigrationEventDao migrationEventDao;

    @Override
    public String prepareDatas() {
        try {
            return prepareDatasFromFile("src/test/resources/migration-test.sql");
        } catch (Exception ex) {
            logger.error("Prepare data from file failed", ex);
        }
        return "";
    }

    @Test
    @DirtiesContext
    public void testBuildMigrationEvent() {

        MigrationEvent event = migrationEventDao.buildMigrationEvent(2);
        Assert.assertNotNull(event);
        Assert.assertEquals(2, event.getMigrationCluster(2).getMigrationShards().size());
    }

    @Test
    @DirtiesContext
    public void testShardResult() {

        MigrationEvent event = migrationEventDao.buildMigrationEvent(2);

        // update check
        MigrationCluster migrationCluster = event.getMigrationClusters().get(0);
        List<MigrationShard> migrationShards = migrationCluster.getMigrationShards();
        migrationShards.forEach(migrationShard -> {
            ShardMigrationResult result = randomResult();
            ((DefaultMigrationShard) migrationShard).updateShardMigrationResult(result);
            migrationShard.update(null, null);
        });


        MigrationEvent eventNew = migrationEventDao.buildMigrationEvent(2);
        MigrationCluster migrationClusterNew = eventNew.getMigrationClusters().get(0);
        Assert.assertEquals(migrationCluster.clusterName(), migrationClusterNew.clusterName());

        List<MigrationShard> migrationShardsNew = migrationClusterNew.getMigrationShards();
        Assert.assertEquals(migrationShards.size(), migrationShardsNew.size());

        for (int i = 0; i < migrationShards.size(); i++) {

            ShardMigrationResult result = migrationShards.get(i).getShardMigrationResult();
            ShardMigrationResult resultNew = migrationShardsNew.get(i).getShardMigrationResult();

            Assert.assertFalse(result == resultNew);
            Assert.assertEquals(result, resultNew);
        }
    }

    private ShardMigrationResult randomResult() {

        DefaultShardMigrationResult result = new DefaultShardMigrationResult();
        for (ShardMigrationStep step : ShardMigrationStep.values()) {
            int random = randomInt();
            boolean success = random % 2 == 0 ? true : false;
            result.updateStepResult(step, success, randomString(10));
        }
        return result;
    }


}
