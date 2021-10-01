package mathhelper.twf.config

enum class ErrorLevel { INFO , WARNING , ERROR , FATAL_ERROR }

data class ConfigurationError (
        val description: String,
        val objectType: String,
        val objectValue: String,
        val positionInObject: Int,
        val errorLevel: ErrorLevel = ErrorLevel.ERROR
)