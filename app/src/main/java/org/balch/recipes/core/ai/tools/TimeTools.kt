package org.balch.recipes.core.ai.tools

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolResult
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
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
        ) : ToolResult.TextSerializable() {
            override fun textForLLM(): String {
                return "Current datetime: $datetime, Date: $date, Time: $time, Timezone: $timezone"
            }
        }

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

    /**
     * Tool for adding a duration to a date
     */
    class AddDatetimeTool(
        val defaultTimeZone: TimeZone = UTC_ZONE,
        val clock: Clock = CLOCK,
    ) : Tool<AddDatetimeTool.Args, AddDatetimeTool.Result>() {
        @Serializable
        data class Args(
            @property:LLMDescription("The date to add to in ISO format (e.g., '2023-05-20')")
            val date: String,
            @property:LLMDescription("The number of days to add")
            val days: Int,
            @property:LLMDescription("The number of hours to add")
            val hours: Int,
            @property:LLMDescription("The number of minutes to add")
            val minutes: Int
        )

        @Serializable
        data class Result(
            val date: String,
            val originalDate: String,
            val daysAdded: Int,
            val hoursAdded: Int,
            val minutesAdded: Int
        ) : ToolResult.TextSerializable() {
            override fun textForLLM(): String {
                return buildString {
                    append("Date: $date")
                    if (originalDate.isBlank()) {
                        append(" (starting from today)")
                    } else {
                        append(" (starting from $originalDate)")
                    }

                    if (daysAdded != 0 || hoursAdded != 0 || minutesAdded != 0) {
                        append(" after adding")

                        if (daysAdded != 0) {
                            append(" $daysAdded days")
                        }

                        if (hoursAdded != 0) {
                            if (daysAdded != 0) append(",")
                            append(" $hoursAdded hours")
                        }

                        if (minutesAdded != 0) {
                            if (daysAdded != 0 || hoursAdded != 0) append(",")
                            append(" $minutesAdded minutes")
                        }
                    }
                }
            }
        }

        override val argsSerializer = Args.serializer()
        override val resultSerializer: KSerializer<Result> = Result.serializer()

        override val name = "add_datetime"
        override val description =
            "Add a duration to a date. Use this tool when you need to calculate offsets, such as tomorrow, in two days, etc."

        override suspend fun execute(args: Args): Result {
            val baseDate = if (args.date.isNotBlank()) {
                try {
                    LocalDate.parse(args.date)
                } catch (_: Exception) {
                    // Use current date if parsing fails
                    clock.now().toLocalDateTime(defaultTimeZone).date
                }
            } else {
                clock.now().toLocalDateTime(defaultTimeZone).date
            }

            // Convert to LocalDateTime to handle hours and minutes
            val baseDateTime = LocalDateTime(baseDate.year, baseDate.month, baseDate.day, 0, 0)
            val baseInstant = baseDateTime.toInstant(defaultTimeZone)

            val period = DateTimePeriod(
                days = args.days,
                hours = args.hours,
                minutes = args.minutes
            )

            val newInstant = baseInstant.plus(period, defaultTimeZone)
            val resultDate = newInstant.toLocalDateTime(defaultTimeZone).date.toString()

            return Result(
                date = resultDate,
                originalDate = args.date,
                daysAdded = args.days,
                hoursAdded = args.hours,
                minutesAdded = args.minutes
            )
        }
    }
}

