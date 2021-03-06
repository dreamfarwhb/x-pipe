package com.ctrip.xpipe.redis.console.migration.status;

import java.lang.reflect.InvocationTargetException;

import com.ctrip.xpipe.redis.console.migration.model.MigrationCluster;
import com.ctrip.xpipe.redis.console.migration.status.migration.MigrationAbortedState;
import com.ctrip.xpipe.redis.console.migration.status.migration.MigrationCheckingState;
import com.ctrip.xpipe.redis.console.migration.status.migration.MigrationForceEndState;
import com.ctrip.xpipe.redis.console.migration.status.migration.MigrationInitiatedState;
import com.ctrip.xpipe.redis.console.migration.status.migration.MigrationMigratingState;
import com.ctrip.xpipe.redis.console.migration.status.migration.MigrationPartialSuccessState;
import com.ctrip.xpipe.redis.console.migration.status.migration.MigrationPublishState;
import com.ctrip.xpipe.redis.console.migration.status.migration.MigrationPartialSuccessRollBackState;
import com.ctrip.xpipe.redis.console.migration.status.migration.MigrationSuccessState;

/**
 * @author shyin
 *
 * Dec 8, 2016
 */
public enum MigrationStatus {
	
	Initiated(MigrationInitiatedState.class, ClusterStatus.Lock, false, 0),
	Checking(MigrationCheckingState.class, ClusterStatus.Lock, false, 10),
	Migrating(MigrationMigratingState.class, ClusterStatus.Migrating, false, 30),
	PartialSuccess(MigrationPartialSuccessState.class, ClusterStatus.Migrating, false, 40),
	Publish(MigrationPublishState.class, ClusterStatus.TmpMigrated, false, 80),

	RollBack(MigrationPartialSuccessRollBackState.class, ClusterStatus.Rollback, false, 30),
	Aborted(MigrationAbortedState.class, ClusterStatus.Normal, true, 100),
	Success(MigrationSuccessState.class, ClusterStatus.Normal, true, 100),
	ForceEnd(MigrationForceEndState.class, ClusterStatus.Normal, true, 100);

	private final  Class<MigrationState> classMigrationState;
	private final ClusterStatus clusterStatus;
	private boolean isTerminated;
	private int  	percent;
	
	@SuppressWarnings("unchecked")
	MigrationStatus(Class<?> classMigrationState, ClusterStatus clusterStatus, boolean isTerminated, int percent){
		this.classMigrationState = (Class<MigrationState>) classMigrationState;
		this.clusterStatus = clusterStatus;
		this.isTerminated = isTerminated;
		this.percent = percent;
	}
	
	public ClusterStatus getClusterStatus(){
		return clusterStatus;
	}
	
	public MigrationState createMigrationState(MigrationCluster migrationCluster){
		try {
			return classMigrationState.getConstructor(MigrationCluster.class).newInstance(migrationCluster);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("[createMigrationState]" + this, e);
		}
	}

	public boolean isTerminated(){
		return isTerminated;
	}

	public int getPercent(){
		return percent;
	}

}
