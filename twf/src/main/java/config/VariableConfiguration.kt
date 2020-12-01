package config

data class VariableReplacementRule (
        val left: String,
        val right: String
)

class VariableConfiguration {
    var variableImmediateReplacementRules = listOf<VariableReplacementRule>(
            VariableReplacementRule("e", "2.7182818284590452353602874713526"),
            VariableReplacementRule("pi", "3.1415926535897932384626433832795"),
            VariableReplacementRule("&#x3C0", "3.1415926535897932384626433832795"),
            VariableReplacementRule("&#x3C0;", "3.1415926535897932384626433832795")
    )
}