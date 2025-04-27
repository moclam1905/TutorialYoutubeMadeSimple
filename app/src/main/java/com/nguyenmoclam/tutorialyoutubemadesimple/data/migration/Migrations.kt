package com.nguyenmoclam.tutorialyoutubemadesimple.data.migration

import androidx.room.migration.Migration

/**
 * Helper class that provides all database migrations for the application.
 * Used to centralize migration management.
 */
object Migrations {
    /**
     * Gets all migrations needed for the database.
     *
     * @return An array of all migrations.
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            Migration1To2()
        )
    }
} 