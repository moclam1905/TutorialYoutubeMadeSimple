package com.nguyenmoclam.tutorialyoutubemadesimple.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migration from version 1 to 2.
 * Adds the token_usage table for tracking API usage.
 */
class Migration1To2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create token_usage table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `token_usage` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `modelId` TEXT NOT NULL,
                `modelName` TEXT NOT NULL,
                `promptTokens` INTEGER NOT NULL,
                `completionTokens` INTEGER NOT NULL,
                `totalTokens` INTEGER NOT NULL,
                `estimatedCost` REAL NOT NULL,
                `timestamp` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        
        // Create index for faster lookups by model
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_token_usage_modelId` ON `token_usage` (`modelId`)
            """.trimIndent()
        )
        
        // Create index for faster lookups by timestamp
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_token_usage_timestamp` ON `token_usage` (`timestamp`)
            """.trimIndent()
        )
    }
} 