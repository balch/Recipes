package org.balch.recipes.core.ai.tools

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Tools for getting time information
 */
@OptIn(ExperimentalTime::class)
sealed interface TimeTools {
    companion object {
        private val CLOCK = Clock.System
        private val UTC_ZONE = TimeZone.UTC
    }

    /**
     * Tool for getting the current date and time
     */
    class CurrentDatetimeTool(
        val defaultTimeZone: TimeZone = TimeZone.currentSystemDefault(),
        val clock: Clock = CLOCK,
    ) : Tool<CurrentDatetimeTool.Args, CurrentDatetimeTool.Result>() {
        @Serializable
        data class Args(
            @property:LLMDescription("The timezone to get the current date and time in (e.g., 'UTC', 'America/New_York', 'Europe/London'). Defaults to null to use the current users timezone.")
            val timezone: String? = null
        )

        @Serializable
        data class Result(
            val datetime: String,
            val date: String,
            val time: String,
            val timezone: String
        )

        override val argsSerializer = Args.serializer()
        override val resultSerializer: KSerializer<Result> =
            Result.serializer()

        override val name = "current_datetime"
        override val description = "Get the current date and time in the specified timezone"

        override suspend fun execute(args: Args): Result {
            val zoneId =
                args.timezone?.let {
                    try { TimeZone.of(it) }
                    catch( _: Exception) { null }
                } ?: defaultTimeZone

            val now = clock.now()
            val localDateTime = now.toLocalDateTime(zoneId)
            val offset = zoneId.offsetAt(now)

            val time = localDateTime.time
            val timeStr = "${time.hour.toString().padStart(2, '0')}:${
                time.minute.toString().padStart(2, '0')
            }:${time.second.toString().padStart(2, '0')}"

            return Result(
                datetime = "${localDateTime.date}T$timeStr$offset",
                date = localDateTime.date.toString(),
                time = timeStr,
                timezone = zoneId.id
            )
        }
    }
}

