package com.qmetric.penfold.app.store.postgres

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.qmetric.penfold.app.DatabaseConfiguration

class PostgresConnectionPoolFactory {
  def create(poolConfig: DatabaseConfiguration) = {
    val dataSource = new ComboPooledDataSource

    dataSource.setDriverClass(poolConfig.driver)
    dataSource.setJdbcUrl(poolConfig.url)
    dataSource.setUser(poolConfig.username)
    dataSource.setPassword(poolConfig.password)
    dataSource.setMaxPoolSize(poolConfig.poolSize)
    dataSource.setPreferredTestQuery("select 1")
    dataSource.setIdleConnectionTestPeriod(60)

    dataSource
  }
}
