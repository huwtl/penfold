package com.qmetric.penfold.app.store.postgres

import com.qmetric.penfold.app.DatabaseConfiguration
import com.zaxxer.hikari.HikariDataSource

class PostgresConnectionPoolFactory {
  def create(poolConfig: DatabaseConfiguration) = {
    val dataSource = new HikariDataSource
    dataSource.setJdbcUrl(poolConfig.url)
    dataSource.setUsername(poolConfig.username)
    dataSource.setPassword(poolConfig.password)
    dataSource.setDriverClassName(poolConfig.driver)
    dataSource.setMaximumPoolSize(poolConfig.poolSize)
    dataSource.setConnectionTestQuery("select 1")

    dataSource
  }
}
